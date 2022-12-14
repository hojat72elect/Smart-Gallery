package ca.on.sudbury.hojat.smartgallery.extensions

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.ActivityNotFoundException
import android.content.ContentProviderOperation
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.media.MediaScannerConnection
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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.FragmentActivity
import ca.on.sudbury.hojat.smartgallery.BuildConfig
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.base.SimpleActivity
import ca.on.sudbury.hojat.smartgallery.databases.GalleryDatabase
import ca.on.sudbury.hojat.smartgallery.dialogs.AppSideLoadedDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.ConfirmationAdvancedDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.ConfirmationDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.PickDirectoryDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.SecurityDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.WhatsNewDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.WritePermissionDialogFragment
import ca.on.sudbury.hojat.smartgallery.helpers.CREATE_DOCUMENT_SDK_30
import ca.on.sudbury.hojat.smartgallery.helpers.DARK_GREY
import ca.on.sudbury.hojat.smartgallery.helpers.EXTRA_SHOW_ADVANCED
import ca.on.sudbury.hojat.smartgallery.helpers.IS_FROM_GALLERY
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_APNG
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_CROPPER
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_EXOPLAYER
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_FILTERS
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_GESTURE_VIEWS
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_GIF_DRAWABLE
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_GLIDE
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_PANORAMA_VIEW
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_PATTERN
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_PICASSO
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_REPRINT
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_RTL
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_SANSELAN
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_SUBSAMPLING
import ca.on.sudbury.hojat.smartgallery.helpers.NOMEDIA
import ca.on.sudbury.hojat.smartgallery.helpers.OPEN_DOCUMENT_TREE_FOR_ANDROID_DATA_OR_OBB
import ca.on.sudbury.hojat.smartgallery.helpers.OPEN_DOCUMENT_TREE_FOR_SDK_30
import ca.on.sudbury.hojat.smartgallery.helpers.OPEN_DOCUMENT_TREE_OTG
import ca.on.sudbury.hojat.smartgallery.helpers.OPEN_DOCUMENT_TREE_SD
import ca.on.sudbury.hojat.smartgallery.helpers.REAL_FILE_PATH
import ca.on.sudbury.hojat.smartgallery.helpers.RECYCLE_BIN
import ca.on.sudbury.hojat.smartgallery.helpers.REQUEST_EDIT_IMAGE
import ca.on.sudbury.hojat.smartgallery.helpers.REQUEST_SET_AS
import ca.on.sudbury.hojat.smartgallery.helpers.SIDELOADING_FALSE
import ca.on.sudbury.hojat.smartgallery.helpers.SIDELOADING_TRUE
import ca.on.sudbury.hojat.smartgallery.models.Android30RenameFormat
import ca.on.sudbury.hojat.smartgallery.models.DateTaken
import ca.on.sudbury.hojat.smartgallery.models.FaqItem
import ca.on.sudbury.hojat.smartgallery.models.FileDirItem
import ca.on.sudbury.hojat.smartgallery.models.Release
import ca.on.sudbury.hojat.smartgallery.usecases.EmptyTheRecycleBinUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.GetFileExtensionUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.GetFileSizeUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.GetMimeTypeUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.HideKeyboardUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsNougatPlusUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsPathOnOtgUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsPathOnSdUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsRPlusUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsSPlusUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.RunOnBackgroundThreadUseCase
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

private fun createSAFFileSdk30(owner: Context, path: String): Boolean {
    return try {
        val treeUri = owner.createFirstParentTreeUri(path)
        val parentPath = path.getParentPath()
        if (!owner.getDoesFilePathExistSdk30(parentPath)) {
            owner.createSAFDirectorySdk30(parentPath)
        }

        val documentId = owner.getSAFDocumentId(parentPath)
        val parentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
        DocumentsContract.createDocument(
            owner.contentResolver,
            parentUri,
            path.getMimeType(),
            path.getFilenameFromPath()
        ) != null
    } catch (e: IllegalStateException) {
        if (owner is AppCompatActivity) {
            Toast.makeText(owner, e.toString(), Toast.LENGTH_LONG).show()
        }
        false
    }
}

