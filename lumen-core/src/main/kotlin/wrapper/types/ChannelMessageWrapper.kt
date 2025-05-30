package me.bottdev.lumencore.wrapper.types

import me.bottdev.lumencore.messages.ILumenMessage
import me.bottdev.lumencore.wrapper.IMessageWrapper
import java.util.*

data class ChannelMessageWrapper(
    val from: String?,
    val channelId: String,
    val self: Boolean = false,
    override var payload: ILumenMessage
) : IMessageWrapper {
    override val id: String = UUID.randomUUID().toString()
}