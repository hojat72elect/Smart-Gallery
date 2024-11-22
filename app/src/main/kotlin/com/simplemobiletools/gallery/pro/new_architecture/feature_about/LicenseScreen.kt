package com.simplemobiletools.gallery.pro.new_architecture.feature_about

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.new_architecture.shared.ui.lists.SimpleLazyListScaffold
import com.simplemobiletools.gallery.pro.new_architecture.shared.ui.settings.SettingsHorizontalDivider
import com.simplemobiletools.gallery.pro.new_architecture.shared.ui.theme.SimpleTheme
import com.simplemobiletools.gallery.pro.models.License
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun LicenseScreen(
    goBack: () -> Unit,
    thirdPartyLicenses: ImmutableList<License>,
    onLicenseClick: (urlId: Int) -> Unit,
) {
    SimpleLazyListScaffold(
        title = stringResource(id = R.string.third_party_licences),
        goBack = goBack
    ) {
        itemsIndexed(thirdPartyLicenses) { index, license ->
            Column {
                LicenseItem(license, onLicenseClick)
                if (index != thirdPartyLicenses.lastIndex) {
                    SettingsHorizontalDivider(modifier = Modifier.padding(bottom = SimpleTheme.dimens.padding.small))
                }
            }
        }
    }
}

@Composable
private fun LicenseItem(
    license: License,
    onLicenseClick: (urlId: Int) -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = stringResource(id = license.titleId),
                modifier = Modifier
                    .clickable {
                        onLicenseClick(license.urlId)
                    }
            )
        },
        supportingContent = {
            Text(
                text = stringResource(id = license.textId),
                modifier = Modifier.padding(top = SimpleTheme.dimens.padding.extraSmall),
            )
        },
        colors = ListItemDefaults.colors(
            headlineColor = SimpleTheme.colorScheme.primary,
            supportingColor = SimpleTheme.colorScheme.onSurface
        )
    )
}


