package me.bottdev.lumenserver.routes

import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import me.bottdev.lumenserver.logger
import me.bottdev.lumenserver.services.UserService
import me.bottdev.lumenserver.services.client.Client
import me.bottdev.lumenserver.services.client.ClientService

fun Application.connectionRoute() {
    routing {
        webSocket("/connect") {
            handleConnectionSocket()
        }
    }
}

private suspend fun DefaultWebSocketServerSession.handleConnectionSocket() {

    val id = call.request.queryParameters["id"]
    if (id == null) {
        logger.info("Missing client id")
        close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Missing client Id"))
        return
    }

    if (ClientService.contains(id)) {
        logger.info("Client with id \"$id\" is already connected!")
        close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Client with id \"$id\" is already connected!"))
        return
    }

    val username = call.request.queryParameters["username"]
    if (username == null) {
        logger.info("Missing user's name")
        close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Missing user's name"))
        return
    }

    val password = call.request.queryParameters["password"]
    if (password == null) {
        logger.info("Missing user's password")
        close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Missing user's password"))
        return
    }

    val user = UserService.authenticate(username, password)
    if (user == null) {
        logger.info("Failed to authenticate user")
        close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Failed to authenticate user"))
        return
    }

    val address = call.request.origin.remoteAddress
    val client = Client(id, address, user, this)
    ClientService.add(client)

    try {
        client.receive()

    } finally {
        ClientService.remove(id)
    }
}