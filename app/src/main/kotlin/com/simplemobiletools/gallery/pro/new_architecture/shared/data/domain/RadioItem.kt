package com.simplemobiletools.gallery.pro.new_architecture.shared.data.domain

import androidx.compose.runtime.Immutable

@Immutable
data class RadioItem(val id: Int, val title: String, val value: Any = id)
