package ca.hojat.smart.gallery.feature_media_viewer

import android.os.Bundle
import androidx.media3.common.util.UnstableApi

@UnstableApi
class VideoActivity : PhotoVideoActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        mIsVideo = true
        super.onCreate(savedInstanceState)
    }
}
