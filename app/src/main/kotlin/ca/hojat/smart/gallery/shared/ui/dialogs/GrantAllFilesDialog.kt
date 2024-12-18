package ca.hojat.smart.gallery.shared.ui.dialogs

import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.databinding.DialogGrantAllFilesBinding
import ca.hojat.smart.gallery.shared.activities.BaseActivity
import ca.hojat.smart.gallery.shared.extensions.applyColorFilter
import ca.hojat.smart.gallery.shared.extensions.getAlertDialogBuilder
import ca.hojat.smart.gallery.shared.extensions.getProperTextColor
import ca.hojat.smart.gallery.shared.extensions.setupDialogStuff

class GrantAllFilesDialog(val activity: BaseActivity) {
    init {
        val binding = DialogGrantAllFilesBinding.inflate(activity.layoutInflater)
        binding.grantAllFilesImage.applyColorFilter(activity.getProperTextColor())

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { _, _ -> activity.launchGrantAllFilesIntent() }
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this) { }
            }
    }
}
