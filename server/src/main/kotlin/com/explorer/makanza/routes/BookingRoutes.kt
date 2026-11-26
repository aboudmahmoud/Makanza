package com.explorer.makanza.routes

import com.explorer.makanza.models.*
import com.explorer.makanza.services.BookingService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.bookingRoutes(bookingService: BookingService) {
    authenticate("auth-jwt") {
        route("/api/v1/bookings") {

            post {
                val uId = call.userId ?: return@post call.respond(HttpStatusCode.Unauthorized, ApiErrorResponse("UNAUTHORIZED", "Not authenticated"))
                val req = call.receive<CreateBookingRequest>()
                val created = bookingService.createBooking(uId, req)
                call.respond(HttpStatusCode.Created, created)
            }

            patch("/{id}/status") {
                val uId = call.userId ?: return@patch call.respond(HttpStatusCode.Unauthorized, ApiErrorResponse("UNAUTHORIZED", "Not authenticated"))
                val bookingId = call.parameters["id"] ?: return@patch call.respond(HttpStatusCode.BadRequest, ApiErrorResponse("BAD_REQUEST", "Booking ID required"))
                val req = call.receive<UpdateBookingStatusRequest>()
                val updated = bookingService.updateBookingStatus(uId, bookingId, req.status)
                call.respond(HttpStatusCode.OK, updated)
            }

            post("/{id}/cancel") {
                val uId = call.userId ?: return@post call.respond(HttpStatusCode.Unauthorized, ApiErrorResponse("UNAUTHORIZED", "Not authenticated"))
                val bookingId = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest, ApiErrorResponse("BAD_REQUEST", "Booking ID required"))
                val cancelled = bookingService.cancelBooking(uId, bookingId)
                call.respond(HttpStatusCode.OK, cancelled)
            }
        }

        route("/api/v1/users/me") {
            get("/bookings") {
                val uId = call.userId ?: return@get call.respond(HttpStatusCode.Unauthorized, ApiErrorResponse("UNAUTHORIZED", "Not authenticated"))
                val myBookings = bookingService.getUserBookings(uId)
                call.respond(HttpStatusCode.OK, myBookings)
            }

            get("/booking-requests") {
                val uId = call.userId ?: return@get call.respond(HttpStatusCode.Unauthorized, ApiErrorResponse("UNAUTHORIZED", "Not authenticated"))
                val requests = bookingService.getReceivedBookingRequests(uId)
                call.respond(HttpStatusCode.OK, requests)
            }
        }

        get("/api/v1/apartments/{id}/bookings") {
            val uId = call.userId ?: return@get call.respond(HttpStatusCode.Unauthorized, ApiErrorResponse("UNAUTHORIZED", "Not authenticated"))
            val apartmentId = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, ApiErrorResponse("BAD_REQUEST", "Apartment ID required"))
            val bookings = bookingService.getApartmentBookingsForOwner(uId, apartmentId)
            call.respond(HttpStatusCode.OK, bookings)
        }
    }
}
