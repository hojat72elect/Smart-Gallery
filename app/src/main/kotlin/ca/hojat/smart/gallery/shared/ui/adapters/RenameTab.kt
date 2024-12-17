package ca.hojat.smart.gallery.shared.ui.adapters

import ca.hojat.smart.gallery.shared.activities.BaseActivity

interface RenameTab {
    fun initTab(activity: BaseActivity, paths: ArrayList<String>)

    fun dialogConfirmed(useMediaFileExtension: Boolean, callback: (success: Boolean) -> Unit)
}
