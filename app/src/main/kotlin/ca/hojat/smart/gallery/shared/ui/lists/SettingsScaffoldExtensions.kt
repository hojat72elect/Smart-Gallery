package ca.hojat.smart.gallery.shared.ui.lists

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalLayoutDirection
import ca.hojat.smart.gallery.shared.ui.system_ui_controller.rememberSystemUiController
import ca.hojat.smart.gallery.shared.ui.extensions.onEventValue
import ca.hojat.smart.gallery.shared.ui.theme.LocalTheme
import ca.hojat.smart.gallery.shared.ui.theme.SimpleTheme
import ca.hojat.smart.gallery.shared.ui.theme.isNotLitWell
import ca.hojat.smart.gallery.shared.ui.theme.isSurfaceLitWell
import ca.hojat.smart.gallery.shared.ui.theme.model.Theme
import ca.hojat.smart.gallery.shared.extensions.getColoredMaterialStatusBarColor
import ca.hojat.smart.gallery.shared.extensions.getContrastColor

@Composable
internal fun SystemUISettingsScaffoldStatusBarColor(scrolledColor: Color) {
    val systemUiController = rememberSystemUiController()
    DisposableEffect(systemUiController) {
        systemUiController.statusBarDarkContentEnabled = scrolledColor.isNotLitWell()
        onDispose { }
    }
}

@Composable
internal fun ScreenBoxSettingsScaffold(
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SimpleTheme.colorScheme.surface)
            .padding(
                top = paddingValues.calculateTopPadding(),
                start = paddingValues.calculateStartPadding(layoutDirection),
                end = paddingValues.calculateEndPadding(layoutDirection)
            )
    ) {
        content()
    }
}

@Composable
internal fun statusBarAndContrastColor(context: Context): Pair<Int, Color> {
    val statusBarColor = onEventValue { context.getColoredMaterialStatusBarColor() }
    val contrastColor by remember(statusBarColor) {
        derivedStateOf { Color(statusBarColor.getContrastColor()) }
    }
    return Pair(statusBarColor, contrastColor)
}

@Composable
internal fun transitionFractionAndScrolledColor(
    scrollBehavior: TopAppBarScrollBehavior,
    contrastColor: Color,
    darkIcons: Boolean = true,
): Pair<Float, Color> {
    val systemUiController = rememberSystemUiController()
    val colorTransitionFraction = scrollBehavior.state.overlappedFraction
    val scrolledColor = lerp(
        start = if (isSurfaceLitWell()) Color.Black else Color.White,
        stop = contrastColor,
        fraction = if (colorTransitionFraction > 0.01f) 1f else 0f
    )

    systemUiController.setStatusBarColor(
        color = Color.Transparent,
        darkIcons = scrolledColor.isNotLitWell() && darkIcons || (LocalTheme.current is Theme.SystemDefaultMaterialYou && !isSystemInDarkTheme())
    )
    return Pair(colorTransitionFraction, scrolledColor)
}
