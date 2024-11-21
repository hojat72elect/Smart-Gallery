package com.simplemobiletools.gallery.pro.compose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun RadioGroupDialogComponent(
    modifier: Modifier = Modifier,
    items: List<String>,
    selected: String?,
    verticalPadding: Dp = 10.dp,
    horizontalPadding: Dp = 20.dp,
    setSelected: (selected: String) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        items.forEach { item ->
            RadioButtonDialogComponent(
                setSelected = setSelected,
                item = item,
                selected = selected,
                modifier = Modifier.padding(
                    vertical = verticalPadding,
                    horizontal = horizontalPadding
                )
            )
        }
    }
}
