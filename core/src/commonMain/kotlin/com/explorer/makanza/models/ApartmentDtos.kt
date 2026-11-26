package com.explorer.makanza.models

import kotlinx.serialization.Serializable

@Serializable
data class ApartmentImageDto(
    val id: String,
    val apartmentId: String,
    val imageUrl: String,
    val sortOrder: Int
)

@Serializable
data class ApartmentDto(
    val id: String,
    val ownerId: String,
    val title: String,
    val description: String,
    val listingType: ListingType = ListingType.RENT,
    val rentalPeriod: RentalPeriod = RentalPeriod.PER_MONTH,
    val price: Double,
    val currency: String = "EGP",
    val country: String = "Egypt",
    val city: String = "Beni Suef",
    val neighborhood: String,
    val address: String,
    val bedrooms: Int,
    val bathrooms: Int,
    val areaSqm: Double,
    val apartmentType: ApartmentType,
    val accommodationType: AccommodationType = AccommodationType.PRIVATE_APARTMENT,
    val availableBeds: Int = 1,
    val genderEligibility: GenderEligibility = GenderEligibility.ANY,
    val nearUniversity: String? = null, // "جامعة بني سويف" or "جامعة النهضة"
    val nearCollege: String? = null,    // "كلية الطب", "كلية الصيدلة", etc.
    val furnished: Boolean,
    val amenities: List<String>,
    val status: ApartmentStatus,
    val approvalStatus: ApprovalStatus = ApprovalStatus.APPROVED,
    val isDemoData: Boolean = false,
    val distanceToUniversity: String? = null,
    val images: List<ApartmentImageDto> = emptyList(),
    val ownerName: String? = null,
    val ownerPhone: String? = null,
    val allowWhatsapp: Boolean = true,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class CreateApartmentRequest(
    val title: String,
    val description: String,
    val listingType: ListingType = ListingType.RENT,
    val rentalPeriod: RentalPeriod = RentalPeriod.PER_MONTH,
    val price: Double,
    val currency: String = "EGP",
    val country: String = "Egypt",
    val city: String = "Beni Suef",
    val neighborhood: String,
    val address: String,
    val bedrooms: Int,
    val bathrooms: Int,
    val areaSqm: Double,
    val apartmentType: ApartmentType = ApartmentType.APARTMENT,
    val accommodationType: AccommodationType = AccommodationType.PRIVATE_APARTMENT,
    val availableBeds: Int = 1,
    val genderEligibility: GenderEligibility = GenderEligibility.ANY,
    val nearUniversity: String? = null,
    val nearCollege: String? = null,
    val furnished: Boolean = true,
    val amenities: List<String> = emptyList(),
    val contactPhone: String,
    val allowWhatsapp: Boolean = true,
    val distanceToUniversity: String? = null,
    val isDemoData: Boolean = false
)

@Serializable
data class UpdateApartmentRequest(
    val title: String? = null,
    val description: String? = null,
    val listingType: ListingType? = null,
    val rentalPeriod: RentalPeriod? = null,
    val price: Double? = null,
    val country: String? = null,
    val city: String? = null,
    val neighborhood: String? = null,
    val address: String? = null,
    val bedrooms: Int? = null,
    val bathrooms: Int? = null,
    val areaSqm: Double? = null,
    val apartmentType: ApartmentType? = null,
    val accommodationType: AccommodationType? = null,
    val availableBeds: Int? = null,
    val genderEligibility: GenderEligibility? = null,
    val nearUniversity: String? = null,
    val nearCollege: String? = null,
    val furnished: Boolean? = null,
    val amenities: List<String>? = null,
    val contactPhone: String? = null,
    val allowWhatsapp: Boolean? = null,
    val distanceToUniversity: String? = null
)

@Serializable
data class UpdateApartmentStatusRequest(
    val status: ApartmentStatus
)

@Serializable
data class UpdateApprovalStatusRequest(
    val approvalStatus: ApprovalStatus
)
