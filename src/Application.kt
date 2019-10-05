import controllers.builds.buildRequest
import controllers.builds.canBuild
import com.google.gson.FieldNamingPolicy
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.routing.routing
import controllers.build_numbers.getNewBuildNumber
import controllers.jira_issues.changeIssuesStatusToQaInProgress

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(ContentNegotiation) {
        gson {
            setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        }
    }

    routing {
        buildRequest()
        canBuild()
        getNewBuildNumber()
        changeIssuesStatusToQaInProgress()
    }
}