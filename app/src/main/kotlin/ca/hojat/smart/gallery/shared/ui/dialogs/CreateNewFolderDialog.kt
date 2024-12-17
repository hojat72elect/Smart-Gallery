package ca.hojat.smart.gallery.shared.ui.dialogs

import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.databinding.DialogCreateNewFolderBinding
import ca.hojat.smart.gallery.shared.activities.BaseActivity
import ca.hojat.smart.gallery.shared.extensions.createAndroidSAFDirectory
import ca.hojat.smart.gallery.shared.extensions.createSAFDirectorySdk30
import ca.hojat.smart.gallery.shared.extensions.getAlertDialogBuilder
import ca.hojat.smart.gallery.shared.extensions.getDocumentFile
import ca.hojat.smart.gallery.shared.extensions.getFilenameFromPath
import ca.hojat.smart.gallery.shared.extensions.getParentPath
import ca.hojat.smart.gallery.shared.extensions.humanizePath
import ca.hojat.smart.gallery.shared.extensions.isAStorageRootFolder
import ca.hojat.smart.gallery.shared.extensions.isAValidFilename
import ca.hojat.smart.gallery.shared.extensions.isAccessibleWithSAFSdk30
import ca.hojat.smart.gallery.shared.extensions.isRestrictedSAFOnlyRoot
import ca.hojat.smart.gallery.shared.extensions.needsStupidWritePermissions
import ca.hojat.smart.gallery.shared.extensions.setupDialogStuff
import ca.hojat.smart.gallery.shared.extensions.showErrorToast
import ca.hojat.smart.gallery.shared.extensions.showKeyboard
import ca.hojat.smart.gallery.shared.extensions.toast
import ca.hojat.smart.gallery.shared.extensions.value
import ca.hojat.smart.gallery.shared.helpers.isRPlus
import java.io.File

@RequiresApi(Build.VERSION_CODES.O)
class CreateNewFolderDialog(
    val activity: BaseActivity,
    val path: String,
    val callback: (path: String) -> Unit
) {
    init {
        val view = DialogCreateNewFolderBinding.inflate(activity.layoutInflater, null, false)
        view.folderPath.setText("${activity.humanizePath(path).trimEnd('/')}/")

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(
                    view.root,
                    this,
                    R.string.create_new_folder
                ) { alertDialog ->
                    alertDialog.showKeyboard(view.folderName)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(View.OnClickListener {
                            val name = view.folderName.value
                            when {
                                name.isEmpty() -> activity.toast(R.string.empty_name)
                                name.isAValidFilename() -> {
                                    val file = File(path, name)
                                    if (file.exists()) {
                                        activity.toast(R.string.name_taken)
                                        return@OnClickListener
                                    }

                                    createFolder("$path/$name", alertDialog)
                                }

                                else -> activity.toast(R.string.invalid_name)
                            }
                        })
                }
            }
    }


    private fun createFolder(path: String, alertDialog: AlertDialog) {
        try {
            when {
                activity.isRestrictedSAFOnlyRoot(path) && activity.createAndroidSAFDirectory(path) -> sendSuccess(
                    alertDialog,
                    path
                )

                activity.isAccessibleWithSAFSdk30(path) -> activity.handleSAFDialogSdk30(path) {
                    if (it && activity.createSAFDirectorySdk30(path)) {
                        sendSuccess(alertDialog, path)
                    }
                }

                activity.needsStupidWritePermissions(path) -> activity.handleSAFDialog(path) {
                    if (it) {
                        try {
                            val documentFile = activity.getDocumentFile(path.getParentPath())
                            val newDir = documentFile?.createDirectory(path.getFilenameFromPath())
                                ?: activity.getDocumentFile(path)
                            if (newDir != null) {
                                sendSuccess(alertDialog, path)
                            } else {
                                activity.toast(R.string.unknown_error_occurred)
                            }
                        } catch (e: SecurityException) {
                            activity.showErrorToast(e)
                        }
                    }
                }

                File(path).mkdirs() -> sendSuccess(alertDialog, path)
                isRPlus() && activity.isAStorageRootFolder(path.getParentPath()) -> activity.handleSAFCreateDocumentDialogSdk30(
                    path
                ) {
                    if (it) {
                        sendSuccess(alertDialog, path)
                    }
                }

                else -> activity.toast(
                    activity.getString(
                        R.string.could_not_create_folder,
                        path.getFilenameFromPath()
                    )
                )
            }
        } catch (e: Exception) {
            activity.showErrorToast(e)
        }
    }

    private fun sendSuccess(alertDialog: AlertDialog, path: String) {
        callback(path.trimEnd('/'))
        alertDialog.dismiss()
    }
}