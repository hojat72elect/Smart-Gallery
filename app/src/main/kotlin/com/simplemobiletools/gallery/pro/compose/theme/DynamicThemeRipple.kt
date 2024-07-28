package com.simplemobiletools.gallery.pro.compose.theme

import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
internal object DynamicThemeRipple : RippleTheme {
    @Composable
    override fun defaultColor(): Color =
        if (isSurfaceLitWell()) ripple_light else LocalContentColor.current

    @Composable
    override fun rippleAlpha(): RippleAlpha = DefaultRippleAlpha

    private val DefaultRippleAlpha = RippleAlpha(
        pressedAlpha = StateTokens.PRESSED_STATE_LAYER_OPACITY,
        focusedAlpha = StateTokens.FOCUS_STATE_LAYER_OPACITY,
        draggedAlpha = StateTokens.DRAGGED_STATE_LAYER_OPACITY,
        hoveredAlpha = StateTokens.HOVER_STATE_LAYER_OPACITY
    )

    @Immutable
    internal object StateTokens {
        const val DRAGGED_STATE_LAYER_OPACITY = 0.16f
        const val FOCUS_STATE_LAYER_OPACITY = 0.12f
        const val HOVER_STATE_LAYER_OPACITY = 0.08f
        const val PRESSED_STATE_LAYER_OPACITY = 0.12f
    }
}



