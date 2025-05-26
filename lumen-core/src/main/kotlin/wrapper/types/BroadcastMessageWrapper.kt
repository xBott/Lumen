package me.bottdev.lumencore.wrapper.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.bottdev.lumencore.messages.ILumenMessage
import me.bottdev.lumencore.wrapper.IMessageWrapper

@Serializable
@SerialName("Broadcast")
data class BroadcastMessageWrapper(
    val from: String,
    val self: Boolean = true,
    override val payload: ILumenMessage
) : IMessageWrapper