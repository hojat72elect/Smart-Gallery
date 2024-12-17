package ca.hojat.smart.gallery.shared.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import ca.hojat.smart.gallery.shared.ui.theme.LocalDimensions
import ca.hojat.smart.gallery.shared.ui.theme.model.Dimensions

@Immutable
object SimpleTheme {
    val dimens: Dimensions
        @Composable
        @ReadOnlyComposable
        get() = LocalDimensions.current

    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography

    val colorScheme: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme

}
