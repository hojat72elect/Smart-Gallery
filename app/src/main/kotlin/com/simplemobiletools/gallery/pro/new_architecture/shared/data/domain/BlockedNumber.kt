package com.simplemobiletools.gallery.pro.new_architecture.shared.data.domain

import androidx.compose.runtime.Immutable

@Immutable
data class BlockedNumber(val id: Long, val number: String, val normalizedNumber: String, val numberToCompare: String, val contactName: String? = null)
