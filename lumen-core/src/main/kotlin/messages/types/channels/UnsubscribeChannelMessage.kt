package me.bottdev.lumencore.messages.types.channels

import me.bottdev.lumencore.messages.IAckable
import me.bottdev.lumencore.messages.ILumenMessage

data class UnsubscribeChannelMessage(
    val channelId: String,
    override val shouldAck: Boolean = false
) : ILumenMessage, IAckable