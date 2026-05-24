package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = BrandCharcoal,
    onPrimary = BrandLinen,
    secondary = BrandSand,
    onSecondary = BrandObsidian,
    tertiary = BrandSage,
    onTertiary = BrandWhite,
    background = BrandLinen,
    onBackground = BrandObsidian,
    surface = BrandWhite,
    onSurface = BrandObsidian,
    surfaceVariant = BrandLinenMatte,
    onSurfaceVariant = BrandCharcoal,
    outline = BrandBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandDarkTextPrimary,
    onPrimary = BrandDarkBackground,
    secondary = BrandDarkAccentSand,
    onSecondary = BrandDarkTextPrimary,
    tertiary = BrandDarkAccentSage,
    onTertiary = BrandDarkBackground,
    background = BrandDarkBackground,
    onBackground = BrandDarkTextPrimary,
    surface = BrandDarkSurface,
    onSurface = BrandDarkTextPrimary,
    surfaceVariant = BrandDarkSurface,
    onSurfaceVariant = BrandDarkTextSecondary,
    outline = BrandDarkBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
