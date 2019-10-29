package utils

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.ContentType.Application
import io.ktor.http.content.TextContent

object JiraRequester {

    suspend inline fun <reified T> get(httpClient: HttpClient, url: String): T {
        return httpClient.use { client ->
            client.get(url) {
                header("cookie", JiraAuthenticationCookieGetter.get(client))
            }
        }
    }

    suspend inline fun <reified T> post(httpClient: HttpClient, url: String, jsonBody: String): T {
        return httpClient.use { client ->
            client.post(url) {
                header("cookie", JiraAuthenticationCookieGetter.get(client))
                body = TextContent(
                    contentType = Application.Json,
                    text = jsonBody
                )
            }
        }
    }
}