package com.explorer.makanza

import com.explorer.makanza.database.DatabaseFactory
import com.explorer.makanza.plugins.*
import com.explorer.makanza.services.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.runBlocking

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init()

    val storageService = LocalStorageService()
    val authService = AuthService()
    val apartmentService = ApartmentService(storageService)
    val bookingService = BookingService(apartmentService)

    runBlocking {
        try {
            DatabaseSeeder.seed(authService, apartmentService)
        } catch (e: Exception) {
            log.warn("Database seeding skipped or failed: ${e.message}")
        }
    }

    configureSerialization()
    configureSecurity()
    configureHTTP()
    configureStatusPages()
    configureRouting(authService, apartmentService, bookingService)
}
