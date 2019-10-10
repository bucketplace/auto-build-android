package requests.processors.commands

import Config
import io.ktor.application.ApplicationCall
import io.ktor.client.request.post
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import kotlinx.coroutines.runBlocking
import requests.RequestProcessor
import utils.HttpClientManager

class BuildProcessor(call: ApplicationCall) : RequestProcessor(call) {

    private val responseUrl: String

    init {
        responseUrl = runBlocking { getSlackResponseUrl().also { println(it) } }
    }

    private suspend fun getSlackResponseUrl(): String {
        return call.receive<Parameters>()["response_url"] ?: throw Exception("response_url이 필요해요!")
    }

    override suspend fun process() {
        respondAccepted()
        requestBuildToBitrise()
            .let { response -> createResponseJson(response) }
            .let { json -> respondToSlack(json) }
    }

    private suspend fun respondAccepted() {
        call.respond(status = HttpStatusCode.Accepted, message = "")
    }

    private suspend fun requestBuildToBitrise(): RequestBuildResponse {
        return HttpClientManager.createClient().use { client ->
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