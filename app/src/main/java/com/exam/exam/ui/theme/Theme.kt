package com.exam.exam.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = MedicalBlue80,
    secondary = MedicalBlueGrey80,
    tertiary = EmergencyRed80,
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFF49454F),
    onPrimary = Color(0xFF003258),
    onSecondary = Color(0xFF2E3132),
    onTertiary = Color(0xFF5F1419),
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
    onSurfaceVariant = Color(0xFFCAC4D0),
    primaryContainer = Color(0xFF004881),
    secondaryContainer = Color(0xFF404648),
    tertiaryContainer = Color(0xFF7D2D2F),
    onPrimaryContainer = MedicalBlue80,
    onSecondaryContainer = MedicalBlueGrey80,
    onTertiaryContainer = EmergencyRed80,
    error = CriticalRed,
    onError = Color.White,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColorScheme = lightColorScheme(
    primary = MedicalBlue40,
    secondary = MedicalBlueGrey40,
    tertiary = EmergencyRed40,
    background = MedicalWhite,
    surface = Color.White,
    surfaceVariant = SurfaceBlue,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = OnSurfaceBlue,
    onSurface = OnSurfaceBlue,
    onSurfaceVariant = Color(0xFF49454F),
    primaryContainer = MedicalBlue80,
    secondaryContainer = MedicalBlueGrey80,
    tertiaryContainer = EmergencyRed80,
    onPrimaryContainer = Color(0xFF001D36),
    onSecondaryContainer = Color(0xFF0F1419),
    onTertiaryContainer = Color(0xFF410E0B),
    error = CriticalRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0)
)

@Composable
fun ExamTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled to use medical theme
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
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}