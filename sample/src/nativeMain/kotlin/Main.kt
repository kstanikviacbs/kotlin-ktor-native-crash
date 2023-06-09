import io.ktor.client.*
import io.ktor.client.engine.curl.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.random.Random

private val httpClient = HttpClient(Curl) {
    install(HttpTimeout) {
        requestTimeoutMillis = 20 * 1000
        connectTimeoutMillis = 5 * 1000
    }
    followRedirects = true
    engine {
        sslVerify = false
        threadsCount = 20
        pipelining = true
    }
}

fun main(args: Array<String>) {
    embeddedServer(CIO, port = 8080, configure = {
        reuseAddress = true
    }) {
        routing {
            get("/health") {
                call.respond(OK)
            }
            get("/{...}") {
                val id = Random.nextInt(0, 300)
                val width = Random.nextInt(200, 2500)
                val height = Random.nextInt(200, 2500)
                val url = getImageUrl(width, height, id)
                val imageBytes = httpClient.get(url).readBytes()
                call.respondBytes(imageBytes, contentType = ContentType("image", "jpg"))
            }
        }
    }.start(wait = true)
}

private fun getImageUrl(width: Int, height: Int, id: Int): String {
    return if (Random.nextInt(0, 4) % 2 == 0) {
        "https://picsum.photos/id/$id/$width/$height"
    } else {
        "https://placeimg.com/$width/$height/any"
    }
}
