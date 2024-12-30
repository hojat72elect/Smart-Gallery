package ca.hojat.smart.gallery.shared.extensions

import android.graphics.Point


fun Point.formatAsResolution() = "$x x $y ${getMPx()}"

private fun Point.getMPx(): String {
    val px = x * y / 1000000f
    val rounded = Math.round(px * 10) / 10f
    return "(${rounded}MP)"
}
