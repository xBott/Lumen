package me.bottdev.lumencore.messages.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.bottdev.lumencore.messages.ILumenMessage

@Serializable
@SerialName("AddClient")
data class AddClientMetadataMessage(
    val id: String,
    val address: String
) : ILumenMessage