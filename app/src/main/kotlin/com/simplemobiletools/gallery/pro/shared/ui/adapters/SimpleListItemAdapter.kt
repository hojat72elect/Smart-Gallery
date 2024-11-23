package com.simplemobiletools.gallery.pro.shared.ui.adapters

import com.simplemobiletools.gallery.pro.databinding.ItemSimpleListBinding
import com.simplemobiletools.gallery.pro.shared.extensions.applyColorFilter
import com.simplemobiletools.gallery.pro.shared.extensions.beVisibleIf
import com.simplemobiletools.gallery.pro.shared.extensions.getProperPrimaryColor
import com.simplemobiletools.gallery.pro.shared.extensions.getProperTextColor
import com.simplemobiletools.gallery.pro.shared.extensions.setImageResourceOrBeGone
import com.simplemobiletools.gallery.pro.shared.data.domain.SimpleListItem

fun setupSimpleListItem(
    view: ItemSimpleListBinding,
    item: SimpleListItem,
    onItemClicked: (SimpleListItem) -> Unit
) {
    view.apply {
        val color = if (item.selected) {
            root.context.getProperPrimaryColor()
        } else {
            root.context.getProperTextColor()
        }

        bottomSheetItemTitle.setText(item.textRes)
        bottomSheetItemTitle.setTextColor(color)
        bottomSheetItemIcon.setImageResourceOrBeGone(item.imageRes)
        bottomSheetItemIcon.applyColorFilter(color)
        bottomSheetSelectedIcon.beVisibleIf(item.selected)
        bottomSheetSelectedIcon.applyColorFilter(color)

        root.setOnClickListener {
            onItemClicked(item)
        }
    }
}
