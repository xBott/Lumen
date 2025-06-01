package me.bottdev.lumencore.wrapper.types

import me.bottdev.lumencore.messages.ILumenMessage
import me.bottdev.lumencore.wrapper.IMessageWrapper
import java.util.*

data class BroadcastMessageWrapper(
    val from: String,
    val self: Boolean = true,
    override var payload: ILumenMessage,
    override var id: String = UUID.randomUUID().toString()
) : IMessageWrapper