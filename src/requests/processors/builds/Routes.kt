package requests.processors.builds

import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get

fun Route.builds() {
    get("/builds/can") { BuildAvailableProcessor(call).process() }
}