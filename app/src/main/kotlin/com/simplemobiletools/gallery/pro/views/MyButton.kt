package com.simplemobiletools.gallery.pro.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.Button

@SuppressLint("AppCompatCustomView")
class MyButton : Button {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    fun setColors(textColor: Int) {
        setTextColor(textColor)
    }
}
