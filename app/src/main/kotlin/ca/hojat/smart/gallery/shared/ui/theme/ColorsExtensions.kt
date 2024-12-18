package ca.hojat.smart.gallery.shared.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

fun Color.isLitWell(threshold: Float = LUMINANCE_THRESHOLD) = luminance() > threshold

fun Color.isNotLitWell(threshold: Float = LUMINANCE_THRESHOLD) = luminance() < threshold
