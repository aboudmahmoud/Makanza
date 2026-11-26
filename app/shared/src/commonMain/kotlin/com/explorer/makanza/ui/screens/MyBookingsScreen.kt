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
import com.explorer.makanza.data.BookingRepository
import com.explorer.makanza.models.BookingDto
import com.explorer.makanza.models.BookingStatus
import com.explorer.makanza.ui.navigation.NavController
import com.explorer.makanza.ui.theme.LocalAppLanguage
import com.explorer.makanza.ui.theme.Strings
import kotlinx.coroutines.launch

@Composable
fun MyBookingsScreen(
    bookingRepository: BookingRepository,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val language = LocalAppLanguage.current
    val scope = rememberCoroutineScope()

    var myBookings by remember { mutableStateOf<List<BookingDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    fun refreshData() {
        isLoading = true
        scope.launch {
            bookingRepository.getMyBookings().onSuccess { myBookings = it }
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
            text = Strings.get("nav_my_bookings", language),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (myBookings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(if (language == "ar") "لم تقم بتقديم أي طلبات حجز بعد" else "You have not submitted any rental booking requests yet")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(myBookings) { booking ->
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
                                    text = booking.apartmentTitle ?: "Student Housing",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.weight(1f)
                                )

                                SuggestionChip(
                                    onClick = { },
                                    label = { Text(Strings.getBookingStatusText(booking.status, language)) }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "${if (language == "ar") "تاريخ الإقامة:" else "Requested Dates:"} ${booking.moveInDate}  ➡️  ${booking.moveOutDate}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )

                            Text(
                                text = "${if (language == "ar") "عدد الأفراد:" else "Occupants:"} ${booking.occupants}",
                                style = MaterialTheme.typography.bodySmall
                            )

                            if (booking.message.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "\"${booking.message}\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (booking.status == BookingStatus.PENDING) {
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            bookingRepository.cancelBooking(booking.id).onSuccess { refreshData() }
                                        }
                                    }
                                ) {
                                    Text(Strings.get("btn_cancel_request", language))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
