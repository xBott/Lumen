package me.bottdev.lumenserver

import com.typesafe.config.ConfigFactory
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.websocket.*
import io.ktor.util.logging.*
import me.bottdev.lumenserver.repositories.UserRepository
import me.bottdev.lumenserver.routes.connectionRoute
import me.bottdev.lumenserver.services.UserService
import java.util.logging.Logger
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

val logger = KtorSimpleLogger("LumenServer")

fun main() {
    embeddedServer(Netty, environment = applicationEngineEnvironment {

        val config = ConfigFactory.load().getConfig("ktor").getConfig("deployment")

        connector {
            host = config.getString("host") ?: "0.0.0.0"
            port = config.getInt("port")
        }

        module(Application::module)
    }).start(wait = true)
}

private fun Application.configureWebSockets() {
    if (pluginOrNull(WebSockets) == null) {
        install(WebSockets) {
            pingPeriod = 15.seconds.toJavaDuration()
            timeout = 15.seconds.toJavaDuration()
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }
    }
}

private fun Application.configureRoutes() {
    connectionRoute()
}


fun Application.module() {
    DatabaseFactory.init()
    configureWebSockets()
    configureRoutes()

    UserRepository.register("admin", "admin")
}