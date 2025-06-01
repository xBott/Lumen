package me.bottdev.lumencore.wrapper.types

import com.fasterxml.jackson.annotation.JsonIgnore
import me.bottdev.lumencore.messages.ILumenMessage
import me.bottdev.lumencore.wrapper.IAckWrapper
import me.bottdev.lumencore.wrapper.IMessageWrapper
import java.util.UUID

data class ServerMessageWrapper(
    val from: String,
    override var payload: ILumenMessage,
    override var id: String = UUID.randomUUID().toString()
) : IMessageWrapper, IAckWrapper {

    @JsonIgnore
    override val ackFrom: String = "SERVER"
    @JsonIgnore
    override val ackTo: String = from

}