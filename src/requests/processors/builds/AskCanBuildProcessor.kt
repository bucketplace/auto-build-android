package requests.processors.builds

import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.response.respondText
import kotlinx.coroutines.runBlocking
import requests.processors.RequestProcessor
import utils.HttpClientManager
import utils.JiraAuthenticationCookieGetter
import utils.JiraRequestManager

class AskCanBuildProcessor(call: ApplicationCall) : RequestProcessor(call) {

    private val appVersion = getAppVersion()

    private fun getAppVersion(): String {
        return call.request.queryParameters["app_version"] ?: throw Exception("app_version이 필요해요!")
    }

    override suspend fun process() {
        JiraRequestManager.getReadyForQaIssueCount(appVersion)
            .let { issueCount -> createResponseText(issueCount) }
            .let { responseText -> respond(responseText) }
    }

    private fun createResponseText(issueCount: Int): String = if (issueCount >= 1) "true" else "false"

    private suspend fun respond(text: String) {
        call.respondText(
            text = text,
            contentType = ContentType.Text.Plain
        )
    }


}