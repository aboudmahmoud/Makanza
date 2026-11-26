package com.explorer.makanza.plugins

import com.explorer.makanza.routes.*
import com.explorer.makanza.services.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Application.configureRouting(
    authService: AuthService,
    apartmentService: ApartmentService,
    bookingService: BookingService
) {
    routing {
        get("/") {
            call.respondText("Makanza Apartment Marketplace API v1.0.0 is running.")
        }

        // Serve uploaded image files
        staticFiles("/uploads", File("./uploads"))

        authRoutes(authService)
        userRoutes(authService)
        apartmentRoutes(apartmentService)
        bookingRoutes(bookingService)
        adminRoutes(authService, apartmentService, bookingService)
        openApiRoutes()
    }
}
