package ca.hojat.smart.gallery.shared.ui.extensions

import androidx.activity.ComponentActivity
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import ca.hojat.smart.gallery.shared.extensions.darkenColor
import ca.hojat.smart.gallery.shared.ui.system_ui_controller.rememberSystemUiController
import ca.hojat.smart.gallery.shared.ui.theme.SimpleTheme
import ca.hojat.smart.gallery.shared.ui.theme.isLitWell


@Composable
fun rememberMutableInteractionSource() = remember { MutableInteractionSource() }

@Composable
fun AdjustNavigationBarColors() {
    val systemUiController = rememberSystemUiController()
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val isSurfaceLitWell = SimpleTheme.colorScheme.surface.isLitWell()
    val navigationBarColor =
        Color(SimpleTheme.colorScheme.surface.toArgb().darkenColor()).copy(alpha = 0.5f)
    DisposableEffect(systemUiController, isSystemInDarkTheme, navigationBarColor) {
        systemUiController.setNavigationBarColor(
            color = navigationBarColor,
            darkIcons = !isSystemInDarkTheme
        )
        systemUiController.navigationBarDarkContentEnabled = isSurfaceLitWell
        onDispose {}
    }
}

@Composable
fun <T : Any> onEventValue(event: Lifecycle.Event = Lifecycle.Event.ON_START, value: () -> T): T {
    val rememberLatestUpdateState by rememberUpdatedState(newValue = value)
    var rememberedValue by remember { mutableStateOf(value()) }
    LifecycleEventEffect(event = event) {
        rememberedValue = rememberLatestUpdateState()
    }
    return rememberedValue
}


@Composable
operator fun PaddingValues.plus(otherPaddingValues: PaddingValues): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current
    return PaddingValues(
        start = calculateLeftPadding(layoutDirection).plus(
            otherPaddingValues.calculateLeftPadding(
                layoutDirection
            )
        ),
        top = calculateTopPadding().plus(otherPaddingValues.calculateTopPadding()),
        end = calculateRightPadding(layoutDirection).plus(
            otherPaddingValues.calculateRightPadding(
                layoutDirection
            )
        ),
        bottom = calculateBottomPadding().plus(otherPaddingValues.calculateBottomPadding())
    )
}

@Composable
fun PaddingValues.plus(vararg otherPaddingValues: PaddingValues): PaddingValues {
    val thisArray = arrayOf(this)
    return PaddingValues(
        start = thisArray.plus(otherPaddingValues).sumOfDps(PaddingValues::calculateStartPadding),
        top = thisArray.plus(otherPaddingValues).sumOfDps(PaddingValues::calculateTopPadding),
        end = thisArray.plus(otherPaddingValues).sumOfDps(PaddingValues::calculateEndPadding),
        bottom = thisArray.plus(otherPaddingValues).sumOfDps(PaddingValues::calculateBottomPadding)
    )
}

@Composable
private fun Array<out PaddingValues>.sumOfDps(aggregator: (PaddingValues, LayoutDirection) -> Dp): Dp {
    val layoutDirection = LocalLayoutDirection.current
    return asSequence().map { paddingValues ->
        aggregator(paddingValues, layoutDirection)
    }.sumOfDps()
}

private fun Array<out PaddingValues>.sumOfDps(aggregator: PaddingValues.() -> Dp): Dp =
    asSequence().map { paddingValues ->
        paddingValues.aggregator()
    }.sumOfDps()


private fun Sequence<Dp>.sumOfDps(): Dp {
    var sum = 0.dp
    for (element in this) {
        sum += element
    }
    return sum
}

fun ComponentActivity.enableEdgeToEdgeSimple() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
}

@Composable
internal fun TransparentSystemBars(darkIcons: Boolean = !isSystemInDarkTheme()) {
    val systemUiController = rememberSystemUiController()

    DisposableEffect(systemUiController, darkIcons) {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = darkIcons
        )
        onDispose { }
    }
}

