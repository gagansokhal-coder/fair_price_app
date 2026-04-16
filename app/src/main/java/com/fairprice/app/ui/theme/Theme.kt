package com.fairprice.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * PDS Fair Price App — Material 3 Theme
 *
 * Light-only theme as per design system ("The Dignified Anchor").
 * The palette is specifically calibrated for outdoor visibility
 * in rural Indian contexts — high contrast, warm surfaces.
 */

private val FairPriceLightColorScheme = lightColorScheme(
    // Primary
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,

    // Secondary
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,

    // Tertiary
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,

    // Error
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,

    // Surface tiers
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    surfaceTint = SurfaceTint,
    surfaceBright = SurfaceBright,
    surfaceDim = SurfaceDim,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainerLowest = SurfaceContainerLowest,

    // Background
    background = Background,
    onBackground = OnBackground,

    // Inverse
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,
    inversePrimary = InversePrimary,

    // Outline
    outline = Outline,
    outlineVariant = OutlineVariant,

    // Scrim
    scrim = Scrim,
)

@Composable
fun FairPriceTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = FairPriceLightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = true
                isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FairPriceTypography,
        shapes = FairPriceShapes,
        content = content
    )
}
