package me.bottdev.lumencore.messages.types.metadata

import me.bottdev.lumencore.messages.IAckable
import me.bottdev.lumencore.messages.ILumenMessage

data class AddClientMetadataMessage(
    val id: String,
    val address: String,
    val channels: Set<String>,
    override val shouldAck: Boolean = false
) : ILumenMessage, IAckable