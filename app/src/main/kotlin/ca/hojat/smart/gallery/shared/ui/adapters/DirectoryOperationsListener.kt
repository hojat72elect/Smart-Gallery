package ca.hojat.smart.gallery.shared.ui.adapters

import ca.hojat.smart.gallery.shared.data.domain.Directory
import java.io.File

interface DirectoryOperationsListener {
    fun refreshItems()

    fun deleteFolders(folders: ArrayList<File>)

    fun recheckPinnedFolders()

    fun updateDirectories(directories: ArrayList<Directory>)
}
