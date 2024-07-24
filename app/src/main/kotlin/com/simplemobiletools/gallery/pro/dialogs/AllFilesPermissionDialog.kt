package com.simplemobiletools.gallery.pro.dialogs

import android.annotation.SuppressLint
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.gallery.pro.activities.BaseSimpleActivity
import com.simplemobiletools.gallery.pro.extensions.getAlertDialogBuilder
import com.simplemobiletools.gallery.pro.extensions.setupDialogStuff
import com.simplemobiletools.gallery.pro.R

@SuppressLint("InflateParams")
class AllFilesPermissionDialog(
    val activity: BaseSimpleActivity,
    message: String = "",
    val callback: (result: Boolean) -> Unit,
    val neutralPressed: () -> Unit
) {
    private var dialog: AlertDialog? = null

    init {
        val view = activity.layoutInflater.inflate(
            com.simplemobiletools.commons.R.layout.dialog_message,
            null
        )
        view.findViewById<TextView>(R.id.message).text = message

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.all_files) { _, _ -> positivePressed() }
            .setNeutralButton(R.string.media_only) { _, _ -> neutralPressed() }
            .apply {
                activity.setupDialogStuff(view, this) { alertDialog ->
                    dialog = alertDialog
                }
            }
    }

    private fun positivePressed() {
        dialog?.dismiss()
        callback(true)
    }
}
