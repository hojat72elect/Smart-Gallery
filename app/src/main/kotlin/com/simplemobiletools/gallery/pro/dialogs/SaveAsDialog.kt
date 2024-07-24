package com.simplemobiletools.gallery.pro.dialogs

import android.annotation.SuppressLint
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.gallery.pro.activities.BaseSimpleActivity
import com.simplemobiletools.gallery.pro.helpers.isRPlus
import com.simplemobiletools.gallery.pro.databinding.DialogSaveAsBinding
import com.simplemobiletools.gallery.pro.extensions.getAlertDialogBuilder
import com.simplemobiletools.gallery.pro.extensions.getDoesFilePathExist
import com.simplemobiletools.gallery.pro.extensions.getFileUrisFromFileDirItems
import com.simplemobiletools.gallery.pro.extensions.getFilenameFromPath
import com.simplemobiletools.gallery.pro.extensions.getParentPath
import com.simplemobiletools.gallery.pro.extensions.getPicturesDirectoryPath
import com.simplemobiletools.gallery.pro.extensions.hideKeyboard
import com.simplemobiletools.gallery.pro.extensions.humanizePath
import com.simplemobiletools.gallery.pro.extensions.isAValidFilename
import com.simplemobiletools.gallery.pro.extensions.isExternalStorageManager
import com.simplemobiletools.gallery.pro.extensions.isInDownloadDir
import com.simplemobiletools.gallery.pro.extensions.isRestrictedWithSAFSdk30
import com.simplemobiletools.gallery.pro.extensions.setupDialogStuff
import com.simplemobiletools.gallery.pro.extensions.showKeyboard
import com.simplemobiletools.gallery.pro.extensions.toFileDirItem
import com.simplemobiletools.gallery.pro.extensions.toast
import com.simplemobiletools.gallery.pro.extensions.value
import java.io.File

@SuppressLint("SetTextI18n")
class SaveAsDialog(
    val activity: BaseSimpleActivity,
    val path: String,
    private val appendFilename: Boolean,
    private val cancelCallback: (() -> Unit)? = null,
    val callback: (savePath: String) -> Unit
) {
    init {
        var realPath = path.getParentPath()
        if (activity.isRestrictedWithSAFSdk30(realPath) && !activity.isInDownloadDir(realPath)) {
            realPath = activity.getPicturesDirectoryPath(realPath)
        }

        val binding = DialogSaveAsBinding.inflate(activity.layoutInflater).apply {
            folderValue.setText("${activity.humanizePath(realPath).trimEnd('/')}/")

            val fullName = path.getFilenameFromPath()
            val dotAt = fullName.lastIndexOf(".")
            var name = fullName

            if (dotAt > 0) {
                name = fullName.substring(0, dotAt)
                val extension = fullName.substring(dotAt + 1)
                extensionValue.setText(extension)
            }

            if (appendFilename) {
                name += "_1"
            }

            filenameValue.setText(name)
            folderValue.setOnClickListener {
                activity.hideKeyboard(folderValue)
                FilePickerDialog(
                    activity = activity,
                    currPath = realPath,
                    pickFile = false,
                    showHidden = false,
                    showFAB = true,
                    canAddShowHiddenButton = true
                ) {
                    folderValue.setText(activity.humanizePath(it))
                    realPath = it
                }
            }
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(com.simplemobiletools.commons.R.string.ok, null)
            .setNegativeButton(com.simplemobiletools.commons.R.string.cancel) { _, _ -> cancelCallback?.invoke() }
            .setOnCancelListener { cancelCallback?.invoke() }
            .apply {
                activity.setupDialogStuff(
                    binding.root,
                    this,
                    com.simplemobiletools.commons.R.string.save_as
                ) { alertDialog ->
                    alertDialog.showKeyboard(binding.filenameValue)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val filename = binding.filenameValue.value
                        val extension = binding.extensionValue.value

                        if (filename.isEmpty()) {
                            activity.toast(com.simplemobiletools.commons.R.string.filename_cannot_be_empty)
                            return@setOnClickListener
                        }

                        if (extension.isEmpty()) {
                            activity.toast(com.simplemobiletools.commons.R.string.extension_cannot_be_empty)
                            return@setOnClickListener
                        }

                        val newFilename = "$filename.$extension"
                        val newPath = "${realPath.trimEnd('/')}/$newFilename"
                        if (!newFilename.isAValidFilename()) {
                            activity.toast(com.simplemobiletools.commons.R.string.filename_invalid_characters)
                            return@setOnClickListener
                        }

                        if (activity.getDoesFilePathExist(newPath)) {
                            val title = String.format(
                                activity.getString(com.simplemobiletools.commons.R.string.file_already_exists_overwrite),
                                newFilename
                            )
                            ConfirmationDialog(activity, title) {
                                if ((isRPlus() && !isExternalStorageManager())) {
                                    val fileDirItem =
                                        arrayListOf(File(newPath).toFileDirItem(activity))
                                    val fileUris = activity.getFileUrisFromFileDirItems(fileDirItem)
                                    activity.updateSDK30Uris(fileUris) { success ->
                                        if (success) {
                                            selectPath(alertDialog, newPath)
                                        }
                                    }
                                } else {
                                    selectPath(alertDialog, newPath)
                                }
                            }
                        } else {
                            selectPath(alertDialog, newPath)
                        }
                    }
                }
            }
    }

    private fun selectPath(alertDialog: AlertDialog, newPath: String) {
        activity.handleSAFDialogSdk30(newPath) {
            if (!it) {
                return@handleSAFDialogSdk30
            }
            callback(newPath)
            alertDialog.dismiss()
        }
    }
}
