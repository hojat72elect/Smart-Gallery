package ca.on.sudbury.hojat.smartgallery.base

import android.database.ContentObserver
import android.net.Uri
import android.provider.MediaStore.Images
import android.provider.MediaStore.Video
import android.view.WindowManager
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.dialogs.FilePickerDialogFragment
import ca.on.sudbury.hojat.smartgallery.extensions.getParentPath
import ca.on.sudbury.hojat.smartgallery.extensions.getRealPathFromURI
import ca.on.sudbury.hojat.smartgallery.extensions.scanPathRecursively
import ca.on.sudbury.hojat.smartgallery.extensions.addPathToDB
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.extensions.updateDirectoryPath
import ca.on.sudbury.hojat.smartgallery.usecases.IsPiePlusUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.RunOnBackgroundThreadUseCase

/**
 * This is the base Activity used for all our activities.
 */
open class SimpleActivity : BaseSimpleActivity() {
    private val observer = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)
            if (uri != null) {
                val path = getRealPathFromURI(uri)
                if (path != null) {
                    updateDirectoryPath(path.getParentPath())
                    addPathToDB(path)
                }
            }
        }
    }

    override fun getAppIconIDs() = arrayListOf(
        R.mipmap.ic_launcher_red,
        R.mipmap.ic_launcher_pink,
        R.mipmap.ic_launcher_purple,
        R.mipmap.ic_launcher_deep_purple,
        R.mipmap.ic_launcher_indigo,
        R.mipmap.ic_launcher_blue,
        R.mipmap.ic_launcher_light_blue,
        R.mipmap.ic_launcher_cyan,
        R.mipmap.ic_launcher_teal,
        R.mipmap.ic_launcher_green,
        R.mipmap.ic_launcher_light_green,
        R.mipmap.ic_launcher_lime,
        R.mipmap.ic_launcher_yellow,
        R.mipmap.ic_launcher_amber,
        R.mipmap.ic_launcher,
        R.mipmap.ic_launcher_deep_orange,
        R.mipmap.ic_launcher_brown,
        R.mipmap.ic_launcher_blue_grey,
        R.mipmap.ic_launcher_grey_black
    )

    override fun getAppLauncherName() = getString(R.string.app_launcher_name)

    protected fun checkNotchSupport() {
        if (IsPiePlusUseCase()) {
            val cutoutMode = when {
                config.showNotch -> WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                else -> WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
            }

            window.attributes.layoutInDisplayCutoutMode = cutoutMode
            if (config.showNotch) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            }
        }
    }

    protected fun registerFileUpdateListener() {
        contentResolver.registerContentObserver(
            Images.Media.EXTERNAL_CONTENT_URI,
            true,
            observer
        )
        contentResolver.registerContentObserver(
            Video.Media.EXTERNAL_CONTENT_URI,
            true,
            observer
        )
    }

    protected fun unregisterFileUpdateListener() {
        contentResolver.unregisterContentObserver(observer)
    }

    protected fun showAddIncludedFolderDialog(callback: () -> Unit) {
        val callbackAfterDialogConfirmed: (String) -> Unit = { pickedPath ->
            config.lastFilepickerPath = pickedPath
            config.addIncludedFolder(pickedPath)
            callback()
            RunOnBackgroundThreadUseCase {
                scanPathRecursively(pickedPath)
            }
        }
        FilePickerDialogFragment(
            currPath = config.lastFilepickerPath,
            pickFile = false,
            showHidden = config.shouldShowHidden,
            showFAB = false,
            canAddShowHiddenButton = true,
            callback = callbackAfterDialogConfirmed
        ).show(supportFragmentManager, FilePickerDialogFragment.TAG)
    }
}
