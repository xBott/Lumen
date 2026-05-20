# Lumen

Lumen is a Kotlin/JVM message queue (MQ) over WebSockets. It provides:

- `lumen-server`: a Ktor-based WebSocket server with MySQL persistence (Exposed).
- `lumen-client`: a lightweight JVM client that connects to the server via WebSocket.
- `lumen-core`: shared message types/codecs/handlers used by both server and client.

At a high level, clients connect to the server, perform a handshake, and exchange messages that can be routed (direct/broadcast/channel) with optional acknowledgements (ack).

## Requirements

- JDK 17+
- Gradle (the repo includes `./gradlew`)
- A MySQL-compatible database (for `lumen-server`)

## Project Structure

- `lumen-core/` — message model, wrappers, codec, queue, and handlers.
- `lumen-client/` — `SimpleLumenClient` implementation (OkHttp WebSocket).
- `lumen-server/` — Ktor server entrypoint and routes.

## Build

```bash
./gradlew build
```

Run tests:

```bash
./gradlew test
```

## Running The Server

The server entrypoint is `me.bottdev.lumenserver.ApplicationKt`.

Start the server:

```bash
./gradlew :lumen-server:run
```

### Server Configuration (`application.conf`)

`lumen-server` loads configuration via Typesafe Config (`ConfigFactory.load()`), expecting:

- `ktor.deployment.host` / `ktor.deployment.port`
- `database.url` / `database.driver` / `database.user` / `database.password`

Create `lumen-server/src/main/resources/application.conf` (or otherwise provide it on the classpath), for example:

```hocon
ktor {
  deployment {
    host = "0.0.0.0"
    port = 5000
  }
}

database {
  # Example for MySQL:
  url = "jdbc:mysql://localhost:3306/lumen?useSSL=false&serverTimezone=UTC"
  driver = "com.mysql.cj.jdbc.Driver"
  user = "root"
  password = "root"
}
```

On startup the server initializes the database connection and creates the `Users` table (via Exposed).

## Using The Client

`lumen-client` provides `SimpleLumenClient` and a small builder DSL.

Minimal example:

```kotlin
import me.bottdev.lumenclient.types.simpleLumenClient

fun main() {
    val client = simpleLumenClient {
        host = "127.0.0.1"
        port = 5000
        credentials {
            username = "admin"
            password = "admin"
        }
    }

    client.start()
    // ...
    client.stop()
}
```

Connection URL format used by the client:

`ws://{host}:{port}/connect?id={id}&username={username}&password={password}`