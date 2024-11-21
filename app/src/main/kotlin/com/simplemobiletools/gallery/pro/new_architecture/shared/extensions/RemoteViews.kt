package com.simplemobiletools.gallery.pro.new_architecture.shared.extensions

import android.graphics.Color
import android.view.View
import android.widget.RemoteViews

fun RemoteViews.applyColorFilter(id: Int, color: Int) {
    setInt(id, "setColorFilter", color)
    setInt(id, "setImageAlpha", Color.alpha(color))
}


fun RemoteViews.setBackgroundColor(id: Int, color: Int) {
    setInt(id, "setBackgroundColor", color)
}

fun RemoteViews.setText(id: Int, text: String) {
    setTextViewText(id, text)
}

fun RemoteViews.setVisibleIf(id: Int, beVisible: Boolean) {
    val visibility = if (beVisible) View.VISIBLE else View.GONE
    setViewVisibility(id, visibility)
}
