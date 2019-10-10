package requests.processors.builds

import Config
import io.ktor.application.ApplicationCall
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.response.respondText
import kotlinx.coroutines.runBlocking
import requests.RequestProcessor
import utils.HttpClientManager
import utils.JiraAuthenticationCookieGetter


class AskCanBuildProcessor(call: ApplicationCall) : RequestProcessor(call) {

    private val appVersion = getAppVersion()
    private val httpClient = HttpClientManager.createClient()
    private val jiraAuthCookie = runBlocking { JiraAuthenticationCookieGetter.get(httpClient) }

    private fun getAppVersion(): String {
        return call.request.queryParameters["app_version"] ?: throw Exception("app_version이 필요해요!")
    }

    override suspend fun process() {
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