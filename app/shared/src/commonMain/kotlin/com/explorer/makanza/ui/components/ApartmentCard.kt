package com.explorer.makanza.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.explorer.makanza.models.ApartmentDto
import com.explorer.makanza.models.GenderEligibility
import com.explorer.makanza.ui.theme.LocalAppLanguage
import com.explorer.makanza.ui.theme.Strings

@Composable
fun ApartmentCard(
    apartment: ApartmentDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalAppLanguage.current
    val primaryImage = apartment.images.firstOrNull()?.imageUrl

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                if (!primaryImage.isNullOrBlank()) {
                    AsyncImage(
                        model = primaryImage,
                        contentDescription = apartment.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    )
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "🏠",
                                style = MaterialTheme.typography.displayMedium
                            )
                        }
                    }
                }

                // Accommodation Type Tag
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = Strings.getAccommodationTypeText(apartment.accommodationType, language),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Gender Badge
                if (apartment.genderEligibility != GenderEligibility.ANY) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (apartment.genderEligibility == GenderEligibility.FEMALE_ONLY)
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.95f)
                        else
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.95f),
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = if (apartment.genderEligibility == GenderEligibility.FEMALE_ONLY) "👩 طالبات" else "👨 طُلاّب",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Title
                Text(
                    text = apartment.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Price & Rental Period
                Text(
                    text = Strings.formatRent(apartment.price, apartment.rentalPeriod, language),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Near College / Proximity
                if (!apartment.nearCollege.isNullOrBlank()) {
                    Text(
                        text = "🎓 ${apartment.nearCollege}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Location Neighborhood
                Text(
                    text = "📍 ${apartment.neighborhood} - ${apartment.city}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "🖺 ${apartment.bedrooms} ${Strings.get("label_bedrooms", language)}",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = "🛁 ${apartment.bathrooms} ${Strings.get("label_bathrooms", language)}",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = "🛏️ ${apartment.availableBeds} ${Strings.get("label_available_beds", language)}",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    if (apartment.isDemoData) {
                        SuggestionChip(
                            onClick = { },
                            label = {
                                Text(
                                    text = Strings.get("msg_demo_badge", language),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
