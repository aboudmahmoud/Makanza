package com.explorer.makanza.ui.theme

import com.explorer.makanza.models.*

object Strings {

    private val en = mapOf(
        "app_title" to "Makanza",
        "app_subtitle" to "Beni Suef Student Housing",
        "home_headline" to "Find Your Student Housing in Beni Suef",
        "home_subtitle" to "Apartments and shared student accommodation fitting your needs and budget",
        "btn_explore_housing" to "Explore Apartments",
        "btn_owner_publish" to "List Your Property",
        "btn_contact_admin" to "Contact Administration",
        "search_placeholder" to "Search by title, college, neighborhood...",
        "filter_all" to "All Properties",
        "filter_for_rent" to "For Rent",
        "filter_for_sale" to "For Sale",
        "filter_private_apt" to "Private Apartment",
        "filter_shared_housing" to "Shared Housing",
        "filter_private_room" to "Private Room",
        "filter_studio" to "Studio",
        "filter_male_only" to "Male Students Only",
        "filter_female_only" to "Female Students Only",
        "filter_any_gender" to "Any Gender",
        "filter_college" to "Filter by College / Faculty",
        "filter_period" to "Rental Period",
        "period_day" to "Daily",
        "period_month" to "Monthly",
        "period_semester" to "Per Semester",
        "period_year" to "Yearly",
        "nav_browse_all" to "Browse All",
        "nav_publish" to "Publish",
        "nav_login" to "Login",
        "nav_register" to "Register",
        "nav_my_listings" to "My Listings",
        "nav_my_bookings" to "My Bookings",
        "nav_profile" to "Profile",
        "nav_admin_dashboard" to "Admin Dashboard",
        "nav_logout" to "Log Out",
        "section_featured" to "Featured Student Housing",
        "section_recent" to "Recently Added Listings",
        "section_view_all" to "View All",
        "section_owner_cta_title" to "Have an Apartment or Beds in Beni Suef?",
        "section_owner_cta_sub" to "List your property now and connect directly with university students.",
        "label_neighborhood" to "Neighborhood / Area",
        "label_price_rent" to "Rent Price (EGP)",
        "label_bedrooms" to "Bedrooms",
        "label_bathrooms" to "Bathrooms",
        "label_area" to "Area (sqm)",
        "label_available_beds" to "Available Beds",
        "label_furnished" to "Furnished",
        "label_unfurnished" to "Unfurnished",
        "label_distance_uni" to "College / University Proximity",
        "label_publisher" to "Publisher / Owner",
        "label_contact_phone" to "Phone Number",
        "label_whatsapp" to "WhatsApp Contact",
        "btn_request_booking" to "Request Rental Booking",
        "btn_edit" to "Edit",
        "btn_delete" to "Delete",
        "btn_mark_available" to "Mark Available",
        "btn_mark_unavailable" to "Mark Unavailable",
        "btn_approve" to "Approve",
        "btn_reject" to "Reject",
        "btn_cancel_request" to "Cancel Request",
        "btn_accept" to "Accept",
        "dialog_booking_title" to "Request Student Rental Booking",
        "label_move_in" to "Move-in Date (YYYY-MM-DD)",
        "label_move_out" to "Move-out Date (YYYY-MM-DD)",
        "label_occupants" to "Number of Occupants / Students",
        "label_message_optional" to "Message to Owner (Optional)",
        "btn_submit_booking" to "Submit Booking Inquiry",
        "msg_booking_submitted" to "Booking inquiry submitted successfully! Awaiting owner's review.",
        "msg_no_results" to "No student housing found matching your search.",
        "msg_demo_badge" to "DEMO DATA",
        "tab_login" to "Login",
        "tab_register" to "Register New Account",
        "label_full_name" to "Full Name",
        "label_email" to "Email Address",
        "label_phone" to "Phone Number",
        "label_password" to "Password",
        "label_confirm_password" to "Confirm Password",
        "label_role" to "Account Type",
        "role_student" to "University Student",
        "role_owner" to "Property Owner",
        "role_admin" to "Administrator",
        "admin_stats_title" to "System Overview Statistics",
        "admin_stat_apartments" to "Total Properties",
        "admin_stat_available" to "Available Properties",
        "admin_stat_pending_approval" to "Pending Approval",
        "admin_stat_students" to "Registered Students",
        "admin_stat_owners" to "Property Owners",
        "admin_stat_bookings" to "Total Booking Requests",
        "admin_tab_apartments" to "Manage All Listings",
        "admin_tab_bookings" to "Manage Booking Requests",
        "admin_tab_users" to "Registered Users"
    )

