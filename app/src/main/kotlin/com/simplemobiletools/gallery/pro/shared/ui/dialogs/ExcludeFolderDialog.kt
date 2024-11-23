package com.simplemobiletools.gallery.pro.shared.ui.dialogs

import android.view.ViewGroup
import android.widget.RadioGroup
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.databinding.RadioButtonBinding
import com.simplemobiletools.gallery.pro.shared.extensions.beVisibleIf
import com.simplemobiletools.gallery.pro.shared.extensions.getAlertDialogBuilder
import com.simplemobiletools.gallery.pro.shared.extensions.getBasePath
import com.simplemobiletools.gallery.pro.shared.extensions.setupDialogStuff
import com.simplemobiletools.gallery.pro.databinding.DialogExcludeFolderBinding
import com.simplemobiletools.gallery.pro.shared.extensions.config
import com.simplemobiletools.gallery.pro.shared.activities.BaseActivity

class ExcludeFolderDialog(
    val activity: BaseActivity,
    private val selectedPaths: List<String>,
    val callback: () -> Unit
) {
    private val alternativePaths = getAlternativePathsList()
    private var radioGroup: RadioGroup? = null

    init {
        val binding = DialogExcludeFolderBinding.inflate(activity.layoutInflater).apply {
            excludeFolderParent.beVisibleIf(alternativePaths.size > 1)

            radioGroup = excludeFolderRadioGroup
            excludeFolderRadioGroup.beVisibleIf(alternativePaths.size > 1)
        }

        alternativePaths.forEachIndexed { index, _ ->
            val radioButton = RadioButtonBinding.inflate(activity.layoutInflater).root.apply {
                text = alternativePaths[index]
                isChecked = index == 0
                id = index
            }
            radioGroup!!.addView(
                radioButton,
                RadioGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { _, _ -> dialogConfirmed() }
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this)
            }
    }

    private fun dialogConfirmed() {
        val path =
            if (alternativePaths.isEmpty()) selectedPaths[0] else alternativePaths[radioGroup!!.checkedRadioButtonId]
        activity.config.addExcludedFolder(path)
        callback()
    }

    private fun getAlternativePathsList(): List<String> {
        val pathsList = ArrayList<String>()
        if (selectedPaths.size > 1)
            return pathsList

        val path = selectedPaths[0]
        var basePath = path.getBasePath(activity)
        val relativePath = path.substring(basePath.length)
        val parts = relativePath.split("/").filter(String::isNotEmpty)
        if (parts.isEmpty())
            return pathsList

        pathsList.add(basePath)
        if (basePath == "/")
            basePath = ""

        for (part in parts) {
            basePath += "/$part"
            pathsList.add(basePath)
        }

        return pathsList.reversed()
    }
}
