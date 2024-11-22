package com.simplemobiletools.gallery.pro.new_architecture.shared.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.simplemobiletools.gallery.pro.new_architecture.shared.ui.theme.SimpleTheme

@Composable
fun SettingsGroup(
    modifier: Modifier = Modifier,
    title: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        if (title != null) {
            SettingsGroupTitle(title = title)
        }
        content()
    }
}

@Composable
fun SettingsGroupTitle(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SimpleTheme.dimens.padding.extraLarge),
        contentAlignment = Alignment.CenterStart
    ) {
        val primary = SimpleTheme.colorScheme.primary
        val titleStyle = SimpleTheme.typography.headlineMedium.copy(color = primary)
        ProvideTextStyle(value = titleStyle) { title() }
    }
}


