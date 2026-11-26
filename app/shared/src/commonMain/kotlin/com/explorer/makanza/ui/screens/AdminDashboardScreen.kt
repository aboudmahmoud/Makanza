package com.explorer.makanza.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.explorer.makanza.data.ApartmentRepository
import com.explorer.makanza.data.BookingRepository
import com.explorer.makanza.models.*
import com.explorer.makanza.ui.navigation.NavController
import com.explorer.makanza.ui.navigation.Screen
import com.explorer.makanza.ui.theme.LocalAppLanguage
import com.explorer.makanza.ui.theme.Strings
import kotlinx.coroutines.launch

@Composable
fun AdminDashboardScreen(
    apartmentRepository: ApartmentRepository,
    bookingRepository: BookingRepository,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val language = LocalAppLanguage.current
    val scope = rememberCoroutineScope()

    var selectedTabIndex by remember { mutableStateOf(0) }

    var stats by remember { mutableStateOf<AdminDashboardStatsDto?>(null) }
    var allApartments by remember { mutableStateOf<List<ApartmentDto>>(emptyList()) }
    var allBookings by remember { mutableStateOf<List<BookingDto>>(emptyList()) }
    var allUsers by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    fun refreshAdminData() {
        isLoading = true
        scope.launch {
            apartmentRepository.getAdminDashboardStats().onSuccess { stats = it }
            apartmentRepository.getAdminApartments(ApartmentFilterParams(pageSize = 50)).onSuccess { allApartments = it.items }
            apartmentRepository.getAdminBookings().onSuccess { allBookings = it }
            apartmentRepository.getAdminUsers().onSuccess { allUsers = it }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        refreshAdminData()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚙️ ${Strings.get("nav_admin_dashboard", language)}",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )

            Button(onClick = { navController.navigateTo(Screen.PublishApartment()) }) {
                Text("➕ ${Strings.get("btn_owner_publish", language)}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TabRow(selectedTabIndex = selectedTabIndex) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text(if (language == "ar") "نظرة عامة" else "Overview") }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text(Strings.get("admin_tab_apartments", language)) }
            )
            Tab(
                selected = selectedTabIndex == 2,
                onClick = { selectedTabIndex = 2 },
                text = { Text(Strings.get("admin_tab_bookings", language)) }
            )
            Tab(
                selected = selectedTabIndex == 3,
                onClick = { selectedTabIndex = 3 },
                text = { Text(Strings.get("admin_tab_users", language)) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            when (selectedTabIndex) {
                0 -> {
                    // Overview Stats Tab
                    stats?.let { s ->
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                text = Strings.get("admin_stats_title", language),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatCard(title = Strings.get("admin_stat_apartments", language), value = s.totalApartments.toString(), modifier = Modifier.weight(1f))
                                StatCard(title = Strings.get("admin_stat_available", language), value = s.availableApartments.toString(), modifier = Modifier.weight(1f))
                                StatCard(title = Strings.get("admin_stat_pending_approval", language), value = s.pendingApprovalApartments.toString(), modifier = Modifier.weight(1f))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatCard(title = Strings.get("admin_stat_students", language), value = s.totalStudents.toString(), modifier = Modifier.weight(1f))
                                StatCard(title = Strings.get("admin_stat_owners", language), value = s.totalPropertyOwners.toString(), modifier = Modifier.weight(1f))
                                StatCard(title = Strings.get("admin_stat_bookings", language), value = s.totalBookings.toString(), modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                1 -> {
                    // All Apartments Tab with Approval
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(allApartments) { apt ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = apt.title,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            modifier = Modifier.weight(1f)
                                        )

                                        SuggestionChip(
                                            onClick = { },
                                            label = { Text(Strings.getApprovalStatusText(apt.approvalStatus, language)) }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "${apt.neighborhood}, ${apt.city} • ${Strings.formatRent(apt.price, apt.rentalPeriod, language)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            if (apt.approvalStatus == ApprovalStatus.PENDING_APPROVAL || apt.approvalStatus == ApprovalStatus.REJECTED) {
                                                Button(
                                                    onClick = {
                                                        scope.launch {
                                                            apartmentRepository.updateApprovalStatus(apt.id, ApprovalStatus.APPROVED).onSuccess { refreshAdminData() }
                                                        }
                                                    }
                                                ) {
                                                    Text(Strings.get("btn_approve", language))
                                                }
                                            }

                                            if (apt.approvalStatus == ApprovalStatus.PENDING_APPROVAL || apt.approvalStatus == ApprovalStatus.APPROVED) {
                                                OutlinedButton(
                                                    onClick = {
                                                        scope.launch {
                                                            apartmentRepository.updateApprovalStatus(apt.id, ApprovalStatus.REJECTED).onSuccess { refreshAdminData() }
                                                        }
                                                    }
                                                ) {
                                                    Text(Strings.get("btn_reject", language))
                                                }
                                            }
                                        }

                                        IconButton(
                                            onClick = {
                                                scope.launch {
                                                    apartmentRepository.deleteApartment(apt.id).onSuccess { refreshAdminData() }
                                                }
                                            }
                                        ) {
                                            Text("🗑️")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // All Bookings Tab
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(allBookings) { booking ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = booking.apartmentTitle ?: "Apartment Booking",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )

                                        AssistChip(
                                            onClick = { },
                                            label = { Text(Strings.getBookingStatusText(booking.status, language)) }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "${if (language == "ar") "الطالب:" else "Student:"} ${booking.requesterName ?: "N/A"} (${booking.requesterPhone ?: ""})",
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Text(
                                        text = "${if (language == "ar") "المالك:" else "Owner:"} ${booking.ownerName ?: "N/A"}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Text(
                                        text = "${booking.moveInDate} ➡️ ${booking.moveOutDate} (${booking.occupants} occupants)",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // All Users Tab
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(allUsers) { user ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = user.fullName,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "${user.email} • ${user.phone}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    SuggestionChip(
                                        onClick = { },
                                        label = { Text(Strings.getRoleText(user.role, language)) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
