package ca.hojat.smart.gallery.shared.ui.dialogs.alert_dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun rememberAlertDialogState(
    isShownInitially: Boolean = false
) = remember { AlertDialogState(isShownInitially) }


@Stable
class AlertDialogState(isShownInitially: Boolean = false) {

    private var isShown by mutableStateOf(isShownInitially)

    fun show() {
        if (isShown) {
            isShown = false
        }
        isShown = true
    }

    fun hide() {
        isShown = false
    }

    @Composable
    fun DialogMember(
        content: @Composable () -> Unit
    ) {
        if (isShown) {
            content()
        }
    }
}
