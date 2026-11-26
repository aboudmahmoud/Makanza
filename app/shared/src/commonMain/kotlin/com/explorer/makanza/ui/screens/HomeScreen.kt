package com.explorer.makanza.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

@Composable
fun HomeScreen(
    apartmentRepository: ApartmentRepository,
    navController: NavController,
    currentUser: UserDto?,
    modifier: Modifier = Modifier
) {
    val language = LocalAppLanguage.current
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedUniversity by remember { mutableStateOf<String?>(null) }
    var selectedCollege by remember { mutableStateOf<String?>(null) }
    var selectedGender by remember { mutableStateOf<GenderEligibility?>(null) }
    var selectedPeriod by remember { mutableStateOf<RentalPeriod?>(null) }

    var featuredListings by remember { mutableStateOf<List<ApartmentDto>>(emptyList()) }
    var recentListings by remember { mutableStateOf<List<ApartmentDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val universities = listOf("جامعة بني سويف", "جامعة النهضة")
    val colleges = listOf("كلية الطب", "كلية الصيدلة", "كلية الهندسة", "كلية العلوم", "كلية التمريض", "المجمع الطبي")

    LaunchedEffect(selectedUniversity, selectedCollege, selectedGender, selectedPeriod) {
        isLoading = true
        scope.launch {
            apartmentRepository.getFeaturedApartments().onSuccess {
                featuredListings = it
            }
            apartmentRepository.getApartments(
                ApartmentFilterParams(
                    q = searchQuery,
                    nearUniversity = selectedUniversity,
                    nearCollege = selectedCollege,
                    genderEligibility = selectedGender,
                    rentalPeriod = selectedPeriod,
                    pageSize = 6
                )
            ).onSuccess {
                recentListings = it.items
            }
            isLoading = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Hero Section
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = Strings.get("home_headline", language),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = Strings.get("home_subtitle", language),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(Strings.get("search_placeholder", language)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    trailingIcon = {
                        IconButton(onClick = {
                            navController.navigateTo(Screen.AllApartments(initialQuery = searchQuery))
                        }) {
                            Text("🔍")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 1. FILTER: University (جامعة بني سويف / جامعة النهضة)
                Text(
                    text = "🏫 فلتر الجامعة:",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.align(Alignment.Start)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedUniversity == null,
                        onClick = { selectedUniversity = null },
                        label = { Text("جميع الجامعات") }
                    )
                    universities.forEach { uni ->
                        FilterChip(
                            selected = selectedUniversity == uni,
                            onClick = { selectedUniversity = if (selectedUniversity == uni) null else uni },
                            label = { Text(uni) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. FILTER: College (الكليات)
                Text(
                    text = "🎓 فلتر الكلية:",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.align(Alignment.Start)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(colleges) { col ->
                        FilterChip(
                            selected = selectedCollege == col,
                            onClick = { selectedCollege = if (selectedCollege == col) null else col },
                            label = { Text(col) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. FILTER: Rental Duration (مدة ونظام الإيجار)
                Text(
                    text = "📅 مدّة ونظام الحجز والإيجار:",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.align(Alignment.Start)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedPeriod == RentalPeriod.PER_MONTH,
                        onClick = { selectedPeriod = if (selectedPeriod == RentalPeriod.PER_MONTH) null else RentalPeriod.PER_MONTH },
                        label = { Text("شهري") }
                    )
                    FilterChip(
                        selected = selectedPeriod == RentalPeriod.PER_SEMESTER,
                        onClick = { selectedPeriod = if (selectedPeriod == RentalPeriod.PER_SEMESTER) null else RentalPeriod.PER_SEMESTER },
                        label = { Text("بالترم") }
                    )
                    FilterChip(
                        selected = selectedPeriod == RentalPeriod.PER_DAY,
                        onClick = { selectedPeriod = if (selectedPeriod == RentalPeriod.PER_DAY) null else RentalPeriod.PER_DAY },
                        label = { Text("يومي") }
                    )
                    FilterChip(
                        selected = selectedPeriod == RentalPeriod.PER_YEAR,
                        onClick = { selectedPeriod = if (selectedPeriod == RentalPeriod.PER_YEAR) null else RentalPeriod.PER_YEAR },
                        label = { Text("سنوي") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 4. FILTER: Gender (طُلاب / طالبات)
                Text(
                    text = "👥 نوع السكن (طُلاب / طالبات):",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.align(Alignment.Start)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedGender == null,
                        onClick = { selectedGender = null },
                        label = { Text("الكل") }
                    )
                    FilterChip(
                        selected = selectedGender == GenderEligibility.MALE_ONLY,
                        onClick = { selectedGender = if (selectedGender == GenderEligibility.MALE_ONLY) null else GenderEligibility.MALE_ONLY },
                        label = { Text("👨 طُلاب فقط") }
                    )
                    FilterChip(
                        selected = selectedGender == GenderEligibility.FEMALE_ONLY,
                        onClick = { selectedGender = if (selectedGender == GenderEligibility.FEMALE_ONLY) null else GenderEligibility.FEMALE_ONLY },
                        label = { Text("👩 طالبات فقط") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Content Section
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Strings.get("section_featured", language),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                TextButton(onClick = { navController.navigateTo(Screen.AllApartments()) }) {
                    Text(Strings.get("section_view_all", language))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (recentListings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(Strings.get("msg_no_results", language))
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 1200.dp)
                ) {
                    items(recentListings) { apt ->
                        ApartmentCard(
                            apartment = apt,
                            onClick = { navController.navigateTo(Screen.ApartmentDetails(apt.id)) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Guest & Owner Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "تستطيع نشر شقتك أو حجز سكن كـ زائر 👤",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "اعرض عقاراتك السكنية أو قدم طلب حجز شقة مباشرة بدون الحاجة لتسجيل دخول مسبق.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { navController.navigateTo(Screen.PublishApartment()) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Text("➕ أضف عقارك الآن كـ زائر")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
