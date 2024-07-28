package com.simplemobiletools.gallery.pro.compose.bottom_sheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun rememberBottomSheetDialogState(
    openBottomSheet: Boolean = false,
) = remember {
    BottomSheetDialogState(
        openBottomSheet = openBottomSheet
    )
}

@Stable
class BottomSheetDialogState(
    openBottomSheet: Boolean = false,
) {

    private var isOpen by mutableStateOf(openBottomSheet)

    fun close() {
        isOpen = false
    }

    fun open() {
        isOpen = true
    }
}
