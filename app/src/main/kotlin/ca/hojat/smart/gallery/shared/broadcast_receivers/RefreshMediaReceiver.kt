package ca.hojat.smart.gallery.shared.broadcast_receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ca.hojat.smart.gallery.shared.helpers.REFRESH_PATH
import ca.hojat.smart.gallery.shared.extensions.addPathToDB

class RefreshMediaReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val path = intent.getStringExtra(REFRESH_PATH) ?: return
        context.addPathToDB(path)
    }
}
