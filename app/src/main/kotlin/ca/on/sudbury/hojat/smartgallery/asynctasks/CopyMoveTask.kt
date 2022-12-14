package ca.on.sudbury.hojat.smartgallery.asynctasks

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.os.AsyncTask
import android.os.Handler
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.util.Pair
import androidx.documentfile.provider.DocumentFile
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.extensions.getDoesFilePathExist
import ca.on.sudbury.hojat.smartgallery.extensions.getFilenameFromPath
import ca.on.sudbury.hojat.smartgallery.extensions.createDirectorySync
import ca.on.sudbury.hojat.smartgallery.extensions.getDocumentFile
import ca.on.sudbury.hojat.smartgallery.extensions.isRestrictedSAFOnlyRoot
import ca.on.sudbury.hojat.smartgallery.extensions.getAndroidSAFFileItems
import ca.on.sudbury.hojat.smartgallery.extensions.isAccessibleWithSAFSdk30
import ca.on.sudbury.hojat.smartgallery.extensions.getDocumentSdk30
import ca.on.sudbury.hojat.smartgallery.extensions.toFileDirItem
import ca.on.sudbury.hojat.smartgallery.extensions.isMediaFile
import ca.on.sudbury.hojat.smartgallery.extensions.getFileOutputStreamSync
import ca.on.sudbury.hojat.smartgallery.extensions.getMimeType
import ca.on.sudbury.hojat.smartgallery.extensions.getFileInputStreamSync
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig
import ca.on.sudbury.hojat.smartgallery.extensions.isRestrictedWithSAFSdk30
import ca.on.sudbury.hojat.smartgallery.extensions.deleteFileBg
import ca.on.sudbury.hojat.smartgallery.extensions.deleteFromMediaStore
import ca.on.sudbury.hojat.smartgallery.extensions.getFileUrisFromFileDirItems
import ca.on.sudbury.hojat.smartgallery.extensions.getLongValue
import ca.on.sudbury.hojat.smartgallery.extensions.getIntValue
import ca.on.sudbury.hojat.smartgallery.extensions.isSDCardSetAsDefaultStorage
import ca.on.sudbury.hojat.smartgallery.extensions.rescanPaths
import ca.on.sudbury.hojat.smartgallery.helpers.CONFLICT_KEEP_BOTH
import ca.on.sudbury.hojat.smartgallery.helpers.CONFLICT_SKIP
import ca.on.sudbury.hojat.smartgallery.helpers.getConflictResolution
import ca.on.sudbury.hojat.smartgallery.interfaces.CopyMoveListener
import ca.on.sudbury.hojat.smartgallery.models.FileDirItem
import ca.on.sudbury.hojat.smartgallery.usecases.IsOreoPlusUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsRPlusUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsSPlusUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsPathOnOtgUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsPathOnSdUseCase
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.lang.ref.WeakReference

