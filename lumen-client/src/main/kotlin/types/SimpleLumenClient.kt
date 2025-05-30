package me.bottdev.lumenclient.types

import me.bottdev.lumenclient.ILumenClient
import me.bottdev.lumenclient.LumenCredentials
import me.bottdev.lumenclient.Metadata
import me.bottdev.lumencore.MessageCodec
import me.bottdev.lumencore.MessageHandler
import me.bottdev.lumencore.MessageQueue
import me.bottdev.lumencore.messages.types.*
import me.bottdev.lumencore.messages.types.channels.SubscribeChannelMessage
import me.bottdev.lumencore.messages.types.channels.UnsubscribeChannelMessage
import me.bottdev.lumencore.messages.types.handshake.HandshakeRequestMessage
import me.bottdev.lumencore.messages.types.handshake.HandshakeResponseMessage
import me.bottdev.lumencore.messages.types.metadata.AddClientMetadataMessage
import me.bottdev.lumencore.messages.types.metadata.RemoveClientMetadataMessage
import me.bottdev.lumencore.wrapper.IMessageWrapper
import me.bottdev.lumencore.wrapper.WrapperHandler
import me.bottdev.lumencore.wrapper.types.DirectMessageWrapper
import okhttp3.*
import okio.ByteString
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CountDownLatch


class SimpleLumenClient(
    override val id: String,
    override val host: String,
    override val port: Int,
    override val credentials: LumenCredentials
) : ILumenClient {

    private val logger = LoggerFactory.getLogger("LumenClient-$id")

    override val codec = MessageCodec.default
    private val messageHandler = MessageHandler().apply {
        register(HandshakeResponseMessage::class.java) { message, _ ->
            logger.info("[Handshake] Success!")
            metadata.setClients(message.clients.map {
                Metadata.Client(it.id, it.address, it.channels)
            })
        }
        register(AddClientMetadataMessage::class.java) { message, _ ->
            logger.info("[Metadata] adding new client: ${message.id}")
            metadata.addClient(Metadata.Client(message.id, message.address, message.channels))
        }
        register(RemoveClientMetadataMessage::class.java) { message, _ ->
            logger.info("[Metadata] removing client: ${message.id}")
            metadata.removeClient(message.id)
        }
        register(SubscribeChannelMessage::class.java) { message, _ ->
            logger.info("[Channels] Subscribed to channel ${message.channelId}")
            _channels.add(message.channelId)
        }
        register(UnsubscribeChannelMessage::class.java) { message, _ ->
            logger.info("[Channels] Unsubscribed from channel ${message.channelId}")
            _channels.remove(message.channelId)
        }
        register(TextMessage::class.java) { message, from ->
            logger.info("[Text] from $from: ${message.text}")
        }
        register(RoutedMessage::class.java) { message, _ ->
            val routedMessage = codec.decodeMessage(message.raw)
            handle(routedMessage)
        }
        register(AckMessage::class.java) { message, from ->
            logger.info("[Ack] $from has received ${message.messageId} ${message.name}")
        }
    }
    override val wrapperHandler = WrapperHandler(messageHandler).apply {
        register(DirectMessageWrapper::class.java) { wrapper, handler ->
            handler.handle(wrapper.payload, wrapper.from)
        }
    }
    override val messageQueue: MessageQueue = MessageQueue(this, wrapperHandler)

    private var socket: WebSocket? = null
    private var running = false
    override val isConnected: Boolean
        get() = running

    override var metadata: Metadata = Metadata(this)
    private val _channels = mutableSetOf<String>()
    private val channels: Set<String>
        get() = _channels.toSet()

    private val client = OkHttpClient()

    override fun start() {

        logger.info("Starting LumenClient...")

        try {

            val latch = CountDownLatch(1)
            val url = "ws://$host:$port/connect?id=$id&username=${credentials.username}&password=${credentials.password}"

            val request = Request.Builder()
                .url(url)
                .build()

            socket = client.newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    running = true
                    logger.info("Connected to server!")
                    sendServer(HandshakeRequestMessage())
                    latch.countDown()
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    handleMessage(text)
                }

                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    logger.info("Received binary data.")
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    logger.info("Closing: $reason")
                    webSocket.close(1000, null)
                    running = false
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    logger.info("Connection error: " + t.message)
                    running = false
                }
            })
            latch.await()
        } catch (ex: InterruptedException) {
            ex.printStackTrace()
        }
    }

    override fun stop() {

        if (socket != null) {

            logger.info("Initiating LumenClient shutdown...")

            socket!!.close(1000, "Client shutdown")
            running = false

            logger.info("LumenClient closed successfully.")

        }

    }

    override fun send(wrappedMessage: IMessageWrapper) {
        socket?.apply {
            val encodedMessage = codec.encodeWrapper(wrappedMessage)
            send(encodedMessage)
        }
    }

}

class SimpleLumenClientContext {

    var id = UUID.randomUUID().toString()
    var host = "0.0.0.0"
    var port = 5000
    private val credentials = LumenCredentials()

    fun credentials(block: LumenCredentials.() -> Unit) {
        credentials.block()
    }

    fun build(): SimpleLumenClient = SimpleLumenClient(
        id, host, port, credentials
    )
}

fun simpleLumenClient(block: SimpleLumenClientContext.() -> Unit): SimpleLumenClient {
    val lumenClientContext = SimpleLumenClientContext()
    lumenClientContext.block()
    return lumenClientContext.build()
}