package com.explorer.makanza.routes

import com.explorer.makanza.models.*
import com.explorer.makanza.services.AuthService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

val ApplicationCall.userId: String?
    get() = principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asString()

fun Route.authRoutes(authService: AuthService) {
    route("/api/v1/auth") {

        post("/register") {
            val req = call.receive<RegisterRequest>()
            val response = authService.register(req)
            call.respond(HttpStatusCode.Created, response)
        }

        post("/login") {
            val req = call.receive<LoginRequest>()
            val response = authService.login(req)
            call.respond(HttpStatusCode.OK, response)
        }

        post("/logout") {
            // Statetess JWT logout acknowledgement
            call.respond(HttpStatusCode.OK, mapOf("message" to "Successfully logged out"))
        }

        authenticate("auth-jwt") {
            get("/me") {
                val uId = call.userId ?: return@get call.respond(HttpStatusCode.Unauthorized, ApiErrorResponse("UNAUTHORIZED", "Not authenticated"))
                val user = authService.getUserById(uId) ?: return@get call.respond(HttpStatusCode.NotFound, ApiErrorResponse("USER_NOT_FOUND", "User profile not found"))
                call.respond(HttpStatusCode.OK, user)
            }
        }
    }
}
