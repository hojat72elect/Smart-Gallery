package ca.hojat.smart.gallery.shared.ui.components

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ca.hojat.smart.gallery.shared.ui.dialogs.alert_dialog.dialogTextColor
import ca.hojat.smart.gallery.shared.ui.extensions.rememberMutableInteractionSource
import ca.hojat.smart.gallery.shared.ui.theme.SimpleTheme

@Composable
fun RadioButtonDialogComponent(
    modifier: Modifier = Modifier,
    setSelected: (selected: String) -> Unit,
    item: String,
    selected: String?
) {
    val interactionSource = rememberMutableInteractionSource()
    val indication = LocalIndication.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = { setSelected(item) },
                interactionSource = interactionSource,
                indication = indication
            )
            .then(modifier)
    ) {
        RadioButton(
            selected = selected == item,
            onClick = null,
            enabled = true,
            colors = RadioButtonDefaults.colors(
                selectedColor = SimpleTheme.colorScheme.primary
            ),
        )
        Text(
            text = item,
            modifier = Modifier.padding(start = SimpleTheme.dimens.padding.medium),
            color = dialogTextColor
        )
    }
}


