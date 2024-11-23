package com.simplemobiletools.gallery.pro.feature_media_viewer

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.media3.common.util.UnstableApi

@RequiresApi(Build.VERSION_CODES.O)
@UnstableApi
class VideoActivity : PhotoVideoActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        mIsVideo = true
        super.onCreate(savedInstanceState)
    }
}
