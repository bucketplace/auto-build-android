package co.bsscco

import com.google.gson.FieldNamingPolicy
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.response.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.util.toMap

fun Route.canBuild() {
    get("/can_build") {
        val appVersion = getAppVersion(call)

        respondToUser(call, getReadyForQaIssueCount(appVersion))
    }
}

fun getAppVersion(call: ApplicationCall): String {
    return call.request.queryParameters["app_version"]
        ?.also { println(it) }
        ?: throw Exception("app_version이 필요해요!")
}

private suspend fun getReadyForQaIssueCount(appVersion: String): Int {
    return createHttpClient().use { client ->
        val jiraAuthCookie = getJiraAuthenticationCookie(client)
        getReadyForQaIssues(client, jiraAuthCookie, appVersion)
    }
}

private fun createHttpClient(): HttpClient {
    return HttpClient(OkHttp) {
        install(JsonFeature) {
            serializer = GsonSerializer {
                setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            }
        }
    }
}

private suspend fun getJiraAuthenticationCookie(client: HttpClient): String {
    val response = client.post<HttpResponse>(Config.JIRA_LOGIN_URL) {
        body = TextContent(
            contentType = ContentType.Application.Json,
            text = Config.JIRA_LOGIN_BODY
        )
    }
    return response.headers.toMap()["set-cookie"]?.joinToString("; ") ?: throw Exception("지라 로그인 실패!")
}

private suspend fun getReadyForQaIssues(client: HttpClient, jiraAuthCookie: String, appVersion: String): Int {
    return client.get<ReadyForQaIssuesResponse>(Config.getJiraReadyForQaIssuesUrl(appVersion)) {
        header("cookie", jiraAuthCookie)
    }.total
}

private suspend fun respondToUser(call: ApplicationCall, readyForQaIssueCount: Int) {
    respondText(call, if (readyForQaIssueCount >= 1) "true" else "false")
}

private suspend fun respondText(call: ApplicationCall, text: String) {
    call.respondText(
        text = text,
        contentType = ContentType.Text.Plain
    )
}

data class ReadyForQaIssuesResponse(val total: Int)