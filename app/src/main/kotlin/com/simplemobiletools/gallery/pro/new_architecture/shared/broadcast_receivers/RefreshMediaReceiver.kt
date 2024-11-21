package com.simplemobiletools.gallery.pro.new_architecture.shared.broadcast_receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.REFRESH_PATH
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.addPathToDB

class RefreshMediaReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val path = intent.getStringExtra(REFRESH_PATH) ?: return
        context.addPathToDB(path)
    }
}
