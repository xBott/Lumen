import kotlinx.coroutines.delay
import me.bottdev.lumenclient.lumenClient
import java.util.UUID

suspend fun main() {
    val client = lumenClient {
        id = UUID.randomUUID().toString()
        host = "localhost"
        port = 8081
        credentials {
            username = "admin"
            password = "admin"
        }
    }

    client.start()

    delay(1_000)

    while (client.isConnected) {
        delay(500)
    }

}