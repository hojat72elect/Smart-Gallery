package ca.hojat.smart.gallery.shared.ui.dialogs

import android.annotation.SuppressLint
import androidx.appcompat.app.AlertDialog
import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.databinding.DialogSaveAsBinding
import ca.hojat.smart.gallery.shared.activities.BaseActivity
import ca.hojat.smart.gallery.shared.extensions.getAlertDialogBuilder
import ca.hojat.smart.gallery.shared.extensions.getDoesFilePathExist
import ca.hojat.smart.gallery.shared.extensions.getFileUrisFromFileDirItems
import ca.hojat.smart.gallery.shared.extensions.getFilenameFromPath
import ca.hojat.smart.gallery.shared.extensions.getParentPath
import ca.hojat.smart.gallery.shared.extensions.getPicturesDirectoryPath
import ca.hojat.smart.gallery.shared.extensions.hideKeyboard
import ca.hojat.smart.gallery.shared.extensions.humanizePath
import ca.hojat.smart.gallery.shared.extensions.isAValidFilename
import ca.hojat.smart.gallery.shared.extensions.isExternalStorageManager
import ca.hojat.smart.gallery.shared.extensions.isInDownloadDir
import ca.hojat.smart.gallery.shared.extensions.isRestrictedWithSAFSdk30
import ca.hojat.smart.gallery.shared.extensions.setupDialogStuff
import ca.hojat.smart.gallery.shared.extensions.showKeyboard
import ca.hojat.smart.gallery.shared.extensions.toFileDirItem
import ca.hojat.smart.gallery.shared.extensions.toast
import ca.hojat.smart.gallery.shared.extensions.value
import java.io.File

@SuppressLint("SetTextI18n")
class SaveAsDialog(
    val activity: BaseActivity,
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
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel) { _, _ -> cancelCallback?.invoke() }
            .setOnCancelListener { cancelCallback?.invoke() }
            .apply {
                activity.setupDialogStuff(
                    binding.root,
                    this,
                    R.string.save_as
                ) { alertDialog ->
                    alertDialog.showKeyboard(binding.filenameValue)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val filename = binding.filenameValue.value
                        val extension = binding.extensionValue.value

                        if (filename.isEmpty()) {
                            activity.toast(R.string.filename_cannot_be_empty)
                            return@setOnClickListener
                        }

                        if (extension.isEmpty()) {
                            activity.toast(R.string.extension_cannot_be_empty)
                            return@setOnClickListener
                        }

                        val newFilename = "$filename.$extension"
                        val newPath = "${realPath.trimEnd('/')}/$newFilename"
                        if (!newFilename.isAValidFilename()) {
                            activity.toast(R.string.filename_invalid_characters)
                            return@setOnClickListener
                        }

                        if (activity.getDoesFilePathExist(newPath)) {
                            val title = String.format(
                                activity.getString(R.string.file_already_exists_overwrite),
                                newFilename
                            )
                            ConfirmationDialog(activity, title) {
                                if (!isExternalStorageManager()) {
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
