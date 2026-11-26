package com.explorer.makanza.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.explorer.makanza.data.ApartmentRepository
import com.explorer.makanza.data.BookingRepository
import com.explorer.makanza.models.*
import com.explorer.makanza.ui.components.BookingDialog
import com.explorer.makanza.ui.navigation.NavController
import com.explorer.makanza.ui.navigation.Screen
import com.explorer.makanza.ui.theme.LocalAppLanguage
import com.explorer.makanza.ui.theme.Strings
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ApartmentDetailsScreen(
    apartmentId: String,
    apartmentRepository: ApartmentRepository,
    bookingRepository: BookingRepository,
    navController: NavController,
    currentUser: UserDto?,
    modifier: Modifier = Modifier
) {
    val language = LocalAppLanguage.current
    val scope = rememberCoroutineScope()

    var apartment by remember { mutableStateOf<ApartmentDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorText by remember { mutableStateOf<String?>(null) }

    var showBookingDialog by remember { mutableStateOf(false) }
    var isSubmittingBooking by remember { mutableStateOf(false) }
    var bookingErrorMessage by remember { mutableStateOf<String?>(null) }
    var bookingSuccessMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(apartmentId) {
        isLoading = true
        apartmentRepository.getApartmentById(apartmentId).fold(
            onSuccess = { apartment = it },
            onFailure = { errorText = it.message ?: "Failed to load details" }
        )
        isLoading = false
    }

    if (isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val apt = apartment
    if (apt == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(errorText ?: if (language == "ar") "العقار غير موجود" else "Apartment not found")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.popBack() }) {
                    Text(if (language == "ar") "رجوع" else "Go Back")
                }
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBack() }) {
                Text("⬅️", style = MaterialTheme.typography.titleMedium)
            }

            Text(
                text = Strings.getAccommodationTypeText(apt.accommodationType, language),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            IconButton(onClick = { /* Info */ }) {
                Text("ℹ️", style = MaterialTheme.typography.titleMedium)
            }
        }

        // Image Gallery
        val imagesList = if (apt.images.isNotEmpty()) apt.images.map { it.imageUrl } else listOf("https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?auto=format&fit=crop&w=1200&q=80")
        var selectedImageIndex by remember { mutableStateOf(0) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            AsyncImage(
                model = imagesList.getOrNull(selectedImageIndex) ?: imagesList.first(),
                contentDescription = apt.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        if (imagesList.size > 1) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(imagesList.size) { idx ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedImageIndex == idx) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = imagesList[idx],
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Gray),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Strings.formatRent(apt.price, apt.rentalPeriod, language),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                AssistChip(
                    onClick = { },
                    label = { Text(apt.status.name) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = apt.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📍", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${apt.address}, ${apt.neighborhood}, ${apt.city}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            apt.nearCollege?.let { college ->
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎓", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "القرب من $college",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            if (apt.genderEligibility != GenderEligibility.ANY) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (apt.genderEligibility == GenderEligibility.FEMALE_ONLY) "👩" else "👨", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = Strings.getGenderText(apt.genderEligibility, language),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Specs Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(Strings.get("label_bedrooms", language), style = MaterialTheme.typography.labelMedium)
                        Text("${apt.bedrooms}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(Strings.get("label_bathrooms", language), style = MaterialTheme.typography.labelMedium)
                        Text("${apt.bathrooms}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(Strings.get("label_available_beds", language), style = MaterialTheme.typography.labelMedium)
                        Text("${apt.availableBeds}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(Strings.get("label_furnished", language), style = MaterialTheme.typography.labelMedium)
                        Text(if (apt.furnished) (if (language == "ar") "نعم" else "Yes") else (if (language == "ar") "لا" else "No"), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Description
            Text(
                text = if (language == "ar") "الوصف" else "Description",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = apt.description,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Amenities Tags
            if (apt.amenities.isNotEmpty()) {
                Text(
                    text = if (language == "ar") "المرافق والخدمات" else "Amenities & Features",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    apt.amenities.forEach { amenity ->
                        SuggestionChip(
                            onClick = { },
                            label = { Text(amenity) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Publisher & Booking Action Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Strings.get("label_publisher", language),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    apt.ownerName?.let { name ->
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    apt.ownerPhone?.let { phone ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📞", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(phone, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    bookingSuccessMessage?.let { success ->
                        Surface(
                            color = Color(0xFFD4EDDA),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = success,
                                color = Color(0xFF155724),
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (apt.listingType == ListingType.RENT && apt.status == ApartmentStatus.AVAILABLE) {
                            Button(
                                onClick = { showBookingDialog = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (currentUser == null) "طلب حجز السكن (كـ زائر 👤)" else Strings.get("btn_request_booking", language))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Booking Request Dialog
    if (showBookingDialog) {
        BookingDialog(
            apartment = apt,
            currentUser = currentUser,
            onDismiss = { showBookingDialog = false; bookingErrorMessage = null },
            isSubmitting = isSubmittingBooking,
            errorMessage = bookingErrorMessage,
            onSubmit = { bookingReq ->
                isSubmittingBooking = true
                bookingErrorMessage = null
                scope.launch {
                    bookingRepository.createBooking(bookingReq).fold(
                        onSuccess = {
                            isSubmittingBooking = false
                            showBookingDialog = false
                            bookingSuccessMessage = Strings.get("msg_booking_submitted", language)
                        },
                        onFailure = { err ->
                            isSubmittingBooking = false
                            bookingErrorMessage = err.message ?: "Failed to submit booking request"
                        }
                    )
                }
            }
        )
    }
}
