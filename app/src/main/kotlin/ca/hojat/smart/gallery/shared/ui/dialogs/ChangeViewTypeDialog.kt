package ca.hojat.smart.gallery.shared.ui.dialogs

import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.databinding.DialogChangeViewTypeBinding
import ca.hojat.smart.gallery.shared.extensions.beVisibleIf
import ca.hojat.smart.gallery.shared.extensions.config
import ca.hojat.smart.gallery.shared.extensions.getAlertDialogBuilder
import ca.hojat.smart.gallery.shared.extensions.setupDialogStuff
import ca.hojat.smart.gallery.shared.helpers.SHOW_ALL
import ca.hojat.smart.gallery.shared.helpers.VIEW_TYPE_GRID
import ca.hojat.smart.gallery.shared.helpers.VIEW_TYPE_LIST
import ca.hojat.smart.gallery.shared.activities.BaseActivity

class ChangeViewTypeDialog(
    val activity: BaseActivity,
    private val fromFoldersView: Boolean,
    val path: String = "",
    val callback: () -> Unit
) {
    private val binding = DialogChangeViewTypeBinding.inflate(activity.layoutInflater)
    private var config = activity.config
    private var pathToUse = path.ifEmpty { SHOW_ALL }

    init {
        binding.apply {
            val viewToCheck = if (fromFoldersView) {
                if (config.viewTypeFolders == VIEW_TYPE_GRID) {
                    changeViewTypeDialogRadioGrid.id
                } else {
                    changeViewTypeDialogRadioList.id
                }
            } else {
                val currViewType = config.getFolderViewType(pathToUse)
                if (currViewType == VIEW_TYPE_GRID) {
                    changeViewTypeDialogRadioGrid.id
                } else {
                    changeViewTypeDialogRadioList.id
                }
            }

            changeViewTypeDialogRadio.check(viewToCheck)
            changeViewTypeDialogGroupDirectSubfolders.apply {
                beVisibleIf(fromFoldersView)
                isChecked = config.groupDirectSubfolders
            }

            changeViewTypeDialogUseForThisFolder.apply {
                beVisibleIf(!fromFoldersView)
                isChecked = config.hasCustomViewType(pathToUse)
            }
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { _, _ -> dialogConfirmed() }
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this)
            }
    }

    private fun dialogConfirmed() {
        val viewType =
            if (binding.changeViewTypeDialogRadio.checkedRadioButtonId == binding.changeViewTypeDialogRadioGrid.id) {
                VIEW_TYPE_GRID
            } else {
                VIEW_TYPE_LIST
            }

        if (fromFoldersView) {
            config.viewTypeFolders = viewType
            config.groupDirectSubfolders =
                binding.changeViewTypeDialogGroupDirectSubfolders.isChecked
        } else {
            if (binding.changeViewTypeDialogUseForThisFolder.isChecked) {
                config.saveFolderViewType(pathToUse, viewType)
            } else {
                config.removeFolderViewType(pathToUse)
                config.viewTypeFiles = viewType
            }
        }


        callback()
    }
}
