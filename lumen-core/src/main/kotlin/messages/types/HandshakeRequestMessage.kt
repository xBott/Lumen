package me.bottdev.lumencore.messages.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.bottdev.lumencore.messages.ILumenMessage

@Serializable
@SerialName("HandshakeRequest")
class HandshakeRequestMessage : ILumenMessage