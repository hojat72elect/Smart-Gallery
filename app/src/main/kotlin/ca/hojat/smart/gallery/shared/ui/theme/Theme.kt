package ca.hojat.smart.gallery.shared.ui.theme

import android.app.ActivityManager
import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import ca.hojat.smart.gallery.shared.ui.extensions.config
import ca.hojat.smart.gallery.shared.ui.theme.model.Theme
import ca.hojat.smart.gallery.shared.ui.theme.model.Theme.Companion.systemDefaultMaterialYou
import ca.hojat.smart.gallery.shared.extensions.getActivity
import ca.hojat.smart.gallery.shared.extensions.getAppIconIds
import ca.hojat.smart.gallery.shared.extensions.getAppLauncherName
import ca.hojat.smart.gallery.shared.helpers.BaseConfig

@Composable
internal fun Theme(
    theme: Theme = systemDefaultMaterialYou(),
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    val context = LocalContext.current
    val baseConfig = remember { context.config }
    val isSystemInDarkTheme = isSystemInDarkTheme()

    val colorScheme = if (!view.isInEditMode) {
        when (theme) {
            is Theme.SystemDefaultMaterialYou -> {
                if (isSystemInDarkTheme) {
                    dynamicDarkColorScheme(context)
                } else {
                    dynamicLightColorScheme(context)
                }
            }

            is Theme.Custom, is Theme.Dark -> darkColorScheme(
                primary = theme.primaryColor,
                surface = theme.backgroundColor,
                onSurface = theme.textColor
            )

            is Theme.White -> darkColorScheme(
                primary = Color(theme.accentColor),
                surface = theme.backgroundColor,
                tertiary = theme.primaryColor,
                onSurface = theme.textColor,
            )

            is Theme.BlackAndWhite -> darkColorScheme(
                primary = Color(theme.accentColor),
                surface = theme.backgroundColor,
                tertiary = theme.primaryColor,
                onSurface = theme.textColor
            )

        }
    } else {
        previewColorScheme()
    }

    SideEffect {
        updateRecentsAppIcon(baseConfig, context)
    }

    val dimensions = CommonDimensions

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = Shapes,
        content = {
            CompositionLocalProvider(
                LocalRippleTheme provides DynamicThemeRipple,
                LocalTheme provides theme,
                LocalDimensions provides dimensions
            ) {
                content()
            }
        },
    )
}

val LocalTheme: ProvidableCompositionLocal<Theme> =
    staticCompositionLocalOf { Theme.Custom(1, 1, 1, 1) }

@Composable
private fun previewColorScheme() = if (isSystemInDarkTheme()) {
    darkColorScheme
} else {
    lightColorScheme
}


private fun updateRecentsAppIcon(baseConfig: BaseConfig, context: Context) {
    if (baseConfig.isUsingModifiedAppIcon) {
        val appIconIDs = context.getAppIconIds()
        val currentAppIconColorIndex = baseConfig.getCurrentAppIconColorIndex(context)
        if (appIconIDs.size - 1 < currentAppIconColorIndex) {
            return
        }

        val recentsIcon =
            BitmapFactory.decodeResource(context.resources, appIconIDs[currentAppIconColorIndex])
        val title = context.getAppLauncherName()
        val color = baseConfig.primaryColor

        val description = ActivityManager.TaskDescription(title, recentsIcon, color)
        context.getActivity().setTaskDescription(description)
    }
}