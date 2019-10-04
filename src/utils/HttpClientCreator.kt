package utils

import com.google.gson.FieldNamingPolicy
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature

object HttpClientCreator {

    fun create(): HttpClient {
        return HttpClient(OkHttp) {
            install(JsonFeature) {
                serializer = GsonSerializer {
                    setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                }
            }
        }
    }
}