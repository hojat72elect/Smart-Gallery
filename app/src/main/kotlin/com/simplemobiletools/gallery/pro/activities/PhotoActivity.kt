package com.simplemobiletools.gallery.pro.activities

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.media3.common.util.UnstableApi
import com.simplemobiletools.gallery.pro.new_architecture.feature_media_viewer.PhotoVideoActivity

@SuppressLint("NewApi")
@UnstableApi
class PhotoActivity : PhotoVideoActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        mIsVideo = false
        super.onCreate(savedInstanceState)
    }
}
