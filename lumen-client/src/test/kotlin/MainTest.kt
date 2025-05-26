import kotlinx.coroutines.runBlocking
import me.bottdev.lumenclient.lumenClient
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class ClientTest {

    @Test
    fun `should connect the client`() {

        val client = lumenClient {
            id = "test"
            host = "localhost"
            port = 8081
            credentials {
                username = "admin"
                password = "admin"
            }
        }

        runBlocking {
            client.connect()
        }

        assertTrue { true }

    }

}
