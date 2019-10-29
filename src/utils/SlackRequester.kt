package utils

import Config
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.response.HttpResponse
import io.ktor.http.ContentType.Application
import io.ktor.http.content.TextContent

object SlackRequester {

    suspend fun post(httpClient: HttpClient, url: String, jsonBody: String): HttpResponse {
        return httpClient.use { client ->
            client.post(url) {
                header("Authorization", "Bearer ${Config.APP_ACCESS_TOKEN}")
                body = TextContent(
                    contentType = Application.Json,
                    text = jsonBody
                )
            }
        }
    }
}