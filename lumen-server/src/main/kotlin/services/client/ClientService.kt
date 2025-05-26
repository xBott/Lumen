package me.bottdev.lumenserver.services.client

import kotlinx.coroutines.runBlocking
import me.bottdev.lumencore.MessageCodec
import me.bottdev.lumencore.MessageHandler
import me.bottdev.lumencore.messages.ILumenMessage
import me.bottdev.lumencore.messages.types.AddClientMetadataMessage
import me.bottdev.lumencore.messages.types.HandshakeRequestMessage
import me.bottdev.lumencore.messages.types.HandshakeResponseMessage
import me.bottdev.lumencore.messages.types.RemoveClientMetadataMessage
import me.bottdev.lumencore.wrapper.WrapperHandler
import me.bottdev.lumencore.wrapper.types.BroadcastMessageWrapper
import me.bottdev.lumencore.wrapper.types.DirectMessageWrapper
import me.bottdev.lumencore.wrapper.types.ServerMessageWrapper
import me.bottdev.lumenserver.logger
import java.util.concurrent.ConcurrentHashMap

object ClientService {

    val codec = MessageCodec.default

    private val messageHandler = MessageHandler().apply {
        register(HandshakeRequestMessage::class) { _, from ->

            if (from == null) return@register

            val responseMessage = HandshakeResponseMessage(
                getAll().map { HandshakeResponseMessage.ClientInfo(it.id, it.address) }
            )

            val directWrapper = DirectMessageWrapper(null, from, responseMessage)
            runBlocking {
                get(from)?.send(directWrapper)
            }

        }
    }
    val wrapperHandler = WrapperHandler(messageHandler).apply {
        register(ServerMessageWrapper::class) { wrapper, messageHandler ->
            val from = wrapper.from
            logger.info("Handling server message from $from")
            messageHandler.handle(wrapper.payload, from)
        }
        register(BroadcastMessageWrapper::class) { wrapper, _ ->

            logger.info("Routing broadcast message to all clients")

            val from = wrapper.from
            val self = wrapper.self
            val payload = wrapper.payload
            getAll().forEach { client ->
                if (from != client.id || self) {
                    val directWrapper = DirectMessageWrapper(from, client.id, payload)
                    runBlocking {
                        client.send(directWrapper)
                    }
                }
            }
        }
        register(DirectMessageWrapper::class) { wrapper, _ ->
            val from = wrapper.from
            val to = wrapper.to
            logger.info("Routing direct message from $from to $to")
            runBlocking {
                get(to)?.send(wrapper)
            }
        }
    }

    private val clients = mutableMapOf<String, Client>()

    fun getAll(): List<Client> = clients.values.toList()

    fun get(id: String): Client? = clients[id]

    fun add(client: Client) {
        val id = client.id

        if (clients.contains(id)) return
        clients[id] = client

        broadcast(AddClientMetadataMessage(client.id, client.address), listOf(client.id))

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