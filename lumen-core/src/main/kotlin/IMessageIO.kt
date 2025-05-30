package me.bottdev.lumencore

import me.bottdev.lumencore.wrapper.IMessageWrapper
import me.bottdev.lumencore.wrapper.WrapperHandler

interface IMessageIO {

    val codec: MessageCodec
    val wrapperHandler: WrapperHandler
    val messageQueue: MessageQueue

    fun handleMessage(encodedWrappedMessage: String) {
        val wrappedMessage = codec.decodeWrapper(encodedWrappedMessage)
        messageQueue.add(wrappedMessage)
    }

    fun send(wrappedMessage: IMessageWrapper)

}