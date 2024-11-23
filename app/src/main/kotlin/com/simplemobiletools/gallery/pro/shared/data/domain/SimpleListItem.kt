package com.simplemobiletools.gallery.pro.shared.data.domain

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize

@Parcelize
@Immutable
data class SimpleListItem(
    val id: Int,
    val textRes: Int,
    val imageRes: Int? = null,
    val selected: Boolean = false
) : Parcelable
