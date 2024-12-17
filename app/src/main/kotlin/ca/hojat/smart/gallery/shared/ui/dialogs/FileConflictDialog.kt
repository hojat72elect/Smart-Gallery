package ca.hojat.smart.gallery.shared.ui.dialogs

import android.app.Activity
import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.databinding.DialogFileConflictBinding
import ca.hojat.smart.gallery.shared.data.domain.FileDirItem
import ca.hojat.smart.gallery.shared.extensions.baseConfig
import ca.hojat.smart.gallery.shared.extensions.beVisibleIf
import ca.hojat.smart.gallery.shared.extensions.getAlertDialogBuilder
import ca.hojat.smart.gallery.shared.extensions.setupDialogStuff
import ca.hojat.smart.gallery.shared.helpers.CONFLICT_KEEP_BOTH
import ca.hojat.smart.gallery.shared.helpers.CONFLICT_MERGE
import ca.hojat.smart.gallery.shared.helpers.CONFLICT_OVERWRITE
import ca.hojat.smart.gallery.shared.helpers.CONFLICT_SKIP

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

