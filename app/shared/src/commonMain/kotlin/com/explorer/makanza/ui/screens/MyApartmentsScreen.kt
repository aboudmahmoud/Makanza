package com.explorer.makanza.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun MyApartmentsScreen(
    apartmentRepository: ApartmentRepository,
    bookingRepository: BookingRepository,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val language = LocalAppLanguage.current
    val scope = rememberCoroutineScope()

    var selectedTabIndex by remember { mutableStateOf(0) } // 0 = Listings, 1 = Received Requests

    var myApartments by remember { mutableStateOf<List<ApartmentDto>>(emptyList()) }
    var receivedRequests by remember { mutableStateOf<List<BookingDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    fun refreshData() {
        isLoading = true
        scope.launch {
            apartmentRepository.getMyApartments().onSuccess { myApartments = it }
            bookingRepository.getReceivedBookingRequests().onSuccess { receivedRequests = it }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        refreshData()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = Strings.get("nav_my_listings", language),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(16.dp))

        TabRow(selectedTabIndex = selectedTabIndex) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("${Strings.get("nav_my_listings", language)} (${myApartments.size})") }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("${Strings.get("admin_stat_bookings", language)} (${receivedRequests.size})") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (selectedTabIndex == 0) {
            // My Listings Tab
            if (myApartments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (language == "ar") "لم تقم بنشر أي عقارات بعد" else "You have not published any listings yet")
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { navController.navigateTo(Screen.PublishApartment()) }) {
                            Text(Strings.get("btn_owner_publish", language))
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(myApartments) { apt ->
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
                                    text = Strings.formatRent(apt.price, apt.rentalPeriod, language),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = { navController.navigateTo(Screen.PublishApartment(editApartmentId = apt.id)) }
                                        ) {
                                            Text("✏️", style = MaterialTheme.typography.labelMedium)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(Strings.get("btn_edit", language))
                                        }

                                        IconButton(
                                            onClick = {
                                                scope.launch {
                                                    apartmentRepository.deleteApartment(apt.id).onSuccess { refreshData() }
                                                }
                                            }
                                        ) {
                                            Text("🗑️", style = MaterialTheme.typography.titleMedium)
                                        }
                                    }

                                    // Status Change Buttons
                                    if (apt.status == ApartmentStatus.AVAILABLE) {
                                        TextButton(
                                            onClick = {
                                                scope.launch {
                                                    apartmentRepository.updateApartmentStatus(apt.id, ApartmentStatus.UNAVAILABLE).onSuccess { refreshData() }
                                                }
                                            }
                                        ) {
                                            Text(Strings.get("btn_mark_unavailable", language))
                                        }
                                    } else {
                                        TextButton(
                                            onClick = {
                                                scope.launch {
                                                    apartmentRepository.updateApartmentStatus(apt.id, ApartmentStatus.AVAILABLE).onSuccess { refreshData() }
                                                }
                                            }
                                        ) {
                                            Text(Strings.get("btn_mark_available", language))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Received Requests Tab
            if (receivedRequests.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(if (language == "ar") "لا توجد طلبات حجز مستلمة حالياً" else "No received booking requests yet")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(receivedRequests) { req ->
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
                                        text = req.apartmentTitle ?: "Housing Request",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )

                                    SuggestionChip(
                                        onClick = { },
                                        label = { Text(Strings.getBookingStatusText(req.status, language)) }
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "${if (language == "ar") "مقدم الطلب:" else "Requester:"} ${req.requesterName ?: "Student"} (${req.requesterPhone ?: ""})",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = "${if (language == "ar") "فترة الإقامة:" else "Dates:"} ${req.moveInDate}  ➡️  ${req.moveOutDate} (${req.occupants} ${if (language == "ar") "طلاب" else "occupants"})",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )

                                if (req.message.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "\"${req.message}\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (req.status == BookingStatus.PENDING) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    bookingRepository.updateBookingStatus(req.id, BookingStatus.ACCEPTED).onSuccess { refreshData() }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Text(Strings.get("btn_accept", language))
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                scope.launch {
                                                    bookingRepository.updateBookingStatus(req.id, BookingStatus.REJECTED).onSuccess { refreshData() }
                                                }
                                            }
                                        ) {
                                            Text(Strings.get("btn_reject", language))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
