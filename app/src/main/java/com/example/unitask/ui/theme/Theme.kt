package com.example.unitask.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.unitask.data.AccentColor
import com.example.unitask.data.AppTheme
import com.example.unitask.data.ThemeSettings

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

/**
 * Genera un esquema de colores oscuro basado en un color de acento.
 */
private fun createDarkColorScheme(accentColor: Color): ColorScheme {
    return darkColorScheme(
        primary = accentColor,
        onPrimary = Color.White,
        primaryContainer = accentColor.copy(alpha = 0.3f),
        onPrimaryContainer = accentColor.copy(alpha = 0.9f),
        secondary = accentColor.copy(alpha = 0.7f),
        tertiary = accentColor.copy(alpha = 0.5f)
    )
}

/**
 * Genera un esquema de colores claro basado en un color de acento.
 */
private fun createLightColorScheme(accentColor: Color): ColorScheme {
    return lightColorScheme(
        primary = accentColor,
        onPrimary = Color.White,
        primaryContainer = accentColor.copy(alpha = 0.15f),
        onPrimaryContainer = accentColor,
        secondary = accentColor.copy(alpha = 0.8f),
        tertiary = accentColor.copy(alpha = 0.6f)
    )
}

/**
 * Tema principal de UniTask con soporte para:
 * - Tema claro/oscuro/sistema
 * - Colores dinámicos (Android 12+)
 * - Colores de acento personalizables
 */
@Composable
fun UniTaskTheme(
    settings: ThemeSettings,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    
    val isDarkTheme = when (settings.theme) {
        AppTheme.SYSTEM -> isSystemInDarkTheme()
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
    }
    
    val colorScheme = when {
        // Si usa colores dinámicos y está disponible (Android 12+)
        settings.useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // Usar color de acento personalizado
        else -> {
            val accentColor = settings.accentColor.seed
            if (isDarkTheme) createDarkColorScheme(accentColor) else createLightColorScheme(accentColor)
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * Tema simplificado para compatibilidad con código existente.
 */
@Composable
fun UniTaskTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val settings = ThemeSettings(
        theme = if (darkTheme) AppTheme.DARK else AppTheme.LIGHT,
        useDynamicColor = dynamicColor
    )
    UniTaskTheme(settings = settings, content = content)
}