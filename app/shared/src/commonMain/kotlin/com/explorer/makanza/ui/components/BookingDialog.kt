package com.explorer.makanza.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.explorer.makanza.models.ApartmentDto
import com.explorer.makanza.models.CreateBookingRequest
import com.explorer.makanza.models.RentalPeriod
import com.explorer.makanza.models.UserDto
import com.explorer.makanza.ui.theme.LocalAppLanguage
import com.explorer.makanza.ui.theme.Strings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDialog(
    apartment: ApartmentDto,
    currentUser: UserDto? = null,
    onDismiss: () -> Unit,
    onSubmit: (CreateBookingRequest) -> Unit,
    isSubmitting: Boolean = false,
    errorMessage: String? = null
) {
    val language = LocalAppLanguage.current

    var selectedRentalPeriod by remember { mutableStateOf(apartment.rentalPeriod) }
    var moveInDate by remember { mutableStateOf("2025-09-01") }
    var moveOutDate by remember { mutableStateOf("2026-06-30") }
    var occupants by remember { mutableStateOf(1) }
    var message by remember { mutableStateOf("") }
    
    // Guest Contact Details
    var guestName by remember { mutableStateOf(currentUser?.fullName ?: "") }
    var guestPhone by remember { mutableStateOf(currentUser?.phone ?: "") }
    var localError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = Strings.get("dialog_booking_title", language),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = apartment.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )

                if (currentUser == null) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "👤 حجز كـ زائر: يرجى إدخال اسمك ورقم هاتفك للتواصل مع المالك",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(8.dp),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }

                    OutlinedTextField(
                        value = guestName,
                        onValueChange = { guestName = it },
                        label = { Text("الاسم الكامل للزائر") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = guestPhone,
                        onValueChange = { guestPhone = it },
                        label = { Text("رقم الهاتف (للتواصل واتساب/اتصال)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Rental Period Selection
                Text(
                    text = "نوع ونظام الإيجار المطلوب:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RentalPeriod.entries.forEach { period ->
                        FilterChip(
                            selected = selectedRentalPeriod == period,
                            onClick = { selectedRentalPeriod = period },
                            label = {
                                Text(
                                    when (period) {
                                        RentalPeriod.PER_DAY -> "يومي"
                                        RentalPeriod.PER_MONTH -> "شهري"
                                        RentalPeriod.PER_SEMESTER -> "بالترم"
                                        RentalPeriod.PER_YEAR -> "سنوي"
                                    },
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                    }
                }

                // Pricing Summary
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == "ar") "القيمة التقديرية:" else "Estimated Price:",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = Strings.formatRent(apartment.price, selectedRentalPeriod, language),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                OutlinedTextField(
                    value = moveInDate,
                    onValueChange = { moveInDate = it },
                    label = { Text(Strings.get("label_move_in", language)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = moveOutDate,
                    onValueChange = { moveOutDate = it },
                    label = { Text(Strings.get("label_move_out", language)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Occupants Counter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Strings.get("label_occupants", language),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = { if (occupants > 1) occupants-- },
                            enabled = occupants > 1,
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("-", style = MaterialTheme.typography.titleMedium)
                        }

                        Text(
                            text = occupants.toString(),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        OutlinedButton(
                            onClick = { occupants++ },
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("+", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text(Strings.get("label_message_optional", language)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                (localError ?: errorMessage)?.let { err ->
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (currentUser == null && (guestName.isBlank() || guestPhone.isBlank())) {
                        localError = "يرجى إدخال الاسم ورقم الهاتف للتواصل كـ زائر"
                        return@Button
                    }
                    if (moveInDate.isBlank() || moveOutDate.isBlank()) {
                        localError = if (language == "ar") "يرجى إدخال تواريخ صحيحة" else "Please enter valid dates"
                        return@Button
                    }
                    if (moveInDate >= moveOutDate) {
                        localError = if (language == "ar") "تاريخ المغادرة يجب أن يكون بعد تاريخ الانتقال" else "Move-out date must be after move-in date"
                        return@Button
                    }
                    localError = null
                    onSubmit(
                        CreateBookingRequest(
                            apartmentId = apartment.id,
                            moveInDate = moveInDate.trim(),
                            moveOutDate = moveOutDate.trim(),
                            rentalPeriod = selectedRentalPeriod,
                            totalPrice = apartment.price,
                            occupants = occupants,
                            message = message.trim(),
                            guestName = guestName.trim().takeIf { it.isNotBlank() },
                            guestPhone = guestPhone.trim().takeIf { it.isNotBlank() }
                        )
                    )
                },
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(Strings.get("btn_submit_booking", language))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text(if (language == "ar") "إلغاء" else "Cancel")
            }
        }
    )
}
