package me.bottdev.lumenclient

import org.slf4j.LoggerFactory

data class Metadata(
    val client: LumenClient,
    val connectedClients: MutableList<Client> = mutableListOf()
) {

    private val logger = LoggerFactory.getLogger("LumenClient-${client.id}")

    data class Client(val id: String, val address: String)

    fun setClients(newClients: List<Client>) {
        connectedClients.clear()
        connectedClients.addAll(newClients.toMutableList())
        log()
    }

    fun addClient(client: Client) {
        if (connectedClients.any { it.id == client.id }) return
        connectedClients.add(client)
        log()

    }

    fun removeClient(id: String) {
        connectedClients.removeIf { it.id == id }
        log()
    }

    fun log() {
        logger.info("[Metadata] current clients:")
        connectedClients.forEach { info ->
            val line =
                if (info.id == client.id)
                    " > Client: ${info.id} with Address: ${info.address} (this)"
                else
                    " > Client: ${info.id} with Address: ${info.address}"
            logger.info(line)
        }
    }

}