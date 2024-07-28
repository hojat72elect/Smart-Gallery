package com.simplemobiletools.gallery.pro.compose.extensions

import android.content.Context
import com.simplemobiletools.gallery.pro.extensions.baseConfig
import com.simplemobiletools.gallery.pro.extensions.random


const val DEVELOPER_PLAY_STORE_URL = "https://play.google.com/store/apps/dev?id=9070296388022589266"
const val FAKE_VERSION_APP_LABEL =
    "You are using a fake version of the app. For your own safety download the original one from www.simplemobiletools.com. Thanks"

fun Context.fakeVersionCheck(
    showConfirmationDialog: () -> Unit
) {
    if (!packageName.startsWith("com.simplemobiletools.", true)) {
        if ((0..50).random() == 10 || baseConfig.appRunCount % 100 == 0) {
            showConfirmationDialog()
        }
    }
}




