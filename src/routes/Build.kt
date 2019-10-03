package co.bsscco

import com.google.gson.FieldNamingPolicy
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.post
import io.ktor.content.TextContent
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.request.receive
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.post

fun Route.build() {
    post("/build") {
        val responseUrl = getSlackResponseUrl(call.receive<Parameters>())

        respondToUser(call, responseUrl, requestBuildToBitrise())
    }
}

fun getSlackResponseUrl(params: Parameters): String {
    return params["response_url"]
        ?.also { println(it) }
        ?: throw Exception("response_url이 필요해요!")
}

private suspend fun requestBuildToBitrise(): RequestBuildResponse {
    return createHttpClient().use { client ->
        client.post<RequestBuildResponse>(Config.BITRISE_TRIGGER_BUILD_REQUEST_URL) {
            body = TextContent(
                contentType = ContentType.Application.Json,
                text = Config.BITRISE_TRIGGER_BUILD_REQUEST_BODY
            )
        }
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

private suspend fun respondToUser(
    call: ApplicationCall,
    responseUrl: String,
    requestBuildResponse: RequestBuildResponse
) {
    if (requestBuildResponse.status == "ok") {
        respondJson(call, createResponseText(responseUrl, requestBuildResponse.buildNumber))
    } else {
        respondJson(call, createFailText(responseUrl))
    }
}

private suspend fun respondJson(call: ApplicationCall, json: String) {
    call.respondText(
        text = json,
        contentType = ContentType.Application.Json
    )
}

private fun createResponseText(responseUrl: String, buildNumber: Int): String {
    return """
                {
                    "response_url": "$responseUrl",
                    "text": "qa-branch 빌드가 시작됩니다. 빌드번호: #$buildNumber\n(이전 빌드는 자동으로 실행종료 됩니다) "
                }
                """
}

private fun createFailText(responseUrl: String): String {
    return """
                {
                    "response_url": "$responseUrl",
                    "text": "bitrise에서 빌드 시작이 실패했습니다."
                }
                """
}

data class RequestBuildResponse(val status: String, val buildNumber: Int)