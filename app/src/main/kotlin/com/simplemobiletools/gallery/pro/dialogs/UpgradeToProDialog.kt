package com.simplemobiletools.gallery.pro.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.databinding.DialogUpgradeToProBinding
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.baseConfig
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getAlertDialogBuilder
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getStoreUrl
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.hideKeyboard
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.launchViewIntent
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.setupDialogStuff

class UpgradeToProDialog(val activity: Activity) {

    init {
        val view = DialogUpgradeToProBinding.inflate(activity.layoutInflater, null, false).apply {
            upgradeToPro.text = activity.getString(R.string.upgrade_to_pro_long)
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.upgrade) { _, _ -> upgradeApp() }
            .setNeutralButton(
                R.string.more_info,
                null
            )     // do not dismiss the dialog on pressing More Info
            .setNegativeButton(R.string.later, null)
            .apply {
                activity.setupDialogStuff(
                    view.root,
                    this,
                    R.string.upgrade_to_pro,
                    cancelOnTouchOutside = false
                ) { alertDialog ->
                    alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                        moreInfo()
                    }
                }
            }
    }

    private fun upgradeApp() {

        with(activity) {
            hideKeyboard()
            try {

                launchViewIntent("market://details?id=${baseConfig.appId.removeSuffix(".debug")}.pro")
            } catch (ignored: Exception) {
                launchViewIntent(getStoreUrl())
            }
        }

    }

    private fun moreInfo() {
        activity.launchViewIntent("https://simplemobiletools.com/upgrade_to_pro")
    }
}
