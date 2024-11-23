package com.simplemobiletools.gallery.pro.shared.ui.adapters

import com.simplemobiletools.gallery.pro.shared.activities.BaseActivity

interface RenameTab {
    fun initTab(activity: BaseActivity, paths: ArrayList<String>)

    fun dialogConfirmed(useMediaFileExtension: Boolean, callback: (success: Boolean) -> Unit)
}
