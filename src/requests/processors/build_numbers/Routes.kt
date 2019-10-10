package requests.processors.build_numbers

import io.ktor.application.call
import io.ktor.routing.Route
import io.ktor.routing.get

fun Route.buildNumbers() {
    get("/build_numbers/new") { GetNewBuildNumberProcessor(call).process() }
}