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
        val params = call.receive<Parameters>()
        println(params.toString())

        requestBuildToBitrise().let { response ->
            val responseUrl = params["response_url"] ?: ""
            if (response.status == "ok") {
                respondText(call, createResponseText(responseUrl, response.buildNumber))
            } else {
                respondText(call, createFailText(responseUrl))
            }
        }
    }
}

private suspend fun requestBuildToBitrise(): BitriseBuildResponse {
    /*
        {
            "status": "ok",
            "message": "webhook processed",
            "slug": "19f5ee8a2a24e844",
            "service": "bitrise",
            "build_slug": "bccc2ffea42a848c",
            "build_number": 467,
            "build_url": "https://app.bitrise.io/build/bccc2ffea42a848c",
            "triggered_workflow": "qa"
        }
         */

    return HttpClient(OkHttp) {
        install(JsonFeature) {
            serializer = GsonSerializer {
                setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            }
        }
    }.use { client ->
        client.post<BitriseBuildResponse>(Config.buildStartUrl) {
            body = TextContent(
                text = Config.buildStartRequestBody,
                contentType = ContentType.Application.Json
            )
        }
    }
}

private suspend fun respondText(call: ApplicationCall, text: String) {
    call.respondText(
        text = text,
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

data class BitriseBuildResponse(val status: String, val buildNumber: Int)