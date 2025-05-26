package me.bottdev.lumencore

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import me.bottdev.lumencore.wrapper.IMessageWrapper
import me.bottdev.lumencore.wrapper.WrapperHandler

class MessageQueue(private val wrapperHandler: WrapperHandler) {

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
                val wrapper = channel.tryReceive().getOrNull() ?: break
                handleWrapper(wrapper)
            }
            isHandling = false
        }

    }

    private fun handleWrapper(wrapper: IMessageWrapper) {
        wrapperHandler.handle(wrapper)
    }

}