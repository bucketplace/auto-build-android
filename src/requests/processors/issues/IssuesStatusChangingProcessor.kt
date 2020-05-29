package requests.processors.issues

import Config
import io.ktor.application.ApplicationCall
import io.ktor.client.response.HttpResponse
import io.ktor.http.ContentType
import io.ktor.response.respondText
import requests.processors.RequestProcessor
import requests.processors.issues.IssuesStatusChangingProcessor.ReadyForQaIssuesResponseBody.Issue
import utils.HttpClientCreator
import utils.JiraRequester

class IssuesStatusChangingProcessor(call: ApplicationCall) : RequestProcessor(call) {

    private data class ReadyForQaIssuesResponseBody(val issues: List<Issue>) {
        data class Issue(val key: String, val fields: Fields) {
            data class Fields(val summary: String)
        }
    }

    private val appVersion = getAppVersion()
    private val httpClient = HttpClientCreator.create()

    private fun getAppVersion(): String {
        return call.request.queryParameters["app_version"] ?: throw Exception("app_version not exists!")
    }

    override suspend fun process() {
        getReadyForQaIssues()
            .also { issues -> changeIssuesStatus(issues) }
            .let { issues -> createIssuesText(issues) }
            .let { text -> respondToClient(text) }
    }

    private suspend fun getReadyForQaIssues(): List<Issue> {
        return JiraRequester.get<ReadyForQaIssuesResponseBody>(
            httpClient,
            Config.getJiraReadyForPdgQaIssuesUrl(appVersion)
        ).issues
    }

    private suspend fun changeIssuesStatus(issues: List<Issue>) {
        issues.forEach { issue ->
            JiraRequester.post<HttpResponse>(
                httpClient,
                Config.getJiraIssuePdgQaInProgressTransitionUrl(issue.key),
                Config.JIRA_ISSUE_PDG_QA_IN_PROGRESS_TRANSITION_POST_BODY
            )
        }
    }

    private fun createIssuesText(issues: List<Issue>): String {
        return issues.fold("", { text, issue ->
            text + "(${Config.getJiraIssueUrl(issue.key)}) ${issue.fields.summary}\n"
        })
    }

    private suspend fun respondToClient(text: String) {
        call.respondText(
            text = text,
            contentType = ContentType.Text.Plain
        )
    }
}