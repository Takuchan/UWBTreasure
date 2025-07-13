package com.takuchan.uwbviaserial.ui.theme

import android.app.Activity
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
    primary = DarkColors.Primary,
    onPrimary = Color.Black, // Text/icon color on primary background
    secondary = DarkColors.Secondary,
    onSecondary = Color.Black, // Text/icon color on secondary background
    tertiary = DarkColors.Accent, // Accent color mapped to tertiary
    onTertiary = Color.Black, // Text/icon color on tertiary background
    background = DarkColors.Background,
    onBackground = Color.White, // Text/icon color on background
    surface = DarkColors.Surface,
    onSurface = Color.White, // Text/icon color on surface
    error = DarkColors.Warning, // Warning color mapped to error
    onError = Color.Black // Text/icon color on error background
)


private val LightColorScheme = lightColorScheme(
    primary = LightColors.Primary,
    onPrimary = Color.White,
    secondary = LightColors.Secondary,
    onSecondary = Color.Black,
    tertiary = LightColors.Accent,
    onTertiary = Color.Black,
    background = LightColors.Background,
    onBackground = LightColors.onBackGround,
    surface = LightColors.Surface,
    error = LightColors.Warning,
    onError = Color.White
)

@Composable
fun UWBviaSerialTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
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