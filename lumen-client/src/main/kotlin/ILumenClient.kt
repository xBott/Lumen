package me.bottdev.lumenclient

import me.bottdev.lumencore.IMessageIO
import me.bottdev.lumencore.messages.ILumenMessage
import me.bottdev.lumencore.messages.types.channels.SubscribeChannelMessage
import me.bottdev.lumencore.messages.types.channels.UnsubscribeChannelMessage
import me.bottdev.lumencore.wrapper.types.BroadcastMessageWrapper
import me.bottdev.lumencore.wrapper.types.ChannelMessageWrapper
import me.bottdev.lumencore.wrapper.types.ServerMessageWrapper

interface ILumenClient : IMessageIO {

    val id: String
    val host: String
    val port: Int
    val credentials: LumenCredentials

    val isConnected: Boolean

    val metadata: Metadata

    fun <T : ILumenMessage> registerMessage(clazz: Class<T>, handler: (T, String?) -> Unit) {
        codec.registerType(clazz)
        wrapperHandler.messageHandler.register(clazz, handler)
    }

    fun start()

    fun stop()

    fun subscribe(channelId: String) {
        sendServer(SubscribeChannelMessage(channelId))
    }

    fun unsubscribe(channelId: String) {
        sendServer(UnsubscribeChannelMessage(channelId))
    }

    fun sendServer(message: ILumenMessage) {
        val wrappedMessage = ServerMessageWrapper(id, message)
        send(wrappedMessage)
    }

    fun sendBroadcast(message: ILumenMessage, self: Boolean = false) {
        val wrappedMessage = BroadcastMessageWrapper(id, self, message)
        send(wrappedMessage)
    }

    fun sendChannel(message: ILumenMessage, channelId: String, self: Boolean = false) {
        val wrappedMessage = ChannelMessageWrapper(id, channelId, self, message)
        send(wrappedMessage)
    }

}