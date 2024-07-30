package com.simplemobiletools.gallery.pro.views

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

class MyLinearLayoutManager(context: Context) : LinearLayoutManager(context) {


    // fixes crash java.lang.IndexOutOfBoundsException: Inconsistency detected...
    // taken from https://stackoverflow.com/a/33985508/1967672
    override fun supportsPredictiveItemAnimations() = false
}
