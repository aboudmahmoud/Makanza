package com.explorer.makanza.routes

import com.explorer.makanza.models.*
import com.explorer.makanza.services.ApartmentService
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.apartmentRoutes(apartmentService: ApartmentService) {
    route("/api/v1/apartments") {

        get {
            val q = call.request.queryParameters["q"]
            val listingType = call.request.queryParameters["listingType"]?.let {
                try { ListingType.valueOf(it.uppercase()) } catch (e: Exception) { null }
            }
            val minPrice = call.request.queryParameters["minPrice"]?.toDoubleOrNull()
            val maxPrice = call.request.queryParameters["maxPrice"]?.toDoubleOrNull()
            val bedrooms = call.request.queryParameters["bedrooms"]?.toIntOrNull()
            val city = call.request.queryParameters["city"]
            val sort = call.request.queryParameters["sort"] ?: "newest"
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 10

            val params = ApartmentFilterParams(
                q = q,
                listingType = listingType,
                minPrice = minPrice,
                maxPrice = maxPrice,
                bedrooms = bedrooms,
                city = city,
                sort = sort,
                page = page,
                pageSize = pageSize
            )

            val result = apartmentService.getApartments(params)
            call.respond(HttpStatusCode.OK, result)
        }

        get("/featured") {
            val featured = apartmentService.getFeaturedApartments()
            call.respond(HttpStatusCode.OK, featured)
        }

        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, ApiErrorResponse("BAD_REQUEST", "Apartment ID required"))
            val apartment = apartmentService.getApartmentById(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, ApiErrorResponse("NOT_FOUND", "Apartment not found"))
            call.respond(HttpStatusCode.OK, apartment)
        }

        authenticate("auth-jwt") {
            post {
                val uId = call.userId ?: return@post call.respond(HttpStatusCode.Unauthorized, ApiErrorResponse("UNAUTHORIZED", "Not authenticated"))
                val req = call.receive<CreateApartmentRequest>()
                val created = apartmentService.createApartment(uId, req)
                call.respond(HttpStatusCode.Created, created)
            }

            put("/{id}") {
                val uId = call.userId ?: return@put call.respond(HttpStatusCode.Unauthorized, ApiErrorResponse("UNAUTHORIZED", "Not authenticated"))
                val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest, ApiErrorResponse("BAD_REQUEST", "Apartment ID required"))
                val req = call.receive<UpdateApartmentRequest>()
                val updated = apartmentService.updateApartment(uId, id, req)
                call.respond(HttpStatusCode.OK, updated)
            }

            patch("/{id}/status") {
                val uId = call.userId ?: return@patch call.respond(HttpStatusCode.Unauthorized, ApiErrorResponse("UNAUTHORIZED", "Not authenticated"))
                val id = call.parameters["id"] ?: return@patch call.respond(HttpStatusCode.BadRequest, ApiErrorResponse("BAD_REQUEST", "Apartment ID required"))
                val req = call.receive<UpdateApartmentStatusRequest>()
                val updated = apartmentService.updateApartmentStatus(uId, id, req.status)
                call.respond(HttpStatusCode.OK, updated)
            }

            delete("/{id}") {
                val uId = call.userId ?: return@delete call.respond(HttpStatusCode.Unauthorized, ApiErrorResponse("UNAUTHORIZED", "Not authenticated"))
                val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest, ApiErrorResponse("BAD_REQUEST", "Apartment ID required"))
                apartmentService.deleteApartment(uId, id)
                call.respond(HttpStatusCode.OK, mapOf("message" to "Apartment deleted successfully"))
            }

            post("/{id}/images") {
                val uId = call.userId ?: return@post call.respond(HttpStatusCode.Unauthorized, ApiErrorResponse("UNAUTHORIZED", "Not authenticated"))
                val apartmentId = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest, ApiErrorResponse("BAD_REQUEST", "Apartment ID required"))

                val multipart = call.receiveMultipart()
                var savedImageDto: ApartmentImageDto? = null

                multipart.forEachPart { part ->
                    if (part is PartData.FileItem) {
                        val fileName = part.originalFileName ?: "image.jpg"
                        val extension = fileName.substringAfterLast(".", "jpg").lowercase()

                        if (extension !in listOf("jpg", "jpeg", "png", "webp")) {
                            part.dispose()
                            return@forEachPart
                        }

                        val bytes = part.streamProvider().readBytes()
                        if (bytes.size > 5 * 1024 * 1024) { // 5MB limit
                            part.dispose()
                            return@forEachPart
                        }

                        savedImageDto = apartmentService.addImageToApartment(uId, apartmentId, bytes, extension)
                        part.dispose()
                    } else {
                        part.dispose()
                    }
                }

                if (savedImageDto != null) {
                    call.respond(HttpStatusCode.Created, savedImageDto!!)
                } else {
                    call.respond(HttpStatusCode.BadRequest, ApiErrorResponse("UPLOAD_FAILED", "No valid image file uploaded"))
                }
            }

            delete("/{id}/images/{imageId}") {
                val uId = call.userId ?: return@delete call.respond(HttpStatusCode.Unauthorized, ApiErrorResponse("UNAUTHORIZED", "Not authenticated"))
                val apartmentId = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest, ApiErrorResponse("BAD_REQUEST", "Apartment ID required"))
                val imageId = call.parameters["imageId"] ?: return@delete call.respond(HttpStatusCode.BadRequest, ApiErrorResponse("BAD_REQUEST", "Image ID required"))

                apartmentService.deleteApartmentImage(uId, apartmentId, imageId)
                call.respond(HttpStatusCode.OK, mapOf("message" to "Image deleted successfully"))
            }
        }
    }

    authenticate("auth-jwt") {
        get("/api/v1/users/me/apartments") {
            val uId = call.userId ?: return@get call.respond(HttpStatusCode.Unauthorized, ApiErrorResponse("UNAUTHORIZED", "Not authenticated"))
            val userApartments = apartmentService.getUserApartments(uId)
            call.respond(HttpStatusCode.OK, userApartments)
        }
    }
}
