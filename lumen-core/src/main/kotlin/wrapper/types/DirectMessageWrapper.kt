package me.bottdev.lumencore.wrapper.types

import me.bottdev.lumencore.messages.ILumenMessage
import me.bottdev.lumencore.wrapper.IAckWrapper
import me.bottdev.lumencore.wrapper.IMessageWrapper
import java.util.*

data class DirectMessageWrapper(
    val from: String?,
    val to: String,
    override var payload: ILumenMessage
) : IMessageWrapper, IAckWrapper {
    override val id: String = UUID.randomUUID().toString()
    override val ackFrom: String = to
    override val ackTo: String? = from
}