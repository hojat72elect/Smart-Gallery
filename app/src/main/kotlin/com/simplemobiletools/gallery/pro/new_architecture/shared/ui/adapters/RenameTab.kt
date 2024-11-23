package com.simplemobiletools.gallery.pro.new_architecture.shared.ui.adapters

import com.simplemobiletools.gallery.pro.new_architecture.shared.activities.BaseActivity

interface RenameTab {
    fun initTab(activity: BaseActivity, paths: ArrayList<String>)

    fun dialogConfirmed(useMediaFileExtension: Boolean, callback: (success: Boolean) -> Unit)
}
