package com.explorer.makanza.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

val PrimaryGold = Color(0xFFC5A059)
val PrimaryGoldDark = Color(0xFF9E7D3B)
val SecondaryTeal = Color(0xFF0F4C81)
val SecondaryTealLight = Color(0xFF1E6091)

val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val LightBackground = Color(0xFFF8F9FA)
val LightSurface = Color(0xFFFFFFFF)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGoldDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF4EBD9),
    onPrimaryContainer = Color(0xFF3B2A00),
    secondary = SecondaryTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD0E4FF),
    onSecondaryContainer = Color(0xFF001D36),
    background = LightBackground,
    onBackground = Color(0xFF1A1A1A),
    surface = LightSurface,
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = Color(0xFF444444)
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGold,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF4A3B18),
    onPrimaryContainer = Color(0xFFFCE8B3),
    secondary = SecondaryTealLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF00325B),
    onSecondaryContainer = Color(0xFFD0E4FF),
    background = DarkBackground,
    onBackground = Color(0xFFE6E6E6),
    surface = DarkSurface,
    onSurface = Color(0xFFE6E6E6),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFCCCCCC)
)

val LocalAppLanguage = compositionLocalOf { "en" }
val LocalThemeIsDark = compositionLocalOf { false }

@Composable
fun MakanzaTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    language: String = "en",
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme
    val layoutDirection = if (language == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(
        LocalAppLanguage provides language,
        LocalThemeIsDark provides isDarkTheme,
        LocalLayoutDirection provides layoutDirection
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}

fun formatPrice(price: Double, isArabic: Boolean = false): String {
    val integerPart = price.toLong().toString().reversed().chunked(3).joinToString(",").reversed()
    return if (isArabic) {
        "$integerPart د.إ"
    } else {
        "$integerPart AED"
    }
}
