package com.explorer.makanza.models

import kotlinx.serialization.Serializable

@Serializable
data class BookingDto(
    val id: String,
    val apartmentId: String,
    val requesterId: String,
    val moveInDate: String,
    val moveOutDate: String,
    val rentalPeriod: RentalPeriod = RentalPeriod.PER_MONTH,
    val totalPrice: Double? = null,
    val occupants: Int,
    val message: String,
    val status: BookingStatus,
    val apartmentTitle: String? = null,
    val apartmentCity: String? = null,
    val apartmentPrice: Double? = null,
    val apartmentImageUrl: String? = null,
    val requesterName: String? = null,
    val requesterEmail: String? = null,
    val requesterPhone: String? = null,
    val ownerName: String? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class CreateBookingRequest(
    val apartmentId: String,
    val moveInDate: String,
    val moveOutDate: String,
    val rentalPeriod: RentalPeriod = RentalPeriod.PER_MONTH,
    val totalPrice: Double? = null,
    val occupants: Int = 1,
    val message: String = "",
    val guestName: String? = null,
    val guestPhone: String? = null
)

@Serializable
data class UpdateBookingStatusRequest(
    val status: BookingStatus
)
