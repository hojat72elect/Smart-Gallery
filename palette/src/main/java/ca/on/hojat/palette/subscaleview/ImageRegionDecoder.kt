package ca.on.hojat.palette.subscaleview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Rect
import android.net.Uri

interface ImageRegionDecoder {
    fun isReady(): Boolean

    fun init(context: Context, uri: Uri): Point

    fun decodeRegion(sRect: Rect, sampleSize: Int): Bitmap

    fun recycle()
}
