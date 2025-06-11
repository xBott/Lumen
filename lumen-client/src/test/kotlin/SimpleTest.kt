import kotlinx.coroutines.runBlocking
import me.bottdev.lumenclient.types.simpleLumenClient
import me.bottdev.lumencore.messages.IAckable
import me.bottdev.lumencore.messages.ILumenMessage
import java.io.File
import java.util.*

class NewFileMessage(
    val directory: String = "",
    val name: String,
    val content: String,
    override val shouldAck: Boolean = false
) : ILumenMessage, IAckable

fun main() = runBlocking {

    val client = simpleLumenClient {
        id = UUID.randomUUID().toString()
        host = "localhost"
        port = 8081
        credentials {
            username = "admin"
            password = "admin"
        }
    }

    client.registerMessage(NewFileMessage::class.java) { message, _ ->
        println("[File] creating new file ${message.name}.txt")

        val directory = File(message.directory)
        if (!directory.exists()) directory.mkdirs()

        val file = directory.resolve("${message.name}.txt")
        if (file.exists()) {
            client.logger.info("[File] file ${message.name}.txt already exists!")
            return@registerMessage
        }

        file.createNewFile()
        file.writeText(message.content)
    }


    client.start()
    client.subscribe("files")

    val newFileMessage = NewFileMessage(
        directory = "TestFiles",
        name = "test_${UUID.randomUUID()}",
        content = "test ".repeat(100),
        shouldAck = true
    )

    client.sendChannel {
        message = newFileMessage
        channelId = "files"
        self = false
        timeOut = 5000
        resendTimes = 3
        onAckSuccess = { wrapper, _ ->
            client.logger.info("${wrapper.id} was successfully received")
        }
        onAckTimeout = { wrapper ->
            client.logger.info("${wrapper.id} was not received")
        }
        onAckResend = { wrapper, iteration ->
            client.logger.info("resending message ${wrapper.id} #$iteration")
        }
    }

}


