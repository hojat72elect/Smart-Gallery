package com.simplemobiletools.gallery.pro.new_architecture.shared.extensions

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

fun EditText.onTextChangeListener(onTextChangedAction: (newText: String) -> Unit) = addTextChangedListener(object :
    TextWatcher {
    override fun afterTextChanged(s: Editable?) {
        onTextChangedAction(s.toString())
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
})

val EditText.value: String get() = text.toString().trim()