package requests.processors.commands

import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.post

fun Route.commands() {
    post("/commands/build") { BuildRequestProcessor(call).process() }
}