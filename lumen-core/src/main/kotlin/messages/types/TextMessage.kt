package me.bottdev.lumencore.messages.types

import me.bottdev.lumencore.messages.IAckable
import me.bottdev.lumencore.messages.ILumenMessage

data class TextMessage(
    val text: String,
) : ILumenMessage, IAckable