package processors.builds

import Config
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.client.request.post
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.request.receive
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.post
import kotlinx.coroutines.runBlocking
import utils.HttpClientCreator

fun Route.buildsRequest() {
    post("/builds/request") { RequestProcessor(call).process() }
}

class RequestProcessor(private val call: ApplicationCall) {

    private val responseUrl: String

    init {
        responseUrl = runBlocking { getSlackResponseUrl().also { println(it) } }
    }

    private suspend fun getSlackResponseUrl(): String {
        return call.receive<Parameters>()["response_url"] ?: throw Exception("response_url이 필요해요!")
    }

    suspend fun process() {
        requestBuildToBitrise()
            .let { response -> createResponseJson(response) }
            .let { json -> respondToSlack(json) }
    }

    private suspend fun requestBuildToBitrise(): RequestBuildResponse {
        return HttpClientCreator.create().use { client ->
            client.post<RequestBuildResponse>(Config.BITRISE_BUILD_START_URL) {
                body = TextContent(
                    contentType = ContentType.Application.Json,
                    text = Config.BITRISE_BUILD_START_POST_BODY
                )
            }
        }
    }

    private fun createResponseJson(requestBuildResponse: RequestBuildResponse): String {
        return if (requestBuildResponse.status == "ok") {
            createSuccessJson()
        } else {
            createFailJson()
        }
    }

    private fun createSuccessJson(): String {
        return """
                {
                    "response_url": "$responseUrl",
                    "text": "qa-branch 빌드가 시작됩니다. (이전 빌드는 자동으로 실행종료 됩니다) "
                }
                """
    }

    private fun createFailJson(): String {
        return """
                {
                    "response_url": "$responseUrl",
                    "text": "bitrise에서 빌드 시작이 실패했습니다."
                }
                """
    }

    private suspend fun respondToSlack(json: String) {
        call.respondText(
            text = json,
            contentType = ContentType.Application.Json
        )
    }

    private data class RequestBuildResponse(val status: String, val buildNumber: Int)
}