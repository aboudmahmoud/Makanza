package com.explorer.makanza.services

import com.explorer.makanza.database.*
import com.explorer.makanza.database.DatabaseFactory.dbQuery
import com.explorer.makanza.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import java.util.UUID

class BookingService(
    private val apartmentService: ApartmentService
) {

    private fun ResultRow.toBookingDto(
        apartment: ResultRow? = null,
        requester: ResultRow? = null,
        owner: ResultRow? = null
    ): BookingDto {
        val aptImage = apartment?.let { aptRow ->
            val aptId = aptRow[ApartmentsTable.id]
            ApartmentImagesTable.selectAll()
                .where(ApartmentImagesTable.apartmentId eq aptId)
                .orderBy(ApartmentImagesTable.sortOrder to SortOrder.ASC)
                .firstOrNull()?.get(ApartmentImagesTable.imageUrl)
        }

        return BookingDto(
            id = this[BookingsTable.id],
            apartmentId = this[BookingsTable.apartmentId],
            requesterId = this[BookingsTable.requesterId],
            moveInDate = this[BookingsTable.moveInDate],
            moveOutDate = this[BookingsTable.moveOutDate],
            rentalPeriod = RentalPeriod.valueOf(this[BookingsTable.rentalPeriod]),
            totalPrice = this[BookingsTable.totalPrice],
            occupants = this[BookingsTable.occupants],
            message = this[BookingsTable.message],
            status = BookingStatus.valueOf(this[BookingsTable.status]),
            apartmentTitle = apartment?.get(ApartmentsTable.title),
            apartmentCity = apartment?.get(ApartmentsTable.city),
            apartmentPrice = apartment?.get(ApartmentsTable.price),
            apartmentImageUrl = aptImage,
            requesterName = requester?.get(UsersTable.fullName),
            requesterEmail = requester?.get(UsersTable.email),
            requesterPhone = requester?.get(UsersTable.phone),
            ownerName = owner?.get(UsersTable.fullName),
            createdAt = this[BookingsTable.createdAt].toString(),
            updatedAt = this[BookingsTable.updatedAt].toString()
        )
    }

    suspend fun createBooking(requesterId: String, req: CreateBookingRequest): BookingDto {
        val apartment = apartmentService.getApartmentById(req.apartmentId)
            ?: throw IllegalArgumentException("Apartment not found")

        if (apartment.listingType != ListingType.RENT) {
            throw IllegalArgumentException("Only rental properties can be booked")
        }

        if (apartment.status != ApartmentStatus.AVAILABLE) {
            throw IllegalArgumentException("This apartment is currently not available for booking")
        }

        if (apartment.ownerId == requesterId) {
            throw IllegalArgumentException("You cannot book your own apartment")
        }

        if (req.moveInDate >= req.moveOutDate) {
            throw IllegalArgumentException("Move-out date must be after move-in date")
        }

        if (req.occupants < 1) {
            throw IllegalArgumentException("Occupants count must be at least 1")
        }

        // Check date overlap with existing ACCEPTED bookings
        val overlappingAccepted = dbQuery {
            BookingsTable.selectAll()
                .where {
                    (BookingsTable.apartmentId eq req.apartmentId) and
                    (BookingsTable.status eq BookingStatus.ACCEPTED.name) and
                    (BookingsTable.moveInDate less req.moveOutDate) and
                    (BookingsTable.moveOutDate greater req.moveInDate)
                }.count()
        }

        if (overlappingAccepted > 0) {
            throw IllegalArgumentException("The requested dates overlap with an existing accepted booking")
        }

        val bookingId = UUID.randomUUID().toString()

        dbQuery {
            BookingsTable.insert {
                it[id] = bookingId
                it[apartmentId] = req.apartmentId
                it[this.requesterId] = requesterId
                it[moveInDate] = req.moveInDate
                it[moveOutDate] = req.moveOutDate
                it[rentalPeriod] = req.rentalPeriod.name
                it[totalPrice] = req.totalPrice ?: apartment.price
                it[occupants] = req.occupants
                it[message] = req.message.trim()
                it[status] = BookingStatus.PENDING.name
            }
        }

        return getBookingById(bookingId) ?: throw IllegalStateException("Booking creation failed")
    }

    suspend fun getBookingById(id: String): BookingDto? = dbQuery {
        val row = BookingsTable.selectAll().where(BookingsTable.id eq id).singleOrNull()
            ?: return@dbQuery null

        val apartmentRow = ApartmentsTable.selectAll().where(ApartmentsTable.id eq row[BookingsTable.apartmentId]).singleOrNull()
        val requesterRow = UsersTable.selectAll().where(UsersTable.id eq row[BookingsTable.requesterId]).singleOrNull()
        val ownerRow = apartmentRow?.let {
            UsersTable.selectAll().where(UsersTable.id eq it[ApartmentsTable.ownerId]).singleOrNull()
        }

        row.toBookingDto(apartment = apartmentRow, requester = requesterRow, owner = ownerRow)
    }

    suspend fun getUserBookings(userId: String): List<BookingDto> = dbQuery {
        val rows = BookingsTable.selectAll()
            .where(BookingsTable.requesterId eq userId)
            .orderBy(BookingsTable.createdAt to SortOrder.DESC)
            .toList()

        rows.map { row ->
            val apartmentRow = ApartmentsTable.selectAll().where(ApartmentsTable.id eq row[BookingsTable.apartmentId]).singleOrNull()
            val requesterRow = UsersTable.selectAll().where(UsersTable.id eq row[BookingsTable.requesterId]).singleOrNull()
            val ownerRow = apartmentRow?.let {
                UsersTable.selectAll().where(UsersTable.id eq it[ApartmentsTable.ownerId]).singleOrNull()
            }
            row.toBookingDto(apartment = apartmentRow, requester = requesterRow, owner = ownerRow)
        }
    }

    suspend fun getApartmentBookingsForOwner(ownerId: String, apartmentId: String): List<BookingDto> = dbQuery {
        val apartment = ApartmentsTable.selectAll().where(ApartmentsTable.id eq apartmentId).singleOrNull()
            ?: throw IllegalArgumentException("Apartment not found")

        if (apartment[ApartmentsTable.ownerId] != ownerId) {
            throw IllegalAccessException("You are not authorized to view bookings for this apartment")
        }

        val rows = BookingsTable.selectAll()
            .where(BookingsTable.apartmentId eq apartmentId)
            .orderBy(BookingsTable.createdAt to SortOrder.DESC)
            .toList()

        rows.map { row ->
            val requesterRow = UsersTable.selectAll().where(UsersTable.id eq row[BookingsTable.requesterId]).singleOrNull()
            val ownerRow = UsersTable.selectAll().where(UsersTable.id eq ownerId).singleOrNull()
            row.toBookingDto(apartment = apartment, requester = requesterRow, owner = ownerRow)
        }
    }

    suspend fun getReceivedBookingRequests(ownerId: String): List<BookingDto> = dbQuery {
        val ownerApartmentIds = ApartmentsTable.selectAll()
            .where(ApartmentsTable.ownerId eq ownerId)
            .map { it[ApartmentsTable.id] }

        if (ownerApartmentIds.isEmpty()) return@dbQuery emptyList()

        val rows = BookingsTable.selectAll()
            .where(BookingsTable.apartmentId inList ownerApartmentIds)
            .orderBy(BookingsTable.createdAt to SortOrder.DESC)
            .toList()

        rows.map { row ->
            val apartmentRow = ApartmentsTable.selectAll().where(ApartmentsTable.id eq row[BookingsTable.apartmentId]).singleOrNull()
            val requesterRow = UsersTable.selectAll().where(UsersTable.id eq row[BookingsTable.requesterId]).singleOrNull()
            val ownerRow = UsersTable.selectAll().where(UsersTable.id eq ownerId).singleOrNull()
            row.toBookingDto(apartment = apartmentRow, requester = requesterRow, owner = ownerRow)
        }
    }

    suspend fun getAllBookingsForAdmin(): List<BookingDto> = dbQuery {
        val rows = BookingsTable.selectAll()
            .orderBy(BookingsTable.createdAt to SortOrder.DESC)
            .toList()

        rows.map { row ->
            val apartmentRow = ApartmentsTable.selectAll().where(ApartmentsTable.id eq row[BookingsTable.apartmentId]).singleOrNull()
            val requesterRow = UsersTable.selectAll().where(UsersTable.id eq row[BookingsTable.requesterId]).singleOrNull()
            val ownerRow = apartmentRow?.let {
                UsersTable.selectAll().where(UsersTable.id eq it[ApartmentsTable.ownerId]).singleOrNull()
            }
            row.toBookingDto(apartment = apartmentRow, requester = requesterRow, owner = ownerRow)
        }
    }

    suspend fun updateBookingStatus(ownerId: String, bookingId: String, newStatus: BookingStatus): BookingDto {
        val booking = getBookingById(bookingId) ?: throw IllegalArgumentException("Booking not found")
        val apartment = apartmentService.getApartmentById(booking.apartmentId)
            ?: throw IllegalArgumentException("Apartment not found")

        if (apartment.ownerId != ownerId) {
            throw IllegalAccessException("Only the apartment publisher can change booking request status")
        }

        if (booking.status != BookingStatus.PENDING) {
            throw IllegalArgumentException("Cannot change status of a booking that is already ${booking.status}")
        }

        if (newStatus != BookingStatus.ACCEPTED && newStatus != BookingStatus.REJECTED) {
            throw IllegalArgumentException("Publisher can only set status to ACCEPTED or REJECTED")
        }

        if (newStatus == BookingStatus.ACCEPTED) {
            // Check overlaps once more atomically
            val overlappingAccepted = dbQuery {
                BookingsTable.selectAll()
                    .where {
                        (BookingsTable.apartmentId eq booking.apartmentId) and
                        (BookingsTable.id neq bookingId) and
                        (BookingsTable.status eq BookingStatus.ACCEPTED.name) and
                        (BookingsTable.moveInDate less booking.moveOutDate) and
                        (BookingsTable.moveOutDate greater booking.moveInDate)
                    }.count()
            }
            if (overlappingAccepted > 0) {
                throw IllegalArgumentException("Cannot accept this booking because dates overlap with another accepted reservation")
            }
        }

        dbQuery {
            BookingsTable.update({ BookingsTable.id eq bookingId }) {
                it[status] = newStatus.name
            }
        }

        return getBookingById(bookingId) ?: throw IllegalStateException("Booking status update failed")
    }

    suspend fun cancelBooking(requesterId: String, bookingId: String): BookingDto {
        val booking = getBookingById(bookingId) ?: throw IllegalArgumentException("Booking not found")

        if (booking.requesterId != requesterId) {
            throw IllegalAccessException("You are not authorized to cancel this booking")
        }

        if (booking.status != BookingStatus.PENDING) {
            throw IllegalArgumentException("Only PENDING bookings can be cancelled")
        }

        dbQuery {
            BookingsTable.update({ BookingsTable.id eq bookingId }) {
                it[status] = BookingStatus.CANCELLED.name
            }
        }

        return getBookingById(bookingId) ?: throw IllegalStateException("Booking cancellation failed")
    }
}
