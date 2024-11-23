package com.simplemobiletools.gallery.pro.shared.broadcast_receivers

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.simplemobiletools.gallery.pro.shared.helpers.ensureBackgroundThread
import com.simplemobiletools.gallery.pro.shared.extensions.updateDirectoryPath
import com.simplemobiletools.gallery.pro.shared.helpers.MediaFetcher

@SuppressLint("UnsafeProtectedBroadcastReceiver")
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ensureBackgroundThread {
            MediaFetcher(context).getFoldersToScan().forEach {
                context.updateDirectoryPath(it)
            }
        }
    }
}
