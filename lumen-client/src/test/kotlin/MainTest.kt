import me.bottdev.lumenclient.types.simpleLumenClient
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class ClientTest {

    @Test
    fun `should connect the client`() {

        val client = simpleLumenClient {
            id = "test"
            host = "localhost"
            port = 8081
            credentials {
                username = "admin"
                password = "admin"
            }
        }

        client.start()

        assertTrue { true }

    }

}
