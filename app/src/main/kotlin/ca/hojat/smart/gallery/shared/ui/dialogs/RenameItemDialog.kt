package ca.hojat.smart.gallery.shared.ui.dialogs

import androidx.appcompat.app.AlertDialog
import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.databinding.DialogRenameItemBinding
import ca.hojat.smart.gallery.shared.activities.BaseActivity
import ca.hojat.smart.gallery.shared.extensions.beGone
import ca.hojat.smart.gallery.shared.extensions.getAlertDialogBuilder
import ca.hojat.smart.gallery.shared.extensions.getDoesFilePathExist
import ca.hojat.smart.gallery.shared.extensions.getFilenameFromPath
import ca.hojat.smart.gallery.shared.extensions.getIsPathDirectory
import ca.hojat.smart.gallery.shared.extensions.getParentPath
import ca.hojat.smart.gallery.shared.extensions.isAValidFilename
import ca.hojat.smart.gallery.shared.extensions.setupDialogStuff
import ca.hojat.smart.gallery.shared.extensions.showKeyboard
import ca.hojat.smart.gallery.shared.extensions.toast
import ca.hojat.smart.gallery.shared.extensions.value

class RenameItemDialog(
    val activity: BaseActivity,
    val path: String,
    val callback: (newPath: String) -> Unit
) {
    init {
        var ignoreClicks = false
        val fullName = path.getFilenameFromPath()
        val dotAt = fullName.lastIndexOf(".")
        var name = fullName

        val view = DialogRenameItemBinding.inflate(activity.layoutInflater, null, false).apply {
            if (dotAt > 0 && !activity.getIsPathDirectory(path)) {
                name = fullName.substring(0, dotAt)
                val extension = fullName.substring(dotAt + 1)
                renameItemExtension.setText(extension)
            } else {
                renameItemExtensionHint.beGone()
            }

            renameItemName.setText(name)
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view.root, this, R.string.rename) { alertDialog ->
                    alertDialog.showKeyboard(view.renameItemName)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        if (ignoreClicks) {
                            return@setOnClickListener
                        }

                        var newName = view.renameItemName.value
                        val newExtension = view.renameItemExtension.value

                        if (newName.isEmpty()) {
                            activity.toast(R.string.empty_name)
                            return@setOnClickListener
                        }

                        if (!newName.isAValidFilename()) {
                            activity.toast(R.string.invalid_name)
                            return@setOnClickListener
                        }

                        val updatedPaths = ArrayList<String>()
                        updatedPaths.add(path)
                        if (newExtension.isNotEmpty()) {
                            newName += ".$newExtension"
                        }

                        if (!activity.getDoesFilePathExist(path)) {
                            activity.toast(
                                String.format(
                                    activity.getString(R.string.source_file_doesnt_exist),
                                    path
                                )
                            )
                            return@setOnClickListener
                        }

                        val newPath = "${path.getParentPath()}/$newName"

                        if (path == newPath) {
                            activity.toast(R.string.name_taken)
                            return@setOnClickListener
                        }

                        if (!path.equals(
                                newPath,
                                ignoreCase = true
                            ) && activity.getDoesFilePathExist(newPath)
                        ) {
                            activity.toast(R.string.name_taken)
                            return@setOnClickListener
                        }

                        updatedPaths.add(newPath)
                        ignoreClicks = true
                        activity.renameFile(path, newPath, false) { success, _ ->
                            ignoreClicks = false
                            if (success) {
                                callback(newPath)
                                alertDialog.dismiss()
                            }
                        }
                    }
                }
            }
    }
}
