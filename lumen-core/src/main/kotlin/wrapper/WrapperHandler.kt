package me.bottdev.lumencore.wrapper

import me.bottdev.lumencore.MessageHandler
import kotlin.reflect.KClass

class WrapperHandler(private val messageHandler: MessageHandler) {

    private val registeredTypes = mutableMapOf<KClass<out IMessageWrapper>, (IMessageWrapper) -> Unit>()

    private fun isRegistered(clazz: KClass<out IMessageWrapper>): Boolean =
        registeredTypes.containsKey(clazz)

    fun <T : IMessageWrapper> register(clazz: KClass<T>, block: (T, MessageHandler) -> Unit) {
        if (isRegistered(clazz)) return

        registeredTypes[clazz] = { message ->
            if (clazz.isInstance(message)) {
                @Suppress("UNCHECKED_CAST")
                block(message as T, messageHandler)
            }
        }
    }

    fun get(clazz: KClass<out IMessageWrapper>): ((IMessageWrapper) -> Unit)? =
        registeredTypes[clazz]

    fun handle(message: IMessageWrapper) {
        get(message::class)?.invoke(message)
    }

}