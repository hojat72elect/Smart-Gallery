package ca.hojat.smart.gallery.shared.extensions

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.drawable.Drawable
import ca.hojat.smart.gallery.shared.extensions.applyColorFilter


@SuppressLint("UseCompatLoadingForDrawables")
fun Resources.getColoredDrawableWithColor(drawableId: Int, color: Int, alpha: Int = 255): Drawable {
    val drawable = getDrawable(drawableId)
    drawable.mutate().applyColorFilter(color)
    drawable.mutate().alpha = alpha
    return drawable
}
