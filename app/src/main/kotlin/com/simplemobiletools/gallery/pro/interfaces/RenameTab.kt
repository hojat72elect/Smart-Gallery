package com.simplemobiletools.gallery.pro.interfaces

import com.simplemobiletools.gallery.pro.new_architecture.BaseActivity

interface RenameTab {
    fun initTab(activity: BaseActivity, paths: ArrayList<String>)

    fun dialogConfirmed(useMediaFileExtension: Boolean, callback: (success: Boolean) -> Unit)
}
