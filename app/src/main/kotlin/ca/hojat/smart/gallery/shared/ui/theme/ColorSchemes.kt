package ca.hojat.smart.gallery.shared.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import ca.hojat.smart.gallery.shared.ui.theme.color_accent
import ca.hojat.smart.gallery.shared.ui.theme.color_primary
import ca.hojat.smart.gallery.shared.ui.theme.color_primary_dark

internal val darkColorScheme = darkColorScheme(
    primary = color_primary,
    secondary = color_primary_dark,
    tertiary = color_accent,
)
internal val lightColorScheme = lightColorScheme(
    primary = color_primary,
    secondary = color_primary_dark,
    tertiary = color_accent,
)
