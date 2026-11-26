package com.explorer.makanza

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.explorer.makanza.data.*
import com.explorer.makanza.ui.components.AppHeader
import com.explorer.makanza.ui.navigation.NavController
import com.explorer.makanza.ui.navigation.Screen
import com.explorer.makanza.ui.screens.*
import com.explorer.makanza.ui.theme.MakanzaTheme
import com.explorer.makanza.ui.theme.Strings

@Composable
fun App() {
    val tokenStorage = remember { TokenStorage() }
    val apiClient = remember { MakanzaApiClient(tokenStorage) }

    val authRepository = remember { AuthRepository(apiClient, tokenStorage) }
    val apartmentRepository = remember { ApartmentRepository(apiClient) }
    val bookingRepository = remember { BookingRepository(apiClient) }

    val navController = remember { NavController() }

    val currentUser by authRepository.currentUser.collectAsState()

    var isDarkTheme by remember { mutableStateOf(false) }
    var language by remember { mutableStateOf("ar") } // Default primary language: Egyptian Arabic (ar)

    LaunchedEffect(Unit) {
        authRepository.restoreSession()
    }

    MakanzaTheme(
        isDarkTheme = isDarkTheme,
        language = language
    ) {
        Scaffold(
            topBar = {
                AppHeader(
                    navController = navController,
                    currentUser = currentUser,
                    onToggleTheme = { isDarkTheme = !isDarkTheme },
                    onToggleLanguage = { language = if (language == "en") "ar" else "en" }
                )
            },
            bottomBar = {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        windowInsets = WindowInsets(0.dp)
                    ) {
                        val current = navController.currentScreen

                        NavigationBarItem(
                            selected = current is Screen.Home,
                            onClick = { navController.navigateTo(Screen.Home) },
                            icon = { Text("🏠", style = MaterialTheme.typography.titleMedium) },
                            label = { Text(if (language == "ar") "الرئيسية" else "Home") }
                        )

                        NavigationBarItem(
                            selected = current is Screen.AllApartments,
                            onClick = { navController.navigateTo(Screen.AllApartments()) },
                            icon = { Text("🔍", style = MaterialTheme.typography.titleMedium) },
                            label = { Text(if (language == "ar") "تصفح" else "Browse") }
                        )

                        NavigationBarItem(
                            selected = current is Screen.PublishApartment,
                            onClick = { navController.navigateTo(Screen.PublishApartment()) },
                            icon = { Text("➕", style = MaterialTheme.typography.titleMedium) },
                            label = { Text(Strings.get("nav_publish", language)) }
                        )

                        NavigationBarItem(
                            selected = current is Screen.MyBookings,
                            onClick = { navController.navigateTo(Screen.MyBookings) },
                            icon = { Text("📅", style = MaterialTheme.typography.titleMedium) },
                            label = { Text(Strings.get("nav_my_bookings", language)) }
                        )

                        NavigationBarItem(
                            selected = current is Screen.Profile,
                            onClick = { navController.navigateTo(Screen.Profile) },
                            icon = { Text("👤", style = MaterialTheme.typography.titleMedium) },
                            label = { Text(Strings.get("nav_profile", language)) }
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (val screen = navController.currentScreen) {
                    is Screen.Home -> {
                        HomeScreen(
                            apartmentRepository = apartmentRepository,
                            navController = navController,
                            currentUser = currentUser
                        )
                    }

                    is Screen.AllApartments -> {
                        AllApartmentsScreen(
                            apartmentRepository = apartmentRepository,
                            navController = navController
                        )
                    }

                    is Screen.ApartmentDetails -> {
                        ApartmentDetailsScreen(
                            apartmentId = screen.apartmentId,
                            apartmentRepository = apartmentRepository,
                            bookingRepository = bookingRepository,
                            navController = navController,
                            currentUser = currentUser
                        )
                    }

                    is Screen.Auth -> {
                        AuthScreen(
                            initialIsRegister = screen.initialIsRegister,
                            authRepository = authRepository,
                            navController = navController
                        )
                    }

                    is Screen.PublishApartment -> {
                        PublishApartmentScreen(
                            editApartmentId = screen.editApartmentId,
                            apartmentRepository = apartmentRepository,
                            navController = navController,
                            currentUser = currentUser
                        )
                    }

                    is Screen.MyApartments -> {
                        MyApartmentsScreen(
                            apartmentRepository = apartmentRepository,
                            bookingRepository = bookingRepository,
                            navController = navController
                        )
                    }

                    is Screen.MyBookings -> {
                        MyBookingsScreen(
                            bookingRepository = bookingRepository,
                            navController = navController
                        )
                    }

                    is Screen.Profile -> {
                        ProfileScreen(
                            currentUser = currentUser,
                            authRepository = authRepository,
                            navController = navController,
                            onToggleTheme = { isDarkTheme = !isDarkTheme },
                            onToggleLanguage = { language = if (language == "en") "ar" else "en" }
                        )
                    }

                    is Screen.AdminDashboard -> {
                        AdminDashboardScreen(
                            apartmentRepository = apartmentRepository,
                            bookingRepository = bookingRepository,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}
