package requests.processors.build_numbers

import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.response.respondText
import requests.processors.RequestProcessor

class GetNewBuildNumberProcessor(call: ApplicationCall) : RequestProcessor(call) {

    private val appVersion = getAppVersion()
    private val lastBuildInfoDb = LastBuildInfoDb(appVersion)

    private fun getAppVersion(): String {
        @Suppress("SpellCheckingInspection")
        return call.request.queryParameters["app_version"] ?: throw Exception("app_version이 필요해요!")
    }

    override suspend fun process() {
        lastBuildInfoDb.getNewBuildNumber()
            .also { buildNumber -> lastBuildInfoDb.saveBuildNumber(buildNumber) }
            .let { buildNumber -> respondToClient(buildNumber.toString()) }
    }

    private suspend fun respondToClient(text: String) {
        call.respondText(
            text = text,
            contentType = ContentType.Text.Plain
        )
    }
}