package com.explorer.makanza.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.explorer.makanza.data.ApartmentRepository
import com.explorer.makanza.models.*
import com.explorer.makanza.ui.navigation.NavController
import com.explorer.makanza.ui.navigation.Screen
import com.explorer.makanza.ui.theme.LocalAppLanguage
import com.explorer.makanza.ui.theme.Strings
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PublishApartmentScreen(
    editApartmentId: String? = null,
    apartmentRepository: ApartmentRepository,
    navController: NavController,
    currentUser: UserDto?,
    modifier: Modifier = Modifier
) {
    val language = LocalAppLanguage.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var rentalPeriod by remember { mutableStateOf(RentalPeriod.PER_MONTH) }
    var nearUniversity by remember { mutableStateOf<String?>("جامعة بني سويف") }
    var nearCollege by remember { mutableStateOf<String?>("كلية الطب") }
    var neighborhood by remember { mutableStateOf("شرق النيل") }
    var address by remember { mutableStateOf("") }
    var bedrooms by remember { mutableStateOf(2) }
    var bathrooms by remember { mutableStateOf(1) }
    var areaSqmText by remember { mutableStateOf("90") }
    var accommodationType by remember { mutableStateOf(AccommodationType.PRIVATE_APARTMENT) }
    var availableBeds by remember { mutableStateOf(1) }
    var genderEligibility by remember { mutableStateOf(GenderEligibility.ANY) }
    var furnished by remember { mutableStateOf(true) }
    var distanceToUniversity by remember { mutableStateOf("") }
    var ownerNameInput by remember { mutableStateOf(currentUser?.fullName ?: "زائر (Guest)") }
    var contactPhone by remember { mutableStateOf(currentUser?.phone ?: "") }

    var imageUrlInput by remember { mutableStateOf("") }
    val imageUrls = remember { mutableStateListOf<String>() }

    val universities = listOf("جامعة بني سويف", "جامعة النهضة")
    val colleges = listOf("كلية الطب", "كلية الصيدلة", "كلية الهندسة", "كلية العلوم", "كلية التمريض", "المجمع الطبي")
    val beniSuefNeighborhoods = listOf("شرق النيل", "صلاح سالم", "كوبري الجامعة", "الكورنيش", "وسط البلد", "الأراضى", "أرض الحرية")
    val availableAmenities = listOf("تكييف", "إنترنت واي فاي", "غسالة ملابس", "ثلاجة", "مكاتب مذاكرة", "شاشة تلفزيون", "مصعد", "غاز طبيعي", "حارس عمارة")
    val selectedAmenities = remember { mutableStateListOf<String>() }

    var isLoadingDetails by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(editApartmentId) {
        if (!editApartmentId.isNullOrBlank()) {
            isLoadingDetails = true
            apartmentRepository.getApartmentById(editApartmentId).onSuccess { apt ->
                title = apt.title
                description = apt.description
                priceText = apt.price.toString()
                rentalPeriod = apt.rentalPeriod
                nearUniversity = apt.nearUniversity
                nearCollege = apt.nearCollege
                neighborhood = apt.neighborhood
                address = apt.address
                bedrooms = apt.bedrooms
                bathrooms = apt.bathrooms
                areaSqmText = apt.areaSqm.toString()
                accommodationType = apt.accommodationType
                availableBeds = apt.availableBeds
                genderEligibility = apt.genderEligibility
                furnished = apt.furnished
                distanceToUniversity = apt.distanceToUniversity ?: ""
                contactPhone = apt.ownerPhone ?: currentUser?.phone ?: ""
                selectedAmenities.clear()
                selectedAmenities.addAll(apt.amenities)
                imageUrls.clear()
                imageUrls.addAll(apt.images.map { it.imageUrl })
            }
            isLoadingDetails = false
        }
    }

    if (isLoadingDetails) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            IconButton(onClick = { navController.popBack() }) {
                Text("⬅️", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (editApartmentId == null) (if (currentUser == null) "أضف عقار كـ زائر 👤" else Strings.get("btn_owner_publish", language)) else Strings.get("btn_edit", language),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (currentUser == null) {
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = "👤 خاصية الزائر تتيح لك إضافة عقارك مباشرة بدون تسجيل دخول. فقط أدخل بيانات التواصل الخاصة بك بالأسفل.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Target University
                Text(
                    text = "🏫 الجامعة القريبة من العقار:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    universities.forEach { uni ->
                        FilterChip(
                            selected = nearUniversity == uni,
                            onClick = { nearUniversity = uni },
                            label = { Text("🏫 $uni") }
                        )
                    }
                }

                // 2. Target College
                Text(
                    text = "🎓 الكلية / الجامعة الأقرب للسكن:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colleges.forEach { col ->
                        FilterChip(
                            selected = nearCollege == col,
                            onClick = { nearCollege = col },
                            label = { Text("🎓 $col") }
                        )
                    }
                }

                // 3. Rental Duration Period
                Text(
                    text = "📅 نظام ونوع مدة الإيجار:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RentalPeriod.entries.forEach { period ->
                        FilterChip(
                            selected = rentalPeriod == period,
                            onClick = { rentalPeriod = period },
                            label = {
                                Text(
                                    when (period) {
                                        RentalPeriod.PER_DAY -> "يومي"
                                        RentalPeriod.PER_MONTH -> "شهري"
                                        RentalPeriod.PER_SEMESTER -> "بالترم"
                                        RentalPeriod.PER_YEAR -> "سنوي"
                                    }
                                )
                            }
                        )
                    }
                }

                // 4. Accommodation Type Selection
                Text(
                    text = if (language == "ar") "نوع السكن الطلابية:" else "Housing Type:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AccommodationType.entries.forEach { accType ->
                        FilterChip(
                            selected = accommodationType == accType,
                            onClick = { accommodationType = accType },
                            label = { Text(Strings.getAccommodationTypeText(accType, language)) }
                        )
                    }
                }

                // 5. Gender Eligibility
                Text(
                    text = if (language == "ar") "المستهدف بالسكن:" else "Gender Eligibility:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GenderEligibility.entries.forEach { gender ->
                        FilterChip(
                            selected = genderEligibility == gender,
                            onClick = { genderEligibility = gender },
                            label = { Text(Strings.getGenderText(gender, language)) }
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (language == "ar") "عنوان الإعلان" else "Property Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text(Strings.get("label_price_rent", language)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Neighborhood Selection in Beni Suef
                Text(
                    text = Strings.get("label_neighborhood", language),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    beniSuefNeighborhoods.forEach { neigh ->
                        FilterChip(
                            selected = neighborhood == neigh,
                            onClick = { neighborhood = neigh },
                            label = { Text(neigh) }
                        )
                    }
                }

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(if (language == "ar") "العنوان بالتفصيل" else "Full Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = distanceToUniversity,
                    onValueChange = { distanceToUniversity = it },
                    label = { Text(Strings.get("label_distance_uni", language)) },
                    placeholder = { Text("e.g. 5 دقائق للجامعة") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (currentUser == null) {
                    OutlinedTextField(
                        value = ownerNameInput,
                        onValueChange = { ownerNameInput = it },
                        label = { Text("اسمك الناشر (كـ زائر)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = areaSqmText,
                        onValueChange = { areaSqmText = it },
                        label = { Text(Strings.get("label_area", language)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        label = { Text(Strings.get("label_contact_phone", language)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Furnished Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(Strings.get("label_furnished", language))
                    Switch(checked = furnished, onCheckedChange = { furnished = it })
                }

                // Amenities Checklist
                Text(
                    text = if (language == "ar") "المرافق والخدمات المتوفرة:" else "Amenities:",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableAmenities.forEach { amenity ->
                        val isSelected = selectedAmenities.contains(amenity)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) selectedAmenities.remove(amenity) else selectedAmenities.add(amenity)
                            },
                            label = { Text(amenity) }
                        )
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(if (language == "ar") "وصف الشقة والسكن" else "Full Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6
                )

                // Image URLs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = imageUrlInput,
                        onValueChange = { imageUrlInput = it },
                        placeholder = { Text("https://...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            if (imageUrlInput.isNotBlank()) {
                                imageUrls.add(imageUrlInput.trim())
                                imageUrlInput = ""
                            }
                        }
                    ) {
                        Text("➕", style = MaterialTheme.typography.labelMedium)
                    }
                }

                if (imageUrls.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(imageUrls) { imgUrl ->
                            Box(modifier = Modifier.size(90.dp)) {
                                AsyncImage(
                                    model = imgUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { imageUrls.remove(imgUrl) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                ) {
                                    Text("❌", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                errorMessage?.let { err ->
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val parsedPrice = priceText.toDoubleOrNull()
                        val parsedArea = areaSqmText.toDoubleOrNull()

                        if (title.isBlank() || description.isBlank() || parsedPrice == null || neighborhood.isBlank() || contactPhone.isBlank()) {
                            errorMessage = if (language == "ar") "يرجى ملء جميع البيانات ورقم الهاتف بشكل صحيح" else "Please fill required fields and phone number"
                            return@Button
                        }

                        isSubmitting = true
                        errorMessage = null

                        if (editApartmentId == null) {
                            val createReq = CreateApartmentRequest(
                                title = title.trim(),
                                description = description.trim(),
                                listingType = ListingType.RENT,
                                rentalPeriod = rentalPeriod,
                                price = parsedPrice,
                                currency = "EGP",
                                country = "Egypt",
                                city = "Beni Suef",
                                neighborhood = neighborhood.trim(),
                                address = address.trim(),
                                bedrooms = bedrooms,
                                bathrooms = bathrooms,
                                areaSqm = parsedArea ?: 90.0,
                                apartmentType = ApartmentType.APARTMENT,
                                accommodationType = accommodationType,
                                availableBeds = availableBeds,
                                genderEligibility = genderEligibility,
                                nearUniversity = nearUniversity,
                                nearCollege = nearCollege,
                                furnished = furnished,
                                amenities = selectedAmenities.toList(),
                                contactPhone = contactPhone.trim(),
                                allowWhatsapp = true,
                                distanceToUniversity = distanceToUniversity.takeIf { it.isNotBlank() }
                            )
                            scope.launch {
                                apartmentRepository.createApartment(createReq).fold(
                                    onSuccess = { created ->
                                        isSubmitting = false
                                        navController.navigateTo(Screen.ApartmentDetails(created.id))
                                    },
                                    onFailure = { err ->
                                        isSubmitting = false
                                        errorMessage = err.message ?: "Failed to publish property"
                                    }
                                )
                            }
                        } else {
                            val updateReq = UpdateApartmentRequest(
                                title = title.trim(),
                                description = description.trim(),
                                rentalPeriod = rentalPeriod,
                                price = parsedPrice,
                                neighborhood = neighborhood.trim(),
                                address = address.trim(),
                                bedrooms = bedrooms,
                                bathrooms = bathrooms,
                                areaSqm = parsedArea ?: 90.0,
                                accommodationType = accommodationType,
                                availableBeds = availableBeds,
                                genderEligibility = genderEligibility,
                                nearUniversity = nearUniversity,
                                nearCollege = nearCollege,
                                furnished = furnished,
                                amenities = selectedAmenities.toList(),
                                contactPhone = contactPhone.trim(),
                                distanceToUniversity = distanceToUniversity.takeIf { it.isNotBlank() }
                            )
                            scope.launch {
                                apartmentRepository.updateApartment(editApartmentId, updateReq).fold(
                                    onSuccess = { updated ->
                                        isSubmitting = false
                                        navController.navigateTo(Screen.ApartmentDetails(updated.id))
                                    },
                                    onFailure = { err ->
                                        isSubmitting = false
                                        errorMessage = err.message ?: "Failed to update property"
                                    }
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (editApartmentId == null) (if (currentUser == null) "نشر الإعلان كـ زائر 👤" else Strings.get("btn_owner_publish", language)) else Strings.get("btn_edit", language),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
