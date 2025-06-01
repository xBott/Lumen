package me.bottdev.lumencore

import me.bottdev.lumencore.handlers.AckHandler
import me.bottdev.lumencore.handlers.WrapperHandler
import me.bottdev.lumencore.messages.IAckable
import me.bottdev.lumencore.messages.ILumenMessage
import me.bottdev.lumencore.messages.types.AckMessage
import me.bottdev.lumencore.wrapper.IMessageWrapper
import me.bottdev.lumencore.wrapper.types.BroadcastMessageWrapper
import me.bottdev.lumencore.wrapper.types.ChannelMessageWrapper
import me.bottdev.lumencore.wrapper.types.DirectMessageWrapper
import me.bottdev.lumencore.wrapper.types.ServerMessageWrapper

interface IMessageIO {

    val id: String
    val codec: MessageCodec
    val ackHandler: AckHandler
    val wrapperHandler: WrapperHandler
    val messageQueue: MessageQueue

    fun handleMessage(encodedWrappedMessage: String) {
        val wrappedMessage = codec.decodeWrapper(encodedWrappedMessage)
        messageQueue.add(wrappedMessage)
    }

    fun send(value: String)

    fun sendWrapper(wrappedMessage: IMessageWrapper) {
        val encodedMessage = codec.encodeWrapper(wrappedMessage)
        send(encodedMessage)
    }

    fun <T : IMessageWrapper> send(details: SendDetails<T>) {

        val wrappedMessage = details.wrappedMessage
        sendWrapper(wrappedMessage)

        val payload = wrappedMessage.payload
        if (payload !is IAckable) return
        if (!payload.shouldAck) return

        val ackTask = AckHandler.Task(
            io = this,
            wrappedMessage = wrappedMessage,
            timeOut = details.timeOut,
            resendTimes = details.resendTimes,
            onSuccess = details.onAckSuccess,
            onTimeout = details.onAckTimeout,
            onResend = details.onAckResend
        )

        ackHandler.register(wrappedMessage.id, ackTask)

    }

    fun <T : IMessageWrapper> send(
        wrappedMessage: T,
        timeOut: Long = -1L,
        resendTimes: Int = 0,
        onAckSuccess: ((IMessageWrapper, AckMessage) -> Unit)? = null,
        onAckTimeout: ((IMessageWrapper) -> Unit)? = null,
        onAckResend: ((IMessageWrapper, Int) -> Unit)? = null
    ) {

        val details = SendDetails(
            wrappedMessage,
            timeOut,
            resendTimes,
            onAckSuccess,
            onAckTimeout,
            onAckResend
        )

        send(details)

    }

    fun sendServer(block: SendContext.Server.() -> Unit) {
        val context = SendContext.Server()
        context.block()
        val details = context.build(this) ?: return
        send(details)
    }

    fun sendDirect(block: SendContext.Direct.() -> Unit) {
        val context = SendContext.Direct()
        context.block()
        val details = context.build(this) ?: return
        send(details)
    }

    fun sendBroadcast(block: SendContext.Broadcast.() -> Unit) {
        val context = SendContext.Broadcast()
        context.block()
        val details = context.build(this) ?: return
        send(details)
    }

    fun sendChannel(block: SendContext.Channel.() -> Unit) {
        val context = SendContext.Channel()
        context.block()
        val details = context.build(this) ?: return
        send(details)
    }

    class SendDetails<T : IMessageWrapper>(
        val wrappedMessage: T,
        val timeOut: Long = -1L,
        val resendTimes: Int = 0,
        val onAckSuccess: ((IMessageWrapper, AckMessage) -> Unit)? = null,
        val onAckTimeout: ((IMessageWrapper) -> Unit)? = null,
        val onAckResend: ((IMessageWrapper, Int) -> Unit)? = null
    )

    sealed class SendContext<T : IMessageWrapper> {

        var message: ILumenMessage? = null
        var timeOut: Long = -1
        var resendTimes: Int = 0
        var onAckSuccess: ((IMessageWrapper, AckMessage) -> Unit)? = null
        var onAckTimeout: ((IMessageWrapper) -> Unit)? = null
        var onAckResend: ((IMessageWrapper, Int) -> Unit)? = null

        abstract fun build(io: IMessageIO): SendDetails<T>?

        class Server : SendContext<ServerMessageWrapper>() {

            override fun build(io: IMessageIO): SendDetails<ServerMessageWrapper>? {
                return message?.let {
                    SendDetails(
                        ServerMessageWrapper(io.id, message!!),
                        timeOut,
                        resendTimes,
                        onAckSuccess,
                        onAckTimeout,
                        onAckResend
                    )
                }
            }

        }

        class Direct : SendContext<DirectMessageWrapper>() {

            var to = "unknown"

            override fun build(io: IMessageIO): SendDetails<DirectMessageWrapper>? {
                return message?.let {
                    SendDetails(
                        DirectMessageWrapper(io.id, to, message!!),
                        timeOut,
                        resendTimes,
                        onAckSuccess,
                        onAckTimeout,
                        onAckResend
                    )
                }
            }

        }

        class Broadcast : SendContext<BroadcastMessageWrapper>() {

            var self = false

            override fun build(io: IMessageIO): SendDetails<BroadcastMessageWrapper>? {
                return message?.let {
                    SendDetails(
                        BroadcastMessageWrapper(io.id, self, message!!),
                        timeOut,
                        resendTimes,
                        onAckSuccess,
                        onAckTimeout,
                        onAckResend
                    )
                }
            }

        }

        class Channel : SendContext<ChannelMessageWrapper>() {

            var channelId = "unknown"
            var self = false

            override fun build(io: IMessageIO): SendDetails<ChannelMessageWrapper>? {
                return message?.let {
                    SendDetails(
                        ChannelMessageWrapper(io.id, channelId, self, message!!),
                        timeOut,
                        resendTimes,
                        onAckSuccess,
                        onAckTimeout,
                        onAckResend
                    )
                }
            }

        }

    }

}