package me.bottdev.lumenserver.services.client

import io.ktor.server.websocket.*
import io.ktor.util.logging.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.runBlocking
import me.bottdev.lumencore.IMessageIO
import me.bottdev.lumencore.MessageQueue
import me.bottdev.lumencore.handlers.AckHandler
import me.bottdev.lumenserver.models.User

class Client(
    override val id: String,
    val address: String,
    val user: User,
    private val session: WebSocketServerSession? = null
) : IMessageIO {

    private val logger = KtorSimpleLogger("Client-$id")
    val channels = mutableSetOf<String>()

    override val codec = ClientService.codec
    override val ackHandler = AckHandler()
    override val wrapperHandler = ClientService.wrapperHandler
    override val messageQueue = MessageQueue(this, wrapperHandler)

    suspend fun receive() {
        logger.info("Receive started")
        val sess = session ?: error("Session is not active")
        try {
            for (frame in sess.incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()
                        handleMessage(text)
                    }
                    is Frame.Close -> {
                        logger.info("Sent close frame")
                        break
                    }
                    else -> {}
                }
            }
        } catch (e: ClosedReceiveChannelException) {
            logger.info("Disconnected")
        } catch (e: Exception) {
            logger.error("Error while receiving: ${e.message}", e)
        } finally {
            logger.info("Receive ended")
        }
    }

    override fun send(value: String) {
        session?.apply {
            runBlocking {
                send(Frame.Text(value))
            }
        }
    }

    fun isSubscribed(channelId: String): Boolean = channels.contains(channelId)

    fun subscribeChannel(channelId: String): Boolean {
        if (channels.contains(channelId)) return false
        channels.add(channelId)
        logger.info("Subscribed to channel $channelId")
        return true
    }

    fun unsubscribeChannel(channelId: String): Boolean {
        if (!channels.contains(channelId)) return false
        channels.remove(channelId)
        logger.info("Unsubscribed from channel $channelId")
        return true
    }

}