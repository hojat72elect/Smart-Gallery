package com.simplemobiletools.gallery.pro.new_architecture.shared.extensions

import android.content.Context
import com.simplemobiletools.gallery.pro.new_architecture.shared.data.domain.FileDirItem

fun FileDirItem.isDownloadsFolder() = path.isDownloadsFolder()

fun FileDirItem.isRecycleBinPath(context: Context): Boolean {
    return path.startsWith(context.recycleBinPath)
}