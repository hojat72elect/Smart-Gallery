package com.simplemobiletools.gallery.pro.new_architecture.shared.ui.dialogs

import android.app.Activity
import android.view.LayoutInflater
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.databinding.DialogWhatsNewBinding
import com.simplemobiletools.gallery.pro.models.Release
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getAlertDialogBuilder
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.setupDialogStuff

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

