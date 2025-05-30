package me.bottdev.lumencore.messages.types.channels

import me.bottdev.lumencore.messages.IAckable
import me.bottdev.lumencore.messages.ILumenMessage

data class SubscribeChannelMessage(
    val channelId: String,
) : ILumenMessage, IAckable