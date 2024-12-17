package ca.hojat.smart.gallery.shared.ui.adapters

import ca.hojat.smart.gallery.shared.data.domain.FileDirItem
import ca.hojat.smart.gallery.shared.data.domain.ThumbnailItem

interface MediaOperationsListener {
    fun refreshItems()

    fun tryDeleteFiles(fileDirItems: ArrayList<FileDirItem>, skipRecycleBin: Boolean)

    fun selectedPaths(paths: ArrayList<String>)

    fun updateMediaGridDecoration(media: ArrayList<ThumbnailItem>)
}
