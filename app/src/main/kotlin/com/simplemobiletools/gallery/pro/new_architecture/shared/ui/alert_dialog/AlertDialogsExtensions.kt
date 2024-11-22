package com.simplemobiletools.gallery.pro.new_architecture.shared.ui.alert_dialog

import androidx.compose.foundation.border
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.new_architecture.shared.ui.theme.LocalTheme
import com.simplemobiletools.gallery.pro.new_architecture.shared.ui.theme.Shapes
import com.simplemobiletools.gallery.pro.new_architecture.shared.ui.theme.SimpleTheme
import com.simplemobiletools.gallery.pro.new_architecture.shared.ui.theme.light_grey_stroke
import com.simplemobiletools.gallery.pro.new_architecture.shared.ui.theme.model.Theme
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.baseConfig
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.isSPlus

val dialogContainerColor
    @ReadOnlyComposable
    @Composable get() = when (LocalTheme.current) {
        is Theme.BlackAndWhite -> Color.Black
        is Theme.SystemDefaultMaterialYou -> if (isSPlus()) colorResource(R.color.you_dialog_background_color) else SimpleTheme.colorScheme.surface
        else -> {
            val context = LocalContext.current
            Color(context.baseConfig.backgroundColor)
        }
    }

val dialogShape = Shapes.extraLarge

val dialogElevation = 0.dp

val dialogTextColor @Composable @ReadOnlyComposable get() = SimpleTheme.colorScheme.onSurface

val Modifier.dialogBorder: Modifier
    @ReadOnlyComposable
    @Composable get() =
        when (LocalTheme.current) {
            is Theme.BlackAndWhite -> then(Modifier.border(1.dp, light_grey_stroke, dialogShape))
            else -> Modifier
        }

@Composable
fun DialogSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .dialogBorder,
        shape = dialogShape,
        color = dialogContainerColor,
        tonalElevation = dialogElevation,
    ) {
        content()
    }
}


