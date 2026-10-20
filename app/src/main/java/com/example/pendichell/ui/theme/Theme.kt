package com.example.pendichell.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryPink,
    secondary = PrimaryPurple,
    tertiary = PrimaryBlue,
    background = BackgroundPastel,
    surface = CardBackground,
    onPrimary = Color.White,
    onBackground = TextDark,
    onSurface = TextDark
)

@Composable
fun TaskTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}

@Composable
fun PendichellTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    TaskTheme(content = content)
}
