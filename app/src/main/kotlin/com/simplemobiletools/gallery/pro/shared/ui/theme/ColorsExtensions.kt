package com.simplemobiletools.gallery.pro.shared.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

@get:ReadOnlyComposable
val disabledTextColor @Composable get() = if (isInDarkThemeOrSurfaceIsNotLitWell()) Color.DarkGray else Color.LightGray

@get:ReadOnlyComposable
val iconsColor
    @Composable get() = if (isSurfaceNotLitWell()) {
        Color.White
    } else {
        Color.Black
    }


@Composable
@ReadOnlyComposable
fun preferenceValueColor(isEnabled: Boolean) =
    if (isEnabled) SimpleTheme.colorScheme.onSurface.copy(alpha = 0.6f) else disabledTextColor

@Composable
@ReadOnlyComposable
fun preferenceLabelColor(isEnabled: Boolean) = if (isEnabled) SimpleTheme.colorScheme.onSurface else disabledTextColor

fun Color.isLitWell(threshold: Float = LUMINANCE_THRESHOLD) = luminance() > threshold

fun Color.isNotLitWell(threshold: Float = LUMINANCE_THRESHOLD) = luminance() < threshold
