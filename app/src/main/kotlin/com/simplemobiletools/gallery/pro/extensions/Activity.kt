package com.simplemobiletools.gallery.pro.extensions

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.ContentProviderOperation
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.TransactionTooLargeException
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.MediaStore.Files
import android.provider.MediaStore.Images
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.biometric.auth.AuthPromptCallback
import androidx.biometric.auth.AuthPromptHost
import androidx.biometric.auth.Class2BiometricAuthPrompt
import androidx.core.view.WindowInsetsCompat
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.util.UnstableApi
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.simplemobiletools.gallery.pro.BuildConfig
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.activities.BaseSimpleActivity
import com.simplemobiletools.gallery.pro.activities.MediaActivity
import com.simplemobiletools.gallery.pro.activities.SettingsActivity
import com.simplemobiletools.gallery.pro.activities.SimpleActivity
import com.simplemobiletools.gallery.pro.compose.extensions.DEVELOPER_PLAY_STORE_URL
import com.simplemobiletools.gallery.pro.databinding.DialogTitleBinding
import com.simplemobiletools.gallery.pro.dialogs.AllFilesPermissionDialog
import com.simplemobiletools.gallery.pro.dialogs.AppSideloadedDialog
import com.simplemobiletools.gallery.pro.dialogs.ConfirmationAdvancedDialog
import com.simplemobiletools.gallery.pro.dialogs.ConfirmationDialog
import com.simplemobiletools.gallery.pro.dialogs.DonateDialog
import com.simplemobiletools.gallery.pro.dialogs.PickDirectoryDialog
import com.simplemobiletools.gallery.pro.dialogs.RateStarsDialog
import com.simplemobiletools.gallery.pro.dialogs.ResizeMultipleImagesDialog
import com.simplemobiletools.gallery.pro.dialogs.ResizeWithPathDialog
import com.simplemobiletools.gallery.pro.dialogs.SecurityDialog
import com.simplemobiletools.gallery.pro.dialogs.UpgradeToProDialog
import com.simplemobiletools.gallery.pro.dialogs.WhatsNewDialog
import com.simplemobiletools.gallery.pro.dialogs.WritePermissionDialog
import com.simplemobiletools.gallery.pro.helpers.CREATE_DOCUMENT_SDK_30
import com.simplemobiletools.gallery.pro.helpers.DARK_GREY
import com.simplemobiletools.gallery.pro.helpers.DIRECTORY
import com.simplemobiletools.gallery.pro.helpers.EXTRA_SHOW_ADVANCED
import com.simplemobiletools.gallery.pro.helpers.IS_FROM_GALLERY
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
import com.simplemobiletools.gallery.pro.helpers.MyContentProvider
import com.simplemobiletools.gallery.pro.helpers.NOMEDIA
import com.simplemobiletools.gallery.pro.helpers.OPEN_DOCUMENT_TREE_FOR_ANDROID_DATA_OR_OBB
import com.simplemobiletools.gallery.pro.helpers.OPEN_DOCUMENT_TREE_FOR_SDK_30
import com.simplemobiletools.gallery.pro.helpers.OPEN_DOCUMENT_TREE_OTG
import com.simplemobiletools.gallery.pro.helpers.OPEN_DOCUMENT_TREE_SD
import com.simplemobiletools.gallery.pro.helpers.PROTECTION_FINGERPRINT
import com.simplemobiletools.gallery.pro.helpers.REAL_FILE_PATH
import com.simplemobiletools.gallery.pro.helpers.RECYCLE_BIN
import com.simplemobiletools.gallery.pro.helpers.REQUEST_EDIT_IMAGE
import com.simplemobiletools.gallery.pro.helpers.REQUEST_SET_AS
import com.simplemobiletools.gallery.pro.helpers.SIDELOADING_FALSE
import com.simplemobiletools.gallery.pro.helpers.SIDELOADING_TRUE
import com.simplemobiletools.gallery.pro.helpers.ensureBackgroundThread
import com.simplemobiletools.gallery.pro.helpers.isNougatPlus
import com.simplemobiletools.gallery.pro.helpers.isOnMainThread
import com.simplemobiletools.gallery.pro.helpers.isRPlus
import com.simplemobiletools.gallery.pro.helpers.isSPlus
import com.simplemobiletools.gallery.pro.models.Android30RenameFormat
import com.simplemobiletools.gallery.pro.models.DateTaken
import com.simplemobiletools.gallery.pro.models.FAQItem
import com.simplemobiletools.gallery.pro.models.FileDirItem
import com.simplemobiletools.gallery.pro.models.Release
import com.simplemobiletools.gallery.pro.models.SharedTheme
import com.simplemobiletools.gallery.pro.views.MyTextView
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale

fun Activity.sharePath(path: String) {
    sharePathIntent(path, BuildConfig.APPLICATION_ID)
}

fun Activity.sharePaths(paths: ArrayList<String>) {
    sharePathsIntent(paths, BuildConfig.APPLICATION_ID)
}

fun Activity.shareMediumPath(path: String) {
    sharePath(path)
}

fun Activity.shareMediaPaths(paths: ArrayList<String>) {
    sharePaths(paths)
}

fun Activity.setAs(path: String) {
    setAsIntent(path, BuildConfig.APPLICATION_ID)
}

fun Activity.openPath(
    path: String,
    forceChooser: Boolean,
    extras: HashMap<String, Boolean> = HashMap()
) {
    openPathIntent(path, forceChooser, BuildConfig.APPLICATION_ID, extras = extras)
}

fun Activity.openEditor(path: String, forceChooser: Boolean = false) {
    val newPath = path.removePrefix("file://")
    openEditorIntent(newPath, forceChooser, BuildConfig.APPLICATION_ID)
}

fun Activity.launchCamera() {
    val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
    launchActivityIntent(intent)
}

fun SimpleActivity.launchSettings() {
    hideKeyboard()
    startActivity(Intent(applicationContext, SettingsActivity::class.java))
}

