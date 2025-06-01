package me.bottdev.lumenclient

import me.bottdev.lumencore.IMessageIO
import me.bottdev.lumencore.messages.ILumenMessage
import me.bottdev.lumencore.messages.types.channels.SubscribeChannelMessage
import me.bottdev.lumencore.messages.types.channels.UnsubscribeChannelMessage

interface ILumenClient : IMessageIO {

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
        sendServer {
            message = SubscribeChannelMessage(channelId)
        }
    }

    fun unsubscribe(channelId: String) {
        sendServer {
            message = UnsubscribeChannelMessage(channelId)
        }
    }

}