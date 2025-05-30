package me.bottdev.lumencore

import me.bottdev.lumencore.messages.ILumenMessage

fun interface MessageHandlerBlock<T : ILumenMessage> {
    fun handle(message: T, from: String?)
}

class MessageHandler {

    private val registeredTypes = mutableMapOf<Class<out ILumenMessage>, (ILumenMessage, String?) -> Unit>()

    private fun isRegistered(clazz: Class<out ILumenMessage>): Boolean =
        registeredTypes.containsKey(clazz)

    fun <T : ILumenMessage> register(clazz: Class<T>, block: MessageHandlerBlock<T>) {
        if (isRegistered(clazz)) return

        registeredTypes[clazz] = { message, from ->
            if (clazz.isInstance(message)) {
                @Suppress("UNCHECKED_CAST")
                block.handle(message as T, from)
            }
        }
    }

    fun get(clazz: Class<out ILumenMessage>): ((ILumenMessage, String?) -> Unit)? =
        registeredTypes[clazz]

    fun handle(message: ILumenMessage, from: String? = null) {
        get(message.javaClass)?.invoke(message, from)
    }
}