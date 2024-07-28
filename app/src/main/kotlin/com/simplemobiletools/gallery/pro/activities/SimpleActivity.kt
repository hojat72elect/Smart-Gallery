package com.simplemobiletools.gallery.pro.activities

import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.provider.MediaStore.Images
import android.provider.MediaStore.Video
import android.view.WindowManager
import androidx.annotation.RequiresApi
import com.simplemobiletools.gallery.pro.BuildConfig
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.dialogs.FilePickerDialog
import com.simplemobiletools.gallery.pro.extensions.addPathToDB
import com.simplemobiletools.gallery.pro.extensions.config
import com.simplemobiletools.gallery.pro.extensions.getParentPath
import com.simplemobiletools.gallery.pro.extensions.getRealPathFromURI
import com.simplemobiletools.gallery.pro.extensions.hideKeyboard
import com.simplemobiletools.gallery.pro.extensions.isExternalStorageManager
import com.simplemobiletools.gallery.pro.extensions.scanPathRecursively
import com.simplemobiletools.gallery.pro.extensions.updateDirectoryPath
import com.simplemobiletools.gallery.pro.helpers.LICENSE_APNG
import com.simplemobiletools.gallery.pro.helpers.LICENSE_CROPPER
import com.simplemobiletools.gallery.pro.helpers.LICENSE_EXOPLAYER
import com.simplemobiletools.gallery.pro.helpers.LICENSE_FILTERS
import com.simplemobiletools.gallery.pro.helpers.LICENSE_GESTURE_VIEWS
import com.simplemobiletools.gallery.pro.helpers.LICENSE_GIF_DRAWABLE
import com.simplemobiletools.gallery.pro.helpers.LICENSE_GLIDE
import com.simplemobiletools.gallery.pro.helpers.LICENSE_PANORAMA_VIEW
import com.simplemobiletools.gallery.pro.helpers.LICENSE_PATTERN
import com.simplemobiletools.gallery.pro.helpers.LICENSE_PICASSO
import com.simplemobiletools.gallery.pro.helpers.LICENSE_REPRINT
import com.simplemobiletools.gallery.pro.helpers.LICENSE_RTL
import com.simplemobiletools.gallery.pro.helpers.LICENSE_SANSELAN
import com.simplemobiletools.gallery.pro.helpers.LICENSE_SUBSAMPLING
import com.simplemobiletools.gallery.pro.helpers.ensureBackgroundThread
import com.simplemobiletools.gallery.pro.helpers.isPiePlus
import com.simplemobiletools.gallery.pro.helpers.isRPlus
import com.simplemobiletools.gallery.pro.models.FAQItem

@RequiresApi(Build.VERSION_CODES.O)
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
        if (isPiePlus()) {
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
        try {
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
        } catch (ignored: Exception) {
        }
    }

    protected fun unregisterFileUpdateListener() {
        try {
            contentResolver.unregisterContentObserver(observer)
        } catch (ignored: Exception) {
        }
    }

    protected fun showAddIncludedFolderDialog(callback: () -> Unit) {
        FilePickerDialog(
            activity = this,
            currPath = config.lastFilePickerPath,
            pickFile = false,
            showHidden = config.shouldShowHidden,
            showFAB = false,
            canAddShowHiddenButton = true
        ) {
            config.lastFilePickerPath = it
            config.addIncludedFolder(it)
            callback()
            ensureBackgroundThread {
                scanPathRecursively(it)
            }
        }
    }

    fun launchSettings() {
        hideKeyboard()
        startActivity(Intent(applicationContext, SettingsActivity::class.java))
    }

    fun launchAbout() {
        val licenses =
            LICENSE_GLIDE or LICENSE_CROPPER or LICENSE_RTL or LICENSE_SUBSAMPLING or LICENSE_PATTERN or LICENSE_REPRINT or LICENSE_GIF_DRAWABLE or
                    LICENSE_PICASSO or LICENSE_EXOPLAYER or LICENSE_PANORAMA_VIEW or LICENSE_SANSELAN or LICENSE_FILTERS or LICENSE_GESTURE_VIEWS or LICENSE_APNG

        val faqItems = arrayListOf(
            FAQItem(R.string.faq_3_title, R.string.faq_3_text),
            FAQItem(R.string.faq_12_title, R.string.faq_12_text),
            FAQItem(R.string.faq_7_title, R.string.faq_7_text),
            FAQItem(R.string.faq_14_title, R.string.faq_14_text),
            FAQItem(R.string.faq_1_title, R.string.faq_1_text),
            FAQItem(
                R.string.faq_5_title_commons,
                R.string.faq_5_text_commons
            ),
            FAQItem(R.string.faq_5_title, R.string.faq_5_text),
            FAQItem(R.string.faq_4_title, R.string.faq_4_text),
            FAQItem(R.string.faq_6_title, R.string.faq_6_text),
            FAQItem(R.string.faq_8_title, R.string.faq_8_text),
            FAQItem(R.string.faq_10_title, R.string.faq_10_text),
            FAQItem(R.string.faq_11_title, R.string.faq_11_text),
            FAQItem(R.string.faq_13_title, R.string.faq_13_text),
            FAQItem(R.string.faq_15_title, R.string.faq_15_text),
            FAQItem(R.string.faq_2_title, R.string.faq_2_text),
            FAQItem(R.string.faq_18_title, R.string.faq_18_text),
            FAQItem(
                R.string.faq_9_title_commons,
                R.string.faq_9_text_commons
            ),
        )

        if (!resources.getBoolean(R.bool.hide_google_relations)) {
            faqItems.add(
                FAQItem(
                    R.string.faq_2_title_commons,
                    R.string.faq_2_text_commons
                )
            )
            faqItems.add(
                FAQItem(
                    R.string.faq_6_title_commons,
                    R.string.faq_6_text_commons
                )
            )
            faqItems.add(
                FAQItem(
                    R.string.faq_7_title_commons,
                    R.string.faq_7_text_commons
                )
            )
            faqItems.add(
                FAQItem(
                    R.string.faq_10_title_commons,
                    R.string.faq_10_text_commons
                )
            )
        }

        if (isRPlus() && !isExternalStorageManager()) {
            faqItems.add(
                0,
                FAQItem(
                    R.string.faq_16_title,
                    "${getString(R.string.faq_16_text)} ${getString(R.string.faq_16_text_extra)}"
                )
            )
            faqItems.add(1, FAQItem(R.string.faq_17_title, R.string.faq_17_text))
            faqItems.removeIf { it.text == R.string.faq_7_text }
            faqItems.removeIf { it.text == R.string.faq_14_text }
            faqItems.removeIf { it.text == R.string.faq_8_text }
        }

        startAboutActivity(R.string.app_name, licenses, BuildConfig.VERSION_NAME, faqItems, true)
    }

}
