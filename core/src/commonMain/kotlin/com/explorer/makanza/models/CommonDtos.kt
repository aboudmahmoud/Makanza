package com.explorer.makanza.models

import kotlinx.serialization.Serializable

@Serializable
data class PaginatedResponse<T>(
    val items: List<T>,
    val total: Long,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int
)

@Serializable
data class ApiErrorResponse(
    val code: String,
    val message: String,
    val details: Map<String, String>? = null
)

@Serializable
data class ApartmentFilterParams(
    val q: String? = null,
    val listingType: ListingType? = null,
    val rentalPeriod: RentalPeriod? = null,
    val nearUniversity: String? = null,
    val nearCollege: String? = null,
    val genderEligibility: GenderEligibility? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val bedrooms: Int? = null,
    val city: String? = null,
    val neighborhood: String? = null,
    val accommodationType: AccommodationType? = null,
    val minBeds: Int? = null,
    val furnished: Boolean? = null,
    val status: ApartmentStatus? = null,
    val approvalStatus: ApprovalStatus? = null,
    val sort: String? = "newest", // "newest", "price_asc", "price_desc"
    val page: Int = 1,
    val pageSize: Int = 10
)

@Serializable
data class AdminDashboardStatsDto(
    val totalApartments: Long,
    val availableApartments: Long,
    val pendingApprovalApartments: Long,
    val totalStudents: Long,
    val totalPropertyOwners: Long,
    val totalBookings: Long,
    val pendingBookings: Long,
    val acceptedBookings: Long
)