// It's been used only by "renameFile" extension function
private fun renameCasually(
    owner: BaseSimpleActivity,
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
        if (IsRPlusUseCase() && exception is java.nio.file.FileSystemException) {
            // if we are renaming multiple files at once, we should give the Android 30+ permission dialog all uris together, not one by one
            if (isRenamingMultipleFiles) {
                callback?.invoke(false, Android30RenameFormat.CONTENT_RESOLVER)
            } else {
                val fileUris =
                    owner.getFileUrisFromFileDirItems(arrayListOf(File(oldPath).toFileDirItem(owner)))
                owner.updateSDK30Uris(fileUris) { success ->
                    if (success) {
                        val values = ContentValues().apply {
                            put(Images.Media.DISPLAY_NAME, newPath.getFilenameFromPath())
                        }

                        try {
                            owner.contentResolver.update(fileUris.first(), values, null, null)
                            callback?.invoke(true, Android30RenameFormat.NONE)
                        } catch (e: Exception) {
                            Toast.makeText(owner, e.toString(), Toast.LENGTH_LONG).show()
                            callback?.invoke(false, Android30RenameFormat.NONE)
                        }
                    } else {
                        callback?.invoke(false, Android30RenameFormat.NONE)
                    }
                }
            }
        } else {
            if (exception is IOException && File(oldPath).isDirectory && owner.isRestrictedWithSAFSdk30(
                    oldPath
                )
            ) {
                Toast.makeText(owner, R.string.cannot_rename_folder, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(owner, exception.toString(), Toast.LENGTH_LONG).show()
            }
            callback?.invoke(false, Android30RenameFormat.NONE)
        }
        return
    }

    val oldToTempSucceeds = oldFile.renameTo(tempFile)
    val tempToNewSucceeds = tempFile.renameTo(newFile)
    if (oldToTempSucceeds && tempToNewSucceeds) {
        if (newFile.isDirectory) {
            owner.updateInMediaStore(oldPath, newPath)
            owner.applicationContext.rescanPaths(arrayListOf(newPath)) {
                owner.runOnUiThread {
                    callback?.invoke(true, Android30RenameFormat.NONE)
                }
                if (!oldPath.equals(newPath, true)) {
                    owner.deleteFromMediaStore(oldPath)
                }
                owner.scanPathRecursively(newPath)
            }
        } else {
            if (!owner.baseConfig.keepLastModified) {
                newFile.setLastModified(System.currentTimeMillis())
            }
            owner.updateInMediaStore(oldPath, newPath)
            owner.scanPathsRecursively(arrayListOf(newPath)) {
                if (!oldPath.equals(newPath, true)) {
                    owner.deleteFromMediaStore(oldPath)
                }
                owner.runOnUiThread {
                    callback?.invoke(true, Android30RenameFormat.NONE)
                }
            }
        }
    } else {
        tempFile.delete()
        newFile.delete()
        if (IsRPlusUseCase()) {
            // if we are renaming multiple files at once, we should give the Android 30+ permission dialog all uris together, not one by one
            if (isRenamingMultipleFiles) {
                callback?.invoke(false, Android30RenameFormat.SAF)
            } else {
                val fileUris =
                    owner.getFileUrisFromFileDirItems(arrayListOf(File(oldPath).toFileDirItem(owner)))
                owner.updateSDK30Uris(fileUris) { success ->
                    if (!success) {
                        return@updateSDK30Uris
                    }
                    try {
                        val sourceUri = fileUris.first()
                        val sourceFile = File(oldPath).toFileDirItem(owner)

                        if (oldPath.equals(newPath, true)) {
                            val tempDestination = try {
                                createTempFile(File(sourceFile.path)) ?: return@updateSDK30Uris
                            } catch (exception: Exception) {
                                Toast.makeText(owner, exception.toString(), Toast.LENGTH_LONG)
                                    .show()
                                callback?.invoke(false, Android30RenameFormat.NONE)
                                return@updateSDK30Uris
                            }

                            val copyTempSuccess =
                                owner.copySingleFileSdk30(
                                    sourceFile,
                                    tempDestination.toFileDirItem(owner)
                                )
                            if (copyTempSuccess) {
                                owner.contentResolver.delete(sourceUri, null)
                                tempDestination.renameTo(File(newPath))
                                if (!owner.baseConfig.keepLastModified) {
                                    newFile.setLastModified(System.currentTimeMillis())
                                }
                                owner.updateInMediaStore(oldPath, newPath)
                                owner.scanPathsRecursively(arrayListOf(newPath)) {
                                    owner.runOnUiThread {
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
                            val copySuccessful =
                                owner.copySingleFileSdk30(sourceFile, destinationFile)
                            if (copySuccessful) {
                                if (!owner.baseConfig.keepLastModified) {
                                    newFile.setLastModified(System.currentTimeMillis())
                                }
                                owner.contentResolver.delete(sourceUri, null)
                                owner.updateInMediaStore(oldPath, newPath)
                                owner.scanPathsRecursively(arrayListOf(newPath)) {
                                    owner.runOnUiThread {
                                        callback?.invoke(true, Android30RenameFormat.NONE)
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    owner,
                                    R.string.unknown_error_occurred,
                                    Toast.LENGTH_LONG
                                ).show()
                                callback?.invoke(false, Android30RenameFormat.NONE)
                            }
                        }

                    } catch (e: Exception) {
                        Toast.makeText(owner, e.toString(), Toast.LENGTH_LONG).show()
                        callback?.invoke(false, Android30RenameFormat.NONE)
                    }
                }
            }
        } else {
            Toast.makeText(owner, R.string.unknown_error_occurred, Toast.LENGTH_LONG).show()
            callback?.invoke(false, Android30RenameFormat.NONE)
        }
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
        Toast.makeText(activity, e.toString(), Toast.LENGTH_LONG).show()
        null
    }
}

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

private fun deleteRecursively(file: File): Boolean {
    if (file.isDirectory) {
        val files = file.listFiles() ?: return file.delete()
        for (child in files) {
            deleteRecursively(child)
        }
    }

    return file.delete()
}

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
                callback.invoke(applicationContext.contentResolver.openOutputStream(uri))
            }
        }
        (!IsRPlusUseCase() && (IsPathOnSdUseCase(this, fileDirItem.path) || IsPathOnOtgUseCase(
            this,
            fileDirItem.path
        )) && !isSDCardSetAsDefaultStorage()) -> {
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
                        callback(applicationContext.contentResolver.openOutputStream(document.uri))
                    } catch (e: FileNotFoundException) {
                        Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
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
                            createSAFFileSdk30(this, fileDirItem.path)
                        }
                        applicationContext.contentResolver.openOutputStream(uri)
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
                    applicationContext.contentResolver.openOutputStream(fileUri.first())
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

fun BaseSimpleActivity.getFileOutputStreamSync(
    path: String,
    mimeType: String,
    parentDocumentFile: DocumentFile? = null
): OutputStream? {
    val targetFile = File(path)

    return when {
        isRestrictedSAFOnlyRoot(path) -> {
            val uri = getAndroidSAFUri(path)
            if (!getDoesFilePathExist(path)) {
                createAndroidSAFFile(path)
            }
            applicationContext.contentResolver.openOutputStream(uri)
        }
        (!IsRPlusUseCase() && (
                IsPathOnSdUseCase(this, path) ||
                        IsPathOnOtgUseCase(this, path)) && !isSDCardSetAsDefaultStorage()) -> {
            var documentFile = parentDocumentFile
            if (documentFile == null) {
                if (targetFile.parentFile?.let { getDoesFilePathExist(it.absolutePath) } == true) {
                    documentFile = targetFile.parent?.let { getDocumentFile(it) }
                } else {
                    documentFile = targetFile.parentFile?.let {
                        it.parent?.let { it1 ->
                            getDocumentFile(
                                it1
                            )
                        }
                    }
                    documentFile =
                        targetFile.parentFile?.let { documentFile!!.createDirectory(it.name) }
                            ?: targetFile.parentFile?.let { getDocumentFile(it.absolutePath) }
                }
            }

            if (documentFile == null) {
                val casualOutputStream =
                    createCasualFileOutputStream(
                        this,
                        targetFile
                    )
                return if (casualOutputStream == null) {
                    targetFile.parent?.let { showFileCreateError(it) }
                    null
                } else {
                    casualOutputStream
                }
            }

            try {
                val uri = if (getDoesFilePathExist(path)) {
                    createDocumentUriFromRootTree(path)
                } else {
                    documentFile.createFile(mimeType, path.getFilenameFromPath())!!.uri
                }
                applicationContext.contentResolver.openOutputStream(uri)
            } catch (e: Exception) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                null
            }
        }
        isAccessibleWithSAFSdk30(path) -> {
            try {
                val uri = createDocumentUriUsingFirstParentTreeUri(path)
                if (!getDoesFilePathExist(path)) {
                    createSAFFileSdk30(this, path)
                }
                applicationContext.contentResolver.openOutputStream(uri)
            } catch (e: Exception) {
                null
            } ?: createCasualFileOutputStream(
                this,
                targetFile
            )
        }
        else -> return createCasualFileOutputStream(
            this,
            targetFile
        )
    }
}

fun AppCompatActivity.sharePathIntent(path: String, applicationId: String) {
    RunOnBackgroundThreadUseCase {

        val newUri = getFinalUriFromPath(path, applicationId) ?: return@RunOnBackgroundThreadUseCase
        Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, newUri)
            type = getUriMimeType(path, newUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            try {
                startActivity(Intent.createChooser(this, getString(R.string.share_via)))
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this@sharePathIntent, R.string.no_app_found, Toast.LENGTH_LONG)
                    .show()
            } catch (e: RuntimeException) {
                if (e.cause is TransactionTooLargeException) {
                    Toast.makeText(
                        this@sharePathIntent,
                        R.string.maximum_share_reached,
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(this@sharePathIntent, e.toString(), Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@sharePathIntent, e.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }
}

fun AppCompatActivity.sharePathsIntent(paths: List<String>, applicationId: String) {
    RunOnBackgroundThreadUseCase {

        if (paths.size == 1) {
            sharePathIntent(paths.first(), applicationId)
        } else {
            val uriPaths = ArrayList<String>()
            val newUris = paths.map {
                val uri =
                    getFinalUriFromPath(it, applicationId) ?: return@RunOnBackgroundThreadUseCase
                uriPaths.add(uri.path!!)
                uri
            } as ArrayList<Uri>

            var mimeType = GetMimeTypeUseCase(uriPaths)
            if (mimeType.isEmpty() || mimeType == "*/*") {
                mimeType = GetMimeTypeUseCase(paths)
            }

            Intent().apply {
                action = Intent.ACTION_SEND_MULTIPLE
                type = mimeType
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, newUris)

                try {
                    startActivity(Intent.createChooser(this, getString(R.string.share_via)))
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(this@sharePathsIntent, R.string.no_app_found, Toast.LENGTH_LONG)
                        .show()
                } catch (e: RuntimeException) {
                    if (e.cause is TransactionTooLargeException) {
                        Toast.makeText(
                            this@sharePathsIntent,
                            R.string.maximum_share_reached,
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(this@sharePathsIntent, e.toString(), Toast.LENGTH_LONG)
                            .show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@sharePathsIntent, e.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

fun AppCompatActivity.setAs(path: String) {
    val applicationId = BuildConfig.APPLICATION_ID
    RunOnBackgroundThreadUseCase {
        val newUri = getFinalUriFromPath(path, applicationId) ?: return@RunOnBackgroundThreadUseCase
        Intent().apply {
            action = Intent.ACTION_ATTACH_DATA
            setDataAndType(newUri, getUriMimeType(path, newUri))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val chooser = Intent.createChooser(this, getString(R.string.set_as))

            try {
                startActivityForResult(chooser, REQUEST_SET_AS)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this@setAs, R.string.no_app_found, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this@setAs, e.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }
}

fun AppCompatActivity.openPath(
    path: String,
    forceChooser: Boolean,
    extras: HashMap<String, Boolean> = HashMap()
) {
    openPathIntent(path, forceChooser, BuildConfig.APPLICATION_ID, extras = extras)
}

fun AppCompatActivity.openEditor(path: String, forceChooser: Boolean = false) {
    val newPath = path.removePrefix("file://")
    openEditorIntent(newPath, forceChooser, BuildConfig.APPLICATION_ID)
}

fun AppCompatActivity.getFinalUriFromPath(path: String, applicationId: String): Uri? {
    val uri = try {
        ensurePublicUri(this, path, applicationId)
    } catch (e: Exception) {
        Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
        return null
    }

    if (uri == null) {
        Toast.makeText(this, R.string.unknown_error_occurred, Toast.LENGTH_LONG).show()
        return null
    }

    return uri
}

fun AppCompatActivity.handleDeletePasswordProtection(callback: () -> Unit) {
    if (baseConfig.isDeletePasswordProtectionOn) {

        val callbackAfterSecurityReceived: (
            hash: String,
            type: Int,
            success: Boolean
        ) -> Unit = { _, _, success ->
            if (success) {
                callback()
            }
        }
        SecurityDialogFragment(
            baseConfig.deletePasswordHash,
            baseConfig.deleteProtectionType,
            callbackAfterSecurityReceived
        ).show(
            (this as FragmentActivity).supportFragmentManager,
            SecurityDialogFragment.TAG
        )

    } else {
        callback()
    }
}

fun AppCompatActivity.handleHiddenFolderPasswordProtection(callback: () -> Unit) {
    if (baseConfig.isHiddenPasswordProtectionOn) {

        val callbackAfterSecurityReceived: (hash: String, type: Int, success: Boolean) -> Unit =
            { _, _, success ->
                if (success) {
                    callback()
                }
            }
        SecurityDialogFragment(
            baseConfig.hiddenPasswordHash,
            baseConfig.hiddenProtectionType,
            callbackAfterSecurityReceived
        ).show(
            (this as FragmentActivity).supportFragmentManager,
            SecurityDialogFragment.TAG
        )

    } else {
        callback()
    }
}

fun AppCompatActivity.handleLockedFolderOpening(
    path: String,
    callback: (success: Boolean) -> Unit
) {
    if (baseConfig.isFolderProtected(path)) {
        val callbackAfterSecurityReceived: (hash: String, type: Int, success: Boolean) -> Unit =
            { _, _, success ->
                callback(success)
            }
        SecurityDialogFragment(
            baseConfig.getFolderProtectionHash(path),
            baseConfig.getFolderProtectionType(path),
            callbackAfterSecurityReceived
        ).show(
            (this as FragmentActivity).supportFragmentManager,
            SecurityDialogFragment.TAG
        )

    } else {
        callback(true)
    }
}

fun SimpleActivity.launchAbout() {
    val licenses =
        LICENSE_GLIDE or LICENSE_CROPPER or
                LICENSE_RTL or LICENSE_SUBSAMPLING or
                LICENSE_PATTERN or LICENSE_REPRINT or
                LICENSE_GIF_DRAWABLE or LICENSE_PICASSO or
                LICENSE_EXOPLAYER or LICENSE_PANORAMA_VIEW or
                LICENSE_SANSELAN or LICENSE_FILTERS or
                LICENSE_GESTURE_VIEWS or LICENSE_APNG

    val faqItems = arrayListOf(
        FaqItem(R.string.faq_3_title, R.string.faq_3_text),
        FaqItem(R.string.faq_12_title, R.string.faq_12_text),
        FaqItem(R.string.faq_7_title, R.string.faq_7_text),
        FaqItem(R.string.faq_14_title, R.string.faq_14_text),
        FaqItem(R.string.faq_1_title, R.string.faq_1_text),
        FaqItem(R.string.faq_5_title_commons, R.string.faq_5_text_commons),
        FaqItem(R.string.faq_5_title, R.string.faq_5_text),
        FaqItem(R.string.faq_4_title, R.string.faq_4_text),
        FaqItem(R.string.faq_6_title, R.string.faq_6_text),
        FaqItem(R.string.faq_8_title, R.string.faq_8_text),
        FaqItem(R.string.faq_10_title, R.string.faq_10_text),
        FaqItem(R.string.faq_11_title, R.string.faq_11_text),
        FaqItem(R.string.faq_13_title, R.string.faq_13_text),
        FaqItem(R.string.faq_15_title, R.string.faq_15_text),
        FaqItem(R.string.faq_2_title, R.string.faq_2_text),
        FaqItem(R.string.faq_18_title, R.string.faq_18_text),
        FaqItem(R.string.faq_9_title_commons, R.string.faq_9_text_commons),
    )

    if (!resources.getBoolean(R.bool.hide_google_relations)) {
        faqItems.add(FaqItem(R.string.faq_2_title_commons, R.string.faq_2_text_commons))
        faqItems.add(FaqItem(R.string.faq_6_title_commons, R.string.faq_6_text_commons))
        faqItems.add(FaqItem(R.string.faq_7_title_commons, R.string.faq_7_text_commons))
        faqItems.add(FaqItem(R.string.faq_10_title_commons, R.string.faq_10_text_commons))
    }

    if (IsRPlusUseCase() && !isExternalStorageManager()) {
        faqItems.add(0, FaqItem(R.string.faq_16_title, R.string.faq_16_text))
        faqItems.add(1, FaqItem(R.string.faq_17_title, R.string.faq_17_text))
        faqItems.removeIf { it.text == R.string.faq_7_text }
        faqItems.removeIf { it.text == R.string.faq_14_text }
        faqItems.removeIf { it.text == R.string.faq_8_text }
    }

    startAboutActivity(R.string.app_name, licenses, BuildConfig.VERSION_NAME, faqItems, true)
}

fun BaseSimpleActivity.handleMediaManagementPrompt(callback: () -> Unit) {
    if ((IsSPlusUseCase() && MediaStore.canManageMedia(this)) || isExternalStorageManager()) {
        callback()
    } else if (IsRPlusUseCase() && resources.getBoolean(R.bool.require_all_files_access)) {
        if (Environment.isExternalStorageManager()) {
            callback()
        } else {
            var messagePrompt = getString(R.string.access_storage_prompt)
            if (IsSPlusUseCase()) {
                messagePrompt += "\n\n${getString(R.string.media_management_alternative)}"
            }
            val callbackAfterDialog: (Boolean) -> Unit = { result ->
                if (result) {
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
                            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    finish()
                }
            }
            ConfirmationAdvancedDialogFragment(
                messagePrompt,
                0,
                R.string.ok,
                0,
                callbackAfterDialog
            ).show(
                supportFragmentManager,
                ConfirmationAdvancedDialogFragment.TAG
            )
        }
    } else if (IsSPlusUseCase() && !MediaStore.canManageMedia(this) && !isExternalStorageManager()) {
        val message =
            "${getString(R.string.media_management_prompt)}\n\n${getString(R.string.media_management_note)}"
        val callbackAfterDialogConfirmed: () -> Unit = {
            launchMediaManagementIntent(callback)
        }
        ConfirmationDialogFragment(
            message = message,
            messageId = 0,
            positive = R.string.ok,
            negative = 0,
            callbackAfterDialogConfirmed = callbackAfterDialogConfirmed
        ).show(supportFragmentManager, ConfirmationDialogFragment.TAG)
    } else {
        callback()
    }
}

fun BaseSimpleActivity.addNoMedia(path: String, callback: () -> Unit) {
    val file = File(path, NOMEDIA)
    if (getDoesFilePathExist(file.absolutePath)) {
        callback()
        return
    }

    if (!IsRPlusUseCase() && (
                IsPathOnSdUseCase(this, path) ||
                        IsPathOnOtgUseCase(this, path)) && !isSDCardSetAsDefaultStorage()
    ) {
        handleSAFDialog(file.absolutePath) {
            if (!it) {
                return@handleSAFDialog
            }

            val fileDocument = getDocumentFile(path)
            if (fileDocument?.exists() == true && fileDocument.isDirectory) {
                fileDocument.createFile("", NOMEDIA)
                addNoMediaIntoMediaStore(this, file.absolutePath)
                callback()
            } else {
                Toast.makeText(this, R.string.unknown_error_occurred, Toast.LENGTH_LONG).show()
                callback()
            }
        }
    } else {
        try {
            if (file.createNewFile()) {
                RunOnBackgroundThreadUseCase {
                    addNoMediaIntoMediaStore(this, file.absolutePath)
                }
            } else {
                Toast.makeText(this, R.string.unknown_error_occurred, Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
        }
        callback()
    }
}

private fun addNoMediaIntoMediaStore(owner: BaseSimpleActivity, path: String) {
    try {
        val content = ContentValues().apply {
            put(Files.FileColumns.TITLE, NOMEDIA)
            put(Files.FileColumns.DATA, path)
            put(Files.FileColumns.MEDIA_TYPE, Files.FileColumns.MEDIA_TYPE_NONE)
        }
        owner.contentResolver.insert(Files.getContentUri("external"), content)
    } catch (e: Exception) {
        Toast.makeText(owner, e.toString(), Toast.LENGTH_LONG).show()
    }
}

private fun deleteFile(
    owner: BaseSimpleActivity,
    fileDirItem: FileDirItem,
    allowDeleteFolder: Boolean = false,
    isDeletingMultipleFiles: Boolean,
    callback: ((wasSuccess: Boolean) -> Unit)? = null
) {
    RunOnBackgroundThreadUseCase {
        owner.deleteFileBg(fileDirItem, allowDeleteFolder, isDeletingMultipleFiles, callback)
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
        if (!IsRPlusUseCase() && file.absolutePath.startsWith(internalStoragePath) && !file.canWrite()) {
            callback?.invoke(false)
            return
        }

        var fileDeleted = !IsPathOnOtgUseCase(this, path) &&
                ((!file.exists() && file.length() == 0L) || file.delete())
        if (fileDeleted) {
            deleteFromMediaStore(path) { needsRescan ->
                if (needsRescan) {
                    rescanAndDeletePath(this, path) {
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
                fileDeleted = deleteRecursively(file)
            }

            if (!fileDeleted) {
                if (!IsRPlusUseCase() &&
                    (
                            IsPathOnSdUseCase(this, path) || IsPathOnOtgUseCase(this, path)) &&
                    !isSDCardSetAsDefaultStorage()
                ) {
                    handleSAFDialog(path) {
                        if (it) {
                            trySAFFileDelete(fileDirItem, allowDeleteFolder, callback)
                        }
                    }
                } else if (isAccessibleWithSAFSdk30(path)) {
                    if (IsSPlusUseCase() && MediaStore.canManageMedia(this)) {
                        deleteSdk30(fileDirItem, callback)
                    } else {
                        handleSAFDialogSdk30(path) {
                            if (it) {
                                deleteDocumentWithSAFSdk30(fileDirItem, allowDeleteFolder, callback)
                            }
                        }
                    }
                } else if (IsRPlusUseCase() && !isDeletingMultipleFiles) {
                    deleteSdk30(fileDirItem, callback)
                } else {
                    callback?.invoke(false)
                }
            }
        }
    }
}

fun BaseSimpleActivity.deleteFiles(
    files: List<FileDirItem>,
    allowDeleteFolder: Boolean = false,
    callback: ((wasSuccess: Boolean) -> Unit)? = null
) {
    RunOnBackgroundThreadUseCase {
        deleteFilesBg(this, files, allowDeleteFolder, callback)
    }
}

fun BaseSimpleActivity.removeNoMedia(path: String, callback: (() -> Unit)? = null) {
    val file = File(path, NOMEDIA)
    if (!getDoesFilePathExist(file.absolutePath)) {
        callback?.invoke()
        return
    }

    tryDeleteFileDirItem(
        file.toFileDirItem(applicationContext),
        allowDeleteFolder = false,
        deleteFromDatabase = false
    ) {
        callback?.invoke()
        deleteFromMediaStore(file.absolutePath)
        rescanFolderMedia(path)
    }
}

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
                RunOnBackgroundThreadUseCase {
                    val success = renameAndroidSAFDocument(this, oldPath, newPath)
                    runOnUiThread {
                        callback?.invoke(success, Android30RenameFormat.NONE)
                    }
                }

            } catch (e: Exception) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                runOnUiThread {
                    callback?.invoke(false, Android30RenameFormat.NONE)
                }
            }
        }
    } else if (isAccessibleWithSAFSdk30(oldPath)) {
        if ((IsSPlusUseCase() && MediaStore.canManageMedia(this)) && !File(oldPath).isDirectory && isPathOnInternalStorage(
                oldPath
            )
        ) {
            renameCasually(this, oldPath, newPath, isRenamingMultipleFiles, callback)
        } else {
            handleSAFDialogSdk30(oldPath) {
                if (!it) {
                    return@handleSAFDialogSdk30
                }

                try {
                    RunOnBackgroundThreadUseCase {

                        val success = renameDocumentSdk30(this, oldPath, newPath)
                        if (success) {
                            updateInMediaStore(oldPath, newPath)
                            applicationContext.rescanPaths(arrayListOf(newPath)) {
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
                    Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                    runOnUiThread {
                        callback?.invoke(false, Android30RenameFormat.NONE)
                    }
                }
            }
        }
    } else if (!IsRPlusUseCase() && (
                IsPathOnSdUseCase(this, newPath) ||
                        IsPathOnOtgUseCase(this, newPath)) && !isSDCardSetAsDefaultStorage()
    ) {
        handleSAFDialog(newPath) {
            if (!it) {
                return@handleSAFDialog
            }

            val document = getSomeDocumentFile(oldPath)
            if (document == null || (File(oldPath).isDirectory != document.isDirectory)) {
                runOnUiThread {
                    Toast.makeText(this, R.string.unknown_error_occurred, Toast.LENGTH_LONG).show()
                    callback?.invoke(false, Android30RenameFormat.NONE)
                }
                return@handleSAFDialog
            }

            try {
                RunOnBackgroundThreadUseCase {

                    try {
                        DocumentsContract.renameDocument(
                            applicationContext.contentResolver,
                            document.uri,
                            newPath.getFilenameFromPath()
                        )
                    } catch (ignored: FileNotFoundException) {
                        // FileNotFoundException is thrown in some weird cases, but renaming works just fine
                    } catch (e: Exception) {
                        Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                        callback?.invoke(false, Android30RenameFormat.NONE)
                        return@RunOnBackgroundThreadUseCase
                    }

                    updateInMediaStore(oldPath, newPath)
                    applicationContext.rescanPaths(arrayListOf(oldPath, newPath)) {
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
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                runOnUiThread {
                    callback?.invoke(false, Android30RenameFormat.NONE)
                }
            }
        }
    } else renameCasually(this, oldPath, newPath, isRenamingMultipleFiles, callback)
}

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

        RunOnBackgroundThreadUseCase {
            updateDBMediaPath(oldPath, newPath)
        }
    }
}

fun BaseSimpleActivity.tryCopyMoveFilesTo(
    fileDirItems: ArrayList<FileDirItem>,
    isCopyOperation: Boolean,
    callback: (destinationPath: String) -> Unit
) {
    if (fileDirItems.isEmpty()) {
        Toast.makeText(this, R.string.unknown_error_occurred, Toast.LENGTH_LONG).show()
        return
    }

    val source = fileDirItems[0].getParentPath()
    val callbackAfterDialogConfirmed: (String) -> Unit = { path ->
        val destination = path
        handleSAFDialog(source) {
            if (it) {
                copyMoveFilesTo(
                    fileDirItems,
                    source.trimEnd('/'),
                    destination,
                    isCopyOperation,
                    true,
                    config.shouldShowHidden,
                    callback
                )
            }
        }
    }
    PickDirectoryDialogFragment(
        source,
        showFavoritesBin = false,
        isPickingCopyMoveDestination = true,
        callback = callbackAfterDialogConfirmed
    ).show(supportFragmentManager, PickDirectoryDialogFragment.TAG)
}

fun BaseSimpleActivity.tryDeleteFileDirItem(
    fileDirItem: FileDirItem, allowDeleteFolder: Boolean = false, deleteFromDatabase: Boolean,
    callback: ((wasSuccess: Boolean) -> Unit)? = null
) {
    deleteFile(this, fileDirItem, allowDeleteFolder, isDeletingMultipleFiles = false) {
        if (deleteFromDatabase) {
            RunOnBackgroundThreadUseCase {
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
    RunOnBackgroundThreadUseCase {

        var pathsCnt = paths.size
        val otgPath = config.otgPath

        for (source in paths) {
            if (otgPath.isNotEmpty() && source.startsWith(otgPath)) {
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

                    if (GetFileSizeUseCase(
                            fileDocument,
                            true
                        ) == copiedSize && getDoesFilePathExist(
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
                    Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                    return@RunOnBackgroundThreadUseCase
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
                    Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                    return@RunOnBackgroundThreadUseCase
                }
            }
        }
        callback?.invoke(pathsCnt == 0)
    }
}

fun BaseSimpleActivity.restoreRecycleBinPaths(paths: ArrayList<String>, callback: () -> Unit) {
    RunOnBackgroundThreadUseCase {
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
                    Toast.makeText(
                        this,
                        getString(R.string.restore_to_path, humanizePath(picturesDirectory)),
                        Toast.LENGTH_LONG
                    ).show()
                    shownRestoringToPictures = true
                }
            }

            val lastModified = File(source).lastModified()

            val isShowingSAF = handleSAFDialog(destination) {}
            if (isShowingSAF) {
                return@RunOnBackgroundThreadUseCase
            }

            val isShowingSAFSdk30 = handleSAFDialogSdk30(destination) {}
            if (isShowingSAFSdk30) {
                return@RunOnBackgroundThreadUseCase
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
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
            } finally {
                inputStream?.close()
                out?.close()
            }
        }

        runOnUiThread {
            callback()
        }

        applicationContext.rescanPaths(newPaths) {
            fixDateTaken(newPaths, false)
        }
    }
}

fun BaseSimpleActivity.emptyAndDisableTheRecycleBin(callback: () -> Unit) {
    RunOnBackgroundThreadUseCase {
        EmptyTheRecycleBinUseCase(this) {
            config.useRecycleBin = false
            callback()
        }
    }
}

fun BaseSimpleActivity.showRecycleBinEmptyingDialog(callback: () -> Unit) {
    val callbackAfterDialogConfirmed: () -> Unit = { callback() }
    ConfirmationDialogFragment(
        message = "",
        messageId = R.string.empty_recycle_bin_confirmation,
        positive = R.string.yes,
        negative = R.string.no,
        callbackAfterDialogConfirmed = callbackAfterDialogConfirmed
    ).show(supportFragmentManager, ConfirmationDialogFragment.TAG)
}

fun BaseSimpleActivity.updateFavoritePaths(
    fileDirItems: ArrayList<FileDirItem>,
    destination: String
) {
    RunOnBackgroundThreadUseCase {
        fileDirItems.forEach {
            val newPath = "$destination/${it.name}"
            updateDBMediaPath(it.path, newPath)
        }
    }
}

fun AppCompatActivity.hasNavBar(): Boolean {
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
    val batchSize = 50
    if (showToasts && !hasRescanned) {
        Toast.makeText(this, R.string.fixing, Toast.LENGTH_LONG).show()
    }

    val pathsToRescan = ArrayList<String>()
    try {
        var didUpdateFile = false
        val operations = ArrayList<ContentProviderOperation>()
        RunOnBackgroundThreadUseCase {
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

                    if (operations.size % batchSize == 0) {
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
                    Toast.makeText(this, R.string.no_date_takens_found, Toast.LENGTH_LONG).show()
                }

                runOnUiThread {
                    callback?.invoke()
                }
                return@RunOnBackgroundThreadUseCase
            }

            val resultSize = contentResolver.applyBatch(MediaStore.AUTHORITY, operations).size
            if (resultSize == 0) {
                didUpdateFile = false
            }

            if (hasRescanned || pathsToRescan.isEmpty()) {
                if (dateTakens.isNotEmpty()) {
                    GalleryDatabase.getInstance(applicationContext).DateTakensDao()
                        .insertAll(dateTakens)
                }

                runOnUiThread {
                    if (showToasts) {
                        Toast.makeText(
                            this,
                            if (didUpdateFile) R.string.dates_fixed_successfully else R.string.unknown_error_occurred,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    callback?.invoke()
                }
            } else {
                applicationContext.rescanPaths(pathsToRescan) {
                    fixDateTaken(paths, showToasts, true, callback)
                }
            }
        }
    } catch (e: Exception) {
        if (showToasts) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
        }
    }
}

fun AppCompatActivity.getShortcutImage(tmb: String, drawable: Drawable, callback: () -> Unit) {
    RunOnBackgroundThreadUseCase {

        val options = RequestOptions()
            .format(DecodeFormat.PREFER_ARGB_8888)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .fitCenter()

        val size = resources.getDimension(R.dimen.shortcut_size).toInt()
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

fun AppCompatActivity.scanPathRecursively(path: String, callback: (() -> Unit)? = null) {
    applicationContext.scanPathRecursively(path, callback)
}

fun AppCompatActivity.scanPathsRecursively(paths: List<String>, callback: (() -> Unit)? = null) {
    applicationContext.scanPathsRecursively(paths, callback)
}

fun BaseSimpleActivity.showFileCreateError(path: String) {
    val error = String.format(getString(R.string.could_not_create_file), path)
    baseConfig.sdTreeUri = ""
    Toast.makeText(this, error, Toast.LENGTH_LONG).show()
}

@TargetApi(Build.VERSION_CODES.N)
fun AppCompatActivity.showFileOnMap(path: String) {
    val exif = try {
        if (path.startsWith("content://") && IsNougatPlusUseCase()) {
            ExifInterface(contentResolver.openInputStream(Uri.parse(path))!!)
        } else {
            ExifInterface(path)
        }
    } catch (e: Exception) {
        Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
        return
    }

    val latLon = FloatArray(2)
    if (exif.getLatLong(latLon)) {
        showLocationOnMap("${latLon[0]}, ${latLon[1]}")
    } else {
        Toast.makeText(this, R.string.unknown_location, Toast.LENGTH_LONG).show()
    }
}

fun AppCompatActivity.showLocationOnMap(coordinates: String) {
    val uriBegin = "geo:${coordinates.replace(" ", "")}"
    val encodedQuery = Uri.encode(coordinates)
    val uriString = "$uriBegin?q=$encodedQuery&z=16"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString))
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, R.string.no_app_found, Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
    }
}

fun AppCompatActivity.tryGenericMimeType(intent: Intent, mimeType: String, uri: Uri): Boolean {
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

fun AppCompatActivity.handleExcludedFolderPasswordProtection(callback: () -> Unit) {
    if (config.isExcludedPasswordProtectionOn) {
        val callbackAfterSecurityReceived: (hash: String, type: Int, success: Boolean) -> Unit =
            { _, _, success ->
                if (success) {
                    callback()
                }
            }
        SecurityDialogFragment(
            config.excludedPasswordHash,
            config.excludedProtectionType,
            callbackAfterSecurityReceived
        ).show((this as FragmentActivity).supportFragmentManager, SecurityDialogFragment.TAG)
    } else {
        callback()
    }
}

fun BaseSimpleActivity.isShowingSAFDialog(path: String): Boolean {
    return if ((!IsRPlusUseCase() &&
                IsPathOnSdUseCase(this, path) &&
                !isSDCardSetAsDefaultStorage() &&
                (baseConfig.sdTreeUri.isEmpty() ||
                        !hasProperStoredTreeUri(false)))
    ) {
        runOnUiThread {
            if (!isDestroyed && !isFinishing) {
                val funAfterPermissionGranted: () -> Unit = {
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
                            Toast.makeText(
                                this@isShowingSAFDialog,
                                R.string.system_service_disabled,
                                Toast.LENGTH_LONG
                            ).show()
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@isShowingSAFDialog,
                                R.string.unknown_error_occurred, Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
                WritePermissionDialogFragment(
                    WritePermissionDialogFragment.Mode.SdCard,
                    funAfterPermissionGranted
                ).show(
                    supportFragmentManager,
                    WritePermissionDialogFragment.TAG
                )
            }
        }
        true
    } else {
        false
    }
}

private fun hasProperStoredFirstParentUri(owner: Context, path: String): Boolean {
    val firstParentUri = owner.createFirstParentTreeUri(path)
    return owner.contentResolver.persistedUriPermissions.any { it.uri.toString() == firstParentUri.toString() }
}

@SuppressLint("InlinedApi")
fun BaseSimpleActivity.isShowingSAFDialogSdk30(path: String): Boolean {
    return if (isAccessibleWithSAFSdk30(path) && !hasProperStoredFirstParentUri(this, path)) {
        runOnUiThread {
            if (!isDestroyed && !isFinishing) {
                val level = getFirstParentLevel(path)

                val funAfterPermissionGranted: () -> Unit = {
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
                            Toast.makeText(
                                this@isShowingSAFDialogSdk30,
                                R.string.system_service_disabled,
                                Toast.LENGTH_LONG
                            ).show()
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@isShowingSAFDialogSdk30,
                                R.string.unknown_error_occurred, Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
                WritePermissionDialogFragment(
                    WritePermissionDialogFragment.Mode.OpenDocumentTreeSDK30(
                        path.getFirstParentPath(
                            this,
                            level
                        )
                    ), funAfterPermissionGranted
                ).show(
                    supportFragmentManager,
                    WritePermissionDialogFragment.TAG
                )
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
                val funAfterPermissionGranted: () -> Unit = {
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
                            Toast.makeText(
                                this@isShowingSAFCreateDocumentDialogSdk30,
                                R.string.system_service_disabled,
                                Toast.LENGTH_LONG
                            ).show()
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@isShowingSAFCreateDocumentDialogSdk30,
                                R.string.unknown_error_occurred,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
                WritePermissionDialogFragment(
                    WritePermissionDialogFragment.Mode.CreateDocumentSDK30,
                    funAfterPermissionGranted
                ).show(
                    supportFragmentManager,
                    WritePermissionDialogFragment.TAG
                )
            }
        }
        true
    } else {
        false
    }
}

fun BaseSimpleActivity.isShowingAndroidSAFDialog(path: String): Boolean {
    return if (isRestrictedSAFOnlyRoot(path) && (getAndroidTreeUri(path).isEmpty() || !hasProperStoredAndroidTreeUri(
            path
        ))
    ) {
        runOnUiThread {
            if (!isDestroyed && !isFinishing) {
                val callback: (Boolean) -> Unit = { result ->
                    if (result) {
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
                                Toast.makeText(
                                    this@isShowingAndroidSAFDialog,
                                    R.string.system_service_disabled,
                                    Toast.LENGTH_LONG
                                ).show()
                            } catch (e: Exception) {
                                Toast.makeText(
                                    this@isShowingAndroidSAFDialog,
                                    R.string.unknown_error_occurred,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
                ConfirmationAdvancedDialogFragment(
                    message = "",
                    messageId = R.string.confirm_storage_access_android_text,
                    positive = R.string.ok,
                    negative = R.string.cancel,
                    callback = callback
                ).show(
                    supportFragmentManager,
                    ConfirmationAdvancedDialogFragment.TAG
                )
            }
        }
        true
    } else {
        false
    }
}

fun BaseSimpleActivity.showOTGPermissionDialog(path: String) {
    runOnUiThread {
        if (!isDestroyed && !isFinishing) {
            val funAfterPermissionGranted: () -> Unit = {
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
                        Toast.makeText(
                            this@showOTGPermissionDialog,
                            R.string.system_service_disabled,
                            Toast.LENGTH_LONG
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@showOTGPermissionDialog,
                            R.string.unknown_error_occurred,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            WritePermissionDialogFragment(
                WritePermissionDialogFragment.Mode.Otg,
                funAfterPermissionGranted
            ).show(supportFragmentManager, WritePermissionDialogFragment.TAG)
        }
    }
}

fun AppCompatActivity.launchViewIntent(url: String) {
    HideKeyboardUseCase(this)
    RunOnBackgroundThreadUseCase {
        Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            try {
                startActivity(this)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    this@launchViewIntent,
                    R.string.no_browser_found,
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    this@launchViewIntent,
                    e.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

fun AppCompatActivity.redirectToRateUs() {
    HideKeyboardUseCase(this)
    try {
        launchViewIntent("market://details?id=${packageName.removeSuffix(".debug")}")
    } catch (ignored: ActivityNotFoundException) {
        launchViewIntent(getStoreUrl())
    }
}

fun AppCompatActivity.openEditorIntent(path: String, forceChooser: Boolean, applicationId: String) {
    RunOnBackgroundThreadUseCase {
        val newUri = getFinalUriFromPath(path, applicationId) ?: return@RunOnBackgroundThreadUseCase
        Intent().apply {
            action = Intent.ACTION_EDIT
            setDataAndType(newUri, getUriMimeType(path, newUri))
            if (!IsRPlusUseCase() || (IsRPlusUseCase() && (hasProperStoredDocumentUriSdk30(path) || Environment.isExternalStorageManager()))) {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }

            val parent = path.getParentPath()
            val newFilename = "${path.getFilenameFromPath().substringBeforeLast('.')}_1"
            val extension = GetFileExtensionUseCase(path)
            val newFilePath = File(parent, "$newFilename.$extension")

            val outputUri = if (IsPathOnOtgUseCase(
                    this@openEditorIntent,
                    path
                )
            ) newUri else getFinalUriFromPath(
                "$newFilePath",
                applicationId
            )
            if (!IsRPlusUseCase()) {
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

            if (!IsRPlusUseCase()) {
                putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
            }

            putExtra(REAL_FILE_PATH, path)

            try {
                val chooser = Intent.createChooser(this, getString(R.string.edit_with))
                startActivityForResult(if (forceChooser) chooser else this, REQUEST_EDIT_IMAGE)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    this@openEditorIntent,
                    R.string.no_app_found,
                    Toast.LENGTH_LONG
                )
                    .show()
            } catch (e: Exception) {
                Toast.makeText(
                    this@openEditorIntent,
                    e.toString(),
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        }
    }
}

fun AppCompatActivity.openPathIntent(
    path: String,
    forceChooser: Boolean,
    applicationId: String,
    forceMimeType: String = "",
    extras: java.util.HashMap<String, Boolean> = java.util.HashMap()
) {
    RunOnBackgroundThreadUseCase {

        val newUri = getFinalUriFromPath(path, applicationId) ?: return@RunOnBackgroundThreadUseCase
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
                    Toast.makeText(
                        this@openPathIntent,
                        R.string.no_app_found,
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@openPathIntent,
                    e.toString(),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
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
        WhatsNewDialogFragment(newReleases).show(supportFragmentManager, WhatsNewDialogFragment.TAG)
    }

    baseConfig.lastVersion = currVersion
}

private fun deleteFilesBg(
    owner: BaseSimpleActivity,
    files: List<FileDirItem>,
    allowDeleteFolder: Boolean = false,
    callback: ((wasSuccess: Boolean) -> Unit)? = null
) {
    if (files.isEmpty()) {
        owner.runOnUiThread {
            callback?.invoke(true)
        }
        return
    }

    val firstFile = files.first()
    owner.handleSAFDialog(firstFile.path) {
        if (!it) {
            return@handleSAFDialog
        }

        owner.checkManageMediaOrHandleSAFDialogSdk30(firstFile.path) {
            if (!it) {
                return@checkManageMediaOrHandleSAFDialogSdk30
            }

            val recycleBinPath = firstFile.path.startsWith(owner.recycleBinPath)

            if ((IsSPlusUseCase() && MediaStore.canManageMedia(owner)) && !recycleBinPath) {
                val fileUris = owner.getFileUrisFromFileDirItems(files)

                owner.deleteSDK30Uris(fileUris) { success ->
                    owner.runOnUiThread {
                        callback?.invoke(success)
                    }
                }
            } else {
                deleteFilesCasual(owner, files, allowDeleteFolder, callback)
            }
        }
    }
}

private fun deleteFilesCasual(
    owner: BaseSimpleActivity,
    files: List<FileDirItem>,
    allowDeleteFolder: Boolean = false,
    callback: ((wasSuccess: Boolean) -> Unit)? = null
) {
    var wasSuccess = false
    val failedFileDirItems = java.util.ArrayList<FileDirItem>()
    files.forEachIndexed { index, file ->
        owner.deleteFileBg(file, allowDeleteFolder, true) {
            if (it) {
                wasSuccess = true
            } else {
                failedFileDirItems.add(file)
            }

            if (index == files.lastIndex) {
                if (IsRPlusUseCase() && failedFileDirItems.isNotEmpty()) {
                    val fileUris = owner.getFileUrisFromFileDirItems(failedFileDirItems)
                    owner.deleteSDK30Uris(fileUris) { success ->
                        owner.runOnUiThread {
                            callback?.invoke(success)
                        }
                    }
                } else {
                    owner.runOnUiThread {
                        callback?.invoke(wasSuccess)
                    }
                }
            }
        }
    }
}

private fun createTempFile(file: File): File? {
    return if (file.isDirectory) {
        createTempDir("temp", "${System.currentTimeMillis()}", file.parentFile)
    } else {
        if (IsRPlusUseCase()) {
            // this can throw FileSystemException, lets catch and handle it at the place calling this function
            kotlin.io.path.createTempFile(
                file.parentFile?.toPath(),
                "temp",
                "${System.currentTimeMillis()}"
            ).toFile()
        } else {
            createTempFile("temp", "${System.currentTimeMillis()}", file.parentFile)
        }
    }
}

fun BaseSimpleActivity.createDirectorySync(directory: String): Boolean {
    if (getDoesFilePathExist(directory)) {
        return true
    }

    if (!IsRPlusUseCase() &&
        (IsPathOnSdUseCase(this, directory) || IsPathOnOtgUseCase(this, directory)) &&
        !isSDCardSetAsDefaultStorage()
    ) {
        val documentFile = getDocumentFile(directory.getParentPath()) ?: return false
        val newDir =
            documentFile.createDirectory(directory.getFilenameFromPath()) ?: getDocumentFile(
                directory
            )
        return newDir != null
    }

    if (isRestrictedSAFOnlyRoot(directory)) {
        return createAndroidSAFDirectory(directory)
    }

    if (isAccessibleWithSAFSdk30(directory)) {
        return createSAFDirectorySdk30(directory)
    }

    return File(directory).mkdirs()
}

fun AppCompatActivity.checkAppSideloading(): Boolean {
    val isSideloaded = when (baseConfig.appSideloadingStatus) {
        SIDELOADING_TRUE -> true
        SIDELOADING_FALSE -> false
        else -> isAppSideloaded(this)
    }

    baseConfig.appSideloadingStatus = if (isSideloaded) SIDELOADING_TRUE else SIDELOADING_FALSE
    if (isSideloaded) {
        showSideloadingDialog()
    }

    return isSideloaded
}

@SuppressLint("UseCompatLoadingForDrawables")
private fun isAppSideloaded(owner: AppCompatActivity): Boolean {
    return try {
        owner.getDrawable(R.drawable.ic_camera_vector)
        false
    } catch (e: Exception) {
        true
    }
}

fun AppCompatActivity.showSideloadingDialog() {
    val funAfterDialogCancelled = { finish() }
    AppSideLoadedDialogFragment(funAfterDialogCancelled).show(
        supportFragmentManager,
        AppSideLoadedDialogFragment.TAG
    )
}

fun BaseSimpleActivity.copySingleFileSdk30(source: FileDirItem, destination: FileDirItem): Boolean {
    val directory = destination.getParentPath()
    if (!createDirectorySync(directory)) {
        val error = String.format(getString(R.string.could_not_create_folder), directory)
        Toast.makeText(this, error, Toast.LENGTH_LONG).show()
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
                copyOldLastModified(this, source.path, destination.path)
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

private fun copyOldLastModified(
    owner: BaseSimpleActivity,
    sourcePath: String,
    destinationPath: String
) {
    val projection =
        arrayOf(Images.Media.DATE_TAKEN, Images.Media.DATE_MODIFIED)
    val uri = Files.getContentUri("external")
    val selection = "${MediaStore.MediaColumns.DATA} = ?"
    var selectionArgs = arrayOf(sourcePath)
    val cursor =
        owner.applicationContext.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            null
        )

    cursor?.use {
        if (cursor.moveToFirst()) {
            val dateTaken = cursor.getLongValue(Images.Media.DATE_TAKEN)
            val dateModified = cursor.getIntValue(Images.Media.DATE_MODIFIED)

            val values = ContentValues().apply {
                put(Images.Media.DATE_TAKEN, dateTaken)
                put(Images.Media.DATE_MODIFIED, dateModified)
            }

            selectionArgs = arrayOf(destinationPath)
            owner.applicationContext.contentResolver.update(uri, values, selection, selectionArgs)
        }
    }
}

fun AppCompatActivity.getThemeId(
    color: Int = baseConfig.primaryColor,
    showTransparentTop: Boolean = false
) =
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

private fun ensurePublicUri(owner: Context, path: String, applicationId: String): Uri? {
    return when {
        owner.hasProperStoredAndroidTreeUri(path) && owner.isRestrictedSAFOnlyRoot(path) -> {
            owner.getAndroidSAFUri(path)
        }
        owner.hasProperStoredDocumentUriSdk30(path) && owner.isAccessibleWithSAFSdk30(path) -> {
            owner.createDocumentUriUsingFirstParentTreeUri(path)
        }
        IsPathOnOtgUseCase(owner, path) -> {
            owner.getDocumentFile(path)?.uri
        }
        else -> {
            val uri = Uri.parse(path)
            if (uri.scheme == "content") {
                uri
            } else {
                val newPath = if (uri.toString().startsWith("/")) uri.toString() else uri.path
                val file = newPath?.let { File(it) }
                file?.let { owner.getFilePublicUri(it, applicationId) }
            }
        }
    }
}

private fun renameAndroidSAFDocument(owner: Context, oldPath: String, newPath: String): Boolean {
    return try {
        val treeUri = owner.getAndroidTreeUri(oldPath).toUri()
        val documentId = owner.createAndroidSAFDocumentId(oldPath)
        val parentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
        DocumentsContract.renameDocument(
            owner.contentResolver,
            parentUri,
            newPath.getFilenameFromPath()
        ) != null
    } catch (e: IllegalStateException) {
        Toast.makeText(owner, e.toString(), Toast.LENGTH_LONG).show()
        false
    }
}

private fun renameDocumentSdk30(owner: Context, oldPath: String, newPath: String): Boolean {
    return try {
        val treeUri = owner.createFirstParentTreeUri(oldPath)
        val documentId = owner.getSAFDocumentId(oldPath)
        val parentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
        DocumentsContract.renameDocument(
            owner.contentResolver,
            parentUri,
            newPath.getFilenameFromPath()
        ) != null
    } catch (e: IllegalStateException) {
        Toast.makeText(owner, e.toString(), Toast.LENGTH_LONG).show()
        false
    }
}

private fun rescanAndDeletePath(owner: Context, path: String, callback: () -> Unit) {
    val scanFileMaxDuration = 1000L
    val scanFileHandler = Handler(Looper.getMainLooper())
    scanFileHandler.postDelayed({
        callback()
    }, scanFileMaxDuration)

    MediaScannerConnection.scanFile(owner.applicationContext, arrayOf(path), null) { path, uri ->
        scanFileHandler.removeCallbacksAndMessages(null)
        try {
            owner.applicationContext.contentResolver.delete(uri, null, null)
        } catch (exception: NullPointerException) {
            Timber.e("error while deleting a folder: ${exception.message}")
        }
        callback()
    }
}