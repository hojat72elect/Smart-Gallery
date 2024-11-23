package com.simplemobiletools.gallery.pro.shared.ui.bottom_sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.simplemobiletools.gallery.pro.shared.ui.dialogs.alert_dialog.dialogContainerColor
import com.simplemobiletools.gallery.pro.shared.ui.dialogs.alert_dialog.dialogElevation
import com.simplemobiletools.gallery.pro.shared.ui.theme.LocalTheme
import com.simplemobiletools.gallery.pro.shared.ui.theme.Shapes
import com.simplemobiletools.gallery.pro.shared.ui.theme.light_grey_stroke
import com.simplemobiletools.gallery.pro.shared.ui.theme.model.Theme

val bottomSheetDialogShape = Shapes.extraLarge.copy(
    bottomEnd = CornerSize(0f),
    bottomStart = CornerSize(0f)
)

val Modifier.bottomSheetDialogBorder: Modifier
    @ReadOnlyComposable
    @Composable get() =
        when (LocalTheme.current) {
            is Theme.BlackAndWhite -> then(
                Modifier.border(
                    2.dp,
                    light_grey_stroke,
                    bottomSheetDialogShape
                )
            )

            else -> Modifier
        }

@Composable
fun BottomSheetSpacerEdgeToEdge() {
    Spacer(modifier = Modifier.padding(bottom = 42.dp))
}

@Composable
fun BottomSheetColumnDialogSurface(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .bottomSheetDialogBorder,
        shape = bottomSheetDialogShape,
        color = dialogContainerColor,
        tonalElevation = dialogElevation,
    ) {
        Column(modifier = Modifier.background(dialogContainerColor)) {
            content()
        }
    }
}
