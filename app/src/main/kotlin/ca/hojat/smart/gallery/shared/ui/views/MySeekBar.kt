package ca.hojat.smart.gallery.shared.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.SeekBar
import ca.hojat.smart.gallery.shared.extensions.applyColorFilter

@SuppressLint("AppCompatCustomView")
class MySeekBar : SeekBar {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    fun setColors(accentColor: Int) {
        progressDrawable.applyColorFilter(accentColor)
        thumb?.applyColorFilter(accentColor)
    }
}
