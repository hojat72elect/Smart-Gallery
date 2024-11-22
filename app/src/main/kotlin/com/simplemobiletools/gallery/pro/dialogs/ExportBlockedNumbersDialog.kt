package com.simplemobiletools.gallery.pro.dialogs

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.databinding.DialogExportBlockedNumbersBinding
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.baseConfig
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.beGone
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getAlertDialogBuilder
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getCurrentFormattedDateTime
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getParentPath
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.humanizePath
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.internalStoragePath
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.isAValidFilename
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.setupDialogStuff
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.showKeyboard
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.toast
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.value
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.BLOCKED_NUMBERS_EXPORT_EXTENSION
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.ensureBackgroundThread
import com.simplemobiletools.gallery.pro.new_architecture.shared.BaseActivity
import java.io.File

@SuppressLint("SetTextI18n")
@RequiresApi(Build.VERSION_CODES.O)
class ExportBlockedNumbersDialog(
    val activity: BaseActivity,
    val path: String,
    private val hidePath: Boolean,
    callback: (file: File) -> Unit,
) {
    private var realPath = path.ifEmpty { activity.internalStoragePath }
    private val config = activity.baseConfig

    init {
        val view =
            DialogExportBlockedNumbersBinding.inflate(activity.layoutInflater, null, false).apply {
                exportBlockedNumbersFolder.text = activity.humanizePath(realPath)
                exportBlockedNumbersFilename.setText("${activity.getString(R.string.blocked_numbers)}_${getCurrentFormattedDateTime()}")

                if (hidePath) {
                    exportBlockedNumbersFolderLabel.beGone()
                    exportBlockedNumbersFolder.beGone()
                } else {
                    exportBlockedNumbersFolder.setOnClickListener {
                        FilePickerDialog(activity, realPath, false, showFAB = true) {
                            exportBlockedNumbersFolder.text = activity.humanizePath(it)
                            realPath = it
                        }
                    }
                }
            }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(
                    view.root,
                    this,
                    R.string.export_blocked_numbers
                ) { alertDialog ->
                    alertDialog.showKeyboard(view.exportBlockedNumbersFilename)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val filename = view.exportBlockedNumbersFilename.value
                        when {
                            filename.isEmpty() -> activity.toast(R.string.empty_name)
                            filename.isAValidFilename() -> {
                                val file =
                                    File(realPath, "$filename$BLOCKED_NUMBERS_EXPORT_EXTENSION")
                                if (!hidePath && file.exists()) {
                                    activity.toast(R.string.name_taken)
                                    return@setOnClickListener
                                }

                                ensureBackgroundThread {
                                    config.lastBlockedNumbersExportPath =
                                        file.absolutePath.getParentPath()
                                    callback(file)
                                    alertDialog.dismiss()
                                }
                            }

                            else -> activity.toast(R.string.invalid_name)
                        }
                    }
                }
            }
    }
}
