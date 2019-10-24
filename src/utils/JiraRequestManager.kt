package utils

import io.ktor.client.request.get
import io.ktor.client.request.header

object JiraRequestManager {

    private data class ReadyForQaIssuesResponse(val total: Int)

    private val httpClient by lazy { HttpClientManager.createClient() }

    suspend fun getReadyForQaIssueCount(appVersion: String): Int {
        return httpClient.use { client ->
            client.get<ReadyForQaIssuesResponse>(Config.getJiraReadyForQaIssuesUrl(appVersion)) {
                header("cookie", JiraAuthenticationCookieGetter.get(client))
            }
        }.total
    }
}