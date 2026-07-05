package de.disaai.chathilfe.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ChatHilfeDarkColorScheme = darkColorScheme(
    primary = ChatHilfePrimary,
    background = ChatHilfeBackground,
    surface = ChatHilfeSurface,
    onBackground = ChatHilfeOnBackground,
    onSurface = ChatHilfeOnBackground,
)

@Composable
fun ChatHilfeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ChatHilfeDarkColorScheme,
        typography = ChatHilfeTypography,
        content = content,
    )
}
