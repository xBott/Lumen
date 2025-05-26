package me.bottdev.lumencore.wrapper.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.bottdev.lumencore.messages.ILumenMessage
import me.bottdev.lumencore.wrapper.IMessageWrapper

@Serializable
@SerialName("Server")
data class ServerMessageWrapper(
    val from: String,
    override val payload: ILumenMessage
) : IMessageWrapper