class CopyMoveTask(
    @SuppressLint("StaticFieldLeak") val activity: BaseSimpleActivity,
    private val copyOnly: Boolean,
    private val copyMediaOnly: Boolean,
    private val conflictResolutions: LinkedHashMap<String, Int>,
    listener: CopyMoveListener,
    private val copyHidden: Boolean
) : AsyncTask<Pair<ArrayList<FileDirItem>, String>, Void, Boolean>() {
    private val INITIAL_PROGRESS_DELAY = 3000L
    private val PROGRESS_RECHECK_INTERVAL = 500L

    private var mListener: WeakReference<CopyMoveListener>? = null
    private var mTransferredFiles = ArrayList<FileDirItem>()
    private var mFileDirItemsToDelete =
        ArrayList<FileDirItem>()        // confirm the deletion of files on Android 11 from Downloads and Android at once
    private var mDocuments = LinkedHashMap<String, DocumentFile?>()
    private var mFiles = ArrayList<FileDirItem>()
    private var mFileCountToCopy = 0
    private var mDestinationPath = ""

    // progress indication
    private var mNotificationBuilder: NotificationCompat.Builder
    private var mCurrFilename = ""
    private var mCurrentProgress = 0L
    private var mMaxSize = 0
    private var mNotifId = 0
    private var mIsTaskOver = false
    private var mProgressHandler = Handler()

    init {
        mListener = WeakReference(listener)
        mNotificationBuilder = NotificationCompat.Builder(activity)
    }

    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg params: Pair<ArrayList<FileDirItem>, String>): Boolean {
        if (params.isEmpty()) {
            return false
        }

        val pair = params[0]
        mFiles = pair.first!!
        mDestinationPath = pair.second!!
        mFileCountToCopy = mFiles.size
        mNotifId = (System.currentTimeMillis() / 1000).toInt()
        mMaxSize = 0
        for (file in mFiles) {
            if (file.size == 0L) {
                file.size = file.getProperSize(activity, copyHidden)
            }

            val newPath = "$mDestinationPath/${file.name}"
            val fileExists = activity.getDoesFilePathExist(newPath)
            if (getConflictResolution(
                    conflictResolutions,
                    newPath
                ) != CONFLICT_SKIP || !fileExists
            ) {
                mMaxSize += (file.size / 1000).toInt()
            }
        }

        mProgressHandler.postDelayed({
            initProgressNotification()
            updateProgress()
        }, INITIAL_PROGRESS_DELAY)

        for (file in mFiles) {
            try {
                val newPath = "$mDestinationPath/${file.name}"
                var newFileDirItem =
                    FileDirItem(newPath, newPath.getFilenameFromPath(), file.isDirectory)
                if (activity.getDoesFilePathExist(newPath)) {
                    val resolution = getConflictResolution(conflictResolutions, newPath)
                    if (resolution == CONFLICT_SKIP) {
                        mFileCountToCopy--
                        continue
                    } else if (resolution == CONFLICT_KEEP_BOTH) {
                        val newFile = activity.getAlternativeFile(File(newFileDirItem.path))
                        newFileDirItem =
                            FileDirItem(newFile.path, newFile.name, newFile.isDirectory)
                    }
                }

                copy(file, newFileDirItem)
            } catch (e: Exception) {
                Toast.makeText(activity, e.toString(),Toast.LENGTH_LONG).show()
                return false
            }
        }

        return true
    }

    @Deprecated("Deprecated in Java")
    override fun onPostExecute(success: Boolean) {
        if (activity.isFinishing || activity.isDestroyed) {
            return
        }

        deleteProtectedFiles()
        mProgressHandler.removeCallbacksAndMessages(null)
        notificationManager(activity).cancel(mNotifId)
        val listener = mListener?.get() ?: return

        if (success) {
            listener.copySucceeded(
                copyOnly,
                mTransferredFiles.size >= mFileCountToCopy,
                mDestinationPath,
                mTransferredFiles.size == 1
            )
        } else {
            listener.copyFailed()
        }
    }

    private fun initProgressNotification() {
        val channelId = "Copy/Move"
        val title = activity.getString(if (copyOnly) R.string.copying else R.string.moving)
        if (IsOreoPlusUseCase()) {
            val importance = NotificationManager.IMPORTANCE_LOW
            NotificationChannel(channelId, title, importance).apply {
                enableLights(false)
                enableVibration(false)
                notificationManager(activity).createNotificationChannel(this)
            }
        }

        mNotificationBuilder.setContentTitle(title)
            .setSmallIcon(R.drawable.ic_copy_vector)
            .setChannelId(channelId)
    }

    private fun updateProgress() {
        if (mIsTaskOver) {
            notificationManager(activity).cancel(mNotifId)
            cancel(true)
            return
        }

        mNotificationBuilder.apply {
            setContentText(mCurrFilename)
            setProgress(mMaxSize, (mCurrentProgress / 1000).toInt(), false)
            notificationManager(activity).notify(mNotifId, build())
        }

        mProgressHandler.removeCallbacksAndMessages(null)
        mProgressHandler.postDelayed({
            updateProgress()

            if (mCurrentProgress / 1000 >= mMaxSize) {
                mIsTaskOver = true
            }
        }, PROGRESS_RECHECK_INTERVAL)
    }

    private fun copy(source: FileDirItem, destination: FileDirItem) {
        if (source.isDirectory) {
            copyDirectory(source, destination.path)
        } else {
            copyFile(source, destination)
        }
    }

    private fun copyDirectory(source: FileDirItem, destinationPath: String) {
        if (!activity.createDirectorySync(destinationPath)) {
            val error =
                String.format(activity.getString(R.string.could_not_create_folder), destinationPath)
            Toast.makeText(activity, error,Toast.LENGTH_LONG).show()
            return
        }

        if (IsPathOnOtgUseCase(activity, source.path)) {
            val children = activity.getDocumentFile(source.path)?.listFiles() ?: return
            for (child in children) {
                val newPath = "$destinationPath/${child.name}"
                if (File(newPath).exists()) {
                    continue
                }

                val oldPath = "${source.path}/${child.name}"
                val oldFileDirItem =
                    FileDirItem(oldPath, child.name!!, child.isDirectory, 0, child.length())
                val newFileDirItem = FileDirItem(newPath, child.name!!, child.isDirectory)
                copy(oldFileDirItem, newFileDirItem)
            }
            mTransferredFiles.add(source)
        } else if (activity.isRestrictedSAFOnlyRoot(source.path)) {
            activity.getAndroidSAFFileItems(source.path, true) { files ->
                for (child in files) {
                    val newPath = "$destinationPath/${child.name}"
                    if (activity.getDoesFilePathExist(newPath)) {
                        continue
                    }

                    val oldPath = "${source.path}/${child.name}"
                    val oldFileDirItem =
                        FileDirItem(oldPath, child.name, child.isDirectory, 0, child.size)
                    val newFileDirItem = FileDirItem(newPath, child.name, child.isDirectory)
                    copy(oldFileDirItem, newFileDirItem)
                }
                mTransferredFiles.add(source)
            }
        } else if (activity.isAccessibleWithSAFSdk30(source.path)) {
            val children = activity.getDocumentSdk30(source.path)?.listFiles() ?: return
            for (child in children) {
                val newPath = "$destinationPath/${child.name}"
                if (File(newPath).exists()) {
                    continue
                }

                val oldPath = "${source.path}/${child.name}"
                val oldFileDirItem =
                    FileDirItem(oldPath, child.name!!, child.isDirectory, 0, child.length())
                val newFileDirItem = FileDirItem(newPath, child.name!!, child.isDirectory)
                copy(oldFileDirItem, newFileDirItem)
            }
            mTransferredFiles.add(source)
        } else {
            val children = File(source.path).list()
            for (child in children!!) {
                val newPath = "$destinationPath/$child"
                if (activity.getDoesFilePathExist(newPath)) {
                    continue
                }

                val oldFile = File(source.path, child)
                val oldFileDirItem = oldFile.toFileDirItem(activity)
                val newFileDirItem =
                    FileDirItem(newPath, newPath.getFilenameFromPath(), oldFile.isDirectory)
                copy(oldFileDirItem, newFileDirItem)
            }
            mTransferredFiles.add(source)
        }
    }

    private fun copyFile(source: FileDirItem, destination: FileDirItem) {
        if (copyMediaOnly && !source.path.isMediaFile()) {
            mCurrentProgress += source.size
            return
        }

        val directory = destination.getParentPath()
        if (!activity.createDirectorySync(directory)) {
            val error =
                String.format(activity.getString(R.string.could_not_create_folder), directory)
            Toast.makeText(activity, error,Toast.LENGTH_LONG).show()
            mCurrentProgress += source.size
            return
        }

        mCurrFilename = source.name
        var inputStream: InputStream? = null
        var out: OutputStream? = null
        try {
            if (!mDocuments.containsKey(directory) &&
                with(activity) {
                    !IsRPlusUseCase() && (
                            IsPathOnSdUseCase(this, destination.path) ||
                                    IsPathOnOtgUseCase(this, destination.path)) &&
                            !isSDCardSetAsDefaultStorage()
                }
            ) {
                mDocuments[directory] = activity.getDocumentFile(directory)
            }

            out = activity.getFileOutputStreamSync(
                destination.path,
                source.path.getMimeType(),
                mDocuments[directory]
            )
            inputStream = activity.getFileInputStreamSync(source.path)!!

            var copiedSize = 0L
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var bytes = inputStream.read(buffer)
            while (bytes >= 0) {
                out!!.write(buffer, 0, bytes)
                copiedSize += bytes
                mCurrentProgress += bytes
                bytes = inputStream.read(buffer)
            }

            out?.flush()

            if (source.size == copiedSize && activity.getDoesFilePathExist(destination.path)) {
                mTransferredFiles.add(source)
                if (copyOnly) {
                    activity.applicationContext.rescanPaths(arrayListOf(destination.path)) {
                        if (activity.baseConfig.keepLastModified) {
                            updateLastModifiedValues(source, destination)
                            activity.applicationContext.rescanPaths(arrayListOf(destination.path))
                        }
                    }
                } else if (activity.baseConfig.keepLastModified) {
                    updateLastModifiedValues(source, destination)
                    activity.applicationContext.rescanPaths(arrayListOf(destination.path))
                    inputStream.close()
                    out?.close()
                    deleteSourceFile(source)
                } else {
                    inputStream.close()
                    out?.close()
                    deleteSourceFile(source)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(activity, e.toString(),Toast.LENGTH_LONG).show()
        } finally {
            inputStream?.close()
            out?.close()
        }
    }

    private fun updateLastModifiedValues(source: FileDirItem, destination: FileDirItem) {
        copyOldLastModified(source.path, destination.path)
        val lastModified = File(source.path).lastModified()
        if (lastModified != 0L) {
            File(destination.path).setLastModified(lastModified)
        }
    }

    private fun deleteSourceFile(source: FileDirItem) {
        if (activity.isRestrictedWithSAFSdk30(source.path) && !(IsSPlusUseCase() && MediaStore.canManageMedia(
                activity
            ))
        ) {
            mFileDirItemsToDelete.add(source)
        } else {
            activity.deleteFileBg(source, isDeletingMultipleFiles = false)
            activity.deleteFromMediaStore(source.path)
        }
    }

    // if we delete multiple files from Downloads folder on Android 11 or 12 without being a Media Management app, show the confirmation dialog just once
    private fun deleteProtectedFiles() {
        if (mFileDirItemsToDelete.isNotEmpty()) {
            val fileUris = activity.getFileUrisFromFileDirItems(mFileDirItemsToDelete)
            activity.deleteSDK30Uris(fileUris) { success ->
                if (success) {
                    mFileDirItemsToDelete.forEach {
                        activity.deleteFromMediaStore(it.path)
                    }
                }
            }
        }
    }

    private fun copyOldLastModified(sourcePath: String, destinationPath: String) {
        val projection = arrayOf(
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_MODIFIED
        )

        val uri = MediaStore.Files.getContentUri("external")
        val selection = "${MediaStore.MediaColumns.DATA} = ?"
        var selectionArgs = arrayOf(sourcePath)
        val cursor = activity.applicationContext.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            null
        )

        cursor?.use {
            if (cursor.moveToFirst()) {
                val dateTaken = cursor.getLongValue(MediaStore.Images.Media.DATE_TAKEN)
                val dateModified = cursor.getIntValue(MediaStore.Images.Media.DATE_MODIFIED)

                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DATE_TAKEN, dateTaken)
                    put(MediaStore.Images.Media.DATE_MODIFIED, dateModified)
                }

                selectionArgs = arrayOf(destinationPath)
                activity.applicationContext.contentResolver.update(
                    uri,
                    values,
                    selection,
                    selectionArgs
                )
            }
        }
    }

    private fun notificationManager(owner: Context): NotificationManager =
        owner.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
}