    private val ar = mapOf(
        "app_title" to "مكانزا",
        "app_subtitle" to "سكن الطلاب بمدينة بني سويف",
        "home_headline" to "دور على سكنك في بني سويف",
        "home_subtitle" to "شقق وسكن مشترك يناسب احتياجاتك وميزانيتك الطلابية",
        "btn_explore_housing" to "استكشف الشقق",
        "btn_owner_publish" to "اعرض شقتك",
        "btn_contact_admin" to "تواصل مع الإدارة",
        "search_placeholder" to "ابحث باسم الشقة، الكلية، الحي...",
        "filter_all" to "جميع العقارات",
        "filter_for_rent" to "للإيجار",
        "filter_for_sale" to "للبيع",
        "filter_private_apt" to "شقة خاصة",
        "filter_shared_housing" to "سكن مشترك",
        "filter_private_room" to "غرفة خاصة",
        "filter_studio" to "استوديو",
        "filter_male_only" to "طُلاب فقط",
        "filter_female_only" to "طالبات فقط",
        "filter_any_gender" to "الكل",
        "filter_college" to "الفلترة حسب الكلية",
        "filter_period" to "نظام الإيجار",
        "period_day" to "باليوم",
        "period_month" to "بالشهر",
        "period_semester" to "بالترم",
        "period_year" to "بالسنة",
        "nav_browse_all" to "تصفح العقارات",
        "nav_publish" to "أضف عقار",
        "nav_login" to "دخول",
        "nav_register" to "حساب جديد",
        "nav_my_listings" to "عقاراتي",
        "nav_my_bookings" to "طلباتي",
        "nav_profile" to "حسابي",
        "nav_admin_dashboard" to "لوحة التحكم",
        "nav_logout" to "تسجيل الخروج",
        "section_featured" to "سكن الطلاب المميز في بني سويف",
        "section_recent" to "أحدث العقارات المضافة",
        "section_view_all" to "عرض الكل",
        "section_owner_cta_title" to "هل تملك شقة أو أسِرّة للإيجار في بني سويف؟",
        "section_owner_cta_sub" to "اعرض سكنك الآن وتواصل مباشرة مع طلاب جامعة بني سويف.",
        "label_neighborhood" to "الحي / المنطقة",
        "label_price_rent" to "قيمة الإيجار (ج.م.)",
        "label_bedrooms" to "غرف النوم",
        "label_bathrooms" to "الحمامات",
        "label_area" to "المساحة (م²)",
        "label_available_beds" to "الأسِرّة المتاحة",
        "label_furnished" to "مفروشة بالكامل",
        "label_unfurnished" to "غير مفروشة",
        "label_distance_uni" to "القرب من الكلية / الجامعة",
        "label_publisher" to "الناشر / المالك",
        "label_contact_phone" to "رقم الهاتف",
        "label_whatsapp" to "تواصل واتساب",
        "btn_request_booking" to "طلب حجز السكن",
        "btn_edit" to "تعديل",
        "btn_delete" to "حذف",
        "btn_mark_available" to "تفعيل الإعلان",
        "btn_mark_unavailable" to "تعطيل الإعلان",
        "btn_approve" to "اعتماد الإعلان",
        "btn_reject" to "رفض الإعلان",
        "btn_cancel_request" to "إلغاء الطلب",
        "btn_accept" to "موافقة",
        "dialog_booking_title" to "طلب حجز سكن طلابي",
        "label_move_in" to "تاريخ الانتقال (YYYY-MM-DD)",
        "label_move_out" to "تاريخ المغادرة (YYYY-MM-DD)",
        "label_occupants" to "عدد الطلاب الساكنين",
        "label_message_optional" to "رسالة للمالك (اختياري)",
        "btn_submit_booking" to "إرسال طلب الحجز",
        "msg_booking_submitted" to "تم إرسال طلب الحجز بنجاح! الإعلان قيد الانتظار لحين رد المالك.",
        "msg_no_results" to "لا توجد شقق أو سكن مشترك يطابق بحثك حالياً.",
        "msg_demo_badge" to "بيانات تجريبية",
        "tab_login" to "تسجيل الدخول",
        "tab_register" to "إنشاء حساب جديد",
        "label_full_name" to "الاسم الكامل",
        "label_email" to "البريد الإلكتروني",
        "label_phone" to "رقم الهاتف",
        "label_password" to "كلمة السر",
        "label_confirm_password" to "تأكيد كلمة السر",
        "label_role" to "نوع الحساب",
        "role_student" to "طالب جامعي",
        "role_owner" to "مالك عقار",
        "role_admin" to "مدير النظام",
        "admin_stats_title" to "إحصائيات المنصة الشاملة",
        "admin_stat_apartments" to "إجمالي العقارات",
        "admin_stat_available" to "العقارات المتاحة",
        "admin_stat_pending_approval" to "في انتظار المراجعة",
        "admin_stat_students" to "الطلاب المسجلون",
        "admin_stat_owners" to "الملاّك المسجلون",
        "admin_stat_bookings" to "إجمالي طلبات الحجز",
        "admin_tab_apartments" to "إدارة كافة العقارات",
        "admin_tab_bookings" to "إدارة طلبات الحجز",
        "admin_tab_users" to "المستخدمون المسجلون"
    )

