package com.simplemobiletools.gallery.pro.new_architecture.shared.helpers

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.simplemobiletools.gallery.pro.new_architecture.shared.data.domain.Medium
import com.simplemobiletools.gallery.pro.new_architecture.shared.data.domain.ThumbnailItem

class GridSpacingItemDecoration(
    val spanCount: Int,
    private val spacing: Int,
    private val isScrollingHorizontally: Boolean,
    private val addSideSpacing: Boolean,
    var items: ArrayList<ThumbnailItem>,
    private val useGridPosition: Boolean
) : RecyclerView.ItemDecoration() {

    override fun toString() =
        "spanCount: $spanCount, spacing: $spacing, isScrollingHorizontally: $isScrollingHorizontally, addSideSpacing: $addSideSpacing, " +
                "items: ${items.hashCode()}, useGridPosition: $useGridPosition"

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (spacing <= 1) {
            return
        }

        val position = parent.getChildAdapterPosition(view)
        val medium = items.getOrNull(position) as? Medium ?: return
        val gridPositionToUse = if (useGridPosition) medium.gridPosition else position
        val column = gridPositionToUse % spanCount

        if (isScrollingHorizontally) {
            if (addSideSpacing) {
                outRect.top = spacing - column * spacing / spanCount
                outRect.bottom = (column + 1) * spacing / spanCount
                outRect.right = spacing

                if (position < spanCount) {
                    outRect.left = spacing
                }
            } else {
                outRect.top = column * spacing / spanCount
                outRect.bottom = spacing - (column + 1) * spacing / spanCount
                if (position >= spanCount) {
                    outRect.left = spacing
                }
            }
        } else {
            if (addSideSpacing) {
                outRect.left = spacing - column * spacing / spanCount
                outRect.right = (column + 1) * spacing / spanCount
                outRect.bottom = spacing

                if (position < spanCount && !useGridPosition) {
                    outRect.top = spacing
                }
            } else {
                outRect.left = column * spacing / spanCount
                outRect.right = spacing - (column + 1) * spacing / spanCount

                if (gridPositionToUse >= spanCount) {
                    outRect.top = spacing
                }
            }
        }
    }
}