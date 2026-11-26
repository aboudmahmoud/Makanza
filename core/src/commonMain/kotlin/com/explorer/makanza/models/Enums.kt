package com.explorer.makanza.models

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    STUDENT,
    PROPERTY_OWNER,
    ADMIN
}

@Serializable
enum class ListingType {
    RENT,
    SALE
}

@Serializable
enum class RentalPeriod {
    PER_DAY,
    PER_MONTH,
    PER_SEMESTER,
    PER_YEAR
}

@Serializable
enum class AccommodationType {
    PRIVATE_APARTMENT,
    SHARED_HOUSING,
    PRIVATE_ROOM,
    STUDIO,
    VILLA
}

@Serializable
enum class GenderEligibility {
    ANY,
    MALE_ONLY,
    FEMALE_ONLY
}

@Serializable
enum class ApartmentStatus {
    AVAILABLE,
    RENTED,
    SOLD,
    UNAVAILABLE
}

@Serializable
enum class ApprovalStatus {
    PENDING_APPROVAL,
    APPROVED,
    REJECTED
}

@Serializable
enum class ApartmentType {
    APARTMENT,
    VILLA,
    PENTHOUSE,
    TOWNHOUSE,
    STUDIO
}

@Serializable
enum class BookingStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    CANCELLED,
    COMPLETED
}
