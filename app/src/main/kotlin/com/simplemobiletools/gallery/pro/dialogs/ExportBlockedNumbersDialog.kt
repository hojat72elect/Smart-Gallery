package com.simplemobiletools.gallery.pro.dialogs

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.databinding.DialogExportBlockedNumbersBinding
import com.simplemobiletools.gallery.pro.activities.BaseSimpleActivity
import com.simplemobiletools.gallery.pro.extensions.baseConfig
import com.simplemobiletools.gallery.pro.extensions.beGone
import com.simplemobiletools.gallery.pro.extensions.getAlertDialogBuilder
import com.simplemobiletools.gallery.pro.extensions.getCurrentFormattedDateTime
import com.simplemobiletools.gallery.pro.extensions.getParentPath
import com.simplemobiletools.gallery.pro.extensions.humanizePath
import com.simplemobiletools.gallery.pro.extensions.internalStoragePath
import com.simplemobiletools.gallery.pro.extensions.isAValidFilename
import com.simplemobiletools.gallery.pro.extensions.setupDialogStuff
import com.simplemobiletools.gallery.pro.extensions.showKeyboard
import com.simplemobiletools.gallery.pro.extensions.toast
import com.simplemobiletools.gallery.pro.extensions.value
import com.simplemobiletools.gallery.pro.helpers.BLOCKED_NUMBERS_EXPORT_EXTENSION
import com.simplemobiletools.gallery.pro.helpers.ensureBackgroundThread
import java.io.File

@SuppressLint("SetTextI18n")
@RequiresApi(Build.VERSION_CODES.O)
class ExportBlockedNumbersDialog(
    val activity: BaseSimpleActivity,
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
