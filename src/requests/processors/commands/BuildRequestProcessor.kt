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
import kotlinx.coroutines.runBlocking
import requests.processors.RequestProcessor
import utils.HttpClientCreator
import utils.SlackRequester

class BuildRequestProcessor(call: ApplicationCall) : RequestProcessor(call) {

    private data class RequestBuildResponseBody(val status: String, val buildNumber: Int)

    private val parameters by lazy { getParams() }
    private val branch = getBranch()
    private val responseUrl = getSlackResponseUrl()
    private val httpClient = HttpClientCreator.create()
    private val slackRequestBodyCreator = SlackRequestBodyCreator()

    private fun getParams(): Parameters = runBlocking { call.receive<Parameters>() }

    private fun getBranch(): String {
        return parameters["text"].takeUnless { it.isNullOrEmpty() } ?: Config.DEFAULT_BRANCH
    }

    private fun getSlackResponseUrl(): String {
        @Suppress("SpellCheckingInspection")
        return parameters["response_url"] ?: throw Exception("response_url이 필요해요!")
    }

    override suspend fun process() {
        respondAccepted()
        @Suppress("ComplexRedundantLet")
        requestBuildToBitrise()
            .let { response -> createSuccessOrFailJson(response) }
            .let { json -> respondToSlack(json) }
    }

    private suspend fun respondAccepted() {
        call.respond(status = HttpStatusCode.Accepted, message = "")
    }

    @Suppress("SpellCheckingInspection")
    private suspend fun requestBuildToBitrise(): RequestBuildResponseBody {
        return httpClient.use { client ->
            client.post(Config.BITRISE_BUILD_START_URL) {
                body = TextContent(
                    contentType = ContentType.Application.Json,
                    text = Config.getBitriseBuildTriggerUrl(branch)
                )
            }
        }
    }

    private fun createSuccessOrFailJson(responseBody: RequestBuildResponseBody): String {
        return if (responseBody.status == "ok") {
            slackRequestBodyCreator.createSuccessJson(branch)
        } else {
            slackRequestBodyCreator.createFailJson()
        }
    }

    private suspend fun respondToSlack(json: String) {
        SlackRequester.post(httpClient, responseUrl, json)
    }
}