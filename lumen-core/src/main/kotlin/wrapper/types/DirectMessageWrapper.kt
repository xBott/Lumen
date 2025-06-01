package me.bottdev.lumencore.wrapper.types

import com.fasterxml.jackson.annotation.JsonIgnore
import me.bottdev.lumencore.messages.ILumenMessage
import me.bottdev.lumencore.wrapper.IAckWrapper
import me.bottdev.lumencore.wrapper.IMessageWrapper
import java.util.*

data class DirectMessageWrapper(
    val from: String?,
    val to: String,
    override var payload: ILumenMessage,
    override var id: String = UUID.randomUUID().toString()
) : IMessageWrapper, IAckWrapper {

    @JsonIgnore
    override val ackFrom: String = to
    @JsonIgnore
    override val ackTo: String? = from

}