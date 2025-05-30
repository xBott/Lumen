package me.bottdev.lumencore

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import me.bottdev.lumencore.messages.ILumenMessage
import me.bottdev.lumencore.messages.types.AckMessage
import me.bottdev.lumencore.messages.types.TextMessage
import me.bottdev.lumencore.messages.types.RoutedMessage
import me.bottdev.lumencore.messages.types.channels.SubscribeChannelMessage
import me.bottdev.lumencore.messages.types.channels.UnsubscribeChannelMessage
import me.bottdev.lumencore.messages.types.handshake.HandshakeRequestMessage
import me.bottdev.lumencore.messages.types.handshake.HandshakeResponseMessage
import me.bottdev.lumencore.messages.types.metadata.AddClientMetadataMessage
import me.bottdev.lumencore.messages.types.metadata.RemoveClientMetadataMessage
import me.bottdev.lumencore.wrapper.IMessageWrapper

class MessageCodec {

    companion object {

        @JvmStatic
        val default = MessageCodec().apply {
            registerType(HandshakeRequestMessage::class.java)
            registerType(HandshakeResponseMessage::class.java)
            registerType(AddClientMetadataMessage::class.java)
            registerType(RemoveClientMetadataMessage::class.java)
            registerType(SubscribeChannelMessage::class.java)
            registerType(UnsubscribeChannelMessage::class.java)
            registerType(TextMessage::class.java)
            registerType(RoutedMessage::class.java)
            registerType(AckMessage::class.java)
        }

    }

    class ILumenMessageDeserializer(
        private val typeMap: Map<String, Class<out ILumenMessage>>
    ) : JsonDeserializer<ILumenMessage>() {

        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ILumenMessage {
            val node = p.codec.readTree<ObjectNode>(p)
            val typeName = node.get("type")?.asText()

            val clazz = typeMap[typeName]
            return if (clazz != null) {
                p.codec.treeToValue(node, clazz)
            } else {
                RoutedMessage(node.toString())
            }
        }

    }

    class MessageWrapperDeserializer(
        private val messageDeserializer: ILumenMessageDeserializer
    ) : JsonDeserializer<IMessageWrapper>() {

        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): IMessageWrapper {
            val node = p.codec.readTree<ObjectNode>(p)
            val payloadNode = node.get("payload") as ObjectNode
            val id = node.get("id").toString()

            val payloadParser = payloadNode.traverse(p.codec)
            val payload = messageDeserializer.deserialize(payloadParser, ctxt)

            return object : IMessageWrapper {
                override val id: String = id
                override var payload: ILumenMessage = payload
            }
        }
    }


    private val registeredSerializers = mutableMapOf<String, Class<out ILumenMessage>>()

    private var objectMapper: ObjectMapper = buildMapper()

    private fun buildMapper(): ObjectMapper {

        val messageDeserializer = ILumenMessageDeserializer(registeredSerializers)
        val module = SimpleModule().apply {
            addDeserializer(ILumenMessage::class.java, messageDeserializer)
            addDeserializer(IMessageWrapper::class.java, MessageWrapperDeserializer(messageDeserializer))
        }

        return ObjectMapper()
            .registerModule(KotlinModule.Builder().build())
            .registerModule(module)
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    private fun <T : ILumenMessage> isRegistered(clazz: Class<T>): Boolean = registeredSerializers.containsKey(clazz.simpleName)

    fun <T : ILumenMessage> registerType(
        clazz: Class<T>,
    ) {
        if (isRegistered(clazz)) return
        registeredSerializers[clazz.simpleName] = clazz
        objectMapper = buildMapper()
    }

    fun encodeWrapper(wrappedMessage: IMessageWrapper): String {

        val payload = wrappedMessage.payload

        val className = payload::class.java.simpleName
        if (!isRegistered(payload::class.java)) error("$className is not registered in codec!")

        val node = objectMapper.valueToTree<ObjectNode>(wrappedMessage)
        val payloadNode = node.get("payload") as ObjectNode
        payloadNode.put("type", className)

        return objectMapper.writeValueAsString(node)

    }

    fun decodeWrapper(encodedWrappedMessage: String): IMessageWrapper {
        return objectMapper.readValue(encodedWrappedMessage)
    }

    fun decodeMessage(encodedMessage: String): ILumenMessage {
        return objectMapper.readValue(encodedMessage)
    }

}