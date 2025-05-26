package me.bottdev.lumenclient

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import kotlinx.coroutines.*
import me.bottdev.lumencore.MessageCodec
import me.bottdev.lumencore.MessageHandler
import me.bottdev.lumencore.messages.types.AddClientMetadataMessage
import me.bottdev.lumencore.messages.types.HandshakeRequestMessage
import me.bottdev.lumencore.messages.types.HandshakeResponseMessage
import me.bottdev.lumencore.messages.types.RemoveClientMetadataMessage
import me.bottdev.lumencore.wrapper.WrapperHandler
import me.bottdev.lumencore.wrapper.types.DirectMessageWrapper
import org.slf4j.LoggerFactory
import java.util.*

class LumenClient(
    val id: String,
    private val host: String,
    private val port: Int,
    private val credentials: LumenCredentials
) {

    private val logger = LoggerFactory.getLogger("LumenClient-$id")

    val codec = MessageCodec.default
    private val messageHandler = MessageHandler().apply {
        register(HandshakeResponseMessage::class) { message, _ ->
            logger.info("[Handshake]")
            metadata.setClients(message.clients.map { Metadata.Client(it.id, it.address) })
        }
        register(AddClientMetadataMessage::class) { message, _ ->
            logger.info("[Metadata] adding new client: ${message.id}")
            metadata.addClient(Metadata.Client(message.id, message.address))
        }
        register(RemoveClientMetadataMessage::class) { message, _ ->
            logger.info("[Metadata] removing client: ${message.id}")
            metadata.removeClient(message.id)
        }
    }
    val wrapperHandler = WrapperHandler(messageHandler).apply {
        register(DirectMessageWrapper::class) { wrapper, handler ->
            val payload = wrapper.payload
            handler.handle(payload)
        }
    }

    private var connection: Connection? = null
    val isConnected: Boolean
        get() = connection != null

    var metadata: Metadata = Metadata(this)

    private val connectionDeferred = CompletableDeferred<Connection>()
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    private val httpClient = HttpClient(CIO) {
        install(WebSockets) {
            pingInterval = 30_000
            maxFrameSize = Long.MAX_VALUE
        }
    }

    fun start() {
        logger.info("Starting LumenClient...")
        scope.launch {
            try {
                connect()
            } catch (e: Exception) {
                logger.info("Failed to start client: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    suspend fun connect() {
        logger.info("Connection started...")

        try {
            logger.info("Creating WebSocket connection to ws://$host:$port/connect...")

            httpClient.webSocket(
                host = host,
                port = port,
                path = "/connect?id=$id&username=${credentials.username}&password=${credentials.password}"
            ) {
                logger.info("WebSocket connection established!")

                if (this.isActive) {
                    logger.info("Successfully authenticated!")

                    connection = Connection(this@LumenClient, this)
                    connectionDeferred.complete(connection!!)

                    withConnection {
                        server(HandshakeRequestMessage())
                    }

                    try {
                        if (connection == null) {
                            logger.info("Connection is null, closing...")
                            close()
                        } else {
                            logger.info("Starting to receive messages...")
                            connection?.receive()
                        }

                    } catch (cancellationException: CancellationException) {
                        logger.info("Coroutine cancelled, exiting receive loop")

                    } catch (receiveException: Exception) {
                        logger.info("Error during message receiving: ${receiveException.message}")
                        receiveException.printStackTrace()
                    } finally {
                        val reason = closeReason.await()
                        logger.info("Connection closed: ${reason?.message ?: "Unknown reason"}")
                        connection = null

                        if (!connectionDeferred.isCompleted) {
                            connectionDeferred.completeExceptionally(Exception("Connection failed"))
                        }
                    }
                } else {
                    val reason = closeReason.await()
                    val errorMsg = "Failed to connect: ${reason?.message ?: "Connection not active"}"
                    logger.info(errorMsg)

                    if (!connectionDeferred.isCompleted) {
                        connectionDeferred.completeExceptionally(Exception(errorMsg))
                    }
                }
            }
        } catch (e: Exception) {
            logger.info("Exception during WebSocket connection: ${e.message}")
            e.printStackTrace()

            if (!connectionDeferred.isCompleted) {
                connectionDeferred.completeExceptionally(e)
            }
        }
    }

    fun withConnection(timeoutMs: Long = 10000, block: suspend Connection.() -> Unit) = runBlocking {

        try {
            logger.info("Waiting for connection...")
            val connection = withTimeout(timeoutMs) {
                connectionDeferred.await()
            }

            logger.info("Connection acquired, executing block...")
            connection.block()

        } catch (e: TimeoutCancellationException) {
            logger.info("Timeout waiting for connection ($timeoutMs ms)")

        } catch (e: Exception) {
            logger.info("Error waiting for connection: ${e.message}")
            e.printStackTrace()
        }

    }

    fun close() = runBlocking {

        logger.info("Initiating LumenClient shutdown...")

        try {
            withTimeout(15000) {
                if (connectionDeferred.isCompleted) {
                    connectionDeferred.await()
                }
            }

            connection = null
            httpClient.close()
            logger.info("Closed connection and HTTP client...")

            job.complete()

            logger.info("LumenClient closed successfully.")

        } catch (e: TimeoutCancellationException) {
            logger.info("Timeout during shutdown, forcing close...")
            httpClient.close()

        } catch (e: Exception) {
            logger.info("Error during shutdown: ${e.message}")
            e.printStackTrace()
            httpClient.close()
        }
    }

}

class LumenClientContext {

    var id = UUID.randomUUID().toString()
    var host = "0.0.0.0"
    var port = 5000
    private val credentials = LumenCredentials()

    fun credentials(block: LumenCredentials.() -> Unit) {
        credentials.block()
    }

    fun build(): LumenClient = LumenClient(
        id, host, port, credentials
    )
}

fun lumenClient(block: LumenClientContext.() -> Unit): LumenClient {
    val lumenClientContext = LumenClientContext()
    lumenClientContext.block()
    return lumenClientContext.build()
}