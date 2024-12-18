package ca.hojat.smart.gallery.shared.ui.theme

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.luminance

internal const val LUMINANCE_THRESHOLD = 0.5f

@Composable
@ReadOnlyComposable
fun isSurfaceLitWell(threshold: Float = LUMINANCE_THRESHOLD) = SimpleTheme.colorScheme.surface.luminance() > threshold

internal fun Context.isDarkMode(): Boolean {
    val darkModeFlag = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return darkModeFlag == Configuration.UI_MODE_NIGHT_YES
}
