package requests.processors.build_numbers

import db.collections.LastBuildInfo
import io.jsondb.JsonDBTemplate
import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.response.respondText
import requests.RequestProcessor

class GetNewBuildNumberProcessor(call: ApplicationCall) : RequestProcessor(call) {

    companion object {
        private const val DB_FILES_LOCATION = "./db"
        private const val BASE_SCAN_PACKAGE = "db.collections"
    }

    private val appVersion = getAppVersion()
    private val jsonDb: JsonDBTemplate = getJsonDb()

    private fun getAppVersion(): String {
        return call.request.queryParameters["app_version"] ?: throw Exception("app_version이 필요해요!")
    }

    private fun getJsonDb(): JsonDBTemplate {
        return JsonDBTemplate(
            DB_FILES_LOCATION,
            BASE_SCAN_PACKAGE
        )
            .also { jsonDb ->
                if (!jsonDb.collectionExists(LastBuildInfo::class.java)) {
                    jsonDb.createCollection(LastBuildInfo::class.java)
                }
            }
    }

    override suspend fun process() {
        getNewLastBuildNumber()
            .also { buildNumber -> saveBuildNumber(buildNumber) }
            .let { buildNumber -> createResponseText(buildNumber) }
            .let { responseText -> respond(responseText) }
    }

    private fun getNewLastBuildNumber(): Int {
        return jsonDb.findById<LastBuildInfo>(appVersion, LastBuildInfo::class.java)?.buildNumber?.let { it + 1 } ?: 1
    }

    private fun saveBuildNumber(buildNumber: Int) {
        jsonDb.upsert<LastBuildInfo>(LastBuildInfo(appVersion, buildNumber))
    }

    private fun createResponseText(buildNumber: Int) = buildNumber.toString()

    private suspend fun respond(text: String) {
        call.respondText(
            text = text,
            contentType = ContentType.Text.Plain
        )
    }
}