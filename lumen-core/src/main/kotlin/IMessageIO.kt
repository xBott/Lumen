package me.bottdev.lumencore

import me.bottdev.lumencore.wrapper.IMessageWrapper
import me.bottdev.lumencore.wrapper.WrapperHandler

interface IMessageIO {

    val codec: MessageCodec
    val wrapperHandler: WrapperHandler
    val messageQueue: MessageQueue

    suspend fun receive()

    fun handleMessage(encodedWrappedMessage: String) {
        val wrappedMessage = codec.decode(encodedWrappedMessage)
        messageQueue.add(wrappedMessage)
    }

    suspend fun send(wrappedMessage: IMessageWrapper)

}