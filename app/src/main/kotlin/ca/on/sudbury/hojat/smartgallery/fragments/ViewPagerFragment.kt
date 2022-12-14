package ca.on.sudbury.hojat.smartgallery.fragments

import android.provider.MediaStore
import android.provider.MediaStore.Files
import android.provider.MediaStore.Images
import android.view.MotionEvent
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.extensions.getDoesFilePathExist
import ca.on.sudbury.hojat.smartgallery.extensions.getResolution
import ca.on.sudbury.hojat.smartgallery.extensions.getOTGPublicPath
import ca.on.sudbury.hojat.smartgallery.extensions.getLongValue
import ca.on.sudbury.hojat.smartgallery.extensions.formatDate
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.helpers.ExtendedDetails
import ca.on.sudbury.hojat.smartgallery.helpers.MAX_CLOSE_DOWN_GESTURE_DURATION
import ca.on.sudbury.hojat.smartgallery.models.Medium
import ca.on.sudbury.hojat.smartgallery.usecases.FormatFileSizeUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.GetCameraModelUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.GetDateTakenUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.GetGeneralPropertiesUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.GetMegaPixelUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsPathOnOtgUseCase
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
        if (detailsFlag and ExtendedDetails.Name.id != 0) {
            medium.name.let { if (it.isNotEmpty()) details.appendLine(it) }
        }

        if (detailsFlag and ExtendedDetails.Path.id != 0) {
            path.let { if (it.isNotEmpty()) details.appendLine(it) }
        }

        if (detailsFlag and ExtendedDetails.Size.id != 0) {
            FormatFileSizeUseCase(file.length()).let { if (it.isNotEmpty()) details.appendLine(it) }
        }

        if (detailsFlag and ExtendedDetails.Resolution.id != 0) {
            with(requireContext().getResolution(file.absolutePath)) {
                "${this?.x} x ${this?.y} ${GetMegaPixelUseCase(this)}".let {
                    if (it.isNotEmpty()) details.appendLine(
                        it
                    )
                }
            }
        }

        if (detailsFlag and ExtendedDetails.LastModified.id != 0) {
            getFileLastModified(file).let { if (it.isNotEmpty()) details.appendLine(it) }
        }

        if (detailsFlag and ExtendedDetails.DateTaken.id != 0) {
            GetDateTakenUseCase(requireContext(), exif)
                .let { if (it.isNotEmpty()) details.appendLine(it) }
        }

        if (detailsFlag and ExtendedDetails.CameraModel.id != 0) {
            GetCameraModelUseCase(exif).let { if (it.isNotEmpty()) details.appendLine(it) }
        }

        if (detailsFlag and ExtendedDetails.ExifProperties.id != 0) {
            GetGeneralPropertiesUseCase(exif).let { if (it.isNotEmpty()) details.appendLine(it) }
        }

        if (detailsFlag and ExtendedDetails.Gps.id != 0) {
            getLatLonAltitude(medium.path).let { if (it.isNotEmpty()) details.appendLine(it) }
        }
        return details.toString().trim()
    }

    fun getPathToLoad(medium: Medium) =
        if (IsPathOnOtgUseCase(context, medium.path))
            medium.path.getOTGPublicPath(requireContext()) else medium.path

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
                mTouchDownX = event.x
                mTouchDownY = event.y
            }
            MotionEvent.ACTION_POINTER_DOWN -> mIgnoreCloseDown = true
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val diffX = mTouchDownX - event.x
                val diffY = mTouchDownY - event.y

                val downGestureDuration = System.currentTimeMillis() - mTouchDownTime
                if (!mIgnoreCloseDown && abs(diffY) > abs(diffX) && diffY < -mCloseDownThreshold && downGestureDuration < MAX_CLOSE_DOWN_GESTURE_DURATION && context?.config?.allowDownGesture == true) {
                    activity?.finish()
                    activity?.overridePendingTransition(0, R.anim.slide_down)
                }
                mIgnoreCloseDown = false
            }
        }
    }
}
