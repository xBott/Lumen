package me.bottdev.lumencore.messages.types.metadata

import me.bottdev.lumencore.messages.IAckable
import me.bottdev.lumencore.messages.ILumenMessage

data class RemoveClientMetadataMessage(
    val id: String,
) : ILumenMessage, IAckable