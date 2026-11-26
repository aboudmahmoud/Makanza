package com.explorer.makanza.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.explorer.makanza.data.ApartmentRepository
import com.explorer.makanza.models.*
import com.explorer.makanza.ui.components.ApartmentCard
import com.explorer.makanza.ui.navigation.NavController
import com.explorer.makanza.ui.navigation.Screen
import com.explorer.makanza.ui.theme.LocalAppLanguage
import com.explorer.makanza.ui.theme.Strings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllApartmentsScreen(
    apartmentRepository: ApartmentRepository,
    navController: NavController,
    initialQuery: String? = null,
    modifier: Modifier = Modifier
) {
    val language = LocalAppLanguage.current
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf(initialQuery ?: "") }
    var selectedUniversity by remember { mutableStateOf<String?>(null) }
    var selectedCollege by remember { mutableStateOf<String?>(null) }
    var selectedAccommodationType by remember { mutableStateOf<AccommodationType?>(null) }
    var selectedGender by remember { mutableStateOf<GenderEligibility?>(null) }
    var selectedRentalPeriod by remember { mutableStateOf<RentalPeriod?>(null) }

    var apartments by remember { mutableStateOf<List<ApartmentDto>>(emptyList()) }
    var totalCount by remember { mutableStateOf(0L) }
    var isLoading by remember { mutableStateOf(true) }

    val universities = listOf("جامعة بني سويف", "جامعة النهضة")
    val colleges = listOf("كلية الطب", "كلية الصيدلة", "كلية الهندسة", "كلية العلوم", "كلية التمريض", "المجمع الطبي")

    fun fetchApartments() {
        isLoading = true
        scope.launch {
            val params = ApartmentFilterParams(
                q = searchQuery.takeIf { it.isNotBlank() },
                nearUniversity = selectedUniversity,
                nearCollege = selectedCollege,
                accommodationType = selectedAccommodationType,
                genderEligibility = selectedGender,
                rentalPeriod = selectedRentalPeriod,
                pageSize = 20
            )
            apartmentRepository.getApartments(params).onSuccess {
                apartments = it.items
                totalCount = it.total
            }
            isLoading = false
        }
    }

    LaunchedEffect(searchQuery, selectedUniversity, selectedCollege, selectedAccommodationType, selectedGender, selectedRentalPeriod) {
        fetchApartments()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = Strings.get("nav_browse_all", language),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(Strings.get("search_placeholder", language)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Text("❌")
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // FILTER 1: Universities (جامعة بني سويف / جامعة النهضة)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selectedUniversity == null,
                    onClick = { selectedUniversity = null },
                    label = { Text("جميع الجامعات") }
                )
            }
            items(universities) { uni ->
                FilterChip(
                    selected = selectedUniversity == uni,
                    onClick = { selectedUniversity = if (selectedUniversity == uni) null else uni },
                    label = { Text("🏫 $uni") }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // FILTER 2: Colleges (الكليات)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(colleges) { college ->
                FilterChip(
                    selected = selectedCollege == college,
                    onClick = { selectedCollege = if (selectedCollege == college) null else college },
                    label = { Text("🎓 $college") }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // FILTER 3: Rental Period (مدة الإيجار)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedRentalPeriod == null,
                onClick = { selectedRentalPeriod = null },
                label = { Text("كل المدد") }
            )
            RentalPeriod.entries.forEach { period ->
                FilterChip(
                    selected = selectedRentalPeriod == period,
                    onClick = { selectedRentalPeriod = if (selectedRentalPeriod == period) null else period },
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

        Spacer(modifier = Modifier.height(6.dp))

        // FILTER 4: Accommodation Type & Gender
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(AccommodationType.entries) { type ->
                FilterChip(
                    selected = selectedAccommodationType == type,
                    onClick = { selectedAccommodationType = if (selectedAccommodationType == type) null else type },
                    label = { Text(Strings.getAccommodationTypeText(type, language)) }
                )
            }
            item {
                FilterChip(
                    selected = selectedGender == GenderEligibility.MALE_ONLY,
                    onClick = { selectedGender = if (selectedGender == GenderEligibility.MALE_ONLY) null else GenderEligibility.MALE_ONLY },
                    label = { Text("👨 طُلاب فقط") }
                )
            }
            item {
                FilterChip(
                    selected = selectedGender == GenderEligibility.FEMALE_ONLY,
                    onClick = { selectedGender = if (selectedGender == GenderEligibility.FEMALE_ONLY) null else GenderEligibility.FEMALE_ONLY },
                    label = { Text("👩 طالبات فقط") }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Results count
        Text(
            text = "تم العثور على $totalCount سكن طلابي",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (apartments.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = Strings.get("msg_no_results", language),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 280.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(apartments) { apt ->
                    ApartmentCard(
                        apartment = apt,
                        onClick = { navController.navigateTo(Screen.ApartmentDetails(apt.id)) }
                    )
                }
            }
        }
    }
}
