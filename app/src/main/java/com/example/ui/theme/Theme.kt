package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = Color(0xFF4CAF50), // Vibrant leafy green for dark mode
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF1B5E20),
    onPrimaryContainer = Color(0xFFE8F5E9),
    secondary = Color(0xFF3A3B3C),
    onSecondary = Color(0xFFE4E6EB),
    background = Color(0xFF18191A), // Official Facebook dark mode bg
    onBackground = Color(0xFFE4E6EB),
    surface = Color(0xFF242526), // Official Facebook dark mode card surface
    onSurface = Color(0xFFE4E6EB),
    outline = Color(0xFF3E4042),
    surfaceVariant = Color(0xFF3A3B3C),
    onSurfaceVariant = Color(0xFFB0B3B8)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = FacebookBlue,
    onPrimary = FacebookOnPrimary,
    primaryContainer = FacebookBlueContainer,
    onPrimaryContainer = FacebookOnBlueContainer,
    secondary = FacebookSecondary,
    onSecondary = FacebookOnSecondary,
    secondaryContainer = FacebookSecondaryContainer,
    onSecondaryContainer = FacebookOnSecondaryContainer,
    background = FacebookBg,
    onBackground = FacebookOnBackground,
    surface = FacebookSurface,
    onSurface = FacebookOnSurface,
    outline = FacebookOutline,
    surfaceVariant = FacebookSurfaceVariant,
    onSurfaceVariant = FacebookOnSurfaceVariant
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is disabled by default to preserve the gorgeous Sleek Interface design
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
