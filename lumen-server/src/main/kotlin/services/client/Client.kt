package me.bottdev.lumenserver.services.client

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import me.bottdev.lumencore.IMessageIO
import me.bottdev.lumencore.MessageQueue
import me.bottdev.lumencore.wrapper.IMessageWrapper
import me.bottdev.lumenserver.logger
import me.bottdev.lumenserver.models.User

class Client(
    val id: String,
    val address: String,
    val user: User,
    private val session: WebSocketServerSession? = null
) : IMessageIO {

    override val codec = ClientService.codec
    override val wrapperHandler = ClientService.wrapperHandler
    override val messageQueue = MessageQueue(wrapperHandler)

    override suspend fun receive() {
        val sess = session ?: error("Session is not active")
        try {
            for (frame in sess.incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()
                        handleMessage(text)
                    }
                    is Frame.Close -> {
                        logger.info("Client $id sent close frame")
                        break
                    }
                    else -> {}
                }
            }
        } catch (e: ClosedReceiveChannelException) {
            logger.info("Client $id disconnected")
        } catch (e: Exception) {
            logger.error("Error while receiving from client ${id}: ${e.message}", e)
        } finally {
            logger.info("Receive ended for client $id")
        }
    }

    override suspend fun send(wrappedMessage: IMessageWrapper) {
        session?.apply {
            val encodedMessage = codec.encode(wrappedMessage)
            send(Frame.Text(encodedMessage))
        }
    }

}