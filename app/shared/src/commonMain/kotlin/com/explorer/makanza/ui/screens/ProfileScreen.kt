package com.explorer.makanza.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.explorer.makanza.data.AuthRepository
import com.explorer.makanza.models.UserDto
import com.explorer.makanza.models.UserRole
import com.explorer.makanza.ui.navigation.NavController
import com.explorer.makanza.ui.navigation.Screen
import com.explorer.makanza.ui.theme.LocalAppLanguage
import com.explorer.makanza.ui.theme.LocalThemeIsDark
import com.explorer.makanza.ui.theme.Strings

@Composable
fun ProfileScreen(
    currentUser: UserDto?,
    authRepository: AuthRepository,
    navController: NavController,
    onToggleTheme: () -> Unit,
    onToggleLanguage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalAppLanguage.current
    val isDark = LocalThemeIsDark.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = currentUser?.fullName?.take(1)?.uppercase() ?: "👤",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = currentUser?.fullName ?: (if (language == "ar") "زائر (Guest User)" else "Guest User"),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(4.dp))

        AssistChip(
            onClick = { },
            label = { Text(if (currentUser != null) Strings.getRoleText(currentUser.role, language) else "زائر") }
        )

        if (currentUser != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = currentUser.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = currentUser.phone,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Profile Action Options
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (currentUser?.role == UserRole.ADMIN) {
                    Button(
                        onClick = { navController.navigateTo(Screen.AdminDashboard) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("⚙️ ${Strings.get("nav_admin_dashboard", language)}")
                    }
                }

                Button(
                    onClick = { navController.navigateTo(Screen.PublishApartment()) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("➕ ${if (currentUser == null) "أضف عقار كـ زائر" else Strings.get("btn_owner_publish", language)}")
                }

                Button(
                    onClick = { navController.navigateTo(Screen.MyBookings) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("📅 ${if (currentUser == null) "طلبات الحجز (كـ زائر)" else Strings.get("nav_my_bookings", language)}")
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (language == "ar") "المظهر الداكن (Dark Mode)" else "Dark Theme")
                    Switch(checked = isDark, onCheckedChange = { onToggleTheme() })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (language == "ar") "اللغة (Language)" else "Language")
                    TextButton(onClick = onToggleLanguage) {
                        Text(if (language == "ar") "English" else "العربية")
                    }
                }

                HorizontalDivider()

                if (currentUser != null) {
                    OutlinedButton(
                        onClick = {
                            authRepository.logout()
                            navController.popToRoot()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(Strings.get("nav_logout", language))
                    }
                } else {
                    Button(
                        onClick = { navController.navigateTo(Screen.Auth(initialIsRegister = false)) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🔑 ${Strings.get("nav_login", language)}")
                    }
                }
            }
        }
    }
}
