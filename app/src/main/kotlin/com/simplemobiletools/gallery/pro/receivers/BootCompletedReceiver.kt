package com.simplemobiletools.gallery.pro.receivers

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.ensureBackgroundThread
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.updateDirectoryPath
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.MediaFetcher

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
