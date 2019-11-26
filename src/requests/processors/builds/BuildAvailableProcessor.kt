package requests.processors.builds

import Config
import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.response.respondText
import requests.processors.RequestProcessor
import utils.HttpClientCreator
import utils.JiraRequester

class BuildAvailableProcessor(call: ApplicationCall) : RequestProcessor(call) {

    private data class ReadyForQaIssuesResponseBody(val total: Int)

    private val appVersion = getAppVersion()
    private val httpClient = HttpClientCreator.create()

    private fun getAppVersion(): String {
        return call.request.queryParameters["app_version"] ?: throw Exception("app_version not exists!")
    }

    override suspend fun process() {
        @Suppress("ComplexRedundantLet")
        getReadyForQaIssueCount()
            .let { issueCount -> createTrueOrFalseText(issueCount) }
            .let { text -> respondToClient(text) }
    }

    private suspend fun getReadyForQaIssueCount(): Int {
        return JiraRequester.get<ReadyForQaIssuesResponseBody>(
            httpClient,
            Config.getJiraReadyForQaIssuesUrl(appVersion)
        ).total
    }

    private fun createTrueOrFalseText(issueCount: Int): String {
        return if (issueCount >= 1) "true" else "false"
    }

    private suspend fun respondToClient(text: String) {
        call.respondText(
            text = text,
            contentType = ContentType.Text.Plain
        )
    }
}