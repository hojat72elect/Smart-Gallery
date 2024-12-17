package ca.hojat.smart.gallery.shared.broadcast_receivers

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ca.hojat.smart.gallery.shared.helpers.ensureBackgroundThread
import ca.hojat.smart.gallery.shared.extensions.updateDirectoryPath
import ca.hojat.smart.gallery.shared.helpers.MediaFetcher

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
