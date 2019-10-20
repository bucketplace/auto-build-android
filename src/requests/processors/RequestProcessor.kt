package requests.processors

import io.ktor.application.ApplicationCall

abstract class RequestProcessor(protected val call: ApplicationCall) {

    abstract suspend fun process()
}