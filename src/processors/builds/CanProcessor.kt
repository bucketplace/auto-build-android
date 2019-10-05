package processors.builds

import Config
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.response.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.content.TextContent
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.util.toMap
import utils.HttpClientCreator

fun Route.buildsCan() {
    get("/builds/can") { CanProcessor(call).process() }
}

class CanProcessor(private val call: ApplicationCall) {

    private val appVersion: String
    private val httpClient = HttpClientCreator.create()

    init {
        appVersion = getAppVersion().also { println(it) }
    }

    private fun getAppVersion(): String {
        return call.request.queryParameters["app_version"] ?: throw Exception("app_version이 필요해요!")
    }

    suspend fun process() {
        getReadyForQaIssueCount()
            .let { issueCount -> createResponseText(issueCount) }
            .let { responseText -> respond(responseText) }
    }

    private suspend fun getReadyForQaIssueCount(): Int {
        return getReadyForQaIssues(getJiraAuthenticationCookie())
    }

    private suspend fun getJiraAuthenticationCookie(): String {
        val response = httpClient.use { client ->
            client.post<HttpResponse>(Config.JIRA_LOGIN_URL) {
                body = TextContent(
                    contentType = ContentType.Application.Json,
                    text = Config.JIRA_LOGIN_POST_BODY
                )
            }
        }
        return getCookie(response.headers)
    }

    private fun getCookie(headers: Headers): String {
        return headers.toMap()["set-cookie"]?.joinToString("; ") ?: throw Exception("지라 로그인 실패!")
    }

    private suspend fun getReadyForQaIssues(jiraAuthCookie: String): Int {
        return httpClient.use { client ->
            client.get<ReadyForQaIssuesResponse>(Config.getJiraReadyForQaIssuesUrl(appVersion)) {
                header("cookie", jiraAuthCookie)
            }
        }.total
    }

    private fun createResponseText(issueCount: Int): String = if (issueCount >= 1) "true" else "false"

    private suspend fun respond(text: String) {
        call.respondText(
            text = text,
            contentType = ContentType.Text.Plain
        )
    }

    private data class ReadyForQaIssuesResponse(val total: Int)
}