package ca.hojat.smart.gallery.shared.data.domain

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

@Immutable
data class LanguageContributor(@DrawableRes val iconId: Int, @StringRes val labelId: Int, @StringRes val contributorsId: Int)
