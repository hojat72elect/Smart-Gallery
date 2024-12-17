package ca.hojat.smart.gallery.shared.ui.dialogs

import android.app.Activity
import android.view.LayoutInflater
import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.databinding.DialogWhatsNewBinding
import ca.hojat.smart.gallery.shared.data.domain.Release
import ca.hojat.smart.gallery.shared.extensions.getAlertDialogBuilder
import ca.hojat.smart.gallery.shared.extensions.setupDialogStuff

class WhatsNewDialog(val activity: Activity, private val releases: List<Release>) {
    init {
        val view = DialogWhatsNewBinding.inflate(LayoutInflater.from(activity), null, false)
        view.whatsNewContent.text = getNewReleases()

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .apply {
                activity.setupDialogStuff(
                    view.root,
                    this,
                    R.string.whats_new,
                    cancelOnTouchOutside = false
                )
            }
    }

    private fun getNewReleases(): String {
        val stringBuilder = StringBuilder()

        releases.forEach {release ->
            val parts = activity.getString(release.textId).split("\n").map(String::trim)
            parts.forEach { releasePart ->
                stringBuilder.append("- $releasePart\n")
            }
        }

        return stringBuilder.toString()
    }
}

