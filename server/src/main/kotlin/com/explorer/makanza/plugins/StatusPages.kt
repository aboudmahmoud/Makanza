package com.explorer.makanza.plugins

import com.explorer.makanza.models.ApiErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ApiErrorResponse(code = "BAD_REQUEST", message = cause.message ?: "Invalid request parameters")
            )
        }
        exception<IllegalAccessException> { call, cause ->
            call.respond(
                HttpStatusCode.Forbidden,
                ApiErrorResponse(code = "FORBIDDEN", message = cause.message ?: "Access denied")
            )
        }
        exception<NoSuchElementException> { call, cause ->
            call.respond(
                HttpStatusCode.NotFound,
                ApiErrorResponse(code = "NOT_FOUND", message = cause.message ?: "Resource not found")
            )
        }
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled exception: ", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiErrorResponse(code = "INTERNAL_SERVER_ERROR", message = "An internal server error occurred")
            )
        }
    }
}
