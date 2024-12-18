package ca.hojat.smart.gallery.shared.ui.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.databinding.DialogMessageBinding
import ca.hojat.smart.gallery.shared.extensions.getAlertDialogBuilder
import ca.hojat.smart.gallery.shared.extensions.setupDialogStuff

/**
 * A simple dialog without any view, just a messageId, a positive button and optionally a negative button
 *
 * @param activity has to be activity context to avoid some Theme.AppCompat issues
 * @param message the dialogs message, can be any String. If empty, messageId is used
 * @param messageId the dialogs messageId ID. Used only if message is empty
 * @param positive positive buttons text ID
 * @param negative negative buttons text ID (optional)
 * @param callback an anonymous function
 */
class ConfirmationDialog(
    activity: Activity,
    message: String = "",
    messageId: Int = R.string.proceed_with_deletion, positive: Int = R.string.yes,
    negative: Int = R.string.no,
    private val cancelOnTouchOutside: Boolean = true,
    dialogTitle: String = "",
    val callback: () -> Unit
) {
    private var dialog: AlertDialog? = null

    init {
        val view = DialogMessageBinding.inflate(activity.layoutInflater, null, false)
        view.message.text = message.ifEmpty { activity.resources.getString(messageId) }

        val builder = activity.getAlertDialogBuilder()
            .setPositiveButton(positive) { _, _ -> dialogConfirmed() }

        if (negative != 0) {
            builder.setNegativeButton(negative, null)
        }

        builder.apply {
            activity.setupDialogStuff(
                view.root,
                this,
                titleText = dialogTitle,
                cancelOnTouchOutside = cancelOnTouchOutside
            ) { alertDialog ->
                dialog = alertDialog
            }
        }
    }

    private fun dialogConfirmed() {
        dialog?.dismiss()
        callback()
    }
}


