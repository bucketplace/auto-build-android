package utils

import Config
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.ContentType.Application
import io.ktor.http.content.TextContent

object SlackRequestManager {

    suspend inline fun <reified T> respondCommand(responseUrl: String, json: String): T {
        return HttpClientManager.createClient().use { client ->
            client.post<T>(responseUrl) {
                header("Authorization", "Bearer ${Config.APP_ACCESS_TOKEN}")
                body = TextContent(
                    contentType = Application.Json,
                    text = json
                )
            }
        }
    }
}