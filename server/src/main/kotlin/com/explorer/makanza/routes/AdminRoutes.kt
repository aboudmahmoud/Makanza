package com.explorer.makanza.routes

import com.explorer.makanza.models.*
import com.explorer.makanza.services.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

val ApplicationCall.userRole: UserRole?
    get() = principal<JWTPrincipal>()?.payload?.getClaim("role")?.asString()?.let {
        try { UserRole.valueOf(it) } catch (e: Exception) { null }
    }

fun Route.adminRoutes(
    authService: AuthService,
    apartmentService: ApartmentService,
    bookingService: BookingService
) {
    authenticate("auth-jwt") {
        route("/api/v1/admin") {

            // Interceptor to verify ADMIN role
            intercept(ApplicationCallPipeline.Call) {
                if (call.userRole != UserRole.ADMIN) {
                    call.respond(HttpStatusCode.Forbidden, ApiErrorResponse("FORBIDDEN", "Admin access required"))
                    finish()
                }
            }

            get("/dashboard/stats") {
                val stats = apartmentService.getAdminDashboardStats()
                call.respond(HttpStatusCode.OK, stats)
            }

            get("/apartments") {
                val q = call.request.queryParameters["q"]
                val status = call.request.queryParameters["status"]?.let {
                    try { ApartmentStatus.valueOf(it.uppercase()) } catch (e: Exception) { null }
                }
                val approvalStatus = call.request.queryParameters["approvalStatus"]?.let {
                    try { ApprovalStatus.valueOf(it.uppercase()) } catch (e: Exception) { null }
                }
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 10

                val params = ApartmentFilterParams(
                    q = q,
                    status = status,
                    approvalStatus = approvalStatus,
                    page = page,
                    pageSize = pageSize
                )
                val result = apartmentService.getApartments(params, isAdminRequest = true)
                call.respond(HttpStatusCode.OK, result)
            }

            post("/apartments") {
                val adminId = call.userId ?: return@post call.respond(HttpStatusCode.Unauthorized, ApiErrorResponse("UNAUTHORIZED", "Not authenticated"))
                val req = call.receive<CreateApartmentRequest>()
                val created = apartmentService.createApartment(adminId, req, autoApprove = true)
                call.respond(HttpStatusCode.Created, created)
            }

            put("/apartments/{id}") {
                val adminId = call.userId ?: return@put call.respond(HttpStatusCode.Unauthorized, ApiErrorResponse("UNAUTHORIZED", "Not authenticated"))
                val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest, ApiErrorResponse("BAD_REQUEST", "Apartment ID required"))
                val req = call.receive<UpdateApartmentRequest>()
                val updated = apartmentService.updateApartment(adminId, id, req, isAdmin = true)
                call.respond(HttpStatusCode.OK, updated)
            }

            patch("/apartments/{id}/approval") {
                val id = call.parameters["id"] ?: return@patch call.respond(HttpStatusCode.BadRequest, ApiErrorResponse("BAD_REQUEST", "Apartment ID required"))
                val req = call.receive<UpdateApprovalStatusRequest>()
                val updated = apartmentService.updateApprovalStatus(id, req.approvalStatus)
                call.respond(HttpStatusCode.OK, updated)
            }

            patch("/apartments/{id}/status") {
                val adminId = call.userId ?: return@patch call.respond(HttpStatusCode.Unauthorized, ApiErrorResponse("UNAUTHORIZED", "Not authenticated"))
                val id = call.parameters["id"] ?: return@patch call.respond(HttpStatusCode.BadRequest, ApiErrorResponse("BAD_REQUEST", "Apartment ID required"))
                val req = call.receive<UpdateApartmentStatusRequest>()
                val updated = apartmentService.updateApartmentStatus(adminId, id, req.status, isAdmin = true)
                call.respond(HttpStatusCode.OK, updated)
            }

            delete("/apartments/{id}") {
                val adminId = call.userId ?: return@delete call.respond(HttpStatusCode.Unauthorized, ApiErrorResponse("UNAUTHORIZED", "Not authenticated"))
                val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest, ApiErrorResponse("BAD_REQUEST", "Apartment ID required"))
                apartmentService.deleteApartment(adminId, id, isAdmin = true)
                call.respond(HttpStatusCode.OK, mapOf("message" to "Apartment deleted by admin"))
            }

            get("/bookings") {
                val bookings = bookingService.getAllBookingsForAdmin()
                call.respond(HttpStatusCode.OK, bookings)
            }

            get("/users") {
                val users = authService.getAllUsers()
                call.respond(HttpStatusCode.OK, users)
            }
        }
    }
}
