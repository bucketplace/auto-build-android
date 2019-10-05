package processors.builds

import Config
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import kotlinx.coroutines.runBlocking
import utils.HttpClientCreator
import utils.JiraAuthenticationCookieGetter

fun Route.buildsCan() {
    get("/builds/can") { CanProcessor(call).process() }
}

class CanProcessor(private val call: ApplicationCall) {

    private val appVersion: String
    private val httpClient = HttpClientCreator.create()
    private val jiraAuthCookie: String

    init {
        appVersion = getAppVersion().also { println(it) }
        jiraAuthCookie = runBlocking { JiraAuthenticationCookieGetter.get(httpClient) }
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