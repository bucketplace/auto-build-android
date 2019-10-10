package requests.processors.commands

import Config
import io.ktor.application.ApplicationCall
import io.ktor.client.request.post
import io.ktor.client.response.HttpResponse
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
import utils.SlackRequestManager

class BuildProcessor(call: ApplicationCall) : RequestProcessor(call) {

    private val responseUrl = runBlocking { getSlackResponseUrl() }
    private val httpClient = HttpClientManager.createClient()

    private suspend fun getSlackResponseUrl(): String {
        return call.receive<Parameters>()["response_url"] ?: throw Exception("response_url이 필요해요!")
    }

    override suspend fun process() {
        respondAccepted()
        val response = requestBuildToBitrise()
        sendResponseMessage(createResponseMessageJson(response))
    }

    private suspend fun respondAccepted() {
        call.respond(status = HttpStatusCode.Accepted, message = "")
    }

    private suspend fun requestBuildToBitrise(): RequestBuildResponse {
        return httpClient.use { client ->
            client.post<RequestBuildResponse>(Config.BITRISE_BUILD_START_URL) {
                body = TextContent(
                    contentType = ContentType.Application.Json,
                    text = Config.BITRISE_BUILD_START_POST_BODY
                )
            }
        }
    }

    private fun createResponseMessageJson(response: RequestBuildResponse): String {
        return if (response.status == "ok") {
            createBuildRequestSuccessJson()
        } else {
            createBuildRequestFailJson()
        }
    }

    private fun createBuildRequestSuccessJson(): String {
        return """
                {
                    "text": "qa-branch 빌드가 시작됩니다. (이전 빌드는 자동으로 실행종료 됩니다) "
                }
                """
    }

    private fun createBuildRequestFailJson(): String {
        return """
                {
                    "text": "bitrise에서 빌드 시작이 실패했습니다."
                }
                """
    }

    private suspend fun sendResponseMessage(messageJson: String) {
        SlackRequestManager.respondCommand<HttpResponse>(responseUrl, messageJson)
    }

    private data class RequestBuildResponse(val status: String, val buildNumber: Int)
}