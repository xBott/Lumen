package me.bottdev.lumenserver.services.client

import kotlinx.coroutines.runBlocking
import me.bottdev.lumencore.MessageCodec
import me.bottdev.lumencore.handlers.MessageHandler
import me.bottdev.lumencore.messages.ILumenMessage
import me.bottdev.lumencore.messages.types.channels.SubscribeChannelMessage
import me.bottdev.lumencore.messages.types.channels.UnsubscribeChannelMessage
import me.bottdev.lumencore.messages.types.metadata.AddClientMetadataMessage
import me.bottdev.lumencore.messages.types.handshake.HandshakeRequestMessage
import me.bottdev.lumencore.messages.types.handshake.HandshakeResponseMessage
import me.bottdev.lumencore.messages.types.metadata.RemoveClientMetadataMessage
import me.bottdev.lumencore.handlers.WrapperHandler
import me.bottdev.lumencore.wrapper.types.BroadcastMessageWrapper
import me.bottdev.lumencore.wrapper.types.ChannelMessageWrapper
import me.bottdev.lumencore.wrapper.types.DirectMessageWrapper
import me.bottdev.lumencore.wrapper.types.ServerMessageWrapper
import me.bottdev.lumenserver.logger

object ClientService {

    val codec = MessageCodec.default

    private val messageHandler = MessageHandler().apply {
        register(HandshakeRequestMessage::class.java) { _, from ->

            if (from == null) return@register

            val responseMessage = HandshakeResponseMessage(
                getAll().map { HandshakeResponseMessage.ClientInfo(it.id, it.address, it.channels) }
            )

            val directWrapper = DirectMessageWrapper(null, from, responseMessage)
            get(from)?.send(directWrapper)

        }

        register(SubscribeChannelMessage::class.java) { message, from ->

            if (from == null) return@register
            val channelId = message.channelId

            get(from)?.apply {
                val success = subscribeChannel(channelId)
                if (success) send(DirectMessageWrapper(null, from, SubscribeChannelMessage(channelId)))
            }

        }

        register(UnsubscribeChannelMessage::class.java) { message, from ->

            if (from == null) return@register
            val channelId = message.channelId

            get(from)?.apply {
                val success = unsubscribeChannel(channelId)
                if (success) send(DirectMessageWrapper(null, from, UnsubscribeChannelMessage(channelId)))
            }

        }

    }
    val wrapperHandler = WrapperHandler(messageHandler).apply {
        register(ServerMessageWrapper::class.java) { wrapper, messageHandler ->

            val from = wrapper.from
            logger.info("Handling server message from $from")
            messageHandler.handle(wrapper.payload, from)

        }

        register(BroadcastMessageWrapper::class.java) { wrapper, _ ->

            logger.info("Routing broadcast message to all clients")

            val id = wrapper.id
            val from = wrapper.from
            val self = wrapper.self
            val payload = wrapper.payload

            getAll().forEach { client ->
                if (from != client.id || self) {
                    logger.info(" > Message routed to ${client.id}")
                    val directWrapper = DirectMessageWrapper(from, client.id, payload, id)
                    client.send(directWrapper)
                }
            }

        }

        register(DirectMessageWrapper::class.java) { wrapper, _ ->

            val from = wrapper.from
            val to = wrapper.to

            logger.info("Routing direct message from $from to $to")

            get(to)?.send(wrapper)

        }

        register(ChannelMessageWrapper::class.java) { wrapper, _ ->

            val id = wrapper.id
            val from = wrapper.from
            val channelId = wrapper.channelId
            val self = wrapper.self
            val payload = wrapper.payload

            logger.info("Routing $channelId channel message from $from")

            getSubscribed(channelId).forEach { client ->
                if (from != client.id || self) {
                    logger.info(" > Message routed to ${client.id}")

                    val directWrapper = DirectMessageWrapper(from, client.id, payload, id)
                    client.send(directWrapper)
                }
            }

        }

    }

    private val clients = mutableMapOf<String, Client>()

    fun getAll(): List<Client> = clients.values.toList()

    fun getSubscribed(channelId: String): List<Client> = clients.values.filter { it.isSubscribed(channelId) }

    fun get(id: String): Client? = clients[id]

    fun contains(id: String): Boolean = clients.containsKey(id)

    fun add(client: Client) {
        val id = client.id

        if (clients.contains(id)) return
        clients[id] = client

        broadcast(AddClientMetadataMessage(client.id, client.address, client.channels), listOf(client.id))

        logger.info("Client ${client.id} successfully connected as ${client.user.username}!")

    }

    fun remove(id: String) {
        logger.info("Trying to disconnect client $id...")

        if (!clients.containsKey(id)) return
        clients.remove(id)

        broadcast(RemoveClientMetadataMessage(id), listOf(id))

        logger.info("Client $id successfully disconnected.")

    }

    fun broadcast(message: ILumenMessage, blacklist: List<String> = listOf()) {
        val allClients = getAll()
        if (allClients.isEmpty()) return
        allClients.forEach { client ->
            if (blacklist.contains(client.id)) return@forEach
            val directWrapper = DirectMessageWrapper("SERVER", client.id, message)
            runBlocking {
                client.send(directWrapper)
            }
        }
    }

}