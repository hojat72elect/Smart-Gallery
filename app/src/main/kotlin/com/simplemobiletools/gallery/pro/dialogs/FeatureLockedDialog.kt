package com.simplemobiletools.gallery.pro.dialogs

import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.compose.alert_dialog.AlertDialogState
import com.simplemobiletools.gallery.pro.compose.alert_dialog.rememberAlertDialogState
import com.simplemobiletools.gallery.pro.compose.components.LinkifyTextComponent
import com.simplemobiletools.gallery.pro.compose.extensions.MyDevices
import com.simplemobiletools.gallery.pro.compose.extensions.composeDonateIntent
import com.simplemobiletools.gallery.pro.compose.extensions.rememberMutableInteractionSource
import com.simplemobiletools.gallery.pro.compose.theme.AppThemeSurface
import com.simplemobiletools.gallery.pro.compose.theme.SimpleTheme
import com.simplemobiletools.gallery.pro.extensions.fromHtml

@Composable
fun FeatureLockedAlertDialog(
    alertDialogState: AlertDialogState,
    modifier: Modifier = Modifier,
    cancelCallback: () -> Unit
) {
    val donateIntent = composeDonateIntent()
    androidx.compose.material3.AlertDialog(
        containerColor = dialogContainerColor,
        modifier = modifier
            .dialogBorder,
        properties = DialogProperties(dismissOnClickOutside = false, dismissOnBackPress = false),
        onDismissRequest = cancelCallback,
        shape = dialogShape,
        tonalElevation = dialogElevation,
        dismissButton = {
            TextButton(onClick = {
                cancelCallback()
                alertDialogState.hide()
            }) {
                Text(text = stringResource(id = R.string.later))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                donateIntent()
            }) {
                Text(text = stringResource(id = R.string.purchase))
            }
        },
        title = {
            Box(
                Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    Icons.Filled.Lock,
                    contentDescription = null,
                    modifier = Modifier
                        .size(SimpleTheme.dimens.icon.large)
                        .clickable(
                            indication = null,
                            interactionSource = rememberMutableInteractionSource(),
                            onClick = {
                                donateIntent()
                            }
                        ),
                    colorFilter = ColorFilter.tint(dialogTextColor)
                )
            }
        },
        text = {
            val source = stringResource(id = R.string.features_locked)
            LinkifyTextComponent(
                fontSize = 16.sp,
                removeUnderlines = false,
                modifier = Modifier.fillMaxWidth(),
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            ) {
                source.fromHtml()
            }
        }
    )
}

@Composable
@MyDevices
private fun FeatureLockedAlertDialogPreview() {
    AppThemeSurface {
        FeatureLockedAlertDialog(alertDialogState = rememberAlertDialogState()) {

        }
    }
}
