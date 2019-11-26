package requests.processors.issues

import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.put

fun Route.issues() {
    put("/issues/status") { IssuesStatusChangingProcessor(call).process() }
}