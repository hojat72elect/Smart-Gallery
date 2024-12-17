package ca.hojat.smart.gallery.shared.ui.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.databinding.DialogUpgradeToProBinding
import ca.hojat.smart.gallery.shared.extensions.baseConfig
import ca.hojat.smart.gallery.shared.extensions.getAlertDialogBuilder
import ca.hojat.smart.gallery.shared.extensions.getStoreUrl
import ca.hojat.smart.gallery.shared.extensions.hideKeyboard
import ca.hojat.smart.gallery.shared.extensions.launchViewIntent
import ca.hojat.smart.gallery.shared.extensions.setupDialogStuff

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
