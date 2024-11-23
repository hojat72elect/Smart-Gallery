package com.simplemobiletools.gallery.pro.shared.ui.adapters

import com.simplemobiletools.gallery.pro.shared.data.domain.Directory
import java.io.File

interface DirectoryOperationsListener {
    fun refreshItems()

    fun deleteFolders(folders: ArrayList<File>)

    fun recheckPinnedFolders()

    fun updateDirectories(directories: ArrayList<Directory>)
}
