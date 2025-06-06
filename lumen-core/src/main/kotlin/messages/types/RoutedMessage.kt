package me.bottdev.lumencore.messages.types

import me.bottdev.lumencore.messages.IAckable
import me.bottdev.lumencore.messages.ILumenMessage

data class RoutedMessage(
    val raw: String,
    override val shouldAck: Boolean = false
) : ILumenMessage, IAckable