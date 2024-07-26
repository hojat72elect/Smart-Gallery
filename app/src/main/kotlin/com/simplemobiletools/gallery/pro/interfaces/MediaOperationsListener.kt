package com.simplemobiletools.gallery.pro.interfaces

import com.simplemobiletools.gallery.pro.models.FileDirItem
import com.simplemobiletools.gallery.pro.models.ThumbnailItem

interface MediaOperationsListener {
    fun refreshItems()

    fun tryDeleteFiles(fileDirItems: ArrayList<FileDirItem>, skipRecycleBin: Boolean)

    fun selectedPaths(paths: ArrayList<String>)

    fun updateMediaGridDecoration(media: ArrayList<ThumbnailItem>)
}