@RequiresApi(Build.VERSION_CODES.O)
fun SimpleActivity.launchAbout() {
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

fun BaseSimpleActivity.handleMediaManagementPrompt(callback: () -> Unit) {
    if (canManageMedia() || isExternalStorageManager()) {
        callback()
    } else if (isRPlus() && resources.getBoolean(R.bool.require_all_files_access) && !config.avoidShowingAllFilesPrompt) {
        if (Environment.isExternalStorageManager()) {
            callback()
        } else {
            var messagePrompt =
                getString(R.string.access_storage_prompt)
            messagePrompt += if (isSPlus()) {
                "\n\n${getString(R.string.media_management_alternative)}"
            } else {
                "\n\n${getString(R.string.alternative_media_access)}"
            }

            AllFilesPermissionDialog(this, messagePrompt, callback = { success ->
                if (success) {
                    launchGrantAllFilesIntent()
                }
            }, neutralPressed = {
                if (isSPlus()) {
                    launchMediaManagementIntent(callback)
                } else {
                    config.avoidShowingAllFilesPrompt = true
                }
            })
        }
    } else {
        callback()
    }
}

@RequiresApi(Build.VERSION_CODES.R)
fun BaseSimpleActivity.launchGrantAllFilesIntent() {
    try {
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        intent.addCategory("android.intent.category.DEFAULT")
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    } catch (e: Exception) {
        val intent = Intent()
        intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
        try {
            startActivity(intent)
        } catch (e: Exception) {
            showErrorToast(e)
        }
    }
}

fun AppCompatActivity.showSystemUI() {
    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
}

fun AppCompatActivity.hideSystemUI() {
    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_IMMERSIVE
}

@RequiresApi(Build.VERSION_CODES.O)
fun BaseSimpleActivity.addNoMedia(path: String, callback: () -> Unit) {
    val file = File(path, NOMEDIA)
    if (getDoesFilePathExist(file.absolutePath)) {
        callback()
        return
    }

    if (needsStupidWritePermissions(path)) {
        handleSAFDialog(file.absolutePath) {
            if (!it) {
                return@handleSAFDialog
            }

            val fileDocument = getDocumentFile(path)
            if (fileDocument?.exists() == true && fileDocument.isDirectory) {
                fileDocument.createFile("", NOMEDIA)
                addNoMediaIntoMediaStore(file.absolutePath)
                callback()
            } else {
                toast(R.string.unknown_error_occurred)
                callback()
            }
        }
    } else {
        try {
            if (file.createNewFile()) {
                ensureBackgroundThread {
                    addNoMediaIntoMediaStore(file.absolutePath)
                }
            } else {
                toast(R.string.unknown_error_occurred)
            }
        } catch (e: Exception) {
            showErrorToast(e)
        }
        callback()
    }
}

fun BaseSimpleActivity.addNoMediaIntoMediaStore(path: String) {
    try {
        val content = ContentValues().apply {
            put(Files.FileColumns.TITLE, NOMEDIA)
            put(Files.FileColumns.DATA, path)
            put(Files.FileColumns.MEDIA_TYPE, Files.FileColumns.MEDIA_TYPE_NONE)
        }
        contentResolver.insert(Files.getContentUri("external"), content)
    } catch (e: Exception) {
        showErrorToast(e)
    }
}

fun BaseSimpleActivity.removeNoMedia(path: String, callback: (() -> Unit)? = null) {
    val file = File(path, NOMEDIA)
    if (!getDoesFilePathExist(file.absolutePath)) {
        callback?.invoke()
        return
    }

    tryDeleteFileDirItem(
        fileDirItem = file.toFileDirItem(applicationContext),
        allowDeleteFolder = false,
        deleteFromDatabase = false
    ) {
        callback?.invoke()
        deleteFromMediaStore(file.absolutePath) { needsRescan ->
            if (needsRescan) {
                rescanAndDeletePath(path) {
                    rescanFolderMedia(path)
                }
            } else {
                rescanFolderMedia(path)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun BaseSimpleActivity.toggleFileVisibility(
    oldPath: String,
    hide: Boolean,
    callback: ((newPath: String) -> Unit)? = null
) {
    val path = oldPath.getParentPath()
    var filename = oldPath.getFilenameFromPath()
    if ((hide && filename.startsWith('.')) || (!hide && !filename.startsWith('.'))) {
        callback?.invoke(oldPath)
        return
    }

    filename = if (hide) {
        ".${filename.trimStart('.')}"
    } else {
        filename.substring(1, filename.length)
    }

    val newPath = "$path/$filename"
    renameFile(oldPath, newPath, false) { _, _ ->
        runOnUiThread {
            callback?.invoke(newPath)
        }

        ensureBackgroundThread {
            updateDBMediaPath(oldPath, newPath)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@UnstableApi
fun BaseSimpleActivity.tryCopyMoveFilesTo(
    fileDirItems: ArrayList<FileDirItem>,
    isCopyOperation: Boolean,
    callback: (destinationPath: String) -> Unit
) {
    if (fileDirItems.isEmpty()) {
        toast(R.string.unknown_error_occurred)
        return
    }

    val source = fileDirItems[0].getParentPath()
    PickDirectoryDialog(this, source, true, false, true, false) {
        val destination = it
        handleSAFDialog(source) {
            if (it) {
                copyMoveFilesTo(
                    fileDirItems,
                    source.trimEnd('/'),
                    destination,
                    isCopyOperation,
                    config.shouldShowHidden,
                    callback
                )
            }
        }
    }
}

fun BaseSimpleActivity.tryDeleteFileDirItem(
    fileDirItem: FileDirItem, allowDeleteFolder: Boolean = false, deleteFromDatabase: Boolean,
    callback: ((wasSuccess: Boolean) -> Unit)? = null
) {
    deleteFile(fileDirItem, allowDeleteFolder, isDeletingMultipleFiles = false) {
        if (deleteFromDatabase) {
            ensureBackgroundThread {
                deleteDBPath(fileDirItem.path)
                runOnUiThread {
                    callback?.invoke(it)
                }
            }
        } else {
            callback?.invoke(it)
        }
    }
}

fun BaseSimpleActivity.movePathsInRecycleBin(
    paths: ArrayList<String>,
    callback: ((wasSuccess: Boolean) -> Unit)?
) {
    ensureBackgroundThread {
        var pathsCnt = paths.size
        val OTGPath = config.OTGPath

        for (source in paths) {
            if (OTGPath.isNotEmpty() && source.startsWith(OTGPath)) {
                var inputStream: InputStream? = null
                var out: OutputStream? = null
                try {
                    val destination = "$recycleBinPath/$source"
                    val fileDocument = getSomeDocumentFile(source)
                    inputStream =
                        applicationContext.contentResolver.openInputStream(fileDocument?.uri!!)
                    out = getFileOutputStreamSync(destination, source.getMimeType())

                    var copiedSize = 0L
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var bytes = inputStream!!.read(buffer)
                    while (bytes >= 0) {
                        out!!.write(buffer, 0, bytes)
                        copiedSize += bytes
                        bytes = inputStream.read(buffer)
                    }

                    out?.flush()

                    if (fileDocument.getItemSize(true) == copiedSize && getDoesFilePathExist(
                            destination
                        )
                    ) {
                        mediaDB.updateDeleted(
                            "$RECYCLE_BIN$source",
                            System.currentTimeMillis(),
                            source
                        )
                        pathsCnt--
                    }
                } catch (e: Exception) {
                    showErrorToast(e)
                    return@ensureBackgroundThread
                } finally {
                    inputStream?.close()
                    out?.close()
                }
            } else {
                val file = File(source)
                val internalFile = File(recycleBinPath, source)
                val lastModified = file.lastModified()
                try {
                    if (file.copyRecursively(internalFile, true)) {
                        mediaDB.updateDeleted(
                            "$RECYCLE_BIN$source",
                            System.currentTimeMillis(),
                            source
                        )
                        pathsCnt--

                        if (config.keepLastModified && lastModified != 0L) {
                            internalFile.setLastModified(lastModified)
                        }
                    }
                } catch (e: Exception) {
                    showErrorToast(e)
                    return@ensureBackgroundThread
                }
            }
        }
        callback?.invoke(pathsCnt == 0)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun BaseSimpleActivity.restoreRecycleBinPath(path: String, callback: () -> Unit) {
    restoreRecycleBinPaths(arrayListOf(path), callback)
}

@SuppressLint("UseCompatLoadingForDrawables")
fun Activity.setupDialogStuff(
    view: View,
    dialog: AlertDialog.Builder,
    titleId: Int = 0,
    titleText: String = "",
    cancelOnTouchOutside: Boolean = true,
    callback: ((alertDialog: AlertDialog) -> Unit)? = null
) {
    if (isDestroyed || isFinishing) {
        return
    }

    val textColor = getProperTextColor()
    val backgroundColor = getProperBackgroundColor()
    val primaryColor = getProperPrimaryColor()
    if (view is ViewGroup) {
        updateTextColors(view)
    } else if (view is MyTextView) {
        view.setColors(textColor, primaryColor, backgroundColor)
    }

    if (dialog is MaterialAlertDialogBuilder) {
        dialog.create().apply {
            if (titleId != 0) {
                setTitle(titleId)
            } else if (titleText.isNotEmpty()) {
                setTitle(titleText)
            }

            setView(view)
            setCancelable(cancelOnTouchOutside)
            if (!isFinishing) {
                show()
            }
            getButton(Dialog.BUTTON_POSITIVE)?.setTextColor(primaryColor)
            getButton(Dialog.BUTTON_NEGATIVE)?.setTextColor(primaryColor)
            getButton(Dialog.BUTTON_NEUTRAL)?.setTextColor(primaryColor)
            callback?.invoke(this)
        }
    } else {
        var title: DialogTitleBinding? = null
        if (titleId != 0 || titleText.isNotEmpty()) {
            title = DialogTitleBinding.inflate(layoutInflater, null, false)
            title.dialogTitleTextview.apply {
                if (titleText.isNotEmpty()) {
                    text = titleText
                } else {
                    setText(titleId)
                }
                setTextColor(textColor)
            }
        }

        // if we use the same primary and background color, use the text color for dialog confirmation buttons
        val dialogButtonColor = if (primaryColor == baseConfig.backgroundColor) {
            textColor
        } else {
            primaryColor
        }

        dialog.create().apply {
            setView(view)
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCustomTitle(title?.root)
            setCanceledOnTouchOutside(cancelOnTouchOutside)
            if (!isFinishing) {
                show()
            }
            getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(dialogButtonColor)
            getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(dialogButtonColor)
            getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(dialogButtonColor)

            val bgDrawable = when {
                isBlackAndWhiteTheme() -> resources.getDrawable(
                    R.drawable.black_dialog_background,
                    theme
                )

                baseConfig.isUsingSystemTheme -> resources.getDrawable(
                    R.drawable.dialog_you_background,
                    theme
                )

                else -> resources.getColoredDrawableWithColor(
                    R.drawable.dialog_bg,
                    baseConfig.backgroundColor
                )
            }

            window?.setBackgroundDrawable(bgDrawable)
            callback?.invoke(this)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun BaseSimpleActivity.restoreRecycleBinPaths(paths: ArrayList<String>, callback: () -> Unit) {
    ensureBackgroundThread {
        val newPaths = ArrayList<String>()
        var shownRestoringToPictures = false
        for (source in paths) {
            var destination = source.removePrefix(recycleBinPath)

            val destinationParent = destination.getParentPath()
            if (isRestrictedWithSAFSdk30(destinationParent) && !isInDownloadDir(destinationParent)) {
                // if the file is not writeable on SDK30+, change it to Pictures
                val picturesDirectory = getPicturesDirectoryPath(destination)
                destination = File(picturesDirectory, destination.getFilenameFromPath()).path
                if (!shownRestoringToPictures) {
                    toast(getString(R.string.restore_to_path, humanizePath(picturesDirectory)))
                    shownRestoringToPictures = true
                }
            }

            val lastModified = File(source).lastModified()

            val isShowingSAF = handleSAFDialog(destination) {}
            if (isShowingSAF) {
                return@ensureBackgroundThread
            }

            val isShowingSAFSdk30 = handleSAFDialogSdk30(destination) {}
            if (isShowingSAFSdk30) {
                return@ensureBackgroundThread
            }

            if (getDoesFilePathExist(destination)) {
                val newFile = getAlternativeFile(File(destination))
                destination = newFile.path
            }

            var inputStream: InputStream? = null
            var out: OutputStream? = null
            try {
                out = getFileOutputStreamSync(destination, source.getMimeType())
                inputStream = getFileInputStreamSync(source)

                var copiedSize = 0L
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var bytes = inputStream!!.read(buffer)
                while (bytes >= 0) {
                    out!!.write(buffer, 0, bytes)
                    copiedSize += bytes
                    bytes = inputStream.read(buffer)
                }

                out?.flush()

                if (File(source).length() == copiedSize) {
                    mediaDB.updateDeleted(
                        destination.removePrefix(recycleBinPath),
                        0,
                        "$RECYCLE_BIN${source.removePrefix(recycleBinPath)}"
                    )
                }
                newPaths.add(destination)

                if (config.keepLastModified && lastModified != 0L) {
                    File(destination).setLastModified(lastModified)
                }
            } catch (e: Exception) {
                showErrorToast(e)
            } finally {
                inputStream?.close()
                out?.close()
            }
        }

        runOnUiThread {
            callback()
        }

        rescanPaths(newPaths) {
            fixDateTaken(newPaths, false)
        }
    }
}

fun BaseSimpleActivity.emptyTheRecycleBin(callback: (() -> Unit)? = null) {
    ensureBackgroundThread {
        try {
            recycleBin.deleteRecursively()
            mediaDB.clearRecycleBin()
            directoryDB.deleteRecycleBin()
            toast(R.string.recycle_bin_emptied)
            callback?.invoke()
        } catch (e: Exception) {
            toast(R.string.unknown_error_occurred)
        }
    }
}

fun BaseSimpleActivity.emptyAndDisableTheRecycleBin(callback: () -> Unit) {
    ensureBackgroundThread {
        emptyTheRecycleBin {
            config.useRecycleBin = false
            callback()
        }
    }
}

fun BaseSimpleActivity.showRecycleBinEmptyingDialog(callback: () -> Unit) {
    ConfirmationDialog(
        this,
        "",
        R.string.empty_recycle_bin_confirmation,
        R.string.yes,
        R.string.no
    ) {
        callback()
    }
}

fun BaseSimpleActivity.updateFavoritePaths(
    fileDirItems: ArrayList<FileDirItem>,
    destination: String
) {
    ensureBackgroundThread {
        fileDirItems.forEach {
            val newPath = "$destination/${it.name}"
            updateDBMediaPath(it.path, newPath)
        }
    }
}

fun Activity.hasNavBar(): Boolean {
    val display = windowManager.defaultDisplay

    val realDisplayMetrics = DisplayMetrics()
    display.getRealMetrics(realDisplayMetrics)

    val displayMetrics = DisplayMetrics()
    display.getMetrics(displayMetrics)

    return (realDisplayMetrics.widthPixels - displayMetrics.widthPixels > 0) || (realDisplayMetrics.heightPixels - displayMetrics.heightPixels > 0)
}

fun AppCompatActivity.fixDateTaken(
    paths: ArrayList<String>,
    showToasts: Boolean,
    hasRescanned: Boolean = false,
    callback: (() -> Unit)? = null
) {
    val BATCH_SIZE = 50
    if (showToasts && !hasRescanned) {
        toast(R.string.fixing)
    }

    val pathsToRescan = ArrayList<String>()
    try {
        var didUpdateFile = false
        val operations = ArrayList<ContentProviderOperation>()

        ensureBackgroundThread {
            val dateTakens = ArrayList<DateTaken>()

            for (path in paths) {
                try {
                    val dateTime: String =
                        ExifInterface(path).getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                            ?: ExifInterface(path).getAttribute(ExifInterface.TAG_DATETIME)
                            ?: continue

                    // some formats contain a "T" in the middle, some don't
                    // sample dates: 2015-07-26T14:55:23, 2018:09:05 15:09:05
                    val t = if (dateTime.substring(10, 11) == "T") "\'T\'" else " "
                    val separator = dateTime.substring(4, 5)
                    val format = "yyyy${separator}MM${separator}dd${t}kk:mm:ss"
                    val formatter = SimpleDateFormat(format, Locale.getDefault())
                    val timestamp = formatter.parse(dateTime).time

                    val uri = getFileUri(path)
                    ContentProviderOperation.newUpdate(uri).apply {
                        val selection = "${Images.Media.DATA} = ?"
                        val selectionArgs = arrayOf(path)
                        withSelection(selection, selectionArgs)
                        withValue(Images.Media.DATE_TAKEN, timestamp)
                        operations.add(build())
                    }

                    if (operations.size % BATCH_SIZE == 0) {
                        contentResolver.applyBatch(MediaStore.AUTHORITY, operations)
                        operations.clear()
                    }

                    mediaDB.updateFavoriteDateTaken(path, timestamp)
                    didUpdateFile = true

                    val dateTaken = DateTaken(
                        null,
                        path,
                        path.getFilenameFromPath(),
                        path.getParentPath(),
                        timestamp,
                        (System.currentTimeMillis() / 1000).toInt(),
                        File(path).lastModified()
                    )
                    dateTakens.add(dateTaken)
                    if (!hasRescanned && getFileDateTaken(path) == 0L) {
                        pathsToRescan.add(path)
                    }
                } catch (e: Exception) {
                    continue
                }
            }

            if (!didUpdateFile) {
                if (showToasts) {
                    toast(R.string.no_date_takens_found)
                }

                runOnUiThread {
                    callback?.invoke()
                }
                return@ensureBackgroundThread
            }

            val resultSize = contentResolver.applyBatch(MediaStore.AUTHORITY, operations).size
            if (resultSize == 0) {
                didUpdateFile = false
            }

            if (hasRescanned || pathsToRescan.isEmpty()) {
                if (dateTakens.isNotEmpty()) {
                    dateTakensDB.insertAll(dateTakens)
                }

                runOnUiThread {
                    if (showToasts) {
                        toast(if (didUpdateFile) R.string.dates_fixed_successfully else R.string.unknown_error_occurred)
                    }

                    callback?.invoke()
                }
            } else {
                rescanPaths(pathsToRescan) {
                    fixDateTaken(paths, showToasts, true, callback)
                }
            }
        }
    } catch (e: Exception) {
        if (showToasts) {
            showErrorToast(e)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun BaseSimpleActivity.saveRotatedImageToFile(
    oldPath: String,
    newPath: String,
    degrees: Int,
    showToasts: Boolean,
    callback: () -> Unit
) {
    var newDegrees = degrees
    if (newDegrees < 0) {
        newDegrees += 360
    }

    if (oldPath == newPath && oldPath.isJpg()) {
        if (tryRotateByExif(oldPath, newDegrees, showToasts, callback)) {
            return
        }
    }

    val tmpPath = "$recycleBinPath/.tmp_${newPath.getFilenameFromPath()}"
    val tmpFileDirItem = FileDirItem(tmpPath, tmpPath.getFilenameFromPath())
    try {
        getFileOutputStream(tmpFileDirItem) {
            if (it == null) {
                if (showToasts) {
                    toast(R.string.unknown_error_occurred)
                }
                return@getFileOutputStream
            }

            val oldLastModified = File(oldPath).lastModified()
            if (oldPath.isJpg()) {
                copyFile(oldPath, tmpPath)
                saveExifRotation(ExifInterface(tmpPath), newDegrees)
            } else {
                val inputstream = getFileInputStreamSync(oldPath)
                val bitmap = BitmapFactory.decodeStream(inputstream)
                saveFile(tmpPath, bitmap, it as FileOutputStream, newDegrees)
            }

            copyFile(tmpPath, newPath)
            rescanPaths(arrayListOf(newPath))
            fileRotatedSuccessfully(newPath, oldLastModified)

            it.flush()
            it.close()
            callback.invoke()
        }
    } catch (e: OutOfMemoryError) {
        if (showToasts) {
            toast(R.string.out_of_memory_error)
        }
    } catch (e: Exception) {
        if (showToasts) {
            showErrorToast(e)
        }
    } finally {
        tryDeleteFileDirItem(
            fileDirItem = tmpFileDirItem,
            allowDeleteFolder = false,
            deleteFromDatabase = true
        )
    }
}

@TargetApi(Build.VERSION_CODES.N)
fun Activity.tryRotateByExif(
    path: String,
    degrees: Int,
    showToasts: Boolean,
    callback: () -> Unit
): Boolean {
    return try {
        val file = File(path)
        val oldLastModified = file.lastModified()
        if (saveImageRotation(path, degrees)) {
            fileRotatedSuccessfully(path, oldLastModified)
            callback.invoke()
            if (showToasts) {
                toast(R.string.file_saved)
            }
            true
        } else {
            false
        }
    } catch (e: Exception) {
        // lets not show IOExceptions, rotating is saved just fine even with them
        if (showToasts && e !is IOException) {
            showErrorToast(e)
        }
        false
    }
}

fun Activity.fileRotatedSuccessfully(path: String, lastModified: Long) {
    if (config.keepLastModified && lastModified != 0L) {
        File(path).setLastModified(lastModified)
        updateLastModified(path, lastModified)
    }

    Picasso.get().invalidate(path.getFileKey(lastModified))
    // we cannot refresh a specific image in Glide Cache, so just clear it all
    val glide = Glide.get(applicationContext)
    glide.clearDiskCache()
    runOnUiThread {
        glide.clearMemory()
    }
}

fun BaseSimpleActivity.copyFile(source: String, destination: String) {
    var inputStream: InputStream? = null
    var out: OutputStream? = null
    try {
        out = getFileOutputStreamSync(destination, source.getMimeType())
        inputStream = getFileInputStreamSync(source)
        inputStream!!.copyTo(out!!)
    } catch (e: Exception) {
        showErrorToast(e)
    } finally {
        inputStream?.close()
        out?.close()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun BaseSimpleActivity.ensureWriteAccess(path: String, callback: () -> Unit) {
    when {
        isRestrictedSAFOnlyRoot(path) -> {
            handleAndroidSAFDialog(path) {
                if (!it) {
                    return@handleAndroidSAFDialog
                }
                callback.invoke()
            }
        }

        needsStupidWritePermissions(path) -> {
            handleSAFDialog(path) {
                if (!it) {
                    return@handleSAFDialog
                }
                callback()
            }
        }

        isAccessibleWithSAFSdk30(path) -> {
            handleSAFDialogSdk30(path) {
                if (!it) {
                    return@handleSAFDialogSdk30
                }
                callback()
            }
        }

        else -> {
            callback()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun BaseSimpleActivity.launchResizeMultipleImagesDialog(
    paths: List<String>,
    callback: (() -> Unit)? = null
) {
    ensureBackgroundThread {
        val imagePaths = mutableListOf<String>()
        val imageSizes = mutableListOf<Point>()
        for (path in paths) {
            val size = path.getImageResolution(this)
            if (size != null) {
                imagePaths.add(path)
                imageSizes.add(size)
            }
        }

        runOnUiThread {
            ResizeMultipleImagesDialog(this, imagePaths, imageSizes) {
                callback?.invoke()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun BaseSimpleActivity.launchResizeImageDialog(path: String, callback: (() -> Unit)? = null) {
    val originalSize = path.getImageResolution(this) ?: return
    ResizeWithPathDialog(this, originalSize, path) { newSize, newPath ->
        ensureBackgroundThread {
            val file = File(newPath)
            val pathLastModifiedMap = mapOf(file.absolutePath to file.lastModified())
            try {
                resizeImage(path, newPath, newSize) { success ->
                    if (success) {
                        toast(R.string.file_saved)

                        val paths = arrayListOf(file.absolutePath)
                        rescanPathsAndUpdateLastModified(paths, pathLastModifiedMap) {
                            runOnUiThread {
                                callback?.invoke()
                            }
                        }
                    } else {
                        toast(R.string.image_editing_failed)
                    }
                }
            } catch (e: OutOfMemoryError) {
                toast(R.string.out_of_memory_error)
            } catch (e: Exception) {
                showErrorToast(e)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun BaseSimpleActivity.resizeImage(
    oldPath: String,
    newPath: String,
    size: Point,
    callback: (success: Boolean) -> Unit
) {
    var oldExif: ExifInterface? = null
    if (isNougatPlus()) {
        val inputStream = contentResolver.openInputStream(Uri.fromFile(File(oldPath)))
        oldExif = ExifInterface(inputStream!!)
    }

    val newBitmap =
        Glide.with(applicationContext).asBitmap().load(oldPath).submit(size.x, size.y).get()

    val newFile = File(newPath)
    val newFileDirItem = FileDirItem(newPath, newPath.getFilenameFromPath())
    getFileOutputStream(newFileDirItem, true) { out ->
        if (out != null) {
            out.use {
                try {
                    newBitmap.compress(newFile.absolutePath.getCompressionFormat(), 90, out)

                    if (isNougatPlus()) {
                        val newExif = ExifInterface(newFile.absolutePath)
                        oldExif?.copyNonDimensionAttributesTo(newExif)
                    }
                } catch (ignored: Exception) {
                }

                callback(true)
            }
        } else {
            callback(false)
        }
    }
}

fun BaseSimpleActivity.rescanPathsAndUpdateLastModified(
    paths: ArrayList<String>,
    pathLastModifiedMap: Map<String, Long>,
    callback: () -> Unit
) {
    fixDateTaken(paths, false)
    for (path in paths) {
        val file = File(path)
        val lastModified = pathLastModifiedMap[path]
        if (config.keepLastModified && lastModified != null && lastModified != 0L) {
            File(file.absolutePath).setLastModified(lastModified)
            updateLastModified(file.absolutePath, lastModified)
        }
    }
    rescanPaths(paths, callback)
}

fun saveFile(path: String, bitmap: Bitmap, out: FileOutputStream, degrees: Int) {
    val matrix = Matrix()
    matrix.postRotate(degrees.toFloat())
    val bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    bmp.compress(path.getCompressionFormat(), 90, out)
}

fun Activity.getShortcutImage(tmb: String, drawable: Drawable, callback: () -> Unit) {
    ensureBackgroundThread {
        val options = RequestOptions()
            .format(DecodeFormat.PREFER_ARGB_8888)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .fitCenter()

        val size =
            resources.getDimension(R.dimen.shortcut_size).toInt()
        val builder = Glide.with(this)
            .asDrawable()
            .load(tmb)
            .apply(options)
            .centerCrop()
            .into(size, size)

        try {
            (drawable as LayerDrawable).setDrawableByLayerId(R.id.shortcut_image, builder.get())
        } catch (_: Exception) {
        }

        runOnUiThread {
            callback()
        }
    }
}

@TargetApi(Build.VERSION_CODES.N)
fun Activity.showFileOnMap(path: String) {
    val exif = try {
        if (path.startsWith("content://") && isNougatPlus()) {
            ExifInterface(contentResolver.openInputStream(Uri.parse(path))!!)
        } else {
            ExifInterface(path)
        }
    } catch (e: Exception) {
        showErrorToast(e)
        return
    }

    val latLon = FloatArray(2)
    if (exif.getLatLong(latLon)) {
        showLocationOnMap("${latLon[0]}, ${latLon[1]}")
    } else {
        toast(R.string.unknown_location)
    }
}

fun Activity.handleExcludedFolderPasswordProtection(callback: () -> Unit) {
    if (config.isExcludedPasswordProtectionOn) {
        SecurityDialog(
            this,
            config.excludedPasswordHash,
            config.excludedProtectionType
        ) { _, _, success ->
            if (success) {
                callback()
            }
        }
    } else {
        callback()
    }
}

@UnstableApi
fun Activity.openRecycleBin() {
    Intent(this, MediaActivity::class.java).apply {
        putExtra(DIRECTORY, RECYCLE_BIN)
        startActivity(this)
    }
}

fun Activity.getAlertDialogBuilder() = if (baseConfig.isUsingSystemTheme) {
    MaterialAlertDialogBuilder(this)
} else {
    AlertDialog.Builder(this)
}

@RequiresApi(Build.VERSION_CODES.O)
fun BaseSimpleActivity.deleteFiles(
    files: List<FileDirItem>,
    allowDeleteFolder: Boolean = false,
    callback: ((wasSuccess: Boolean) -> Unit)? = null
) {
    ensureBackgroundThread {
        deleteFilesBg(files, allowDeleteFolder, callback)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun BaseSimpleActivity.deleteFilesBg(
    files: List<FileDirItem>,
    allowDeleteFolder: Boolean = false,
    callback: ((wasSuccess: Boolean) -> Unit)? = null
) {
    if (files.isEmpty()) {
        runOnUiThread {
            callback?.invoke(true)
        }
        return
    }

    val firstFile = files.first()
    val firstFilePath = firstFile.path
    handleSAFDialog(firstFilePath) {
        if (!it) {
            return@handleSAFDialog
        }

        checkManageMediaOrHandleSAFDialogSdk30(firstFilePath) {
            if (!it) {
                return@checkManageMediaOrHandleSAFDialogSdk30
            }

            val recycleBinPath = firstFile.isRecycleBinPath(this)
            if (canManageMedia() && !recycleBinPath && !firstFilePath.doesThisOrParentHaveNoMedia(
                    java.util.HashMap(), null
                )
            ) {
                val fileUris = getFileUrisFromFileDirItems(files)

                deleteSDK30Uris(fileUris) { success ->
                    runOnUiThread {
                        callback?.invoke(success)
                    }
                }
            } else {
                deleteFilesCasual(files, allowDeleteFolder, callback)
            }
        }
    }
}

private fun BaseSimpleActivity.deleteFilesCasual(
    files: List<FileDirItem>,
    allowDeleteFolder: Boolean = false,
    callback: ((wasSuccess: Boolean) -> Unit)? = null
) {
    var wasSuccess = false
    val failedFileDirItems = java.util.ArrayList<FileDirItem>()
    files.forEachIndexed { index, file ->
        deleteFileBg(file, allowDeleteFolder, true) {
            if (it) {
                wasSuccess = true
            } else {
                failedFileDirItems.add(file)
            }

            if (index == files.lastIndex) {
                if (isRPlus() && failedFileDirItems.isNotEmpty()) {
                    val fileUris = getFileUrisFromFileDirItems(failedFileDirItems)
                    deleteSDK30Uris(fileUris) { success ->
                        runOnUiThread {
                            callback?.invoke(success)
                        }
                    }
                } else {
                    runOnUiThread {
                        callback?.invoke(wasSuccess)
                    }
                }
            }
        }
    }
}

fun BaseSimpleActivity.deleteFileBg(
    fileDirItem: FileDirItem,
    allowDeleteFolder: Boolean = false,
    isDeletingMultipleFiles: Boolean,
    callback: ((wasSuccess: Boolean) -> Unit)? = null,
) {
    val path = fileDirItem.path
    if (isRestrictedSAFOnlyRoot(path)) {
        deleteAndroidSAFDirectory(path, allowDeleteFolder, callback)
    } else {
        val file = File(path)
        if (!isRPlus() && file.absolutePath.startsWith(internalStoragePath) && !file.canWrite()) {
            callback?.invoke(false)
            return
        }

        var fileDeleted =
            !isPathOnOTG(path) && ((!file.exists() && file.length() == 0L) || file.delete())
        if (fileDeleted) {
            deleteFromMediaStore(path) { needsRescan ->
                if (needsRescan) {
                    rescanAndDeletePath(path) {
                        runOnUiThread {
                            callback?.invoke(true)
                        }
                    }
                } else {
                    runOnUiThread {
                        callback?.invoke(true)
                    }
                }
            }
        } else {
            if (getIsPathDirectory(file.absolutePath) && allowDeleteFolder) {
                fileDeleted = deleteRecursively(file, this)
            }

            if (!fileDeleted) {
                if (needsStupidWritePermissions(path)) {
                    handleSAFDialog(path) {
                        if (it) {
                            trySAFFileDelete(fileDirItem, allowDeleteFolder, callback)
                        }
                    }
                } else if (isAccessibleWithSAFSdk30(path)) {
                    if (canManageMedia()) {
                        deleteSdk30(fileDirItem, callback)
                    } else {
                        handleSAFDialogSdk30(path) {
                            if (it) {
                                deleteDocumentWithSAFSdk30(fileDirItem, allowDeleteFolder, callback)
                            }
                        }
                    }
                } else if (isRPlus() && !isDeletingMultipleFiles) {
                    deleteSdk30(fileDirItem, callback)
                } else {
                    callback?.invoke(false)
                }
            }
        }
    }
}


fun Activity.checkAppSideloading(): Boolean {
    val isSideloaded = when (baseConfig.appSideloadingStatus) {
        SIDELOADING_TRUE -> true
        SIDELOADING_FALSE -> false
        else -> isAppSideloaded()
    }

    baseConfig.appSideloadingStatus = if (isSideloaded) SIDELOADING_TRUE else SIDELOADING_FALSE
    if (isSideloaded) {
        showSideloadingDialog()
    }

    return isSideloaded
}

fun Activity.rescanPaths(paths: List<String>, callback: (() -> Unit)? = null) {
    applicationContext.rescanPaths(paths, callback)
}

fun Activity.showSideloadingDialog() {
    AppSideloadedDialog(this) {
        finish()
    }
}

fun Activity.showLocationOnMap(coordinates: String) {
    val uriBegin = "geo:${coordinates.replace(" ", "")}"
    val encodedQuery = Uri.encode(coordinates)
    val uriString = "$uriBegin?q=$encodedQuery&z=16"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString))
    launchActivityIntent(intent)
}

fun BaseSimpleActivity.deleteFile(
    fileDirItem: FileDirItem,
    allowDeleteFolder: Boolean = false,
    isDeletingMultipleFiles: Boolean,
    callback: ((wasSuccess: Boolean) -> Unit)? = null
) {
    ensureBackgroundThread {
        deleteFileBg(fileDirItem, allowDeleteFolder, isDeletingMultipleFiles, callback)
    }
}

private fun createCasualFileOutputStream(
    activity: BaseSimpleActivity,
    targetFile: File
): OutputStream? {
    if (targetFile.parentFile?.exists() == false) {
        targetFile.parentFile?.mkdirs()
    }

    return try {
        FileOutputStream(targetFile)
    } catch (e: Exception) {
        activity.showErrorToast(e)
        null
    }
}

fun Activity.launchViewIntent(url: String) {
    hideKeyboard()
    ensureBackgroundThread {
        Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            try {
                startActivity(this)
            } catch (e: ActivityNotFoundException) {
                toast(R.string.no_browser_found)
            } catch (e: Exception) {
                showErrorToast(e)
            }
        }
    }
}

fun Activity.appLaunched(appId: String) {
    baseConfig.internalStoragePath = getInternalStoragePath()
    updateSDCardPath()
    baseConfig.appId = appId
    if (baseConfig.appRunCount == 0) {
        baseConfig.wasOrangeIconChecked = true
        checkAppIconColor()
    } else if (!baseConfig.wasOrangeIconChecked) {
        baseConfig.wasOrangeIconChecked = true
        val primaryColor = resources.getColor(R.color.color_primary)
        if (baseConfig.appIconColor != primaryColor) {
            getAppIconColors().forEachIndexed { index, color ->
                toggleAppIconColor(appId, index, color, false)
            }

            val defaultClassName =
                "${baseConfig.appId.removeSuffix(".debug")}.activities.SplashActivity"
            packageManager.setComponentEnabledSetting(
                ComponentName(baseConfig.appId, defaultClassName),
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                PackageManager.DONT_KILL_APP
            )

            val orangeClassName =
                "${baseConfig.appId.removeSuffix(".debug")}.activities.SplashActivity.Orange"
            packageManager.setComponentEnabledSetting(
                ComponentName(baseConfig.appId, orangeClassName),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

            baseConfig.appIconColor = primaryColor
            baseConfig.lastIconColor = primaryColor
        }
    }

    baseConfig.appRunCount++
    if (baseConfig.appRunCount % 30 == 0 && !isAProApp()) {
        if (!resources.getBoolean(R.bool.hide_google_relations)) {
            showDonateOrUpgradeDialog()
        }
    }

    if (baseConfig.appRunCount % 40 == 0 && !baseConfig.wasAppRated) {
        if (!resources.getBoolean(R.bool.hide_google_relations)) {
            RateStarsDialog(this)
        }
    }
}

fun Activity.showDonateOrUpgradeDialog() {
    if (getCanAppBeUpgraded()) {
        UpgradeToProDialog(this)
    } else if (!isOrWasThankYouInstalled()) {
        DonateDialog(this)
    }
}

fun Activity.isAppInstalledOnSDCard(): Boolean = try {
    val applicationInfo = packageManager.getPackageInfo(packageName, 0).applicationInfo
    (applicationInfo.flags and ApplicationInfo.FLAG_EXTERNAL_STORAGE) == ApplicationInfo.FLAG_EXTERNAL_STORAGE
} catch (e: Exception) {
    false
}

@RequiresApi(Build.VERSION_CODES.O)
fun BaseSimpleActivity.isShowingSAFDialog(path: String): Boolean {
    return if ((!isRPlus() && isPathOnSD(path) && !isSDCardSetAsDefaultStorage() && (baseConfig.sdTreeUri.isEmpty() || !hasProperStoredTreeUri(
            false
        )))
    ) {
        runOnUiThread {
            if (!isDestroyed && !isFinishing) {
                WritePermissionDialog(this, WritePermissionDialog.Mode.SdCard) {
                    Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                        putExtra(EXTRA_SHOW_ADVANCED, true)
                        try {
                            startActivityForResult(this, OPEN_DOCUMENT_TREE_SD)
                            checkedDocumentPath = path
                            return@apply
                        } catch (e: Exception) {
                            type = "*/*"
                        }

                        try {
                            startActivityForResult(this, OPEN_DOCUMENT_TREE_SD)
                            checkedDocumentPath = path
                        } catch (e: ActivityNotFoundException) {
                            toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
                        } catch (e: Exception) {
                            toast(R.string.unknown_error_occurred)
                        }
                    }
                }
            }
        }
        true
    } else {
        false
    }
}

@SuppressLint("InlinedApi")
fun BaseSimpleActivity.isShowingSAFDialogSdk30(path: String): Boolean {
    return if (isAccessibleWithSAFSdk30(path) && !hasProperStoredFirstParentUri(path)) {
        runOnUiThread {
            if (!isDestroyed && !isFinishing) {
                val level = getFirstParentLevel(path)
                WritePermissionDialog(
                    this,
                    WritePermissionDialog.Mode.OpenDocumentTreeSDK30(
                        path.getFirstParentPath(
                            this,
                            level
                        )
                    )
                ) {
                    Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                        putExtra(EXTRA_SHOW_ADVANCED, true)
                        putExtra(
                            DocumentsContract.EXTRA_INITIAL_URI,
                            createFirstParentTreeUriUsingRootTree(path)
                        )
                        try {
                            startActivityForResult(this, OPEN_DOCUMENT_TREE_FOR_SDK_30)
                            checkedDocumentPath = path
                            return@apply
                        } catch (e: Exception) {
                            type = "*/*"
                        }

                        try {
                            startActivityForResult(this, OPEN_DOCUMENT_TREE_FOR_SDK_30)
                            checkedDocumentPath = path
                        } catch (e: ActivityNotFoundException) {
                            toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
                        } catch (e: Exception) {
                            toast(R.string.unknown_error_occurred)
                        }
                    }
                }
            }
        }
        true
    } else {
        false
    }
}

@SuppressLint("InlinedApi")
fun BaseSimpleActivity.isShowingSAFCreateDocumentDialogSdk30(path: String): Boolean {
    return if (!hasProperStoredDocumentUriSdk30(path)) {
        runOnUiThread {
            if (!isDestroyed && !isFinishing) {
                WritePermissionDialog(this, WritePermissionDialog.Mode.CreateDocumentSDK30) {
                    Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        type = DocumentsContract.Document.MIME_TYPE_DIR
                        putExtra(EXTRA_SHOW_ADVANCED, true)
                        addCategory(Intent.CATEGORY_OPENABLE)
                        putExtra(
                            DocumentsContract.EXTRA_INITIAL_URI,
                            buildDocumentUriSdk30(path.getParentPath())
                        )
                        putExtra(Intent.EXTRA_TITLE, path.getFilenameFromPath())
                        try {
                            startActivityForResult(this, CREATE_DOCUMENT_SDK_30)
                            checkedDocumentPath = path
                            return@apply
                        } catch (e: Exception) {
                            type = "*/*"
                        }

                        try {
                            startActivityForResult(this, CREATE_DOCUMENT_SDK_30)
                            checkedDocumentPath = path
                        } catch (e: ActivityNotFoundException) {
                            toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
                        } catch (e: Exception) {
                            toast(R.string.unknown_error_occurred)
                        }
                    }
                }
            }
        }
        true
    } else {
        false
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun BaseSimpleActivity.isShowingAndroidSAFDialog(path: String): Boolean {
    return if (isRestrictedSAFOnlyRoot(path) && (getAndroidTreeUri(path).isEmpty() || !hasProperStoredAndroidTreeUri(
            path
        ))
    ) {
        runOnUiThread {
            if (!isDestroyed && !isFinishing) {
                ConfirmationAdvancedDialog(
                    this,
                    "",
                    R.string.confirm_storage_access_android_text,
                    R.string.ok,
                    R.string.cancel
                ) { success ->
                    if (success) {
                        Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                            putExtra(EXTRA_SHOW_ADVANCED, true)
                            putExtra(
                                DocumentsContract.EXTRA_INITIAL_URI,
                                createAndroidDataOrObbUri(path)
                            )
                            try {
                                startActivityForResult(
                                    this,
                                    OPEN_DOCUMENT_TREE_FOR_ANDROID_DATA_OR_OBB
                                )
                                checkedDocumentPath = path
                                return@apply
                            } catch (e: Exception) {
                                type = "*/*"
                            }

                            try {
                                startActivityForResult(
                                    this,
                                    OPEN_DOCUMENT_TREE_FOR_ANDROID_DATA_OR_OBB
                                )
                                checkedDocumentPath = path
                            } catch (e: ActivityNotFoundException) {
                                toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
                            } catch (e: Exception) {
                                toast(R.string.unknown_error_occurred)
                            }
                        }
                    }
                }
            }
        }
        true
    } else {
        false
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun BaseSimpleActivity.isShowingOTGDialog(path: String): Boolean {
    return if (!isRPlus() && isPathOnOTG(path) && (baseConfig.OTGTreeUri.isEmpty() || !hasProperStoredTreeUri(
            true
        ))
    ) {
        showOTGPermissionDialog(path)
        true
    } else {
        false
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun BaseSimpleActivity.showOTGPermissionDialog(path: String) {
    runOnUiThread {
        if (!isDestroyed && !isFinishing) {
            WritePermissionDialog(this, WritePermissionDialog.Mode.Otg) {
                Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                    try {
                        startActivityForResult(this, OPEN_DOCUMENT_TREE_OTG)
                        checkedDocumentPath = path
                        return@apply
                    } catch (e: Exception) {
                        type = "*/*"
                    }

                    try {
                        startActivityForResult(this, OPEN_DOCUMENT_TREE_OTG)
                        checkedDocumentPath = path
                    } catch (e: ActivityNotFoundException) {
                        toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
                    } catch (e: Exception) {
                        toast(R.string.unknown_error_occurred)
                    }
                }
            }
        }
    }
}

fun Activity.launchPurchaseThankYouIntent() {
    hideKeyboard()
    try {
        launchViewIntent("market://details?id=com.simplemobiletools.thankyou")
    } catch (ignored: Exception) {
        launchViewIntent(getString(R.string.thank_you_url))
    }
}

fun Activity.launchUpgradeToProIntent() {
    hideKeyboard()
    try {
        launchViewIntent("market://details?id=${baseConfig.appId.removeSuffix(".debug")}.pro")
    } catch (ignored: Exception) {
        launchViewIntent(getStoreUrl())
    }
}

fun Activity.launchMoreAppsFromUsIntent() {
    launchViewIntent(DEVELOPER_PLAY_STORE_URL)
}

fun Activity.launchViewIntent(id: Int) = launchViewIntent(getString(id))


fun Activity.redirectToRateUs() {
    hideKeyboard()
    try {
        launchViewIntent("market://details?id=${packageName.removeSuffix(".debug")}")
    } catch (ignored: ActivityNotFoundException) {
        launchViewIntent(getStoreUrl())
    }
}

fun Activity.sharePathIntent(path: String, applicationId: String) {
    ensureBackgroundThread {
        val newUri = getFinalUriFromPath(path, applicationId) ?: return@ensureBackgroundThread
        Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, newUri)
            type = getUriMimeType(path, newUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            grantUriPermission("android", newUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)

            try {
                startActivity(Intent.createChooser(this, getString(R.string.share_via)))
            } catch (e: ActivityNotFoundException) {
                toast(R.string.no_app_found)
            } catch (e: RuntimeException) {
                if (e.cause is TransactionTooLargeException) {
                    toast(R.string.maximum_share_reached)
                } else {
                    showErrorToast(e)
                }
            } catch (e: Exception) {
                showErrorToast(e)
            }
        }
    }
}

fun Activity.sharePathsIntent(paths: List<String>, applicationId: String) {
    ensureBackgroundThread {
        if (paths.size == 1) {
            sharePathIntent(paths.first(), applicationId)
        } else {
            val uriPaths = java.util.ArrayList<String>()
            val newUris = paths.map {
                val uri = getFinalUriFromPath(it, applicationId) ?: return@ensureBackgroundThread
                uriPaths.add(uri.path!!)
                uri
            } as java.util.ArrayList<Uri>

            var mimeType = uriPaths.getMimeType()
            if (mimeType.isEmpty() || mimeType == "*/*") {
                mimeType = paths.getMimeType()
            }

            Intent().apply {
                action = Intent.ACTION_SEND_MULTIPLE
                type = mimeType
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, newUris)

                try {
                    startActivity(Intent.createChooser(this, getString(R.string.share_via)))
                } catch (e: ActivityNotFoundException) {
                    toast(R.string.no_app_found)
                } catch (e: RuntimeException) {
                    if (e.cause is TransactionTooLargeException) {
                        toast(R.string.maximum_share_reached)
                    } else {
                        showErrorToast(e)
                    }
                } catch (e: Exception) {
                    showErrorToast(e)
                }
            }
        }
    }
}

fun Activity.showBiometricPrompt(
    successCallback: ((String, Int) -> Unit)? = null,
    failureCallback: (() -> Unit)? = null
) {
    Class2BiometricAuthPrompt.Builder(getText(R.string.authenticate), getText(R.string.cancel))
        .build()
        .startAuthentication(
            AuthPromptHost(this as FragmentActivity),
            object : AuthPromptCallback() {
                override fun onAuthenticationSucceeded(
                    activity: FragmentActivity?,
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    successCallback?.invoke("", PROTECTION_FINGERPRINT)
                }

                override fun onAuthenticationError(
                    activity: FragmentActivity?,
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    val isCanceledByUser =
                        errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON || errorCode == BiometricPrompt.ERROR_USER_CANCELED
                    if (!isCanceledByUser) {
                        toast(errString.toString())
                    }
                    failureCallback?.invoke()
                }

                override fun onAuthenticationFailed(activity: FragmentActivity?) {
                    toast(R.string.authentication_failed)
                    failureCallback?.invoke()
                }
            }
        )
}

fun Activity.setAsIntent(path: String, applicationId: String) {
    ensureBackgroundThread {
        val newUri = getFinalUriFromPath(path, applicationId) ?: return@ensureBackgroundThread
        Intent().apply {
            action = Intent.ACTION_ATTACH_DATA
            setDataAndType(newUri, getUriMimeType(path, newUri))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val chooser = Intent.createChooser(this, getString(R.string.set_as))

            try {
                startActivityForResult(chooser, REQUEST_SET_AS)
            } catch (e: ActivityNotFoundException) {
                toast(R.string.no_app_found)
            } catch (e: Exception) {
                showErrorToast(e)
            }
        }
    }
}

fun Activity.openEditorIntent(path: String, forceChooser: Boolean, applicationId: String) {
    ensureBackgroundThread {
        val newUri = getFinalUriFromPath(path, applicationId) ?: return@ensureBackgroundThread
        Intent().apply {
            action = Intent.ACTION_EDIT
            setDataAndType(newUri, getUriMimeType(path, newUri))
            if (!isRPlus() || (isRPlus() && (hasProperStoredDocumentUriSdk30(path) || Environment.isExternalStorageManager()))) {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }

            val parent = path.getParentPath()
            val newFilename = "${path.getFilenameFromPath().substringBeforeLast('.')}_1"
            val extension = path.getFilenameExtension()
            val newFilePath = File(parent, "$newFilename.$extension")

            val outputUri = if (isPathOnOTG(path)) newUri else getFinalUriFromPath(
                "$newFilePath",
                applicationId
            )
            if (!isRPlus()) {
                val resInfoList =
                    packageManager.queryIntentActivities(this, PackageManager.MATCH_DEFAULT_ONLY)
                for (resolveInfo in resInfoList) {
                    val packageName = resolveInfo.activityInfo.packageName
                    grantUriPermission(
                        packageName,
                        outputUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
            }

            if (!isRPlus()) {
                putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
            }

            putExtra(REAL_FILE_PATH, path)

            try {
                val chooser = Intent.createChooser(this, getString(R.string.edit_with))
                startActivityForResult(if (forceChooser) chooser else this, REQUEST_EDIT_IMAGE)
            } catch (e: ActivityNotFoundException) {
                toast(R.string.no_app_found)
            } catch (e: Exception) {
                showErrorToast(e)
            }
        }
    }
}

fun Activity.openPathIntent(
    path: String,
    forceChooser: Boolean,
    applicationId: String,
    forceMimeType: String = "",
    extras: java.util.HashMap<String, Boolean> = java.util.HashMap()
) {
    ensureBackgroundThread {
        val newUri = getFinalUriFromPath(path, applicationId) ?: return@ensureBackgroundThread
        val mimeType =
            forceMimeType.ifEmpty { getUriMimeType(path, newUri) }
        Intent().apply {
            action = Intent.ACTION_VIEW
            setDataAndType(newUri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            if (applicationId == "com.simplemobiletools.gallery.pro" || applicationId == "com.simplemobiletools.gallery.pro.debug") {
                putExtra(IS_FROM_GALLERY, true)
            }

            for ((key, value) in extras) {
                putExtra(key, value)
            }

            putExtra(REAL_FILE_PATH, path)

            try {
                val chooser = Intent.createChooser(this, getString(R.string.open_with))
                startActivity(if (forceChooser) chooser else this)
            } catch (e: ActivityNotFoundException) {
                if (!tryGenericMimeType(this, mimeType, newUri)) {
                    toast(R.string.no_app_found)
                }
            } catch (e: Exception) {
                showErrorToast(e)
            }
        }
    }
}

fun Activity.getFinalUriFromPath(path: String, applicationId: String): Uri? {
    val uri = try {
        ensurePublicUri(path, applicationId)
    } catch (e: Exception) {
        showErrorToast(e)
        return null
    }

    if (uri == null) {
        toast(R.string.unknown_error_occurred)
        return null
    }

    return uri
}

fun Activity.tryGenericMimeType(intent: Intent, mimeType: String, uri: Uri): Boolean {
    var genericMimeType = mimeType.getGenericMimeType()
    if (genericMimeType.isEmpty()) {
        genericMimeType = "*/*"
    }

    intent.setDataAndType(uri, genericMimeType)

    return try {
        startActivity(intent)
        true
    } catch (e: Exception) {
        false
    }
}

fun BaseSimpleActivity.checkWhatsNew(releases: List<Release>, currVersion: Int) {
    if (baseConfig.lastVersion == 0) {
        baseConfig.lastVersion = currVersion
        return
    }

    val newReleases = arrayListOf<Release>()
    releases.filterTo(newReleases) { it.id > baseConfig.lastVersion }

    if (newReleases.isNotEmpty()) {
        WhatsNewDialog(this, newReleases)
    }

    baseConfig.lastVersion = currVersion
}


@RequiresApi(Build.VERSION_CODES.O)
private fun BaseSimpleActivity.deleteSdk30(
    fileDirItem: FileDirItem,
    callback: ((wasSuccess: Boolean) -> Unit)?
) {
    val fileUris = getFileUrisFromFileDirItems(arrayListOf(fileDirItem))
    deleteSDK30Uris(fileUris) { success ->
        runOnUiThread {
            callback?.invoke(success)
        }
    }
}

private fun deleteRecursively(file: File, context: Context): Boolean {
    if (file.isDirectory) {
        val files = file.listFiles() ?: return file.delete()
        for (child in files) {
            deleteRecursively(child, context)
        }
    }

    val deleted = file.delete()
    if (deleted) {
        context.deleteFromMediaStore(file.absolutePath)
    }
    return deleted
}

fun Activity.scanPathRecursively(path: String, callback: (() -> Unit)? = null) {
    applicationContext.scanPathRecursively(path, callback)
}

fun Activity.scanPathsRecursively(paths: List<String>, callback: (() -> Unit)? = null) {
    applicationContext.scanPathsRecursively(paths, callback)
}

fun Activity.rescanPath(path: String, callback: (() -> Unit)? = null) {
    applicationContext.rescanPath(path, callback)
}

@RequiresApi(Build.VERSION_CODES.O)
fun BaseSimpleActivity.renameFile(
    oldPath: String,
    newPath: String,
    isRenamingMultipleFiles: Boolean,
    callback: ((success: Boolean, android30RenameFormat: Android30RenameFormat) -> Unit)? = null
) {
    if (isRestrictedSAFOnlyRoot(oldPath)) {
        handleAndroidSAFDialog(oldPath) {
            if (!it) {
                runOnUiThread {
                    callback?.invoke(false, Android30RenameFormat.NONE)
                }
                return@handleAndroidSAFDialog
            }

            try {
                ensureBackgroundThread {
                    val success = renameAndroidSAFDocument(oldPath, newPath)
                    runOnUiThread {
                        callback?.invoke(success, Android30RenameFormat.NONE)
                    }
                }
            } catch (e: Exception) {
                showErrorToast(e)
                runOnUiThread {
                    callback?.invoke(false, Android30RenameFormat.NONE)
                }
            }
        }
    } else if (isAccessibleWithSAFSdk30(oldPath)) {
        if (canManageMedia() && !File(oldPath).isDirectory && isPathOnInternalStorage(oldPath)) {
            renameCasually(oldPath, newPath, isRenamingMultipleFiles, callback)
        } else {
            handleSAFDialogSdk30(oldPath) {
                if (!it) {
                    return@handleSAFDialogSdk30
                }

                try {
                    ensureBackgroundThread {
                        val success = renameDocumentSdk30(oldPath, newPath)
                        if (success) {
                            updateInMediaStore(oldPath, newPath)
                            rescanPath(newPath) {
                                runOnUiThread {
                                    callback?.invoke(true, Android30RenameFormat.NONE)
                                }
                                if (!oldPath.equals(newPath, true)) {
                                    deleteFromMediaStore(oldPath)
                                }
                                scanPathRecursively(newPath)
                            }
                        } else {
                            runOnUiThread {
                                callback?.invoke(false, Android30RenameFormat.NONE)
                            }
                        }
                    }
                } catch (e: Exception) {
                    showErrorToast(e)
                    runOnUiThread {
                        callback?.invoke(false, Android30RenameFormat.NONE)
                    }
                }
            }
        }
    } else if (needsStupidWritePermissions(newPath)) {
        handleSAFDialog(newPath) {
            if (!it) {
                return@handleSAFDialog
            }

            val document = getSomeDocumentFile(oldPath)
            if (document == null || (File(oldPath).isDirectory != document.isDirectory)) {
                runOnUiThread {
                    toast(R.string.unknown_error_occurred)
                    callback?.invoke(false, Android30RenameFormat.NONE)
                }
                return@handleSAFDialog
            }

            try {
                ensureBackgroundThread {
                    try {
                        DocumentsContract.renameDocument(
                            applicationContext.contentResolver,
                            document.uri,
                            newPath.getFilenameFromPath()
                        )
                    } catch (ignored: FileNotFoundException) {
                        // FileNotFoundException is thrown in some weird cases, but renaming works just fine
                    } catch (e: Exception) {
                        showErrorToast(e)
                        callback?.invoke(false, Android30RenameFormat.NONE)
                        return@ensureBackgroundThread
                    }

                    updateInMediaStore(oldPath, newPath)
                    rescanPaths(arrayListOf(oldPath, newPath)) {
                        if (!baseConfig.keepLastModified) {
                            updateLastModified(newPath, System.currentTimeMillis())
                        }
                        deleteFromMediaStore(oldPath)
                        runOnUiThread {
                            callback?.invoke(true, Android30RenameFormat.NONE)
                        }
                    }
                }
            } catch (e: Exception) {
                showErrorToast(e)
                runOnUiThread {
                    callback?.invoke(false, Android30RenameFormat.NONE)
                }
            }
        }
    } else renameCasually(oldPath, newPath, isRenamingMultipleFiles, callback)
}

private fun BaseSimpleActivity.renameCasually(
    oldPath: String,
    newPath: String,
    isRenamingMultipleFiles: Boolean,
    callback: ((success: Boolean, android30RenameFormat: Android30RenameFormat) -> Unit)?
) {
    val oldFile = File(oldPath)
    val newFile = File(newPath)
    val tempFile = try {
        createTempFile(oldFile) ?: return
    } catch (exception: Exception) {
        if (isRPlus() && exception is java.nio.file.FileSystemException) {
            // if we are renaming multiple files at once, we should give the Android 30+ permission dialog all uris together, not one by one
            if (isRenamingMultipleFiles) {
                callback?.invoke(false, Android30RenameFormat.CONTENT_RESOLVER)
            } else {
                val fileUris =
                    getFileUrisFromFileDirItems(arrayListOf(File(oldPath).toFileDirItem(this)))
                updateSDK30Uris(fileUris) { success ->
                    if (success) {
                        val values = ContentValues().apply {
                            put(Images.Media.DISPLAY_NAME, newPath.getFilenameFromPath())
                        }

                        try {
                            contentResolver.update(fileUris.first(), values, null, null)
                            callback?.invoke(true, Android30RenameFormat.NONE)
                        } catch (e: Exception) {
                            showErrorToast(e)
                            callback?.invoke(false, Android30RenameFormat.NONE)
                        }
                    } else {
                        callback?.invoke(false, Android30RenameFormat.NONE)
                    }
                }
            }
        } else {
            if (exception is IOException && File(oldPath).isDirectory && isRestrictedWithSAFSdk30(
                    oldPath
                )
            ) {
                toast(R.string.cannot_rename_folder)
            } else {
                showErrorToast(exception)
            }
            callback?.invoke(false, Android30RenameFormat.NONE)
        }
        return
    }

    val oldToTempSucceeds = oldFile.renameTo(tempFile)
    val tempToNewSucceeds = tempFile.renameTo(newFile)
    if (oldToTempSucceeds && tempToNewSucceeds) {
        if (newFile.isDirectory) {
            updateInMediaStore(oldPath, newPath)
            rescanPath(newPath) {
                runOnUiThread {
                    callback?.invoke(true, Android30RenameFormat.NONE)
                }
                if (!oldPath.equals(newPath, true)) {
                    deleteFromMediaStore(oldPath)
                }
                scanPathRecursively(newPath)
            }
        } else {
            if (!baseConfig.keepLastModified) {
                newFile.setLastModified(System.currentTimeMillis())
            }
            updateInMediaStore(oldPath, newPath)
            scanPathsRecursively(arrayListOf(newPath)) {
                if (!oldPath.equals(newPath, true)) {
                    deleteFromMediaStore(oldPath)
                }
                runOnUiThread {
                    callback?.invoke(true, Android30RenameFormat.NONE)
                }
            }
        }
    } else {
        tempFile.delete()
        newFile.delete()
        if (isRPlus()) {
            // if we are renaming multiple files at once, we should give the Android 30+ permission dialog all uris together, not one by one
            if (isRenamingMultipleFiles) {
                callback?.invoke(false, Android30RenameFormat.SAF)
            } else {
                val fileUris =
                    getFileUrisFromFileDirItems(arrayListOf(File(oldPath).toFileDirItem(this)))
                updateSDK30Uris(fileUris) { success ->
                    if (!success) {
                        return@updateSDK30Uris
                    }
                    try {
                        val sourceUri = fileUris.first()
                        val sourceFile = File(oldPath).toFileDirItem(this)

                        if (oldPath.equals(newPath, true)) {
                            val tempDestination = try {
                                createTempFile(File(sourceFile.path)) ?: return@updateSDK30Uris
                            } catch (exception: Exception) {
                                showErrorToast(exception)
                                callback?.invoke(false, Android30RenameFormat.NONE)
                                return@updateSDK30Uris
                            }

                            val copyTempSuccess =
                                copySingleFileSdk30(sourceFile, tempDestination.toFileDirItem(this))
                            if (copyTempSuccess) {
                                contentResolver.delete(sourceUri, null)
                                tempDestination.renameTo(File(newPath))
                                if (!baseConfig.keepLastModified) {
                                    newFile.setLastModified(System.currentTimeMillis())
                                }
                                updateInMediaStore(oldPath, newPath)
                                scanPathsRecursively(arrayListOf(newPath)) {
                                    runOnUiThread {
                                        callback?.invoke(true, Android30RenameFormat.NONE)
                                    }
                                }
                            } else {
                                callback?.invoke(false, Android30RenameFormat.NONE)
                            }
                        } else {
                            val destinationFile = FileDirItem(
                                newPath,
                                newPath.getFilenameFromPath(),
                                sourceFile.isDirectory,
                                sourceFile.children,
                                sourceFile.size,
                                sourceFile.modified
                            )
                            val copySuccessful = copySingleFileSdk30(sourceFile, destinationFile)
                            if (copySuccessful) {
                                if (!baseConfig.keepLastModified) {
                                    newFile.setLastModified(System.currentTimeMillis())
                                }
                                contentResolver.delete(sourceUri, null)
                                updateInMediaStore(oldPath, newPath)
                                scanPathsRecursively(arrayListOf(newPath)) {
                                    runOnUiThread {
                                        callback?.invoke(true, Android30RenameFormat.NONE)
                                    }
                                }
                            } else {
                                toast(R.string.unknown_error_occurred)
                                callback?.invoke(false, Android30RenameFormat.NONE)
                            }
                        }

                    } catch (e: Exception) {
                        showErrorToast(e)
                        callback?.invoke(false, Android30RenameFormat.NONE)
                    }
                }
            }
        } else {
            toast(R.string.unknown_error_occurred)
            callback?.invoke(false, Android30RenameFormat.NONE)
        }
    }
}

fun createTempFile(file: File): File? {
    return if (file.isDirectory) {
        createTempDir("temp", "${System.currentTimeMillis()}", file.parentFile)
    } else {
        if (isRPlus()) {
            // this can throw FileSystemException, lets catch and handle it at the place calling this function
            kotlin.io.path.createTempFile(
                file.parentFile.toPath(),
                "temp",
                "${System.currentTimeMillis()}"
            ).toFile()
        } else {
            createTempFile("temp", "${System.currentTimeMillis()}", file.parentFile)
        }
    }
}

fun Activity.hideKeyboard() {
    if (isOnMainThread()) {
        hideKeyboardSync()
    } else {
        Handler(Looper.getMainLooper()).post {
            hideKeyboardSync()
        }
    }
}

fun Activity.hideKeyboardSync() {
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow((currentFocus ?: View(this)).windowToken, 0)
    window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    currentFocus?.clearFocus()
}

fun Activity.showKeyboard(et: EditText) {
    et.requestFocus()
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT)
}

fun Activity.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

@RequiresApi(Build.VERSION_CODES.O)
fun BaseSimpleActivity.getFileOutputStream(
    fileDirItem: FileDirItem,
    allowCreatingNewFile: Boolean = false,
    callback: (outputStream: OutputStream?) -> Unit
) {
    val targetFile = File(fileDirItem.path)
    when {
        isRestrictedSAFOnlyRoot(fileDirItem.path) -> {
            handleAndroidSAFDialog(fileDirItem.path) {
                if (!it) {
                    return@handleAndroidSAFDialog
                }

                val uri = getAndroidSAFUri(fileDirItem.path)
                if (!getDoesFilePathExist(fileDirItem.path)) {
                    createAndroidSAFFile(fileDirItem.path)
                }
                callback.invoke(applicationContext.contentResolver.openOutputStream(uri, "wt"))
            }
        }

        needsStupidWritePermissions(fileDirItem.path) -> {
            handleSAFDialog(fileDirItem.path) {
                if (!it) {
                    return@handleSAFDialog
                }

                var document = getDocumentFile(fileDirItem.path)
                if (document == null && allowCreatingNewFile) {
                    document = getDocumentFile(fileDirItem.getParentPath())
                }

                if (document == null) {
                    showFileCreateError(fileDirItem.path)
                    callback(null)
                    return@handleSAFDialog
                }

                if (!getDoesFilePathExist(fileDirItem.path)) {
                    document = getDocumentFile(fileDirItem.path) ?: document.createFile(
                        "",
                        fileDirItem.name
                    )
                }

                if (document?.exists() == true) {
                    try {
                        callback(
                            applicationContext.contentResolver.openOutputStream(
                                document.uri,
                                "wt"
                            )
                        )
                    } catch (e: FileNotFoundException) {
                        showErrorToast(e)
                        callback(null)
                    }
                } else {
                    showFileCreateError(fileDirItem.path)
                    callback(null)
                }
            }
        }

        isAccessibleWithSAFSdk30(fileDirItem.path) -> {
            handleSAFDialogSdk30(fileDirItem.path) {
                if (!it) {
                    return@handleSAFDialogSdk30
                }

                callback.invoke(
                    try {
                        val uri = createDocumentUriUsingFirstParentTreeUri(fileDirItem.path)
                        if (!getDoesFilePathExist(fileDirItem.path)) {
                            createSAFFileSdk30(fileDirItem.path)
                        }
                        applicationContext.contentResolver.openOutputStream(uri, "wt")
                    } catch (e: Exception) {
                        null
                    } ?: createCasualFileOutputStream(this, targetFile)
                )
            }
        }

        isRestrictedWithSAFSdk30(fileDirItem.path) -> {
            callback.invoke(
                try {
                    val fileUri = getFileUrisFromFileDirItems(arrayListOf(fileDirItem))
                    applicationContext.contentResolver.openOutputStream(fileUri.first(), "wt")
                } catch (e: Exception) {
                    null
                } ?: createCasualFileOutputStream(this, targetFile)
            )
        }

        else -> {
            callback.invoke(createCasualFileOutputStream(this, targetFile))
        }
    }
}

fun Activity.handleHiddenFolderPasswordProtection(callback: () -> Unit) {
    if (baseConfig.isHiddenPasswordProtectionOn) {
        SecurityDialog(
            this,
            baseConfig.hiddenPasswordHash,
            baseConfig.hiddenProtectionType
        ) { _, _, success ->
            if (success) {
                callback()
            }
        }
    } else {
        callback()
    }
}

fun Activity.handleAppPasswordProtection(callback: (success: Boolean) -> Unit) {
    if (baseConfig.isAppPasswordProtectionOn) {
        SecurityDialog(
            this,
            baseConfig.appPasswordHash,
            baseConfig.appProtectionType
        ) { _, _, success ->
            callback(success)
        }
    } else {
        callback(true)
    }
}

fun Activity.handleDeletePasswordProtection(callback: () -> Unit) {
    if (baseConfig.isDeletePasswordProtectionOn) {
        SecurityDialog(
            this,
            baseConfig.deletePasswordHash,
            baseConfig.deleteProtectionType
        ) { _, _, success ->
            if (success) {
                callback()
            }
        }
    } else {
        callback()
    }
}

fun Activity.handleLockedFolderOpening(path: String, callback: (success: Boolean) -> Unit) {
    if (baseConfig.isFolderProtected(path)) {
        SecurityDialog(
            this,
            baseConfig.getFolderProtectionHash(path),
            baseConfig.getFolderProtectionType(path)
        ) { _, _, success ->
            callback(success)
        }
    } else {
        callback(true)
    }
}

fun Activity.updateSharedTheme(sharedTheme: SharedTheme) {
    try {
        val contentValues = MyContentProvider.fillThemeContentValues(sharedTheme)
        applicationContext.contentResolver.update(
            MyContentProvider.MY_CONTENT_URI,
            contentValues,
            null,
            null
        )
    } catch (e: Exception) {
        showErrorToast(e)
    }
}

@SuppressLint("UseCompatLoadingForDrawables")
fun Activity.isAppSideloaded(): Boolean {
    return try {
        getDrawable(R.drawable.ic_camera_vector)
        false
    } catch (e: Exception) {
        true
    }
}

fun Activity.onApplyWindowInsets(callback: (WindowInsetsCompat) -> Unit) {
    window.decorView.setOnApplyWindowInsetsListener { view, insets ->
        callback(WindowInsetsCompat.toWindowInsetsCompat(insets))
        view.onApplyWindowInsets(insets)
        insets
    }
}

fun BaseSimpleActivity.copySingleFileSdk30(source: FileDirItem, destination: FileDirItem): Boolean {
    val directory = destination.getParentPath()
    if (!createDirectorySync(directory)) {
        val error = String.format(getString(R.string.could_not_create_folder), directory)
        showErrorToast(error)
        return false
    }

    var inputStream: InputStream? = null
    var out: OutputStream? = null
    try {

        out = getFileOutputStreamSync(destination.path, source.path.getMimeType())
        inputStream = getFileInputStreamSync(source.path)!!

        var copiedSize = 0L
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var bytes = inputStream.read(buffer)
        while (bytes >= 0) {
            out!!.write(buffer, 0, bytes)
            copiedSize += bytes
            bytes = inputStream.read(buffer)
        }

        out?.flush()

        return if (source.size == copiedSize && getDoesFilePathExist(destination.path)) {
            if (baseConfig.keepLastModified) {
                copyOldLastModified(source.path, destination.path)
                val lastModified = File(source.path).lastModified()
                if (lastModified != 0L) {
                    File(destination.path).setLastModified(lastModified)
                }
            }
            true
        } else {
            false
        }
    } finally {
        inputStream?.close()
        out?.close()
    }
}

fun BaseSimpleActivity.copyOldLastModified(sourcePath: String, destinationPath: String) {
    val projection =
        arrayOf(Images.Media.DATE_TAKEN, Images.Media.DATE_MODIFIED)
    val uri = Files.getContentUri("external")
    val selection = "${MediaStore.MediaColumns.DATA} = ?"
    var selectionArgs = arrayOf(sourcePath)
    val cursor =
        applicationContext.contentResolver.query(uri, projection, selection, selectionArgs, null)

    cursor?.use {
        if (cursor.moveToFirst()) {
            val dateTaken = cursor.getLongValue(Images.Media.DATE_TAKEN)
            val dateModified = cursor.getIntValue(Images.Media.DATE_MODIFIED)

            val values = ContentValues().apply {
                put(Images.Media.DATE_TAKEN, dateTaken)
                put(Images.Media.DATE_MODIFIED, dateModified)
            }

            selectionArgs = arrayOf(destinationPath)
            applicationContext.contentResolver.update(uri, values, selection, selectionArgs)
        }
    }
}

fun Activity.getThemeId(color: Int = baseConfig.primaryColor, showTransparentTop: Boolean = false) =
    when {
        baseConfig.isUsingSystemTheme -> if (isUsingSystemDarkTheme()) R.style.AppTheme_Base_System else R.style.AppTheme_Base_System_Light
        isBlackAndWhiteTheme() -> when {
            showTransparentTop -> R.style.AppTheme_BlackAndWhite_NoActionBar
            baseConfig.primaryColor.getContrastColor() == DARK_GREY -> R.style.AppTheme_BlackAndWhite_DarkTextColor
            else -> R.style.AppTheme_BlackAndWhite
        }

        isWhiteTheme() -> when {
            showTransparentTop -> R.style.AppTheme_White_NoActionBar
            baseConfig.primaryColor.getContrastColor() == Color.WHITE -> R.style.AppTheme_White_LightTextColor
            else -> R.style.AppTheme_White
        }

        showTransparentTop -> {
            when (color) {
                -12846 -> R.style.AppTheme_Red_100_core
                -1074534 -> R.style.AppTheme_Red_200_core
                -1739917 -> R.style.AppTheme_Red_300_core
                -1092784 -> R.style.AppTheme_Red_400_core
                -769226 -> R.style.AppTheme_Red_500_core
                -1754827 -> R.style.AppTheme_Red_600_core
                -2937041 -> R.style.AppTheme_Red_700_core
                -3790808 -> R.style.AppTheme_Red_800_core
                -4776932 -> R.style.AppTheme_Red_900_core

                -476208 -> R.style.AppTheme_Pink_100_core
                -749647 -> R.style.AppTheme_Pink_200_core
                -1023342 -> R.style.AppTheme_Pink_300_core
                -1294214 -> R.style.AppTheme_Pink_400_core
                -1499549 -> R.style.AppTheme_Pink_500_core
                -2614432 -> R.style.AppTheme_Pink_600_core
                -4056997 -> R.style.AppTheme_Pink_700_core
                -5434281 -> R.style.AppTheme_Pink_800_core
                -7860657 -> R.style.AppTheme_Pink_900_core

                -1982745 -> R.style.AppTheme_Purple_100_core
                -3238952 -> R.style.AppTheme_Purple_200_core
                -4560696 -> R.style.AppTheme_Purple_300_core
                -5552196 -> R.style.AppTheme_Purple_400_core
                -6543440 -> R.style.AppTheme_Purple_500_core
                -7461718 -> R.style.AppTheme_Purple_600_core
                -8708190 -> R.style.AppTheme_Purple_700_core
                -9823334 -> R.style.AppTheme_Purple_800_core
                -11922292 -> R.style.AppTheme_Purple_900_core

                -3029783 -> R.style.AppTheme_Deep_Purple_100_core
                -5005861 -> R.style.AppTheme_Deep_Purple_200_core
                -6982195 -> R.style.AppTheme_Deep_Purple_300_core
                -8497214 -> R.style.AppTheme_Deep_Purple_400_core
                -10011977 -> R.style.AppTheme_Deep_Purple_500_core
                -10603087 -> R.style.AppTheme_Deep_Purple_600_core
                -11457112 -> R.style.AppTheme_Deep_Purple_700_core
                -12245088 -> R.style.AppTheme_Deep_Purple_800_core
                -13558894 -> R.style.AppTheme_Deep_Purple_900_core

                -3814679 -> R.style.AppTheme_Indigo_100_core
                -6313766 -> R.style.AppTheme_Indigo_200_core
                -8812853 -> R.style.AppTheme_Indigo_300_core
                -10720320 -> R.style.AppTheme_Indigo_400_core
                -12627531 -> R.style.AppTheme_Indigo_500_core
                -13022805 -> R.style.AppTheme_Indigo_600_core
                -13615201 -> R.style.AppTheme_Indigo_700_core
                -14142061 -> R.style.AppTheme_Indigo_800_core
                -15064194 -> R.style.AppTheme_Indigo_900_core

                -4464901 -> R.style.AppTheme_Blue_100_core
                -7288071 -> R.style.AppTheme_Blue_200_core
                -10177034 -> R.style.AppTheme_Blue_300_core
                -12409355 -> R.style.AppTheme_Blue_400_core
                -14575885 -> R.style.AppTheme_Blue_500_core
                -14776091 -> R.style.AppTheme_Blue_600_core
                -15108398 -> R.style.AppTheme_Blue_700_core
                -15374912 -> R.style.AppTheme_Blue_800_core
                -15906911 -> R.style.AppTheme_Blue_900_core

                -4987396 -> R.style.AppTheme_Light_Blue_100_core
                -8268550 -> R.style.AppTheme_Light_Blue_200_core
                -11549705 -> R.style.AppTheme_Light_Blue_300_core
                -14043396 -> R.style.AppTheme_Light_Blue_400_core
                -16537100 -> R.style.AppTheme_Light_Blue_500_core
                -16540699 -> R.style.AppTheme_Light_Blue_600_core
                -16611119 -> R.style.AppTheme_Light_Blue_700_core
                -16615491 -> R.style.AppTheme_Light_Blue_800_core
                -16689253 -> R.style.AppTheme_Light_Blue_900_core

                -5051406 -> R.style.AppTheme_Cyan_100_core
                -8331542 -> R.style.AppTheme_Cyan_200_core
                -11677471 -> R.style.AppTheme_Cyan_300_core
                -14235942 -> R.style.AppTheme_Cyan_400_core
                -16728876 -> R.style.AppTheme_Cyan_500_core
                -16732991 -> R.style.AppTheme_Cyan_600_core
                -16738393 -> R.style.AppTheme_Cyan_700_core
                -16743537 -> R.style.AppTheme_Cyan_800_core
                -16752540 -> R.style.AppTheme_Cyan_900_core

                -5054501 -> R.style.AppTheme_Teal_100_core
                -8336444 -> R.style.AppTheme_Teal_200_core
                -11684180 -> R.style.AppTheme_Teal_300_core
                -14244198 -> R.style.AppTheme_Teal_400_core
                -16738680 -> R.style.AppTheme_Teal_500_core
                -16742021 -> R.style.AppTheme_Teal_600_core
                -16746133 -> R.style.AppTheme_Teal_700_core
                -16750244 -> R.style.AppTheme_Teal_800_core
                -16757440 -> R.style.AppTheme_Teal_900_core

                -3610935 -> R.style.AppTheme_Green_100_core
                -5908825 -> R.style.AppTheme_Green_200_core
                -8271996 -> R.style.AppTheme_Green_300_core
                -10044566 -> R.style.AppTheme_Green_400_core
                -11751600 -> R.style.AppTheme_Green_500_core
                -12345273 -> R.style.AppTheme_Green_600_core
                -13070788 -> R.style.AppTheme_Green_700_core
                -13730510 -> R.style.AppTheme_Green_800_core
                -14983648 -> R.style.AppTheme_Green_900_core

                -2298424 -> R.style.AppTheme_Light_Green_100_core
                -3808859 -> R.style.AppTheme_Light_Green_200_core
                -5319295 -> R.style.AppTheme_Light_Green_300_core
                -6501275 -> R.style.AppTheme_Light_Green_400_core
                -7617718 -> R.style.AppTheme_Light_Green_500_core
                -8604862 -> R.style.AppTheme_Light_Green_600_core
                -9920712 -> R.style.AppTheme_Light_Green_700_core
                -11171025 -> R.style.AppTheme_Light_Green_800_core
                -13407970 -> R.style.AppTheme_Light_Green_900_core

                -985917 -> R.style.AppTheme_Lime_100_core
                -1642852 -> R.style.AppTheme_Lime_200_core
                -2300043 -> R.style.AppTheme_Lime_300_core
                -2825897 -> R.style.AppTheme_Lime_400_core
                -3285959 -> R.style.AppTheme_Lime_500_core
                -4142541 -> R.style.AppTheme_Lime_600_core
                -5983189 -> R.style.AppTheme_Lime_700_core
                -6382300 -> R.style.AppTheme_Lime_800_core
                -8227049 -> R.style.AppTheme_Lime_900_core

                -1596 -> R.style.AppTheme_Yellow_100_core
                -2672 -> R.style.AppTheme_Yellow_200_core
                -3722 -> R.style.AppTheme_Yellow_300_core
                -4520 -> R.style.AppTheme_Yellow_400_core
                -5317 -> R.style.AppTheme_Yellow_500_core
                -141259 -> R.style.AppTheme_Yellow_600_core
                -278483 -> R.style.AppTheme_Yellow_700_core
                -415707 -> R.style.AppTheme_Yellow_800_core
                -688361 -> R.style.AppTheme_Yellow_900_core

                -4941 -> R.style.AppTheme_Amber_100_core
                -8062 -> R.style.AppTheme_Amber_200_core
                -10929 -> R.style.AppTheme_Amber_300_core
                -13784 -> R.style.AppTheme_Amber_400_core
                -16121 -> R.style.AppTheme_Amber_500_core
                -19712 -> R.style.AppTheme_Amber_600_core
                -24576 -> R.style.AppTheme_Amber_700_core
                -28928 -> R.style.AppTheme_Amber_800_core
                -37120 -> R.style.AppTheme_Amber_900_core

                -8014 -> R.style.AppTheme_Orange_100_core
                -13184 -> R.style.AppTheme_Orange_200_core
                -18611 -> R.style.AppTheme_Orange_300_core
                -22746 -> R.style.AppTheme_Orange_400_core
                -26624 -> R.style.AppTheme_Orange_500_core
                -291840 -> R.style.AppTheme_Orange_600_core
                -689152 -> R.style.AppTheme_Orange_700_core
                -1086464 -> R.style.AppTheme_Orange_800_core
                -1683200 -> R.style.AppTheme_Orange_900_core

                -13124 -> R.style.AppTheme_Deep_Orange_100_core
                -21615 -> R.style.AppTheme_Deep_Orange_200_core
                -30107 -> R.style.AppTheme_Deep_Orange_300_core
                -36797 -> R.style.AppTheme_Deep_Orange_400_core
                -43230 -> R.style.AppTheme_Deep_Orange_500_core
                -765666 -> R.style.AppTheme_Deep_Orange_600_core
                -1684967 -> R.style.AppTheme_Deep_Orange_700_core
                -2604267 -> R.style.AppTheme_Deep_Orange_800_core
                -4246004 -> R.style.AppTheme_Deep_Orange_900_core

                -2634552 -> R.style.AppTheme_Brown_100_core
                -4412764 -> R.style.AppTheme_Brown_200_core
                -6190977 -> R.style.AppTheme_Brown_300_core
                -7508381 -> R.style.AppTheme_Brown_400_core
                -8825528 -> R.style.AppTheme_Brown_500_core
                -9614271 -> R.style.AppTheme_Brown_600_core
                -10665929 -> R.style.AppTheme_Brown_700_core
                -11652050 -> R.style.AppTheme_Brown_800_core
                -12703965 -> R.style.AppTheme_Brown_900_core

                -3155748 -> R.style.AppTheme_Blue_Grey_100_core
                -5194811 -> R.style.AppTheme_Blue_Grey_200_core
                -7297874 -> R.style.AppTheme_Blue_Grey_300_core
                -8875876 -> R.style.AppTheme_Blue_Grey_400_core
                -10453621 -> R.style.AppTheme_Blue_Grey_500_core
                -11243910 -> R.style.AppTheme_Blue_Grey_600_core
                -12232092 -> R.style.AppTheme_Blue_Grey_700_core
                -13154481 -> R.style.AppTheme_Blue_Grey_800_core
                -14273992 -> R.style.AppTheme_Blue_Grey_900_core

                -1 -> R.style.AppTheme_Grey_100_core
                -1118482 -> R.style.AppTheme_Grey_200_core
                -2039584 -> R.style.AppTheme_Grey_300_core
                -4342339 -> R.style.AppTheme_Grey_400_core
                -6381922 -> R.style.AppTheme_Grey_500_core
                -9079435 -> R.style.AppTheme_Grey_600_core
                -10395295 -> R.style.AppTheme_Grey_700_core
                -12434878 -> R.style.AppTheme_Grey_800_core
                -16777216 -> R.style.AppTheme_Grey_900_core

                else -> R.style.AppTheme_Orange_700_core
            }
        }

        else -> {
            when (color) {
                -12846 -> R.style.AppTheme_Red_100
                -1074534 -> R.style.AppTheme_Red_200
                -1739917 -> R.style.AppTheme_Red_300
                -1092784 -> R.style.AppTheme_Red_400
                -769226 -> R.style.AppTheme_Red_500
                -1754827 -> R.style.AppTheme_Red_600
                -2937041 -> R.style.AppTheme_Red_700
                -3790808 -> R.style.AppTheme_Red_800
                -4776932 -> R.style.AppTheme_Red_900

                -476208 -> R.style.AppTheme_Pink_100
                -749647 -> R.style.AppTheme_Pink_200
                -1023342 -> R.style.AppTheme_Pink_300
                -1294214 -> R.style.AppTheme_Pink_400
                -1499549 -> R.style.AppTheme_Pink_500
                -2614432 -> R.style.AppTheme_Pink_600
                -4056997 -> R.style.AppTheme_Pink_700
                -5434281 -> R.style.AppTheme_Pink_800
                -7860657 -> R.style.AppTheme_Pink_900

                -1982745 -> R.style.AppTheme_Purple_100
                -3238952 -> R.style.AppTheme_Purple_200
                -4560696 -> R.style.AppTheme_Purple_300
                -5552196 -> R.style.AppTheme_Purple_400
                -6543440 -> R.style.AppTheme_Purple_500
                -7461718 -> R.style.AppTheme_Purple_600
                -8708190 -> R.style.AppTheme_Purple_700
                -9823334 -> R.style.AppTheme_Purple_800
                -11922292 -> R.style.AppTheme_Purple_900

                -3029783 -> R.style.AppTheme_Deep_Purple_100
                -5005861 -> R.style.AppTheme_Deep_Purple_200
                -6982195 -> R.style.AppTheme_Deep_Purple_300
                -8497214 -> R.style.AppTheme_Deep_Purple_400
                -10011977 -> R.style.AppTheme_Deep_Purple_500
                -10603087 -> R.style.AppTheme_Deep_Purple_600
                -11457112 -> R.style.AppTheme_Deep_Purple_700
                -12245088 -> R.style.AppTheme_Deep_Purple_800
                -13558894 -> R.style.AppTheme_Deep_Purple_900

                -3814679 -> R.style.AppTheme_Indigo_100
                -6313766 -> R.style.AppTheme_Indigo_200
                -8812853 -> R.style.AppTheme_Indigo_300
                -10720320 -> R.style.AppTheme_Indigo_400
                -12627531 -> R.style.AppTheme_Indigo_500
                -13022805 -> R.style.AppTheme_Indigo_600
                -13615201 -> R.style.AppTheme_Indigo_700
                -14142061 -> R.style.AppTheme_Indigo_800
                -15064194 -> R.style.AppTheme_Indigo_900

                -4464901 -> R.style.AppTheme_Blue_100
                -7288071 -> R.style.AppTheme_Blue_200
                -10177034 -> R.style.AppTheme_Blue_300
                -12409355 -> R.style.AppTheme_Blue_400
                -14575885 -> R.style.AppTheme_Blue_500
                -14776091 -> R.style.AppTheme_Blue_600
                -15108398 -> R.style.AppTheme_Blue_700
                -15374912 -> R.style.AppTheme_Blue_800
                -15906911 -> R.style.AppTheme_Blue_900

                -4987396 -> R.style.AppTheme_Light_Blue_100
                -8268550 -> R.style.AppTheme_Light_Blue_200
                -11549705 -> R.style.AppTheme_Light_Blue_300
                -14043396 -> R.style.AppTheme_Light_Blue_400
                -16537100 -> R.style.AppTheme_Light_Blue_500
                -16540699 -> R.style.AppTheme_Light_Blue_600
                -16611119 -> R.style.AppTheme_Light_Blue_700
                -16615491 -> R.style.AppTheme_Light_Blue_800
                -16689253 -> R.style.AppTheme_Light_Blue_900

                -5051406 -> R.style.AppTheme_Cyan_100
                -8331542 -> R.style.AppTheme_Cyan_200
                -11677471 -> R.style.AppTheme_Cyan_300
                -14235942 -> R.style.AppTheme_Cyan_400
                -16728876 -> R.style.AppTheme_Cyan_500
                -16732991 -> R.style.AppTheme_Cyan_600
                -16738393 -> R.style.AppTheme_Cyan_700
                -16743537 -> R.style.AppTheme_Cyan_800
                -16752540 -> R.style.AppTheme_Cyan_900

                -5054501 -> R.style.AppTheme_Teal_100
                -8336444 -> R.style.AppTheme_Teal_200
                -11684180 -> R.style.AppTheme_Teal_300
                -14244198 -> R.style.AppTheme_Teal_400
                -16738680 -> R.style.AppTheme_Teal_500
                -16742021 -> R.style.AppTheme_Teal_600
                -16746133 -> R.style.AppTheme_Teal_700
                -16750244 -> R.style.AppTheme_Teal_800
                -16757440 -> R.style.AppTheme_Teal_900

                -3610935 -> R.style.AppTheme_Green_100
                -5908825 -> R.style.AppTheme_Green_200
                -8271996 -> R.style.AppTheme_Green_300
                -10044566 -> R.style.AppTheme_Green_400
                -11751600 -> R.style.AppTheme_Green_500
                -12345273 -> R.style.AppTheme_Green_600
                -13070788 -> R.style.AppTheme_Green_700
                -13730510 -> R.style.AppTheme_Green_800
                -14983648 -> R.style.AppTheme_Green_900

                -2298424 -> R.style.AppTheme_Light_Green_100
                -3808859 -> R.style.AppTheme_Light_Green_200
                -5319295 -> R.style.AppTheme_Light_Green_300
                -6501275 -> R.style.AppTheme_Light_Green_400
                -7617718 -> R.style.AppTheme_Light_Green_500
                -8604862 -> R.style.AppTheme_Light_Green_600
                -9920712 -> R.style.AppTheme_Light_Green_700
                -11171025 -> R.style.AppTheme_Light_Green_800
                -13407970 -> R.style.AppTheme_Light_Green_900

                -985917 -> R.style.AppTheme_Lime_100
                -1642852 -> R.style.AppTheme_Lime_200
                -2300043 -> R.style.AppTheme_Lime_300
                -2825897 -> R.style.AppTheme_Lime_400
                -3285959 -> R.style.AppTheme_Lime_500
                -4142541 -> R.style.AppTheme_Lime_600
                -5983189 -> R.style.AppTheme_Lime_700
                -6382300 -> R.style.AppTheme_Lime_800
                -8227049 -> R.style.AppTheme_Lime_900

                -1596 -> R.style.AppTheme_Yellow_100
                -2672 -> R.style.AppTheme_Yellow_200
                -3722 -> R.style.AppTheme_Yellow_300
                -4520 -> R.style.AppTheme_Yellow_400
                -5317 -> R.style.AppTheme_Yellow_500
                -141259 -> R.style.AppTheme_Yellow_600
                -278483 -> R.style.AppTheme_Yellow_700
                -415707 -> R.style.AppTheme_Yellow_800
                -688361 -> R.style.AppTheme_Yellow_900

                -4941 -> R.style.AppTheme_Amber_100
                -8062 -> R.style.AppTheme_Amber_200
                -10929 -> R.style.AppTheme_Amber_300
                -13784 -> R.style.AppTheme_Amber_400
                -16121 -> R.style.AppTheme_Amber_500
                -19712 -> R.style.AppTheme_Amber_600
                -24576 -> R.style.AppTheme_Amber_700
                -28928 -> R.style.AppTheme_Amber_800
                -37120 -> R.style.AppTheme_Amber_900

                -8014 -> R.style.AppTheme_Orange_100
                -13184 -> R.style.AppTheme_Orange_200
                -18611 -> R.style.AppTheme_Orange_300
                -22746 -> R.style.AppTheme_Orange_400
                -26624 -> R.style.AppTheme_Orange_500
                -291840 -> R.style.AppTheme_Orange_600
                -689152 -> R.style.AppTheme_Orange_700
                -1086464 -> R.style.AppTheme_Orange_800
                -1683200 -> R.style.AppTheme_Orange_900

                -13124 -> R.style.AppTheme_Deep_Orange_100
                -21615 -> R.style.AppTheme_Deep_Orange_200
                -30107 -> R.style.AppTheme_Deep_Orange_300
                -36797 -> R.style.AppTheme_Deep_Orange_400
                -43230 -> R.style.AppTheme_Deep_Orange_500
                -765666 -> R.style.AppTheme_Deep_Orange_600
                -1684967 -> R.style.AppTheme_Deep_Orange_700
                -2604267 -> R.style.AppTheme_Deep_Orange_800
                -4246004 -> R.style.AppTheme_Deep_Orange_900

                -2634552 -> R.style.AppTheme_Brown_100
                -4412764 -> R.style.AppTheme_Brown_200
                -6190977 -> R.style.AppTheme_Brown_300
                -7508381 -> R.style.AppTheme_Brown_400
                -8825528 -> R.style.AppTheme_Brown_500
                -9614271 -> R.style.AppTheme_Brown_600
                -10665929 -> R.style.AppTheme_Brown_700
                -11652050 -> R.style.AppTheme_Brown_800
                -12703965 -> R.style.AppTheme_Brown_900

                -3155748 -> R.style.AppTheme_Blue_Grey_100
                -5194811 -> R.style.AppTheme_Blue_Grey_200
                -7297874 -> R.style.AppTheme_Blue_Grey_300
                -8875876 -> R.style.AppTheme_Blue_Grey_400
                -10453621 -> R.style.AppTheme_Blue_Grey_500
                -11243910 -> R.style.AppTheme_Blue_Grey_600
                -12232092 -> R.style.AppTheme_Blue_Grey_700
                -13154481 -> R.style.AppTheme_Blue_Grey_800
                -14273992 -> R.style.AppTheme_Blue_Grey_900

                -1 -> R.style.AppTheme_Grey_100
                -1118482 -> R.style.AppTheme_Grey_200
                -2039584 -> R.style.AppTheme_Grey_300
                -4342339 -> R.style.AppTheme_Grey_400
                -6381922 -> R.style.AppTheme_Grey_500
                -9079435 -> R.style.AppTheme_Grey_600
                -10395295 -> R.style.AppTheme_Grey_700
                -12434878 -> R.style.AppTheme_Grey_800
                -16777216 -> R.style.AppTheme_Grey_900

                else -> R.style.AppTheme_Orange_700
            }
        }
    }
