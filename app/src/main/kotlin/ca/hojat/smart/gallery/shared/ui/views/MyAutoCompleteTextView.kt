package ca.hojat.smart.gallery.shared.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.AutoCompleteTextView
import ca.hojat.smart.gallery.shared.extensions.adjustAlpha
import ca.hojat.smart.gallery.shared.extensions.applyColorFilter

@SuppressLint("AppCompatCustomView")
class MyAutoCompleteTextView : AutoCompleteTextView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    fun setColors(textColor: Int, accentColor: Int) {
        background?.mutate()?.applyColorFilter(accentColor)

        // requires android:textCursorDrawable="@null" in xml to color the cursor too
        setTextColor(textColor)
        setHintTextColor(textColor.adjustAlpha(0.5f))
        setLinkTextColor(accentColor)
    }
}
