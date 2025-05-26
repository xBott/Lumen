package me.bottdev.lumencore

import kotlinx.serialization.KSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import me.bottdev.lumencore.messages.ILumenMessage
import me.bottdev.lumencore.messages.types.AddClientMetadataMessage
import me.bottdev.lumencore.messages.types.HandshakeRequestMessage
import me.bottdev.lumencore.messages.types.HandshakeResponseMessage
import me.bottdev.lumencore.messages.types.RemoveClientMetadataMessage
import me.bottdev.lumencore.wrapper.IMessageWrapper
import me.bottdev.lumencore.wrapper.types.BroadcastMessageWrapper
import me.bottdev.lumencore.wrapper.types.DirectMessageWrapper
import me.bottdev.lumencore.wrapper.types.ServerMessageWrapper
import kotlin.reflect.KClass

class MessageCodec {

    companion object {

        val default = MessageCodec().apply {
            registerType(HandshakeRequestMessage::class, HandshakeRequestMessage.serializer())
            registerType(HandshakeResponseMessage::class, HandshakeResponseMessage.serializer())
            registerType(AddClientMetadataMessage::class, AddClientMetadataMessage.serializer())
            registerType(RemoveClientMetadataMessage::class, RemoveClientMetadataMessage.serializer())
        }

    }

    private val registeredSerializers = mutableListOf<PolymorphicSerializerEntry>()

    private lateinit var json: Json

    data class PolymorphicSerializerEntry(
        val clazz: KClass<out ILumenMessage>,
        val serializer: KSerializer<out ILumenMessage>
    )

    private fun <T : ILumenMessage> isRegistered(clazz: KClass<T>): Boolean = registeredSerializers.any { it.clazz == clazz }

    fun <T : ILumenMessage> registerType(
        clazz: KClass<T>,
        serializer: KSerializer<T>
    ) {
        if (isRegistered(clazz)) return
        registeredSerializers.add(
            PolymorphicSerializerEntry(clazz, serializer)
        )
        json = buildJson()
    }

    private fun buildJson(): Json {
        return Json {
            serializersModule = SerializersModule {
                polymorphic(IMessageWrapper::class) {
                    subclass(BroadcastMessageWrapper::class)
                    subclass(DirectMessageWrapper::class)
                    subclass(ServerMessageWrapper::class)
                }
                polymorphic(ILumenMessage::class) {
                    registeredSerializers.forEach { (clazz, serializer) ->
                        @Suppress("UNCHECKED_CAST")
                        subclass(clazz as KClass<ILumenMessage>, serializer as KSerializer<ILumenMessage>)
                    }
                }
            }
        }
    }

    fun encode(wrappedMessage: IMessageWrapper): String {
        val message = wrappedMessage.payload
        if (!isRegistered(message::class)) error("${wrappedMessage::class.simpleName} message type is not registered!")
        return json.encodeToString(wrappedMessage)
    }

    fun decode(encodedWrappedMessage: String): IMessageWrapper {
        return json.decodeFromString(encodedWrappedMessage)
    }

}