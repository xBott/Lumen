package me.bottdev.lumenclient

import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import me.bottdev.lumencore.IMessageIO
import me.bottdev.lumencore.MessageQueue
import me.bottdev.lumencore.messages.ILumenMessage
import me.bottdev.lumencore.wrapper.IMessageWrapper
import me.bottdev.lumencore.wrapper.types.BroadcastMessageWrapper
import me.bottdev.lumencore.wrapper.types.ServerMessageWrapper

class Connection(
    private val client: LumenClient,
    private val session: DefaultWebSocketSession
) : IMessageIO {

    override val codec = client.codec
    override val wrapperHandler = client.wrapperHandler
    override val messageQueue = MessageQueue(wrapperHandler)

    override suspend fun receive() {
        session.apply {
            incoming.consumeEach { frame ->
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    handleMessage(text)
                }
            }
        }
    }

    override suspend fun send(wrappedMessage: IMessageWrapper) {
        session.apply {
            val encodedMessage = codec.encode(wrappedMessage)
            send(Frame.Text(encodedMessage))
        }
    }

    suspend fun server(message: ILumenMessage) {
        val wrappedMessage = ServerMessageWrapper(client.id, message)
        send(wrappedMessage)
    }

    suspend fun broadcast(message: ILumenMessage, self: Boolean = false) {
        val wrappedMessage = BroadcastMessageWrapper(client.id, self, message)
        send(wrappedMessage)
    }

}