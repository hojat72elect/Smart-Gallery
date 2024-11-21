package com.simplemobiletools.gallery.pro.dialogs

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.databinding.DialogExportFavoritesBinding
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.beGone
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.config
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getAlertDialogBuilder
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getDoesFilePathExist
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getFilenameFromPath
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.humanizePath
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.internalStoragePath
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.isAValidFilename
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.setupDialogStuff
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.toast
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.value
import com.simplemobiletools.gallery.pro.new_architecture.BaseActivity

@RequiresApi(Build.VERSION_CODES.O)
class ExportFavoritesDialog(
    val activity: BaseActivity,
    private val defaultFilename: String,
    private val hidePath: Boolean,
    callback: (path: String, filename: String) -> Unit
) {
    init {
        val lastUsedFolder = activity.config.lastExportedFavoritesFolder
        var folder =
            if (lastUsedFolder.isNotEmpty() && activity.getDoesFilePathExist(lastUsedFolder)) {
                lastUsedFolder
            } else {
                activity.internalStoragePath
            }

        val binding = DialogExportFavoritesBinding.inflate(activity.layoutInflater).apply {
            exportFavoritesFilename.setText(defaultFilename.removeSuffix(".txt"))

            if (hidePath) {
                exportFavoritesPathLabel.beGone()
                exportFavoritesPath.beGone()
            } else {
                exportFavoritesPath.text = activity.humanizePath(folder)
                exportFavoritesPath.setOnClickListener {
                    FilePickerDialog(activity, folder, false, showFAB = true) {
                        exportFavoritesPath.text = activity.humanizePath(it)
                        folder = it
                    }
                }
            }
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(
                    binding.root,
                    this,
                    R.string.export_favorite_paths
                ) { alertDialog ->
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        var filename = binding.exportFavoritesFilename.value
                        if (filename.isEmpty()) {
                            activity.toast(R.string.filename_cannot_be_empty)
                            return@setOnClickListener
                        }

                        filename += ".txt"
                        val newPath = "${folder.trimEnd('/')}/$filename"
                        if (!newPath.getFilenameFromPath().isAValidFilename()) {
                            activity.toast(R.string.filename_invalid_characters)
                            return@setOnClickListener
                        }

                        activity.config.lastExportedFavoritesFolder = folder
                        if (!hidePath && activity.getDoesFilePathExist(newPath)) {
                            val title = String.format(
                                activity.getString(R.string.file_already_exists_overwrite),
                                newPath.getFilenameFromPath()
                            )
                            ConfirmationDialog(activity, title) {
                                callback(newPath, filename)
                                alertDialog.dismiss()
                            }
                        } else {
                            callback(newPath, filename)
                            alertDialog.dismiss()
                        }
                    }
                }
            }
    }
}
