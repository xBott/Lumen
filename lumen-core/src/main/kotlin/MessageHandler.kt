package me.bottdev.lumencore

import me.bottdev.lumencore.messages.ILumenMessage
import kotlin.reflect.KClass

class MessageHandler {

    private val registeredTypes = mutableMapOf<KClass<out ILumenMessage>, (ILumenMessage, String?) -> Unit>()

    private fun isRegistered(clazz: KClass<out ILumenMessage>): Boolean =
        registeredTypes.containsKey(clazz)

    fun <T : ILumenMessage> register(clazz: KClass<T>, block: (T, String?) -> Unit) {
        if (isRegistered(clazz)) return

        registeredTypes[clazz] = { message, from ->
            if (clazz.isInstance(message)) {
                @Suppress("UNCHECKED_CAST")
                block(message as T, from)
            }
        }
    }

    fun get(clazz: KClass<out ILumenMessage>): ((ILumenMessage, String?) -> Unit)? =
        registeredTypes[clazz]

    fun handle(message: ILumenMessage, from: String? = null) {
        get(message::class)?.invoke(message, from)
    }

}