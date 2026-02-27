package org.openpdf.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import org.openpdf.core.model.settings.ThemeMode

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    error = ErrorLight,
    onError = OnErrorLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    outline = OutlineLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    error = ErrorDark,
    onError = OnErrorDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    outline = OutlineDark,
)

private val AmoledColorScheme = DarkColorScheme.copy(
    background = SurfaceAmoled,
    surface = SurfaceAmoled,
)

@Composable
fun OpenPdfTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val isDarkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.AMOLED -> true
    }

    val colorScheme = when {
        themeMode == ThemeMode.AMOLED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            dynamicDarkColorScheme(LocalContext.current).copy(
                background = SurfaceAmoled,
                surface = SurfaceAmoled,
            )
        }
        themeMode == ThemeMode.AMOLED -> AmoledColorScheme
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && isDarkTheme ->
            dynamicDarkColorScheme(LocalContext.current)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !isDarkTheme ->
            dynamicLightColorScheme(LocalContext.current)
        isDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
