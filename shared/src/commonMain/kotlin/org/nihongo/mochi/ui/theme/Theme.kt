package org.nihongo.mochi.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Définition des couleurs basées sur colors.xml (approximativement)
// Mode Jour
val LightPrimary = Color(0xFF4DB6AC) // button_background (Teal 400)
val LightOnPrimary = Color(0xFFFFFFFF) // button_text (White)
val LightPrimaryContainer = Color(0xFFB2DFDB) // Teal 100
val LightOnPrimaryContainer = Color(0xFF004D40) // Teal 900
val LightSecondary = Color(0xFF8BC34A) // leaf_green
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFDCEDC8) // Light Green 100
val LightOnSecondaryContainer = Color(0xFF33691E) // Light Green 900
val LightTertiary = Color(0xFF2196F3) // blue_500
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFBBDEFB) // Blue 100
val LightOnTertiaryContainer = Color(0xFF0D47A1) // Blue 900
val LightError = Color(0xFFB00020)
val LightOnError = Color(0xFFFFFFFF)
val LightBackground = Color(0xFFE0F7FA) // app_background (Light Cyan)
val LightOnBackground = Color(0xFF263238) // text_color (Blue Grey 900)
val LightSurface = Color(0xFFFFFFFF) // card_background
val LightOnSurface = Color(0xFF000000) // card_text
val LightOutline = Color(0xFF79747E)
val LightSurfaceVariant = Color(0xFFE0E2EC)
val LightOnSurfaceVariant = Color(0xFF44474F)

// Mode Nuit
val DarkPrimary = Color(0xFFFF6D00) // button_background (Dark Orange)
val DarkOnPrimary = Color(0xFFD6D6D6) // text_color
val DarkPrimaryContainer = Color(0xFFE65100) // Orange 900
val DarkOnPrimaryContainer = Color(0xFFFFD180) // Orange A100
val DarkSecondary = Color(0xFF8BC34A) // leaf_green
val DarkOnSecondary = Color(0xFF1B360D) // Darker Green
val DarkSecondaryContainer = Color(0xFF33691E) // Light Green 900
val DarkOnSecondaryContainer = Color(0xFFDCEDC8) // Light Green 100
val DarkTertiary = Color(0xFF90CAF9) // blue_200
val DarkOnTertiary = Color(0xFF0D47A1)
val DarkTertiaryContainer = Color(0xFF1976D2) // blue_700
val DarkOnTertiaryContainer = Color(0xFFBBDEFB)
val DarkError = Color(0xFFCF6679)
val DarkOnError = Color(0xFF601410)
val DarkBackground = Color(0xFF011627) // app_background (Dark Night Blue)
val DarkOnBackground = Color(0xFFD6D6D6) // text_color
val DarkSurface = Color(0xFF1E2A38) // card_background (Dark Blue Grey)
val DarkOnSurface = Color(0xFFD6D6D6) // card_text
val DarkOutline = Color(0xFF938F99)
val DarkSurfaceVariant = Color(0xFF44474F)
val DarkOnSurfaceVariant = Color(0xFFC4C7D0)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    error = DarkError,
    onError = DarkOnError,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    outline = DarkOutline,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    error = LightError,
    onError = LightOnError,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    outline = LightOutline,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    // Note: La gestion de la barre d'état (StatusBar) est spécifique à Android
    // et doit être gérée dans l'Activity ou via un Composable platform-specific
    // si on veut garder ce fichier purement "common".
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
