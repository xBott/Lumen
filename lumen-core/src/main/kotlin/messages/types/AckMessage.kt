package me.bottdev.lumencore.messages.types

import me.bottdev.lumencore.messages.ILumenMessage

data class AckMessage(
    val name: String,
    val messageId: String,
) : ILumenMessage