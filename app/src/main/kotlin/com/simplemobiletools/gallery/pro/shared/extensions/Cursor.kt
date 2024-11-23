package com.simplemobiletools.gallery.pro.shared.extensions

import android.annotation.SuppressLint
import android.database.Cursor


@SuppressLint("Range")
fun Cursor.getStringValue(key: String): String = getString(getColumnIndex(key))

@SuppressLint("Range")
fun Cursor.getIntValue(key: String) = getInt(getColumnIndex(key))

@SuppressLint("Range")
fun Cursor.getLongValue(key: String) = getLong(getColumnIndex(key))

