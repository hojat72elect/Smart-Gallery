package com.simplemobiletools.gallery.pro.shared.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Point
import android.graphics.Rect
import android.net.Uri
import com.davemorrissey.labs.subscaleview.ImageRegionDecoder

class PicassoRegionDecoder(
    private val showHighestQuality: Boolean,
    private val screenWidth: Int,
    private val screenHeight: Int,
    private val minTileDpi: Int
) : ImageRegionDecoder {
    private var decoder: BitmapRegionDecoder? = null
    private val decoderLock = Any()

    override fun init(context: Context, uri: Uri): Point {
        val newUri = Uri.parse(uri.toString().replace("%", "%25").replace("#", "%23"))
        val inputStream = context.contentResolver.openInputStream(newUri)
        decoder = BitmapRegionDecoder.newInstance(inputStream!!, false)
        return Point(decoder!!.width, decoder!!.height)
    }

    override fun decodeRegion(sRect: Rect, sampleSize: Int): Bitmap {
        synchronized(decoderLock) {
            var newSampleSize = sampleSize
            if (!showHighestQuality && minTileDpi == LOW_TILE_DPI) {
                if ((sRect.width() > sRect.height() && screenWidth > screenHeight) || (sRect.height() > sRect.width() && screenHeight > screenWidth)) {
                    if ((sRect.width() / sampleSize > screenWidth || sRect.height() / sampleSize > screenHeight)) {
                        newSampleSize *= 2
                    }
                }
            }

            val options = BitmapFactory.Options()
            options.inSampleSize = newSampleSize
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            val bitmap = decoder!!.decodeRegion(sRect, options)
            return bitmap
                ?: throw RuntimeException("Region decoder returned null bitmap - image format may not be supported")
        }
    }

    override fun isReady() = decoder != null && !decoder!!.isRecycled

    override fun recycle() {
        decoder!!.recycle()
    }
}
