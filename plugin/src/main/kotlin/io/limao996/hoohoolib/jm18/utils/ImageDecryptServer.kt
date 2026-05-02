package io.limao996.hoohoolib.jm18.utils

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytesWriter
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.utils.io.jvm.javaio.toInputStream
import io.ktor.utils.io.jvm.javaio.toOutputStream
import io.limao996.hoohoolib.jm18.JM18_HTTP_PORT
import io.limao996.hoohoolib.utils.httpClient
import java.io.BufferedInputStream
import java.io.InputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class ImageDecryptServer(port: Int) {
    private val server = embeddedServer(CIO, port = port) {

        routing {
            get("/image-decrypt") {
                val imageUrl = call.request.queryParameters["imageUrl"]
                if (imageUrl == null) {
                    call.respond(HttpStatusCode.BadRequest, "Bad Request")
                    return@get
                }
                try {
                    val response = httpClient.get(imageUrl)

                    val originalStream = response.bodyAsChannel().toInputStream()
                    val decryptedStream =
                        decryptAESStream(BufferedInputStream(originalStream, 65535))

                    call.respondBytesWriter(
                        contentType = ContentType.Image.JPEG, status = HttpStatusCode.OK
                    ) {
                        decryptedStream.use { inputStream ->
                            inputStream.copyTo(toOutputStream())
                        }
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, "Bad Request")
                }
            }
        }
    }

    private fun decryptAESStream(inputStream: InputStream): CipherInputStream {
        val key = "f5d965df75336270".toByteArray(Charsets.UTF_8)
        val iv = "97b60394abc2fbe1".toByteArray(Charsets.UTF_8)

        val secretKeySpec = SecretKeySpec(key, "AES")
        val ivParameterSpec = IvParameterSpec(iv)

        val cipher = Cipher.getInstance("AES/CBC/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)

        return CipherInputStream(inputStream, cipher)
    }

    fun start() {
        server.start(wait = false)
    }

    fun stop() {
        server.stop(1000, 2000)
    }
}

fun buildDecryptedImageUrl(url: String) =
    "http://127.0.0.1:$JM18_HTTP_PORT/image-decrypt?imageUrl=$url"