package me.bottdev.lumencore

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import me.bottdev.lumencore.messages.IAckable
import me.bottdev.lumencore.messages.types.AckMessage
import me.bottdev.lumencore.wrapper.IAckWrapper
import me.bottdev.lumencore.wrapper.IMessageWrapper
import me.bottdev.lumencore.handlers.WrapperHandler
import me.bottdev.lumencore.wrapper.types.DirectMessageWrapper

class MessageQueue(private val io: IMessageIO, private val wrapperHandler: WrapperHandler) {

    private var isHandling = false
    private val scope = CoroutineScope(Dispatchers.Default)
    private val channel = Channel<IMessageWrapper>(Channel.UNLIMITED)

    fun add(wrapper: IMessageWrapper) {
        channel.trySend(wrapper)
        handleQueue()
    }

    private fun handleQueue() {
        if (isHandling) return
        isHandling = true

        scope.launch {
            while (true) {
                val wrappedMessage = channel.tryReceive().getOrNull() ?: break
                handleAck(wrappedMessage)
                handleWrapper(wrappedMessage)
            }
            isHandling = false
        }

    }

    private fun handleWrapper(wrappedMessage: IMessageWrapper) {

        wrapperHandler.handle(wrappedMessage)


    }

    private fun handleAck(wrappedMessage: IMessageWrapper) {

        val payload = wrappedMessage.payload

        if (payload is IAckable && wrappedMessage is IAckWrapper) {

            if (!payload.shouldAck) return

            val name = payload::class.java.simpleName
            val to = wrappedMessage.ackTo
            val from = wrappedMessage.ackFrom

            if (from != null && to != null && to != from) {

                val ackMessage = AckMessage(name, wrappedMessage.id)
                val ackWrappedMessage = DirectMessageWrapper(from, to, ackMessage)
                io.send(ackWrappedMessage)
            }

        }

    }

}