    fun get(key: String, language: String = "ar"): String {
        val map = if (language == "ar") ar else en
        return map[key] ?: en[key] ?: key
    }

    fun formatRent(price: Double, period: RentalPeriod = RentalPeriod.PER_MONTH, language: String = "ar"): String {
        val formatted = price.toLong().toString().reversed().chunked(3).joinToString(",").reversed()
        val periodText = if (language == "ar") {
            when (period) {
                RentalPeriod.PER_DAY -> "ج.م. / يومياً"
                RentalPeriod.PER_MONTH -> "ج.م. / شهرياً"
                RentalPeriod.PER_SEMESTER -> "ج.م. / بالترم"
                RentalPeriod.PER_YEAR -> "ج.م. / سنوياً"
            }
        } else {
            when (period) {
                RentalPeriod.PER_DAY -> "EGP / day"
                RentalPeriod.PER_MONTH -> "EGP / month"
                RentalPeriod.PER_SEMESTER -> "EGP / semester"
                RentalPeriod.PER_YEAR -> "EGP / year"
            }
        }
        return "$formatted $periodText"
    }

    fun getAccommodationTypeText(type: AccommodationType, language: String = "ar"): String {
        return if (language == "ar") {
            when (type) {
                AccommodationType.PRIVATE_APARTMENT -> "شقة خاصة"
                AccommodationType.SHARED_HOUSING -> "سكن مشترك"
                AccommodationType.PRIVATE_ROOM -> "غرفة خاصة"
                AccommodationType.STUDIO -> "استوديو"
                AccommodationType.VILLA -> "فيلا"
            }
        } else {
            when (type) {
                AccommodationType.PRIVATE_APARTMENT -> "Private Apartment"
                AccommodationType.SHARED_HOUSING -> "Shared Housing"
                AccommodationType.PRIVATE_ROOM -> "Private Room"
                AccommodationType.STUDIO -> "Studio"
                AccommodationType.VILLA -> "Villa"
            }
        }
    }

    fun getGenderText(gender: GenderEligibility, language: String = "ar"): String {
        return if (language == "ar") {
            when (gender) {
                GenderEligibility.MALE_ONLY -> "طُلاب فقط"
                GenderEligibility.FEMALE_ONLY -> "طالبات فقط"
                GenderEligibility.ANY -> "الكل"
            }
        } else {
            when (gender) {
                GenderEligibility.MALE_ONLY -> "Male Only"
                GenderEligibility.FEMALE_ONLY -> "Female Only"
                GenderEligibility.ANY -> "Any"
            }
        }
    }

    fun getBookingStatusText(status: BookingStatus, language: String = "ar"): String {
        return if (language == "ar") {
            when (status) {
                BookingStatus.PENDING -> "قيد الانتظار"
                BookingStatus.ACCEPTED -> "مقبول"
                BookingStatus.REJECTED -> "مرفوض"
                BookingStatus.CANCELLED -> "ملغي"
                BookingStatus.COMPLETED -> "مكتمل"
            }
        } else {
            status.name
        }
    }

    fun getApprovalStatusText(approvalStatus: ApprovalStatus, language: String = "ar"): String {
        return if (language == "ar") {
            when (approvalStatus) {
                ApprovalStatus.PENDING_APPROVAL -> "قيد مراجعة الإدارة"
                ApprovalStatus.APPROVED -> "معتمد"
                ApprovalStatus.REJECTED -> "مرفوض من الإدارة"
            }
        } else {
            approvalStatus.name
        }
    }

    fun getRoleText(role: UserRole, language: String = "ar"): String {
        return if (language == "ar") {
            when (role) {
                UserRole.STUDENT -> "طالب جامعي"
                UserRole.PROPERTY_OWNER -> "مالك عقار"
                UserRole.ADMIN -> "مدير المنصة"
            }
        } else {
            role.name
        }
    }
}
