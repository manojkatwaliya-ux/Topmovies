package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val CinemaDarkColorScheme = darkColorScheme(
    primary = CinemaCrimson,
    onPrimary = Color.White,
    primaryContainer = CinemaCrimsonDark,
    onPrimaryContainer = Color.White,
    secondary = CinemaGold,
    onSecondary = CinemaObsidian,
    secondaryContainer = CinemaSurfaceHigh,
    onSecondaryContainer = CinemaGold,
    tertiary = CinemaCyan,
    onTertiary = CinemaObsidian,
    background = CinemaObsidian,
    onBackground = CinemaTextPrimary,
    surface = CinemaSurface,
    onSurface = CinemaTextPrimary,
    surfaceVariant = CinemaSurfaceContainer,
    onSurfaceVariant = CinemaTextSecondary,
    surfaceContainerLowest = CinemaObsidian,
    surfaceContainerLow = CinemaSurface,
    surfaceContainer = CinemaSurfaceContainer,
    surfaceContainerHigh = CinemaSurfaceHigh,
    outline = CinemaOutline,
    outlineVariant = CinemaOutline
)

private val CinemaLightColorScheme = lightColorScheme(
    primary = CinemaCrimsonDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDADF),
    onPrimaryContainer = Color(0xFF41000B),
    secondary = Color(0xFFB77400),
    onSecondary = Color.White,
    tertiary = Color(0xFF0284C7),
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = Color(0xFFD0D7E5)
)

val TopMoviesShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun TopMoviesTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) CinemaDarkColorScheme else CinemaLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TopMoviesTypography,
        shapes = TopMoviesShapes,
        content = content
    )
}
