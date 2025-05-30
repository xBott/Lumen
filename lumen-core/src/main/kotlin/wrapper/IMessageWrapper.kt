package me.bottdev.lumencore.wrapper

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import me.bottdev.lumencore.messages.ILumenMessage
import me.bottdev.lumencore.wrapper.types.BroadcastMessageWrapper
import me.bottdev.lumencore.wrapper.types.ChannelMessageWrapper
import me.bottdev.lumencore.wrapper.types.DirectMessageWrapper
import me.bottdev.lumencore.wrapper.types.ServerMessageWrapper

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = DirectMessageWrapper::class, name = "DirectMessageWrapper"),
    JsonSubTypes.Type(value = BroadcastMessageWrapper::class, name = "BroadcastMessageWrapper"),
    JsonSubTypes.Type(value = ChannelMessageWrapper::class, name = "ChannelMessageWrapper"),
    JsonSubTypes.Type(value = ServerMessageWrapper::class, name = "ServerMessageWrapper")
)
interface IMessageWrapper {

    val id: String
    var payload: ILumenMessage

}