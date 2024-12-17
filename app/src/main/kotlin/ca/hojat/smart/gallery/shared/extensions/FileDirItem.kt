package ca.hojat.smart.gallery.shared.extensions

import android.content.Context
import ca.hojat.smart.gallery.shared.data.domain.FileDirItem

fun FileDirItem.isDownloadsFolder() = path.isDownloadsFolder()

fun FileDirItem.isRecycleBinPath(context: Context): Boolean {
    return path.startsWith(context.recycleBinPath)
}