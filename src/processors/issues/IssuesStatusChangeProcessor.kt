package processors.issues

import Config
import processors.issues.IssuesStatusChangeProcessor.ReadyForQaIssuesResponse.Issue
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

fun Route.issuesStatusChange() {
    get("/issues/status/change") { IssuesStatusChangeProcessor(call).process() }
}

class IssuesStatusChangeProcessor(private val call: ApplicationCall) {

    private val appVersion: String
    private val httpClient = HttpClientCreator.create()

    init {
        appVersion = getAppVersion().also { println(it) }
    }

    private fun getAppVersion(): String {
        return call.request.queryParameters["app_version"] ?: throw Exception("app_version이 필요해요!")
    }

    suspend fun process() {
        getReadyForQaIssues()
            .let { issues -> createResponseText(issues) }
            .let { responseText -> respond(responseText) }
    }

    private suspend fun getReadyForQaIssues(): List<Issue> {
        return getJiraAuthenticationCookie().let { jiraAuthCookie ->
            httpClient.use { client ->
                client.get<ReadyForQaIssuesResponse>(Config.getJiraReadyForQaIssuesUrl(appVersion)) {
                    header("cookie", jiraAuthCookie)
                }
            }.issues
        }
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

    private fun createResponseText(issues: List<Issue>): String {
        return issues.fold("", { text, issue ->
            text + "${issue.fields.summary} (${Config.getJiraIssueUrl(issue.key)})\n"
        })
    }

    private suspend fun respond(text: String) {
        call.respondText(
            text = text,
            contentType = ContentType.Text.Plain
        )
    }

    private data class ReadyForQaIssuesResponse(val issues: List<Issue>) {
        data class Issue(val key: String, val fields: Fields) {
            data class Fields(val summary: String)
        }
    }
}