package com.explorer.makanza.services

import at.favre.lib.crypto.bcrypt.BCrypt
import com.explorer.makanza.database.*
import com.explorer.makanza.database.DatabaseFactory.dbQuery
import com.explorer.makanza.models.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.util.UUID

object DatabaseSeeder {

    suspend fun seed(authService: AuthService, apartmentService: ApartmentService) {
        val userCount = dbQuery { UsersTable.selectAll().count() }
        if (userCount > 0) return // Already seeded

        // Seed demo accounts
        val studentUser = authService.register(
            RegisterRequest(
                fullName = "أحمد محمود (طالب)",
                email = "student@makanza.eg",
                phone = "+201001234567",
                password = "Student123!",
                confirmPassword = "Student123!",
                role = UserRole.STUDENT
            )
        )

        val ownerUser1 = authService.register(
            RegisterRequest(
                fullName = "الحاج محمد البنهاوي (مالك)",
                email = "owner@makanza.eg",
                phone = "+201112223334",
                password = "Owner123!",
                confirmPassword = "Owner123!",
                role = UserRole.PROPERTY_OWNER
            )
        )

        val ownerUser2 = authService.register(
            RegisterRequest(
                fullName = "د. عصام عبد الرحمن",
                email = "owner2@makanza.eg",
                phone = "+201223334445",
                password = "Owner123!",
                confirmPassword = "Owner123!",
                role = UserRole.PROPERTY_OWNER
            )
        )

        // Admin Account (Created with explicit ADMIN role)
        val adminId = UUID.randomUUID().toString()
        val adminPasswordHash = BCrypt.withDefaults().hashToString(12, "Admin123!".toCharArray())
        dbQuery {
            UsersTable.insert {
                it[id] = adminId
                it[fullName] = "مدير المنصة (Admin)"
                it[email] = "admin@makanza.eg"
                it[phone] = "+201099998888"
                it[passwordHash] = adminPasswordHash
                it[role] = UserRole.ADMIN.name
            }
        }

        val owners = listOf(ownerUser1.user.id, ownerUser2.user.id)

        val seedListings = listOf(
            CreateApartmentRequest(
                title = "شقة مفروشة فاخرة بالتكييف بجوار جامعة بني سويف - شرق النيل",
                description = "شقة طلابية مفروشة بالكامل أثاث جديد، تقع في حي شرق النيل القريب جداً من كليات الجامعة. تحتوي على 3 غرف نوم مكيفة، صالة واسعة بمكتب للمذاكرة، مطبخ مجهز ببوتاجاز وثلاجة، حمام شيك، وإنترنت سريع.",
                listingType = ListingType.RENT,
                price = 3500.0,
                currency = "EGP",
                country = "Egypt",
                city = "Beni Suef",
                neighborhood = "شرق النيل",
                address = "شارع الجامعة الرئيسي، بجوار المجمع الطبي",
                bedrooms = 3,
                bathrooms = 1,
                areaSqm = 120.0,
                apartmentType = ApartmentType.APARTMENT,
                accommodationType = AccommodationType.PRIVATE_APARTMENT,
                availableBeds = 6,
                genderEligibility = GenderEligibility.MALE_ONLY,
                furnished = true,
                amenities = listOf("تكييف", "إنترنت واي فاي", "غسالة ملابس", "ثلاجة", "مكاتب مذاكرة", "شاشة تلفزيون", "مصعد"),
                contactPhone = "+201112223334",
                allowWhatsapp = true,
                distanceToUniversity = "5 دقائق مشياً للجامعة",
                isDemoData = true
            ) to listOf(
                "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?auto=format&fit=crop&w=1200&q=80",
                "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=1200&q=80"
            ),

            CreateApartmentRequest(
                title = "سرير في سكن طلابي مشترك - صلاح سالم",
                description = "سرير سينجل في غرفة ثنائية بسكن طلابي مجهز. الشقة نظيفة وهادئة ومناسبة جداً للطلاب الراغبين في سكن اقتصادي ومريح. السعر يشمل الفواتير والإنترنت.",
                listingType = ListingType.RENT,
                price = 950.0,
                currency = "EGP",
                country = "Egypt",
                city = "Beni Suef",
                neighborhood = "صلاح سالم",
                address = "شارع صلاح سالم، بالقرب من كوبري بني سويف",
                bedrooms = 2,
                bathrooms = 1,
                areaSqm = 90.0,
                apartmentType = ApartmentType.APARTMENT,
                accommodationType = AccommodationType.SHARED_HOUSING,
                availableBeds = 2,
                genderEligibility = GenderEligibility.MALE_ONLY,
                furnished = true,
                amenities = listOf("إنترنت واي فاي", "سخانات مياه", "غسالة", "مكتب مذاكرة"),
                contactPhone = "+201112223334",
                allowWhatsapp = true,
                distanceToUniversity = "10 دقائق بالسرفيس",
                isDemoData = true
            ) to listOf(
                "https://images.unsplash.com/photo-1555854877-bab0e564b8d5?auto=format&fit=crop&w=1200&q=80"
            ),

            CreateApartmentRequest(
                title = "سكن طالبات بالكامل قبالة كلية الطب - كورنيش بني سويف",
                description = "شقة راقية ومؤمنة بالكامل مخصصة للطالبات فقط. إطلالة ممتازة على الكورنيش، قرب شديد من المجمع الطبي وكلية الطب. أثاث حديث، حارس للعمارة، ونظام أمان.",
                listingType = ListingType.RENT,
                price = 4200.0,
                currency = "EGP",
                country = "Egypt",
                city = "Beni Suef",
                neighborhood = "الكورنيش",
                address = "طريق الكورنيش، برج الصفا أمام المجمع الطبي",
                bedrooms = 3,
                bathrooms = 2,
                areaSqm = 140.0,
                apartmentType = ApartmentType.APARTMENT,
                accommodationType = AccommodationType.PRIVATE_APARTMENT,
                availableBeds = 6,
                genderEligibility = GenderEligibility.FEMALE_ONLY,
                furnished = true,
                amenities = listOf("حارس عمارة", "إنترنت واي فاي", "تكييف", "غسالة اتوماتيك", "مطبخ كامل", "مصعد"),
                contactPhone = "+201223334445",
                allowWhatsapp = true,
                distanceToUniversity = "3 دقائق سيرًا على الأقدام",
                isDemoData = true
            ) to listOf(
                "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?auto=format&fit=crop&w=1200&q=80",
                "https://images.unsplash.com/photo-1484154218962-a197022b5858?auto=format&fit=crop&w=1200&q=80"
            ),

            CreateApartmentRequest(
                title = "غرفة خاصة في سكن شبابي طلابي - كوبري الجامعة",
                description = "غرفة مستقلة بسرير ومكتب وخزانة ملابس في شقة طلابية هادئة. الشقة مشتركة مع طالبين آخرين من جامعة بني سويف. المطبخ والحمام مشتركين.",
                listingType = ListingType.RENT,
                price = 1400.0,
                currency = "EGP",
                country = "Egypt",
                city = "Beni Suef",
                neighborhood = "كوبري الجامعة",
                address = "شارع البحر بجوار كوبري الجامعة",
                bedrooms = 3,
                bathrooms = 1,
                areaSqm = 100.0,
                apartmentType = ApartmentType.APARTMENT,
                accommodationType = AccommodationType.PRIVATE_ROOM,
                availableBeds = 1,
                genderEligibility = GenderEligibility.MALE_ONLY,
                furnished = true,
                amenities = listOf("إنترنت واي فاي", "مكتب خاص", "ثلاجة", "غسالة"),
                contactPhone = "+201112223334",
                allowWhatsapp = true,
                distanceToUniversity = "7 دقائق للجامعة",
                isDemoData = true
            ) to listOf(
                "https://images.unsplash.com/photo-1598928506311-c55ded91a20c?auto=format&fit=crop&w=1200&q=80"
            ),

            CreateApartmentRequest(
                title = "شقة مفروشة اقتصادي للطلاب - وسط البلد",
                description = "شقة 2 غرفة وصالة بسعر اقتصادي ممتاز لمجموعة طلاب. قريبة من محطة القطار والموقف والسوق ومحطات السرفيس للجامعة.",
                listingType = ListingType.RENT,
                price = 2200.0,
                currency = "EGP",
                country = "Egypt",
                city = "Beni Suef",
                neighborhood = "وسط البلد",
                address = "شارع الرياض خلف المحطة",
                bedrooms = 2,
                bathrooms = 1,
                areaSqm = 85.0,
                apartmentType = ApartmentType.APARTMENT,
                accommodationType = AccommodationType.PRIVATE_APARTMENT,
                availableBeds = 4,
                genderEligibility = GenderEligibility.ANY,
                furnished = true,
                amenities = listOf("سخان مياه", "مطبخ مجهز", "قرب محطة القطار والموقف"),
                contactPhone = "+201223334445",
                allowWhatsapp = true,
                distanceToUniversity = "12 دقيقة بالسرفيس",
                isDemoData = true
            ) to listOf(
                "https://images.unsplash.com/photo-1536376072261-38c75010e6c9?auto=format&fit=crop&w=1200&q=80"
            ),

            CreateApartmentRequest(
                title = "استوديو مفروش جديد للطلاب - أرض الحرية",
                description = "استوديو مميز حديث التشطيب (غرفة واسعة + مطبخ أمريكاني + حمام). مجهز بأثاث كامل جديد، مناسب لطالبين أو طالب واحد يفضل الاستقلالية.",
                listingType = ListingType.RENT,
                price = 2800.0,
                currency = "EGP",
                country = "Egypt",
                city = "Beni Suef",
                neighborhood = "أرض الحرية",
                address = "شارع الحرية الرئيسي",
                bedrooms = 1,
                bathrooms = 1,
                areaSqm = 55.0,
                apartmentType = ApartmentType.STUDIO,
                accommodationType = AccommodationType.STUDIO,
                availableBeds = 2,
                genderEligibility = GenderEligibility.ANY,
                furnished = true,
                amenities = listOf("تكييف", "مطبخ أمريكاني", "إنترنت واي فاي", "شاشة LCD"),
                contactPhone = "+201112223334",
                allowWhatsapp = true,
                distanceToUniversity = "8 دقائق للجامعة",
                isDemoData = true
            ) to listOf(
                "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=1200&q=80"
            ),

            CreateApartmentRequest(
                title = "شقة واسعة 4 غرف لمجموعة طلاب - حي الأراضى",
                description = "شقة كبيرة تتسع لـ 8 طلاب على الأكثر، تشطيب لوكس، 4 غرف نوم، صالة كبيرة جداً، 2 حمام، ومطبخ كبير. منطقة هادئة ومخدمة بالكامل.",
                listingType = ListingType.RENT,
                price = 4800.0,
                currency = "EGP",
                country = "Egypt",
                city = "Beni Suef",
                neighborhood = "الأراضى",
                address = "حي الأراضى، بالقرب من المدرسة الفندقية",
                bedrooms = 4,
                bathrooms = 2,
                areaSqm = 160.0,
                apartmentType = ApartmentType.APARTMENT,
                accommodationType = AccommodationType.PRIVATE_APARTMENT,
                availableBeds = 8,
                genderEligibility = GenderEligibility.MALE_ONLY,
                furnished = true,
                amenities = listOf("2 حمام", "غاز طبيعي", "إنترنت واي فاي", "غسالة", "ثلاجة كبيرة"),
                contactPhone = "+201223334445",
                allowWhatsapp = true,
                distanceToUniversity = "10 دقائق بالسرفيس",
                isDemoData = true
            ) to listOf(
                "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=1200&q=80"
            ),

            CreateApartmentRequest(
                title = "سرير في غرفة ثنائية للطالبات - صلاح سالم",
                description = "فرصة لطالبة في سكن طالبات منسق وهادئ. الغرفة تحتوي على سريرين ومكتبين وخصانة ملابس كبيرة. الشقة بها جميع الأجهزة الكهربائية والإنترنت.",
                listingType = ListingType.RENT,
                price = 1100.0,
                currency = "EGP",
                country = "Egypt",
                city = "Beni Suef",
                neighborhood = "صلاح سالم",
                address = "شارع صلاح سالم الفرعي",
                bedrooms = 2,
                bathrooms = 1,
                areaSqm = 95.0,
                apartmentType = ApartmentType.APARTMENT,
                accommodationType = AccommodationType.SHARED_HOUSING,
                availableBeds = 1,
                genderEligibility = GenderEligibility.FEMALE_ONLY,
                furnished = true,
                amenities = listOf("إنترنت", "غاز طبيعي", "غسالة اتوماتيك", "مكتب مذاكرة"),
                contactPhone = "+201223334445",
                allowWhatsapp = true,
                distanceToUniversity = "8 دقائق للجامعة",
                isDemoData = true
            ) to listOf(
                "https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?auto=format&fit=crop&w=1200&q=80"
            )
        )

        seedListings.forEachIndexed { index, (req, imageUrls) ->
            val ownerId = owners[index % owners.size]
            val created = apartmentService.createApartment(ownerId, req, autoApprove = true)

            imageUrls.forEachIndexed { imgIdx, url ->
                val imgId = UUID.randomUUID().toString()
                dbQuery {
                    ApartmentImagesTable.insert {
                        it[id] = imgId
                        it[apartmentId] = created.id
                        it[imageUrl] = url
                        it[sortOrder] = imgIdx
                    }
                }
            }
        }
    }
}
