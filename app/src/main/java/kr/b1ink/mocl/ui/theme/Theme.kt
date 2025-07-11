package kr.b1ink.mocl.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = colorPrimaryDark,
    secondary = colorSecondaryDark,
    tertiary = colorTertiaryDark,
    background = colorBackgroundDark,
    onBackground = colorOnBackgroundDark,
    onTertiary = colorTextSecondaryDark,
    onSecondary = colorOnSecondaryDark,
    secondaryContainer = colorTertiaryDark,
    outlineVariant = Color(0xFF292929),
)

private val LightColorScheme = lightColorScheme(
    primary = colorPrimary,
    secondary = colorSecondary,
    tertiary = colorTertiary,
    background = colorBackground,
    onBackground = colorOnBackground,
    onTertiary = colorTextSecondary,
    onSecondary = colorOnSecondary,
    secondaryContainer = colorOnSecondary,
    outlineVariant = Color(0xFFC9CAC5),
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
//            window.statusBarColor = Color.Transparent.toArgb()
            window.statusBarColor = Color(0x22000000).toArgb()
            WindowCompat.getInsetsController(window, view)
                .apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}