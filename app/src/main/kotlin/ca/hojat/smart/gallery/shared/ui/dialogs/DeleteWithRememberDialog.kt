package ca.hojat.smart.gallery.shared.ui.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import ca.hojat.smart.gallery.shared.extensions.beGoneIf
import ca.hojat.smart.gallery.shared.extensions.getAlertDialogBuilder
import ca.hojat.smart.gallery.shared.extensions.setupDialogStuff
import ca.hojat.smart.gallery.databinding.DialogDeleteWithRememberBinding
import ca.hojat.smart.gallery.R

class DeleteWithRememberDialog(
    private val activity: Activity,
    message: String,
    showSkipRecycleBinOption: Boolean,
    private val callback: (remember: Boolean, skipRecycleBin: Boolean) -> Unit
) {

    private var dialog: AlertDialog? = null
    private val binding = DialogDeleteWithRememberBinding.inflate(activity.layoutInflater)

    init {
        binding.deleteRememberTitle.text = message
        binding.skipTheRecycleBinCheckbox.beGoneIf(!showSkipRecycleBinOption)
        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.yes) { _, _ -> dialogConfirmed() }
            .setNegativeButton(R.string.no, null)
            .apply {
                activity.setupDialogStuff(binding.root, this) { alertDialog ->
                    dialog = alertDialog
                }
            }
    }

    private fun dialogConfirmed() {
        dialog?.dismiss()
        callback(
            binding.deleteRememberCheckbox.isChecked,
            binding.skipTheRecycleBinCheckbox.isChecked
        )
    }
}
