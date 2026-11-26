package com.explorer.makanza.services

import com.explorer.makanza.database.*
import com.explorer.makanza.database.DatabaseFactory.dbQuery
import com.explorer.makanza.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.UUID

class ApartmentService(
    private val storageService: StorageService
) {

    private fun ResultRow.toApartmentImageDto(): ApartmentImageDto = ApartmentImageDto(
        id = this[ApartmentImagesTable.id],
        apartmentId = this[ApartmentImagesTable.apartmentId],
        imageUrl = this[ApartmentImagesTable.imageUrl],
        sortOrder = this[ApartmentImagesTable.sortOrder]
    )

    private fun ResultRow.toApartmentDto(images: List<ApartmentImageDto> = emptyList(), owner: ResultRow? = null): ApartmentDto = ApartmentDto(
        id = this[ApartmentsTable.id],
        ownerId = this[ApartmentsTable.ownerId],
        title = this[ApartmentsTable.title],
        description = this[ApartmentsTable.description],
        listingType = ListingType.valueOf(this[ApartmentsTable.listingType]),
        rentalPeriod = RentalPeriod.valueOf(this[ApartmentsTable.rentalPeriod]),
        price = this[ApartmentsTable.price],
        currency = this[ApartmentsTable.currency],
        country = this[ApartmentsTable.country],
        city = this[ApartmentsTable.city],
        neighborhood = this[ApartmentsTable.neighborhood],
        address = this[ApartmentsTable.address],
        bedrooms = this[ApartmentsTable.bedrooms],
        bathrooms = this[ApartmentsTable.bathrooms],
        areaSqm = this[ApartmentsTable.areaSqm],
        apartmentType = ApartmentType.valueOf(this[ApartmentsTable.apartmentType]),
        accommodationType = AccommodationType.valueOf(this[ApartmentsTable.accommodationType]),
        availableBeds = this[ApartmentsTable.availableBeds],
        genderEligibility = GenderEligibility.valueOf(this[ApartmentsTable.genderEligibility]),
        nearCollege = this[ApartmentsTable.nearCollege],
        furnished = this[ApartmentsTable.furnished],
        amenities = this[ApartmentsTable.amenities].split(",").map { it.trim() }.filter { it.isNotEmpty() },
        status = ApartmentStatus.valueOf(this[ApartmentsTable.status]),
        approvalStatus = ApprovalStatus.valueOf(this[ApartmentsTable.approvalStatus]),
        isDemoData = this[ApartmentsTable.isDemoData],
        distanceToUniversity = this[ApartmentsTable.distanceToUniversity],
        images = images,
        ownerName = owner?.get(UsersTable.fullName),
        ownerPhone = if (this[ApartmentsTable.allowWhatsapp] || owner != null) this[ApartmentsTable.contactPhone] else null,
        allowWhatsapp = this[ApartmentsTable.allowWhatsapp],
        createdAt = this[ApartmentsTable.createdAt].toString(),
        updatedAt = this[ApartmentsTable.updatedAt].toString()
    )

    private suspend fun getImagesForApartment(apartmentId: String): List<ApartmentImageDto> = dbQuery {
        ApartmentImagesTable.selectAll()
            .where(ApartmentImagesTable.apartmentId eq apartmentId)
            .orderBy(ApartmentImagesTable.sortOrder to SortOrder.ASC)
            .map { it.toApartmentImageDto() }
    }

    suspend fun getApartments(params: ApartmentFilterParams, isAdminRequest: Boolean = false): PaginatedResponse<ApartmentDto> = dbQuery {
        var query = ApartmentsTable.innerJoin(UsersTable)
            .selectAll()

        if (!isAdminRequest && params.approvalStatus == null) {
            query = query.where {
                (ApartmentsTable.status eq ApartmentStatus.AVAILABLE.name) and
                (ApartmentsTable.approvalStatus eq ApprovalStatus.APPROVED.name)
            }
        } else {
            params.status?.let { st ->
                query = query.andWhere { ApartmentsTable.status eq st.name }
            }
            params.approvalStatus?.let { appSt ->
                query = query.andWhere { ApartmentsTable.approvalStatus eq appSt.name }
            }
        }

        params.q?.takeIf { it.isNotBlank() }?.let { search ->
            val pattern = "%${search.trim().lowercase()}%"
            query = query.andWhere {
                (ApartmentsTable.title.lowerCase() like pattern) or
                (ApartmentsTable.city.lowerCase() like pattern) or
                (ApartmentsTable.neighborhood.lowerCase() like pattern) or
                (ApartmentsTable.description.lowerCase() like pattern)
            }
        }

        params.listingType?.let { type ->
            query = query.andWhere { ApartmentsTable.listingType eq type.name }
        }

        params.rentalPeriod?.let { period ->
            query = query.andWhere { ApartmentsTable.rentalPeriod eq period.name }
        }

        params.nearCollege?.takeIf { it.isNotBlank() }?.let { col ->
            query = query.andWhere { ApartmentsTable.nearCollege.lowerCase() like "%${col.trim().lowercase()}%" }
        }

        params.neighborhood?.takeIf { it.isNotBlank() }?.let { neigh ->
            query = query.andWhere { ApartmentsTable.neighborhood.lowerCase() eq neigh.trim().lowercase() }
        }

        params.accommodationType?.let { accType ->
            query = query.andWhere { ApartmentsTable.accommodationType eq accType.name }
        }

        params.genderEligibility?.let { gender ->
            query = query.andWhere {
                (ApartmentsTable.genderEligibility eq gender.name) or (ApartmentsTable.genderEligibility eq GenderEligibility.ANY.name)
            }
        }

        params.minBeds?.let { beds ->
            query = query.andWhere { ApartmentsTable.availableBeds greaterEq beds }
        }

        params.minPrice?.let { minP ->
            query = query.andWhere { ApartmentsTable.price greaterEq minP }
        }

        params.maxPrice?.let { maxP ->
            query = query.andWhere { ApartmentsTable.price lessEq maxP }
        }

        params.bedrooms?.let { beds ->
            query = query.andWhere { ApartmentsTable.bedrooms eq beds }
        }

        params.furnished?.let { furn ->
            query = query.andWhere { ApartmentsTable.furnished eq furn }
        }

        val total = query.count()

        query = when (params.sort) {
            "price_asc" -> query.orderBy(ApartmentsTable.price to SortOrder.ASC)
            "price_desc" -> query.orderBy(ApartmentsTable.price to SortOrder.DESC)
            else -> query.orderBy(ApartmentsTable.createdAt to SortOrder.DESC)
        }

        val page = if (params.page < 1) 1 else params.page
        val pageSize = if (params.pageSize < 1) 10 else params.pageSize
        val totalPages = if (total == 0L) 1 else Math.ceil(total.toDouble() / pageSize).toInt()

        val rows = query.limit(pageSize).offset(((page - 1) * pageSize).toLong()).toList()

        val resultList = rows.map { row ->
            val aptId = row[ApartmentsTable.id]
            val images = ApartmentImagesTable.selectAll()
                .where(ApartmentImagesTable.apartmentId eq aptId)
                .orderBy(ApartmentImagesTable.sortOrder to SortOrder.ASC)
                .map { it.toApartmentImageDto() }
            row.toApartmentDto(images = images, owner = row)
        }

        PaginatedResponse(
            items = resultList,
            total = total,
            page = page,
            pageSize = pageSize,
            totalPages = totalPages
        )
    }

    suspend fun getFeaturedApartments(limit: Int = 6): List<ApartmentDto> = dbQuery {
        val rows = ApartmentsTable.innerJoin(UsersTable)
            .selectAll()
            .where {
                (ApartmentsTable.status eq ApartmentStatus.AVAILABLE.name) and
                (ApartmentsTable.approvalStatus eq ApprovalStatus.APPROVED.name)
            }
            .orderBy(ApartmentsTable.createdAt to SortOrder.DESC)
            .limit(limit)
            .toList()

        rows.map { row ->
            val aptId = row[ApartmentsTable.id]
            val images = ApartmentImagesTable.selectAll()
                .where(ApartmentImagesTable.apartmentId eq aptId)
                .orderBy(ApartmentImagesTable.sortOrder to SortOrder.ASC)
                .map { it.toApartmentImageDto() }
            row.toApartmentDto(images = images, owner = row)
        }
    }

    suspend fun getApartmentById(id: String): ApartmentDto? = dbQuery {
        val row = ApartmentsTable.innerJoin(UsersTable)
            .selectAll()
            .where(ApartmentsTable.id eq id)
            .singleOrNull() ?: return@dbQuery null

        val images = ApartmentImagesTable.selectAll()
            .where(ApartmentImagesTable.apartmentId eq id)
            .orderBy(ApartmentImagesTable.sortOrder to SortOrder.ASC)
            .map { it.toApartmentImageDto() }

        row.toApartmentDto(images = images, owner = row)
    }

    suspend fun createApartment(
        ownerId: String,
        req: CreateApartmentRequest,
        autoApprove: Boolean = true
    ): ApartmentDto {
        val apartmentId = UUID.randomUUID().toString()

        dbQuery {
            ApartmentsTable.insert {
                it[id] = apartmentId
                it[this.ownerId] = ownerId
                it[title] = req.title.trim()
                it[description] = req.description.trim()
                it[listingType] = req.listingType.name
                it[rentalPeriod] = req.rentalPeriod.name
                it[price] = req.price
                it[currency] = req.currency
                it[country] = req.country.trim()
                it[city] = req.city.trim()
                it[neighborhood] = req.neighborhood.trim()
                it[address] = req.address.trim()
                it[bedrooms] = req.bedrooms
                it[bathrooms] = req.bathrooms
                it[areaSqm] = req.areaSqm
                it[apartmentType] = req.apartmentType.name
                it[accommodationType] = req.accommodationType.name
                it[availableBeds] = req.availableBeds
                it[genderEligibility] = req.genderEligibility.name
                it[nearCollege] = req.nearCollege
                it[furnished] = req.furnished
                it[amenities] = req.amenities.joinToString(",")
                it[status] = ApartmentStatus.AVAILABLE.name
                it[approvalStatus] = if (autoApprove) ApprovalStatus.APPROVED.name else ApprovalStatus.PENDING_APPROVAL.name
                it[isDemoData] = req.isDemoData
                it[distanceToUniversity] = req.distanceToUniversity
                it[contactPhone] = req.contactPhone.trim()
                it[allowWhatsapp] = req.allowWhatsapp
            }
        }

        return getApartmentById(apartmentId) ?: throw IllegalStateException("Apartment creation failed")
    }

    suspend fun updateApartment(ownerId: String, apartmentId: String, req: UpdateApartmentRequest, isAdmin: Boolean = false): ApartmentDto {
        val existing = getApartmentById(apartmentId) ?: throw IllegalArgumentException("Apartment not found")
        if (!isAdmin && existing.ownerId != ownerId) {
            throw IllegalAccessException("You are not authorized to update this apartment")
        }

        dbQuery {
            ApartmentsTable.update({ ApartmentsTable.id eq apartmentId }) {
                req.title?.let { t -> it[title] = t.trim() }
                req.description?.let { d -> it[description] = d.trim() }
                req.listingType?.let { lt -> it[listingType] = lt.name }
                req.rentalPeriod?.let { rp -> it[rentalPeriod] = rp.name }
                req.price?.let { p -> it[price] = p }
                req.country?.let { c -> it[country] = c.trim() }
                req.city?.let { ci -> it[city] = ci.trim() }
                req.neighborhood?.let { n -> it[neighborhood] = n.trim() }
                req.address?.let { a -> it[address] = a.trim() }
                req.bedrooms?.let { b -> it[bedrooms] = b }
                req.bathrooms?.let { ba -> it[bathrooms] = ba }
                req.areaSqm?.let { sq -> it[areaSqm] = sq }
                req.apartmentType?.let { at -> it[apartmentType] = at.name }
                req.accommodationType?.let { acc -> it[accommodationType] = acc.name }
                req.availableBeds?.let { beds -> it[availableBeds] = beds }
                req.genderEligibility?.let { ge -> it[genderEligibility] = ge.name }
                req.nearCollege?.let { nc -> it[nearCollege] = nc }
                req.furnished?.let { f -> it[furnished] = f }
                req.amenities?.let { am -> it[amenities] = am.joinToString(",") }
                req.contactPhone?.let { cp -> it[contactPhone] = cp.trim() }
                req.allowWhatsapp?.let { aw -> it[allowWhatsapp] = aw }
                req.distanceToUniversity?.let { dist -> it[distanceToUniversity] = dist }
            }
        }

        return getApartmentById(apartmentId) ?: throw IllegalStateException("Apartment update failed")
    }

    suspend fun updateApprovalStatus(apartmentId: String, approvalStatus: ApprovalStatus): ApartmentDto {
        dbQuery {
            ApartmentsTable.update({ ApartmentsTable.id eq apartmentId }) {
                it[this.approvalStatus] = approvalStatus.name
            }
        }
        return getApartmentById(apartmentId) ?: throw IllegalStateException("Approval status update failed")
    }

    suspend fun updateApartmentStatus(ownerId: String, apartmentId: String, status: ApartmentStatus, isAdmin: Boolean = false): ApartmentDto {
        val existing = getApartmentById(apartmentId) ?: throw IllegalArgumentException("Apartment not found")
        if (!isAdmin && existing.ownerId != ownerId) {
            throw IllegalAccessException("You are not authorized to modify this apartment")
        }

        dbQuery {
            ApartmentsTable.update({ ApartmentsTable.id eq apartmentId }) {
                it[this.status] = status.name
            }
        }

        return getApartmentById(apartmentId) ?: throw IllegalStateException("Status update failed")
    }

    suspend fun deleteApartment(ownerId: String, apartmentId: String, isAdmin: Boolean = false) {
        val existing = getApartmentById(apartmentId) ?: throw IllegalArgumentException("Apartment not found")
        if (!isAdmin && existing.ownerId != ownerId) {
            throw IllegalAccessException("You are not authorized to delete this apartment")
        }

        val images = getImagesForApartment(apartmentId)
        images.forEach { img ->
            storageService.deleteImage(img.imageUrl)
        }

        dbQuery {
            ApartmentImagesTable.deleteWhere { ApartmentImagesTable.apartmentId eq apartmentId }
            BookingsTable.deleteWhere { BookingsTable.apartmentId eq apartmentId }
            ApartmentsTable.deleteWhere { ApartmentsTable.id eq apartmentId }
        }
    }

    suspend fun getUserApartments(userId: String): List<ApartmentDto> = dbQuery {
        val rows = ApartmentsTable.innerJoin(UsersTable)
            .selectAll()
            .where(ApartmentsTable.ownerId eq userId)
            .orderBy(ApartmentsTable.createdAt to SortOrder.DESC)
            .toList()

        rows.map { row ->
            val aptId = row[ApartmentsTable.id]
            val images = ApartmentImagesTable.selectAll()
                .where(ApartmentImagesTable.apartmentId eq aptId)
                .orderBy(ApartmentImagesTable.sortOrder to SortOrder.ASC)
                .map { it.toApartmentImageDto() }
            row.toApartmentDto(images = images, owner = row)
        }
    }

    suspend fun addImageToApartment(ownerId: String, apartmentId: String, bytes: ByteArray, extension: String, isAdmin: Boolean = false): ApartmentImageDto {
        val apartment = getApartmentById(apartmentId) ?: throw IllegalArgumentException("Apartment not found")
        if (!isAdmin && apartment.ownerId != ownerId) {
            throw IllegalAccessException("You are not authorized to edit this apartment")
        }

        val imageUrl = storageService.saveImage(bytes, extension)
        val imageId = UUID.randomUUID().toString()

        val nextSortOrder = dbQuery {
            val count = ApartmentImagesTable.selectAll().where(ApartmentImagesTable.apartmentId eq apartmentId).count()
            count.toInt()
        }

        dbQuery {
            ApartmentImagesTable.insert {
                it[id] = imageId
                it[this.apartmentId] = apartmentId
                it[this.imageUrl] = imageUrl
                it[sortOrder] = nextSortOrder
            }
        }

        return ApartmentImageDto(id = imageId, apartmentId = apartmentId, imageUrl = imageUrl, sortOrder = nextSortOrder)
    }

    suspend fun deleteApartmentImage(ownerId: String, apartmentId: String, imageId: String, isAdmin: Boolean = false) {
        val apartment = getApartmentById(apartmentId) ?: throw IllegalArgumentException("Apartment not found")
        if (!isAdmin && apartment.ownerId != ownerId) {
            throw IllegalAccessException("You are not authorized to edit this apartment")
        }

        val imageRow = dbQuery {
            ApartmentImagesTable.selectAll().where(ApartmentImagesTable.id eq imageId).singleOrNull()
        } ?: throw IllegalArgumentException("Image not found")

        val imageUrl = imageRow[ApartmentImagesTable.imageUrl]
        storageService.deleteImage(imageUrl)

        dbQuery {
            ApartmentImagesTable.deleteWhere { ApartmentImagesTable.id eq imageId }
        }
    }

    // Admin Stats Calculation
    suspend fun getAdminDashboardStats(): AdminDashboardStatsDto = dbQuery {
        val totalApartments = ApartmentsTable.selectAll().count()
        val availableApartments = ApartmentsTable.selectAll().where(ApartmentsTable.status eq ApartmentStatus.AVAILABLE.name).count()
        val pendingApprovalApartments = ApartmentsTable.selectAll().where(ApartmentsTable.approvalStatus eq ApprovalStatus.PENDING_APPROVAL.name).count()

        val totalStudents = UsersTable.selectAll().where(UsersTable.role eq UserRole.STUDENT.name).count()
        val totalPropertyOwners = UsersTable.selectAll().where(UsersTable.role eq UserRole.PROPERTY_OWNER.name).count()

        val totalBookings = BookingsTable.selectAll().count()
        val pendingBookings = BookingsTable.selectAll().where(BookingsTable.status eq BookingStatus.PENDING.name).count()
        val acceptedBookings = BookingsTable.selectAll().where(BookingsTable.status eq BookingStatus.ACCEPTED.name).count()

        AdminDashboardStatsDto(
            totalApartments = totalApartments,
            availableApartments = availableApartments,
            pendingApprovalApartments = pendingApprovalApartments,
            totalStudents = totalStudents,
            totalPropertyOwners = totalPropertyOwners,
            totalBookings = totalBookings,
            pendingBookings = pendingBookings,
            acceptedBookings = acceptedBookings
        )
    }
}
