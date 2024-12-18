package ca.hojat.smart.gallery.shared.ui.dialogs

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.databinding.DialogExportBlockedNumbersBinding
import ca.hojat.smart.gallery.shared.extensions.baseConfig
import ca.hojat.smart.gallery.shared.extensions.beGone
import ca.hojat.smart.gallery.shared.extensions.getAlertDialogBuilder
import ca.hojat.smart.gallery.shared.extensions.getCurrentFormattedDateTime
import ca.hojat.smart.gallery.shared.extensions.getParentPath
import ca.hojat.smart.gallery.shared.extensions.humanizePath
import ca.hojat.smart.gallery.shared.extensions.internalStoragePath
import ca.hojat.smart.gallery.shared.extensions.isAValidFilename
import ca.hojat.smart.gallery.shared.extensions.setupDialogStuff
import ca.hojat.smart.gallery.shared.extensions.showKeyboard
import ca.hojat.smart.gallery.shared.extensions.toast
import ca.hojat.smart.gallery.shared.extensions.value
import ca.hojat.smart.gallery.shared.helpers.BLOCKED_NUMBERS_EXPORT_EXTENSION
import ca.hojat.smart.gallery.shared.helpers.ensureBackgroundThread
import ca.hojat.smart.gallery.shared.activities.BaseActivity
import java.io.File

@SuppressLint("SetTextI18n")
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
