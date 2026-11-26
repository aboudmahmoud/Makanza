package com.explorer.makanza

import com.explorer.makanza.database.DatabaseFactory
import com.explorer.makanza.models.*
import com.explorer.makanza.services.*
import kotlinx.coroutines.runBlocking
import org.junit.BeforeClass
import org.junit.Test
import kotlin.test.*

class BackendApiTest {

    companion object {
        private lateinit var authService: AuthService
        private lateinit var apartmentService: ApartmentService
        private lateinit var bookingService: BookingService

        @BeforeClass
        @JvmStatic
        fun setup() {
            System.setProperty("JDBC_DATABASE_URL", "jdbc:h2:mem:testdb_${System.currentTimeMillis()};DB_CLOSE_DELAY=-1;MODE=PostgreSQL")
            DatabaseFactory.init()

            val storageService = LocalStorageService("./build/test-uploads")
            authService = AuthService()
            apartmentService = ApartmentService(storageService)
            bookingService = BookingService(apartmentService)
        }
    }

    @Test
    fun testUserRegistrationAndLogin() {
        runBlocking {
            val registerReq = RegisterRequest(
                fullName = "Student User",
                email = "student_${System.currentTimeMillis()}@makanza.eg",
                phone = "+201000000001",
                password = "SecretPassword123",
                confirmPassword = "SecretPassword123",
                role = UserRole.STUDENT
            )

            val regResponse = authService.register(registerReq)
            assertNotNull(regResponse.token)
            assertEquals("Student User", regResponse.user.fullName)
            assertEquals(UserRole.STUDENT, regResponse.user.role)

            // Test login
            val loginReq = LoginRequest(
                email = registerReq.email,
                password = "SecretPassword123"
            )
            val loginResp = authService.login(loginReq)
            assertNotNull(loginResp.token)
            assertEquals(regResponse.user.id, loginResp.user.id)
            assertEquals(UserRole.STUDENT, loginResp.user.role)
        }
    }

    @Test
    fun testDuplicateEmailRejection() {
        runBlocking {
            val email = "dup_${System.currentTimeMillis()}@makanza.eg"
            val req = RegisterRequest("User One", email, "+201000000002", "Password123", "Password123")
            authService.register(req)

            assertFailsWith<IllegalArgumentException> {
                authService.register(RegisterRequest("User Two", email, "+201000000003", "Password123", "Password123"))
            }
        }
    }

    @Test
    fun testApartmentCreationEditingAndOwnership() {
        runBlocking {
            val ownerReq = RegisterRequest("Owner User", "owner_${System.currentTimeMillis()}@makanza.eg", "+201000000004", "Password123", "Password123", role = UserRole.PROPERTY_OWNER)
            val owner = authService.register(ownerReq)

            val otherReq = RegisterRequest("Other User", "other_${System.currentTimeMillis()}@makanza.eg", "+201000000005", "Password123", "Password123", role = UserRole.STUDENT)
            val other = authService.register(otherReq)

            val createReq = CreateApartmentRequest(
                title = "شقة مفروشة بشرق النيل",
                description = "شقة ممتازة للطلاب",
                listingType = ListingType.RENT,
                price = 3000.0,
                city = "Beni Suef",
                neighborhood = "شرق النيل",
                address = "شارع الجامعة",
                bedrooms = 2,
                bathrooms = 1,
                areaSqm = 95.0,
                apartmentType = ApartmentType.APARTMENT,
                accommodationType = AccommodationType.PRIVATE_APARTMENT,
                furnished = true,
                contactPhone = "+201000000004"
            )

            val created = apartmentService.createApartment(owner.user.id, createReq, autoApprove = true)
            assertEquals("شقة مفروشة بشرق النيل", created.title)
            assertEquals(owner.user.id, created.ownerId)

            // Edit by owner should succeed
            val updated = apartmentService.updateApartment(
                owner.user.id,
                created.id,
                UpdateApartmentRequest(price = 2800.0)
            )
            assertEquals(2800.0, updated.price)

            // Edit by non-owner should throw IllegalAccessException
            assertFailsWith<IllegalAccessException> {
                apartmentService.updateApartment(
                    other.user.id,
                    created.id,
                    UpdateApartmentRequest(price = 2000.0)
                )
            }
        }
    }

