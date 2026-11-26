package com.explorer.makanza.database

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object UsersTable : Table("users") {
    val id = varchar("id", 36)
    val fullName = varchar("full_name", 120)
    val email = varchar("email", 120).uniqueIndex()
    val phone = varchar("phone", 30)
    val passwordHash = varchar("password_hash", 255)
    val role = varchar("role", 30).default("STUDENT")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}

object ApartmentsTable : Table("apartments") {
    val id = varchar("id", 36)
    val ownerId = varchar("owner_id", 36).references(UsersTable.id)
    val title = varchar("title", 200)
    val description = text("description")
    val listingType = varchar("listing_type", 20).default("RENT")
    val rentalPeriod = varchar("rental_period", 30).default("PER_MONTH")
    val price = double("price")
    val currency = varchar("currency", 10).default("EGP")
    val country = varchar("country", 100).default("Egypt")
    val city = varchar("city", 100).default("Beni Suef")
    val neighborhood = varchar("neighborhood", 100)
    val address = varchar("address", 255)
    val bedrooms = integer("bedrooms")
    val bathrooms = integer("bathrooms")
    val areaSqm = double("area_sqm")
    val apartmentType = varchar("apartment_type", 30).default("APARTMENT")
    val accommodationType = varchar("accommodation_type", 30).default("PRIVATE_APARTMENT")
    val availableBeds = integer("available_beds").default(1)
    val genderEligibility = varchar("gender_eligibility", 20).default("ANY")
    val nearCollege = varchar("near_college", 150).nullable()
    val furnished = bool("furnished").default(true)
    val amenities = text("amenities") // Comma-separated
    val status = varchar("status", 20).default("AVAILABLE")
    val approvalStatus = varchar("approval_status", 30).default("APPROVED")
    val isDemoData = bool("is_demo_data").default(false)
    val distanceToUniversity = varchar("distance_to_university", 150).nullable()
    val contactPhone = varchar("contact_phone", 30)
    val allowWhatsapp = bool("allow_whatsapp").default(true)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}

object ApartmentImagesTable : Table("apartment_images") {
    val id = varchar("id", 36)
    val apartmentId = varchar("apartment_id", 36).references(ApartmentsTable.id)
    val imageUrl = varchar("image_url", 500)
    val sortOrder = integer("sort_order").default(0)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}

object BookingsTable : Table("bookings") {
    val id = varchar("id", 36)
    val apartmentId = varchar("apartment_id", 36).references(ApartmentsTable.id)
    val requesterId = varchar("requester_id", 36).references(UsersTable.id)
    val moveInDate = varchar("move_in_date", 20)
    val moveOutDate = varchar("move_out_date", 20)
    val rentalPeriod = varchar("rental_period", 30).default("PER_MONTH")
    val totalPrice = double("total_price").nullable()
    val occupants = integer("occupants")
    val message = text("message")
    val status = varchar("status", 20).default("PENDING")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}
