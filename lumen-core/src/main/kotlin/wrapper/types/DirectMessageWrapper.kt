package me.bottdev.lumencore.wrapper.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.bottdev.lumencore.messages.ILumenMessage
import me.bottdev.lumencore.wrapper.IMessageWrapper

@Serializable
@SerialName("Direct")
data class DirectMessageWrapper(
    val from: String?,
    val to: String,
    override val payload: ILumenMessage
) : IMessageWrapper