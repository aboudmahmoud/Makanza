package com.explorer.makanza.ui.navigation

import androidx.compose.runtime.mutableStateListOf

sealed class Screen {
    object Home : Screen()
    data class AllApartments(val initialQuery: String? = null) : Screen()
    data class ApartmentDetails(val apartmentId: String) : Screen()
    data class Auth(val initialIsRegister: Boolean = false) : Screen()
    data class PublishApartment(val editApartmentId: String? = null) : Screen()
    object MyApartments : Screen()
    object MyBookings : Screen()
    object Profile : Screen()
    object AdminDashboard : Screen()
}

class NavController(initialScreen: Screen = Screen.Home) {
    private val backStack = mutableStateListOf<Screen>(initialScreen)

    val currentScreen: Screen
        get() = backStack.lastOrNull() ?: Screen.Home

    fun navigateTo(screen: Screen) {
        if (currentScreen != screen) {
            backStack.add(screen)
        }
    }

    fun popBack(): Boolean {
        return if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
            true
        } else {
            false
        }
    }

    fun popToRoot() {
        while (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }
}
