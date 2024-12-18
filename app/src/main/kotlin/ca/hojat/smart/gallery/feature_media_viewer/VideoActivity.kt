package ca.hojat.smart.gallery.feature_media_viewer

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.media3.common.util.UnstableApi
import ca.hojat.smart.gallery.feature_media_viewer.PhotoVideoActivity

@UnstableApi
class VideoActivity : PhotoVideoActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        mIsVideo = true
        super.onCreate(savedInstanceState)
    }
}
