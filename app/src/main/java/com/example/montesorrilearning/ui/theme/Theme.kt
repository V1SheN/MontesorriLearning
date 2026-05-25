package com.example.montesorrilearning.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryCyan,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryGlow,
    onPrimaryContainer = PrimaryCyan,
    secondary = SecondaryAmber,
    onSecondary = OnPrimary,
    secondaryContainer = SecondaryGlow,
    onSecondaryContainer = SecondaryAmber,
    tertiary = TertiaryLavender,
    onTertiary = OnPrimary,
    error = ErrorRed,
    onError = OnPrimary,
    errorContainer = ErrorRed.copy(alpha = 0.15f),
    onErrorContainer = ErrorRed,
    background = BackgroundDark,
    onBackground = OnBackground,
    surface = SurfaceDark,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariant,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    surfaceTint = PrimaryCyan,
    inverseSurface = SurfaceLight,
    inverseOnSurface = OnSurfaceLight,
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun MontessoriTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
