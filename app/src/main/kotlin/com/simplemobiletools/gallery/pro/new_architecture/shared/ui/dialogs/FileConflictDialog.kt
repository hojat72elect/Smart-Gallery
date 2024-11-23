package com.simplemobiletools.gallery.pro.new_architecture.shared.ui.dialogs

import android.app.Activity
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.databinding.DialogFileConflictBinding
import com.simplemobiletools.gallery.pro.new_architecture.shared.data.domain.FileDirItem
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.baseConfig
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.beVisibleIf
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getAlertDialogBuilder
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.setupDialogStuff
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.CONFLICT_KEEP_BOTH
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.CONFLICT_MERGE
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.CONFLICT_OVERWRITE
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.CONFLICT_SKIP

class FileConflictDialog(
    val activity: Activity, val fileDirItem: FileDirItem, private val showApplyToAllCheckbox: Boolean,
    val callback: (resolution: Int, applyForAll: Boolean) -> Unit
) {
    val view = DialogFileConflictBinding.inflate(activity.layoutInflater, null, false)

    init {
        view.apply {
            val stringBase =
                if (fileDirItem.isDirectory) R.string.folder_already_exists else R.string.file_already_exists
            conflictDialogTitle.text =
                String.format(activity.getString(stringBase), fileDirItem.name)
            conflictDialogApplyToAll.isChecked = activity.baseConfig.lastConflictApplyToAll
            conflictDialogApplyToAll.beVisibleIf(showApplyToAllCheckbox)
            conflictDialogDivider.root.beVisibleIf(showApplyToAllCheckbox)
            conflictDialogRadioMerge.beVisibleIf(fileDirItem.isDirectory)

            val resolutionButton = when (activity.baseConfig.lastConflictResolution) {
                CONFLICT_OVERWRITE -> conflictDialogRadioOverwrite
                CONFLICT_MERGE -> conflictDialogRadioMerge
                else -> conflictDialogRadioSkip
            }
            resolutionButton.isChecked = true
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { _, _ -> dialogConfirmed() }
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view.root, this)
            }
    }

    private fun dialogConfirmed() {
        val resolution = when (view.conflictDialogRadioGroup.checkedRadioButtonId) {
            view.conflictDialogRadioSkip.id -> CONFLICT_SKIP
            view.conflictDialogRadioMerge.id -> CONFLICT_MERGE
            view.conflictDialogRadioKeepBoth.id -> CONFLICT_KEEP_BOTH
            else -> CONFLICT_OVERWRITE
        }

        val applyToAll = view.conflictDialogApplyToAll.isChecked
        activity.baseConfig.apply {
            lastConflictApplyToAll = applyToAll
            lastConflictResolution = resolution
        }

        callback(resolution, applyToAll)
    }
}

