package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ExplorerGold,
    onPrimary = Color(0xFF241600),
    primaryContainer = Color(0xFF3E2D0E),
    onPrimaryContainer = ExplorerGoldLight,
    secondary = SacredCyan,
    onSecondary = Color(0xFF003738),
    secondaryContainer = Color(0xFF1B3D38),
    onSecondaryContainer = Color(0xFFBCEBE5),
    tertiary = ExplorerGoldLight,
    onTertiary = Color(0xFF382900),
    background = DeepSpruceDark,
    onBackground = Color(0xFFE2ECEA),
    surface = SpruceSurfaceDark,
    onSurface = Color(0xFFE2ECEA),
    surfaceVariant = SpruceSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFC0CEC8)
)

private val LightColorScheme = lightColorScheme(
    primary = ExplorerGoldDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFECC4),
    onPrimaryContainer = Color(0xFF2E1C00),
    secondary = Color(0xFF266763),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC8ECE8),
    onSecondaryContainer = Color(0xFF00201F),
    tertiary = Color(0xFF7A5900),
    onTertiary = Color.White,
    background = ParchmentBackground,
    onBackground = Color(0xFF1C1D1A),
    surface = ParchmentSurface,
    onSurface = Color(0xFF1C1D1A),
    surfaceVariant = ParchmentSurfaceVariant,
    onSurfaceVariant = Color(0xFF49463D)
)

@Composable
fun GeoChronicleTheme(
    darkTheme: Boolean = true, // Default to sleek explorer dark radar mode
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
