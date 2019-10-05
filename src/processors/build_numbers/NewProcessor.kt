package processors.build_numbers

import io.jsondb.JsonDBTemplate
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import db.collections.LastBuildInfo

fun Route.buildNumbersNew() {
    get("/build_numbers/new") { NewProcessor(call).process() }
}

private class NewProcessor(private val call: ApplicationCall) {

    companion object {
        private const val DB_FILES_LOCATION = "./db"
        private const val BASE_SCAN_PACKAGE = "db.collections"
    }

    private val appVersion: String
    private val jsonDb: JsonDBTemplate = getJsonDb()

    init {
        appVersion = getAppVersion().also { println(it) }
    }

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

    suspend fun process() {
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