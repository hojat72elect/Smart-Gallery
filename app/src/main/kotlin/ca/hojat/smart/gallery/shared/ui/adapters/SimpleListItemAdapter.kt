package ca.hojat.smart.gallery.shared.ui.adapters

import ca.hojat.smart.gallery.databinding.ItemSimpleListBinding
import ca.hojat.smart.gallery.shared.extensions.applyColorFilter
import ca.hojat.smart.gallery.shared.extensions.beVisibleIf
import ca.hojat.smart.gallery.shared.extensions.getProperPrimaryColor
import ca.hojat.smart.gallery.shared.extensions.getProperTextColor
import ca.hojat.smart.gallery.shared.extensions.setImageResourceOrBeGone
import ca.hojat.smart.gallery.shared.data.domain.SimpleListItem

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
