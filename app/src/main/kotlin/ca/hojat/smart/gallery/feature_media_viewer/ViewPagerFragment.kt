package ca.hojat.smart.gallery.feature_media_viewer

import android.provider.MediaStore
import android.provider.MediaStore.Files
import android.provider.MediaStore.Images
import android.view.MotionEvent
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.shared.extensions.config
import ca.hojat.smart.gallery.shared.extensions.formatAsResolution
import ca.hojat.smart.gallery.shared.extensions.formatDate
import ca.hojat.smart.gallery.shared.extensions.formatSize
import ca.hojat.smart.gallery.shared.extensions.getDoesFilePathExist
import ca.hojat.smart.gallery.shared.extensions.getExifCameraModel
import ca.hojat.smart.gallery.shared.extensions.getExifDateTaken
import ca.hojat.smart.gallery.shared.extensions.getExifProperties
import ca.hojat.smart.gallery.shared.extensions.getLongValue
import ca.hojat.smart.gallery.shared.extensions.getOTGPublicPath
import ca.hojat.smart.gallery.shared.extensions.getResolution
import ca.hojat.smart.gallery.shared.extensions.isPathOnOTG
import ca.hojat.smart.gallery.shared.helpers.EXT_CAMERA_MODEL
import ca.hojat.smart.gallery.shared.helpers.EXT_DATE_TAKEN
import ca.hojat.smart.gallery.shared.helpers.EXT_EXIF_PROPERTIES
import ca.hojat.smart.gallery.shared.helpers.EXT_GPS
import ca.hojat.smart.gallery.shared.helpers.EXT_LAST_MODIFIED
import ca.hojat.smart.gallery.shared.helpers.EXT_NAME
import ca.hojat.smart.gallery.shared.helpers.EXT_PATH
import ca.hojat.smart.gallery.shared.helpers.EXT_RESOLUTION
import ca.hojat.smart.gallery.shared.helpers.EXT_SIZE
import ca.hojat.smart.gallery.shared.helpers.MAX_CLOSE_DOWN_GESTURE_DURATION
import ca.hojat.smart.gallery.shared.data.domain.Medium
import java.io.File
import kotlin.math.abs

abstract class ViewPagerFragment : Fragment() {
    var listener: FragmentListener? = null

    private var mTouchDownTime = 0L
    private var mTouchDownX = 0f
    private var mTouchDownY = 0f
    private var mCloseDownThreshold = 100f
    private var mIgnoreCloseDown = false

    abstract fun fullscreenToggled(isFullscreen: Boolean)

    interface FragmentListener {
        fun fragmentClicked()

        fun videoEnded(): Boolean

        fun goToPrevItem()

        fun goToNextItem()

        fun launchViewVideoIntent(path: String)

        fun isSlideShowActive(): Boolean
    }

    fun getMediumExtendedDetails(medium: Medium): String {
        val file = File(medium.path)
        if (context?.getDoesFilePathExist(file.absolutePath) == false) {
            return ""
        }

        val path = "${file.parent?.trimEnd('/')}/"
        val exif = try {
            ExifInterface(medium.path)
        } catch (e: Exception) {
            return ""
        }

        val details = StringBuilder()
        val detailsFlag = requireContext().config.extendedDetails
        if (detailsFlag and EXT_NAME != 0) {
            medium.name.let { if (it.isNotEmpty()) details.appendLine(it) }
        }

        if (detailsFlag and EXT_PATH != 0) {
            path.let { if (it.isNotEmpty()) details.appendLine(it) }
        }

        if (detailsFlag and EXT_SIZE != 0) {
            file.length().formatSize().let { if (it.isNotEmpty()) details.appendLine(it) }
        }

        if (detailsFlag and EXT_RESOLUTION != 0) {
            requireContext().getResolution(file.absolutePath)?.formatAsResolution()
                .let { if (it?.isNotEmpty() == true) details.appendLine(it) }
        }

        if (detailsFlag and EXT_LAST_MODIFIED != 0) {
            getFileLastModified(file).let { if (it.isNotEmpty()) details.appendLine(it) }
        }

        if (detailsFlag and EXT_DATE_TAKEN != 0) {
            exif.getExifDateTaken(requireContext())
                .let { if (it.isNotEmpty()) details.appendLine(it) }
        }

        if (detailsFlag and EXT_CAMERA_MODEL != 0) {
            exif.getExifCameraModel().let { if (it.isNotEmpty()) details.appendLine(it) }
        }

        if (detailsFlag and EXT_EXIF_PROPERTIES != 0) {
            exif.getExifProperties().let { if (it.isNotEmpty()) details.appendLine(it) }
        }

        if (detailsFlag and EXT_GPS != 0) {
            getLatLonAltitude(medium.path).let { if (it.isNotEmpty()) details.appendLine(it) }
        }
        return details.toString().trim()
    }

    fun getPathToLoad(medium: Medium) =
        if (context?.isPathOnOTG(medium.path) == true) medium.path.getOTGPublicPath(requireContext()) else medium.path

    private fun getFileLastModified(file: File): String {
        val projection = arrayOf(Images.Media.DATE_MODIFIED)
        val uri = Files.getContentUri("external")
        val selection = "${MediaStore.MediaColumns.DATA} = ?"
        val selectionArgs = arrayOf(file.absolutePath)
        val cursor =
            requireContext().contentResolver.query(uri, projection, selection, selectionArgs, null)
        cursor?.use {
            return if (cursor.moveToFirst()) {
                val dateModified = cursor.getLongValue(Images.Media.DATE_MODIFIED) * 1000L
                dateModified.formatDate(requireContext())
            } else {
                file.lastModified().formatDate(requireContext())
            }
        }
        return ""
    }

    private fun getLatLonAltitude(path: String): String {
        var result = ""
        val exif = try {
            ExifInterface(path)
        } catch (e: Exception) {
            return ""
        }

        val latLon = FloatArray(2)

        if (exif.getLatLong(latLon)) {
            result = "${latLon[0]},  ${latLon[1]}"
        }

        val altitude = exif.getAltitude(0.0)
        if (altitude != 0.0) {
            result += ",  ${altitude}m"
        }

        return result.trimStart(',').trim()
    }

    protected fun handleEvent(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mTouchDownTime = System.currentTimeMillis()
                mTouchDownX = event.rawX
                mTouchDownY = event.rawY
            }

            MotionEvent.ACTION_POINTER_DOWN -> mIgnoreCloseDown = true
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val diffX = mTouchDownX - event.rawX
                val diffY = mTouchDownY - event.rawY

                val downGestureDuration = System.currentTimeMillis() - mTouchDownTime
                if (!mIgnoreCloseDown && (abs(diffY) > abs(diffX)) && (diffY < -mCloseDownThreshold) && downGestureDuration < MAX_CLOSE_DOWN_GESTURE_DURATION && context?.config?.allowDownGesture == true) {
                    activity?.finish()
                    activity?.overridePendingTransition(
                        0,
                        R.anim.slide_down
                    )
                }
                mIgnoreCloseDown = false
            }
        }
    }
}
