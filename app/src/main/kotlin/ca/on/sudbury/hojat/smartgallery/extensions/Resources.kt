package ca.on.sudbury.hojat.smartgallery.extensions

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.drawable.Drawable
import ca.on.sudbury.hojat.smartgallery.usecases.ApplyColorFilterUseCase

@SuppressLint("UseCompatLoadingForDrawables")
fun Resources.getColoredDrawableWithColor(drawableId: Int, color: Int, alpha: Int = 255): Drawable {
    val drawable = getDrawable(drawableId)
    ApplyColorFilterUseCase(drawable.mutate(),color )
    drawable.mutate().alpha = alpha
    return drawable
}

