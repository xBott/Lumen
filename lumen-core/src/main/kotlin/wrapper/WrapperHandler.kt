package me.bottdev.lumencore.wrapper

import me.bottdev.lumencore.MessageHandler

fun interface WrapperHandlerBlock<T : IMessageWrapper> {
    fun handle(wrapper: T, handler: MessageHandler)
}

class WrapperHandler(val messageHandler: MessageHandler) {

    private val registeredTypes = mutableMapOf<Class<out IMessageWrapper>, (IMessageWrapper) -> Unit>()

    private fun isRegistered(clazz: Class<out IMessageWrapper>): Boolean =
        registeredTypes.containsKey(clazz)

    fun <T : IMessageWrapper> register(clazz: Class<T>, block: WrapperHandlerBlock<T>) {
        if (isRegistered(clazz)) return

        registeredTypes[clazz] = { message ->
            if (clazz.isInstance(message)) {
                @Suppress("UNCHECKED_CAST")
                block.handle(message as T, messageHandler)
            }
        }
    }

    fun get(clazz: Class<out IMessageWrapper>): ((IMessageWrapper) -> Unit)? =
        registeredTypes[clazz]

    fun handle(wrappedMessage: IMessageWrapper) {
        get(wrappedMessage.javaClass)?.invoke(wrappedMessage)
    }
}