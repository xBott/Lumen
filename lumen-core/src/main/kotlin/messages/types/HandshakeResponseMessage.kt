package me.bottdev.lumencore.messages.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.bottdev.lumencore.messages.ILumenMessage

@Serializable
@SerialName("HandshakeResponse")
data class HandshakeResponseMessage(
    val clients: List<ClientInfo> = listOf()
) : ILumenMessage {

    @Serializable
    data class ClientInfo(
        val id: String,
        val address: String
    )

}