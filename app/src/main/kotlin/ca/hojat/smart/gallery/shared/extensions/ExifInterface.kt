package ca.hojat.smart.gallery.shared.extensions

import android.content.Context
import androidx.exifinterface.media.ExifInterface
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.text.SimpleDateFormat
import java.util.Locale

fun ExifInterface.copyNonDimensionAttributesTo(destination: ExifInterface) {
    val attributes = ExifInterfaceAttributes.AllNonDimensionAttributes

    attributes.forEach {
        val value = getAttribute(it)
        if (value != null) {
            destination.setAttribute(it, value)
        }
    }

    try {
        destination.saveAttributes()
    } catch (ignored: Exception) {
    }
}

private class ExifInterfaceAttributes {
    companion object {
        val AllNonDimensionAttributes = getAllNonDimensionExifAttributes()

        private fun getAllNonDimensionExifAttributes(): List<String> {
            val tagFields = ExifInterface::class.java.fields.filter { field -> isExif(field) }

            val excludeAttributes = arrayListOf(
                ExifInterface.TAG_IMAGE_LENGTH,
                ExifInterface.TAG_IMAGE_WIDTH,
                ExifInterface.TAG_PIXEL_X_DIMENSION,
                ExifInterface.TAG_PIXEL_Y_DIMENSION,
                ExifInterface.TAG_THUMBNAIL_IMAGE_LENGTH,
                ExifInterface.TAG_THUMBNAIL_IMAGE_WIDTH,
                ExifInterface.TAG_ORIENTATION
            )

            return tagFields
                .map { tagField -> tagField.get(null) as String }
                .filter { x -> !excludeAttributes.contains(x) }
                .distinct()
        }

        private fun isExif(field: Field): Boolean {
            return field.type == String::class.java &&
                    isPublicStaticFinal(field.modifiers) &&
                    field.name.startsWith("TAG_")
        }

        private const val PUBLIC_STATIC_FINAL = Modifier.PUBLIC or Modifier.STATIC or Modifier.FINAL

        private fun isPublicStaticFinal(modifiers: Int): Boolean {
            return modifiers and PUBLIC_STATIC_FINAL > 0
        }
    }
}

fun ExifInterface.getExifCameraModel(): String {
    getAttribute(ExifInterface.TAG_MAKE).let {
        if (it?.isNotEmpty() == true) {
            val model = getAttribute(ExifInterface.TAG_MODEL)
            return "$it $model".trim()
        }
    }
    return ""
}

fun ExifInterface.removeValues() {
    val attributes = arrayListOf(
        // ExifInterface.TAG_ORIENTATION,   // do not remove the orientation, it could lead to unexpected behaviour at displaying the file
        ExifInterface.TAG_APERTURE_VALUE,
        ExifInterface.TAG_DATETIME,
        ExifInterface.TAG_DATETIME_DIGITIZED,
        ExifInterface.TAG_DATETIME_ORIGINAL,
        ExifInterface.TAG_EXPOSURE_TIME,
        ExifInterface.TAG_FLASH,
        ExifInterface.TAG_F_NUMBER,
        ExifInterface.TAG_FOCAL_LENGTH,
        ExifInterface.TAG_GPS_ALTITUDE,
        ExifInterface.TAG_GPS_ALTITUDE_REF,
        ExifInterface.TAG_GPS_DATESTAMP,
        ExifInterface.TAG_GPS_LATITUDE,
        ExifInterface.TAG_GPS_LATITUDE_REF,
        ExifInterface.TAG_GPS_LONGITUDE,
        ExifInterface.TAG_GPS_LONGITUDE_REF,
        ExifInterface.TAG_GPS_PROCESSING_METHOD,
        ExifInterface.TAG_GPS_TIMESTAMP,
        ExifInterface.TAG_IMAGE_LENGTH,
        ExifInterface.TAG_IMAGE_WIDTH,
        ExifInterface.TAG_ISO_SPEED_RATINGS,
        ExifInterface.TAG_MAKE,
        ExifInterface.TAG_MODEL,
        ExifInterface.TAG_WHITE_BALANCE
    )

    attributes.forEach {
        setAttribute(it, null)
    }

    saveAttributes()
}

fun ExifInterface.getExifProperties(): String {
    var exifString = ""
    getAttribute(ExifInterface.TAG_F_NUMBER).let {
        if (it?.isNotEmpty() == true) {
            val number = it.trimEnd('0').trimEnd('.')
            exifString += "F/$number  "
        }
    }

    getAttribute(ExifInterface.TAG_FOCAL_LENGTH).let {
        if (it?.isNotEmpty() == true) {
            val values = it.split('/')
            val focalLength = "${values[0].toDouble() / values[1].toDouble()}mm"
            exifString += "$focalLength  "
        }
    }

    getAttribute(ExifInterface.TAG_EXPOSURE_TIME).let {
        if (it?.isNotEmpty() == true) {
            val exposureValue = it.toFloat()
            exifString += if (exposureValue > 1f) {
                "${exposureValue}s  "
            } else {
                "1/${Math.round(1 / exposureValue)}s  "
            }
        }
    }

    getAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS).let {
        if (it?.isNotEmpty() == true) {
            exifString += "ISO-$it"
        }
    }

    return exifString.trim()
}

fun ExifInterface.getExifDateTaken(context: Context): String {
    val dateTime = getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
        ?: getAttribute(ExifInterface.TAG_DATETIME)
    dateTime.let {
        if (it?.isNotEmpty() == true) {
            try {
                val simpleDateFormat = SimpleDateFormat("yyyy:MM:dd kk:mm:ss", Locale.ENGLISH)
                return simpleDateFormat.parse(it)!!.time.formatDate(context).trim()
            } catch (ignored: Exception) {
            }
        }
    }
    return ""
}