    @Test
    fun testApartmentFilteringAndSearch() {
        runBlocking {
            val owner = authService.register(RegisterRequest("Filter Owner", "filter_${System.currentTimeMillis()}@makanza.eg", "+201000000006", "Pass1234", "Pass1234", role = UserRole.PROPERTY_OWNER))

            apartmentService.createApartment(
                owner.user.id,
                CreateApartmentRequest(
                    title = "سكن شباب في صلاح سالم",
                    description = "ممتازة",
                    listingType = ListingType.RENT,
                    price = 1200.0,
                    city = "Beni Suef",
                    neighborhood = "صلاح سالم",
                    address = "A1",
                    bedrooms = 2,
                    bathrooms = 1,
                    areaSqm = 80.0,
                    apartmentType = ApartmentType.APARTMENT,
                    accommodationType = AccommodationType.SHARED_HOUSING,
                    furnished = true,
                    contactPhone = "+201000000006"
                ),
                autoApprove = true
            )

            apartmentService.createApartment(
                owner.user.id,
                CreateApartmentRequest(
                    title = "شقة بالكورنيش",
                    description = "إطلالة ممتازة",
                    listingType = ListingType.RENT,
                    price = 4000.0,
                    city = "Beni Suef",
                    neighborhood = "الكورنيش",
                    address = "B2",
                    bedrooms = 3,
                    bathrooms = 2,
                    areaSqm = 130.0,
                    apartmentType = ApartmentType.APARTMENT,
                    accommodationType = AccommodationType.PRIVATE_APARTMENT,
                    furnished = true,
                    contactPhone = "+201000000006"
                ),
                autoApprove = true
            )

            val searchResult = apartmentService.getApartments(ApartmentFilterParams(q = "صلاح سالم"))
            assertTrue(searchResult.items.any { it.title.contains("صلاح سالم") })

            val sharedResult = apartmentService.getApartments(ApartmentFilterParams(accommodationType = AccommodationType.SHARED_HOUSING))
            assertTrue(sharedResult.items.all { it.accommodationType == AccommodationType.SHARED_HOUSING })
        }
    }

    @Test
    fun testBookingFlowAndValidations() {
        runBlocking {
            val publisher = authService.register(RegisterRequest("Publisher User", "pub_${System.currentTimeMillis()}@makanza.eg", "+201000000007", "Pass1234", "Pass1234", role = UserRole.PROPERTY_OWNER))
            val tenant = authService.register(RegisterRequest("Tenant Student", "tenant_${System.currentTimeMillis()}@makanza.eg", "+201000000008", "Pass1234", "Pass1234", role = UserRole.STUDENT))

            val apartment = apartmentService.createApartment(
                publisher.user.id,
                CreateApartmentRequest(
                    title = "شقة للإيجار الطلابية",
                    description = "شقة مفروشة",
                    listingType = ListingType.RENT,
                    price = 2500.0,
                    city = "Beni Suef",
                    neighborhood = "شرق النيل",
                    address = "C3",
                    bedrooms = 2,
                    bathrooms = 1,
                    areaSqm = 85.0,
                    apartmentType = ApartmentType.APARTMENT,
                    furnished = true,
                    contactPhone = "+201000000007"
                ),
                autoApprove = true
            )

            // Self booking rejection
            assertFailsWith<IllegalArgumentException> {
                bookingService.createBooking(
                    publisher.user.id,
                    CreateBookingRequest(
                        apartmentId = apartment.id,
                        moveInDate = "2025-09-01",
                        moveOutDate = "2026-06-30",
                        occupants = 2,
                        message = "Self booking"
                    )
                )
            }

            // Invalid dates rejection (move-out before move-in)
            assertFailsWith<IllegalArgumentException> {
                bookingService.createBooking(
                    tenant.user.id,
                    CreateBookingRequest(
                        apartmentId = apartment.id,
                        moveInDate = "2026-06-30",
                        moveOutDate = "2025-09-01",
                        occupants = 2,
                        message = "Invalid dates"
                    )
                )
            }

            // Successful booking request
            val booking1 = bookingService.createBooking(
                tenant.user.id,
                CreateBookingRequest(
                    apartmentId = apartment.id,
                    moveInDate = "2025-09-01",
                    moveOutDate = "2026-06-30",
                    occupants = 2,
                    message = "Looking forward to stay"
                )
            )
            assertEquals(BookingStatus.PENDING, booking1.status)

            // Publisher accepts booking1
            val accepted1 = bookingService.updateBookingStatus(publisher.user.id, booking1.id, BookingStatus.ACCEPTED)
            assertEquals(BookingStatus.ACCEPTED, accepted1.status)

            // Overlapping booking request with an already ACCEPTED reservation is rejected
            val tenant2 = authService.register(RegisterRequest("Student 2", "tenant2_${System.currentTimeMillis()}@makanza.eg", "+201000000009", "Pass1234", "Pass1234", role = UserRole.STUDENT))
            
            assertFailsWith<IllegalArgumentException> {
                bookingService.createBooking(
                    tenant2.user.id,
                    CreateBookingRequest(
                        apartmentId = apartment.id,
                        moveInDate = "2025-10-01",
                        moveOutDate = "2026-05-30",
                        occupants = 1,
                        message = "Overlapping stay"
                    )
                )
            }

            // Cancel booking
            val booking3 = bookingService.createBooking(
                tenant2.user.id,
                CreateBookingRequest(
                    apartmentId = apartment.id,
                    moveInDate = "2026-07-01",
                    moveOutDate = "2026-08-30",
                    occupants = 1,
                    message = "Non overlapping stay"
                )
            )
            val cancelled = bookingService.cancelBooking(tenant2.user.id, booking3.id)
            assertEquals(BookingStatus.CANCELLED, cancelled.status)
        }
    }
}
