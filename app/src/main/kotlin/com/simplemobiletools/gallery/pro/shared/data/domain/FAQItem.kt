package com.simplemobiletools.gallery.pro.shared.data.domain

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import java.io.Serializable

@Immutable
data class FAQItem(@StringRes val title: Int, @StringRes val text: Int) : Serializable
