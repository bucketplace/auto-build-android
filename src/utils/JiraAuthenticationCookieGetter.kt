package utils

import Config
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.response.HttpResponse
import io.ktor.http.ContentType.Application
import io.ktor.http.Headers
import io.ktor.http.content.TextContent
import io.ktor.util.toMap

object JiraAuthenticationCookieGetter {

    suspend fun get(client: HttpClient): String {
        val response = client.use { client ->
            client.post<HttpResponse>(Config.JIRA_LOGIN_URL) {
                body = TextContent(
                    contentType = Application.Json,
                    text = Config.JIRA_LOGIN_POST_BODY
                )
            }
        }
        return getCookie(response.headers)
    }

    private fun getCookie(headers: Headers): String {
        return headers.toMap()["set-cookie"]?.joinToString("; ") ?: throw Exception("지라 로그인 실패!")
    }
}