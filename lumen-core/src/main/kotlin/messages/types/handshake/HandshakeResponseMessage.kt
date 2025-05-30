package me.bottdev.lumencore.messages.types.handshake

import me.bottdev.lumencore.messages.ILumenMessage

data class HandshakeResponseMessage(
    val clients: List<ClientInfo> = listOf()
) : ILumenMessage {

    data class ClientInfo(
        val id: String,
        val address: String,
        val channels: Set<String>
    )

}