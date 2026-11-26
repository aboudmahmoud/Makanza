package com.explorer.makanza.routes

import com.explorer.makanza.models.ApiErrorResponse
import com.explorer.makanza.models.UpdateProfileRequest
import com.explorer.makanza.services.AuthService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(authService: AuthService) {
    authenticate("auth-jwt") {
        route("/api/v1/users/me") {

            get {
                val uId = call.userId ?: return@get call.respond(HttpStatusCode.Unauthorized, ApiErrorResponse("UNAUTHORIZED", "Not authenticated"))
                val user = authService.getUserById(uId) ?: return@get call.respond(HttpStatusCode.NotFound, ApiErrorResponse("USER_NOT_FOUND", "User not found"))
                call.respond(HttpStatusCode.OK, user)
            }

            put {
                val uId = call.userId ?: return@put call.respond(HttpStatusCode.Unauthorized, ApiErrorResponse("UNAUTHORIZED", "Not authenticated"))
                val req = call.receive<UpdateProfileRequest>()
                val updated = authService.updateProfile(uId, req)
                call.respond(HttpStatusCode.OK, updated)
            }
        }
    }
}
