package com.example.montesorrilearning.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val MontessoriColorScheme = lightColorScheme(
    primary = WarmBrown,
    onPrimary = WarmCream,
    primaryContainer = SoftGreen,
    onPrimaryContainer = WarmBrownDark,
    secondary = SoftOrange,
    onSecondary = WarmBrownDark,
    secondaryContainer = SoftOrange.copy(alpha = 0.2f),
    onSecondaryContainer = WarmBrownDark,
    tertiary = SoftTeal,
    onTertiary = WarmBrownDark,
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    surface = CardBackground,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = WarmBrownDark,
    outline = DividerColor
)

private val MontessoriShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

val MontessoriCardShape = CardDefaults.shape.copy(
    topStart = RoundedCornerShape(16.dp),
    topEnd = RoundedCornerShape(16.dp),
    bottomStart = RoundedCornerShape(16.dp),
    bottomEnd = RoundedCornerShape(16.dp)
)

val LargeRoundedShape = RoundedCornerShape(16.dp)
val SmallRoundedShape = RoundedCornerShape(8.dp)

@Composable
fun MontessoriTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MontessoriColorScheme,
        typography = AppTypography,
        shapes = MontessoriShapes,
        content = content
    )
}
