package ca.hojat.smart.gallery.shared.ui.dialogs

import android.app.Activity
import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.databinding.DialogTextviewBinding
import ca.hojat.smart.gallery.shared.extensions.baseConfig
import ca.hojat.smart.gallery.shared.extensions.getAlertDialogBuilder
import ca.hojat.smart.gallery.shared.extensions.setupDialogStuff

class FolderLockingNoticeDialog(val activity: Activity, val callback: () -> Unit) {
    init {
        val view = DialogTextviewBinding.inflate(activity.layoutInflater, null, false).apply {
            textView.text = activity.getString(R.string.lock_folder_notice)
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { _, _ -> dialogConfirmed() }
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view.root, this, R.string.disclaimer)
            }
    }

    private fun dialogConfirmed() {
        activity.baseConfig.wasFolderLockingNoticeShown = true
        callback()
    }
}
