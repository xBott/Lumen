package me.bottdev.lumencore.messages.types.handshake

import me.bottdev.lumencore.messages.IAckable
import me.bottdev.lumencore.messages.ILumenMessage

class HandshakeRequestMessage(override val shouldAck: Boolean = false) : ILumenMessage, IAckable