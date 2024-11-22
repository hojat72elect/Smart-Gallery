package com.simplemobiletools.gallery.pro.dialogs

import android.os.Build
import androidx.annotation.RequiresApi
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.databinding.DialogGrantAllFilesBinding
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.applyColorFilter
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getAlertDialogBuilder
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getProperTextColor
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.setupDialogStuff
import com.simplemobiletools.gallery.pro.new_architecture.shared.activities.BaseActivity

@RequiresApi(Build.VERSION_CODES.R)
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
