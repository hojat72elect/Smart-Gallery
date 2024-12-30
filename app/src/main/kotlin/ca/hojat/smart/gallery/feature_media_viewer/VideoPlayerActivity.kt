package ca.hojat.smart.gallery.feature_media_viewer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.SeekBar
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.ContentDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.databinding.ActivityVideoPlayerBinding
import ca.hojat.smart.gallery.shared.activities.BaseActivity
import ca.hojat.smart.gallery.shared.extensions.actionBarHeight
import ca.hojat.smart.gallery.shared.extensions.beGone
import ca.hojat.smart.gallery.shared.extensions.beVisible
import ca.hojat.smart.gallery.shared.extensions.beVisibleIf
import ca.hojat.smart.gallery.shared.extensions.config
import ca.hojat.smart.gallery.shared.extensions.getColoredDrawableWithColor
import ca.hojat.smart.gallery.shared.extensions.getFilenameFromUri
import ca.hojat.smart.gallery.shared.extensions.getFormattedDuration
import ca.hojat.smart.gallery.shared.extensions.hasNavBar
import ca.hojat.smart.gallery.shared.extensions.hideSystemUI
import ca.hojat.smart.gallery.shared.extensions.navigationBarHeight
import ca.hojat.smart.gallery.shared.extensions.navigationBarOnSide
import ca.hojat.smart.gallery.shared.extensions.navigationBarWidth
import ca.hojat.smart.gallery.shared.extensions.onGlobalLayout
import ca.hojat.smart.gallery.shared.extensions.openPath
import ca.hojat.smart.gallery.shared.extensions.portrait
import ca.hojat.smart.gallery.shared.extensions.shareMediumPath
import ca.hojat.smart.gallery.shared.extensions.showSystemUI
import ca.hojat.smart.gallery.shared.extensions.statusBarHeight
import ca.hojat.smart.gallery.shared.extensions.updateTextColors
import ca.hojat.smart.gallery.shared.extensions.viewBinding
import ca.hojat.smart.gallery.shared.helpers.DRAG_THRESHOLD
import ca.hojat.smart.gallery.shared.helpers.FAST_FORWARD_VIDEO_MS
import ca.hojat.smart.gallery.shared.helpers.GO_TO_NEXT_ITEM
import ca.hojat.smart.gallery.shared.helpers.GO_TO_PREV_ITEM
import ca.hojat.smart.gallery.shared.helpers.HIDE_SYSTEM_UI_DELAY
import ca.hojat.smart.gallery.shared.helpers.MAX_CLOSE_DOWN_GESTURE_DURATION
import ca.hojat.smart.gallery.shared.helpers.ROTATE_BY_ASPECT_RATIO
import ca.hojat.smart.gallery.shared.helpers.ROTATE_BY_DEVICE_ROTATION
import ca.hojat.smart.gallery.shared.helpers.ROTATE_BY_SYSTEM_SETTING
import ca.hojat.smart.gallery.shared.helpers.SHOW_NEXT_ITEM
import ca.hojat.smart.gallery.shared.helpers.SHOW_PREV_ITEM
import ca.hojat.smart.gallery.shared.usecases.ShowToastUseCase
import ca.hojat.smart.gallery.shared.usecases.ShowToastUseCase.invoke
import kotlin.math.abs

@UnstableApi
open class VideoPlayerActivity : BaseActivity(), SeekBar.OnSeekBarChangeListener,
    TextureView.SurfaceTextureListener {


    private var mIsFullscreen = false
    private var mIsPlaying = false
    private var mWasVideoStarted = false
    private var mIsDragged = false
    private var mIsOrientationLocked = false
    private var mScreenWidth = 0
    private var mCurrTime = 0
    private var mDuration = 0
    private var mDragThreshold = 0f
    private var mTouchDownX = 0f
    private var mTouchDownY = 0f
    private var mTouchDownTime = 0L
    private var mProgressAtDown = 0L
    private var mCloseDownThreshold = 100f

    private var mUri: Uri? = null
    private var mExoPlayer: ExoPlayer? = null
    private var mVideoSize = Point(0, 0)
    private var mTimerHandler = Handler()
    private var mPlayWhenReadyHandler = Handler()

    private var mIgnoreCloseDown = false

    private val binding by viewBinding(ActivityVideoPlayerBinding::inflate)

    public override fun onCreate(savedInstanceState: Bundle?) {
        showTransparentTop = true
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupOptionsMenu()
        setupOrientation()
        checkNotchSupport()
        initPlayer()
    }

    override fun onResume() {
        super.onResume()
        binding.topShadow.layoutParams.height = statusBarHeight + actionBarHeight
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        if (config.blackBackground) {
            binding.videoPlayerHolder.background = ColorDrawable(Color.BLACK)
        }

        if (config.maxBrightness) {
            val attributes = window.attributes
            attributes.screenBrightness = 1f
            window.attributes = attributes
        }

        updateTextColors(binding.videoPlayerHolder)

        if (!portrait && navigationBarOnSide && navigationBarWidth > 0) {
            binding.videoToolbar.setPadding(0, 0, navigationBarWidth, 0)
        } else {
            binding.videoToolbar.setPadding(0, 0, 0, 0)
        }
    }

    override fun onPause() {
        super.onPause()
        pauseVideo()

        if (config.rememberLastVideoPosition && mWasVideoStarted) {
            saveVideoProgress()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isChangingConfigurations) {
            pauseVideo()
            binding.bottomVideoTimeHolder.videoCurrTime.text = 0.getFormattedDuration()
            releaseExoPlayer()
            binding.bottomVideoTimeHolder.videoSeekbar.progress = 0
            mTimerHandler.removeCallbacksAndMessages(null)
            mPlayWhenReadyHandler.removeCallbacksAndMessages(null)
        }
    }

    private fun setupOptionsMenu() {
        (binding.videoAppbar.layoutParams as RelativeLayout.LayoutParams).topMargin =
            statusBarHeight
        binding.videoToolbar.apply {
            setTitleTextColor(Color.WHITE)
            overflowIcon = resources.getColoredDrawableWithColor(
                R.drawable.ic_three_dots_vector,
                Color.WHITE
            )
            navigationIcon = resources.getColoredDrawableWithColor(
                R.drawable.ic_arrow_left_vector,
                Color.WHITE
            )
        }

        updateMenuItemColors(binding.videoToolbar.menu, forceWhiteIcons = true)
        binding.videoToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_change_orientation -> changeOrientation()
                R.id.menu_open_with -> openPath(mUri!!.toString(), true)
                R.id.menu_share -> shareMediumPath(mUri!!.toString())
                else -> return@setOnMenuItemClickListener false
            }
            return@setOnMenuItemClickListener true
        }

        binding.videoToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setVideoSize()
        initTimeHolder()
        binding.videoSurfaceFrame.onGlobalLayout {
            binding.videoSurfaceFrame.controller.resetState()
        }

        binding.topShadow.layoutParams.height = statusBarHeight + actionBarHeight
        (binding.videoAppbar.layoutParams as RelativeLayout.LayoutParams).topMargin =
            statusBarHeight
        if (!portrait && navigationBarOnSide && navigationBarWidth > 0) {
            binding.videoToolbar.setPadding(0, 0, navigationBarWidth, 0)
        } else {
            binding.videoToolbar.setPadding(0, 0, 0, 0)
        }
    }

    private fun setupOrientation() {
        if (!mIsOrientationLocked) {
            if (config.screenRotation == ROTATE_BY_DEVICE_ROTATION) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
            } else if (config.screenRotation == ROTATE_BY_SYSTEM_SETTING) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initPlayer() {
        mUri = intent.data ?: return
        binding.videoToolbar.title = getFilenameFromUri(mUri!!)
        initTimeHolder()

        showSystemUI()
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            val isFullscreen = visibility and View.SYSTEM_UI_FLAG_FULLSCREEN != 0
            fullscreenToggled(isFullscreen)
        }

        binding.bottomVideoTimeHolder.videoCurrTime.setOnClickListener { doSkip(false) }
        binding.bottomVideoTimeHolder.videoDuration.setOnClickListener { doSkip(true) }
        binding.bottomVideoTimeHolder.videoTogglePlayPause.setOnClickListener { togglePlayPause() }
        binding.videoSurfaceFrame.setOnClickListener { toggleFullscreen() }
        binding.videoSurfaceFrame.controller.settings.swallowDoubleTaps = true

        binding.bottomVideoTimeHolder.videoNextFile.beVisibleIf(
            intent.getBooleanExtra(
                SHOW_NEXT_ITEM,
                false
            )
        )
        binding.bottomVideoTimeHolder.videoNextFile.setOnClickListener { handleNextFile() }

        binding.bottomVideoTimeHolder.videoPrevFile.beVisibleIf(
            intent.getBooleanExtra(
                SHOW_PREV_ITEM,
                false
            )
        )
        binding.bottomVideoTimeHolder.videoPrevFile.setOnClickListener { handlePrevFile() }

        val gestureDetector =
            GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    handleDoubleTap(e.rawX)
                    return true
                }
            })

        binding.videoSurfaceFrame.setOnTouchListener { _, event ->
            handleEvent(event)
            gestureDetector.onTouchEvent(event)
            false
        }

        initExoPlayer()
        binding.videoSurface.surfaceTextureListener = this

        if (config.allowVideoGestures) {
            binding.videoBrightnessController.initialize(
                this,
                binding.slideInfo,
                true,
                binding.videoPlayerHolder,
                singleTap = { _, _ ->
                    toggleFullscreen()
                },
                doubleTap = { _, _ ->
                    doSkip(false)
                })

            binding.videoVolumeController.initialize(
                this,
                binding.slideInfo,
                false,
                binding.videoPlayerHolder,
                singleTap = { _, _ ->
                    toggleFullscreen()
                },
                doubleTap = { _, _ ->
                    doSkip(true)
                })
        } else {
            binding.videoBrightnessController.beGone()
            binding.videoVolumeController.beGone()
        }

        if (config.hideSystemUI) {
            Handler().postDelayed({
                fullscreenToggled(true)
            }, HIDE_SYSTEM_UI_DELAY)
        }

        mDragThreshold = DRAG_THRESHOLD * resources.displayMetrics.density
    }

    private fun initExoPlayer() {
        val dataSpec = DataSpec(mUri!!)
        val fileDataSource = ContentDataSource(applicationContext)
        try {
            fileDataSource.open(dataSpec)
        } catch (e: Exception) {
            ShowToastUseCase(this, "Error : $e")
        }

        val factory = DataSource.Factory { fileDataSource }
        val mediaSource: MediaSource = ProgressiveMediaSource.Factory(factory)
            .createMediaSource(MediaItem.fromUri(fileDataSource.uri!!))

        mExoPlayer = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(applicationContext))
            .setSeekParameters(SeekParameters.CLOSEST_SYNC)
            .build()
            .apply {
                setMediaSource(mediaSource)
                setAudioAttributes(
                    AudioAttributes
                        .Builder()
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .build(), false
                )
                if (config.loopVideos) {
                    repeatMode = Player.REPEAT_MODE_ONE
                }
                prepare()
                initListeners()
            }
    }

    private fun ExoPlayer.initListeners() {
        addListener(object : Player.Listener {
            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                @Player.DiscontinuityReason reason: Int
            ) {
                // Reset progress views when video loops.
                if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION) {
                    binding.bottomVideoTimeHolder.videoSeekbar.progress = 0
                    binding.bottomVideoTimeHolder.videoCurrTime.text = 0.getFormattedDuration()
                }
            }

            override fun onPlaybackStateChanged(@Player.State playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> videoPrepared()
                    Player.STATE_ENDED -> videoCompleted()
                    Player.STATE_BUFFERING -> {
                        // Nothing is happening here
                    }

                    Player.STATE_IDLE -> {
                        // Nothing is happening here
                    }
                }
            }

            override fun onVideoSizeChanged(videoSize: VideoSize) {
                mVideoSize.x = videoSize.width
                mVideoSize.y = videoSize.height
                setVideoSize()
            }
        })
    }

    private fun videoPrepared() {
        if (!mWasVideoStarted) {
            binding.bottomVideoTimeHolder.videoTogglePlayPause.beVisible()
            mDuration = (mExoPlayer!!.duration / 1000).toInt()
            binding.bottomVideoTimeHolder.videoSeekbar.max = mDuration
            binding.bottomVideoTimeHolder.videoDuration.text = mDuration.getFormattedDuration()
            setPosition(mCurrTime)

            if (config.rememberLastVideoPosition) {
                setLastVideoSavedPosition()
            }

            if (config.autoplayVideos) {
                resumeVideo()
            } else {
                binding.bottomVideoTimeHolder.videoTogglePlayPause.setImageResource(R.drawable.ic_play_outline_vector)
            }
        }
    }

    private fun handleDoubleTap(x: Float) {
        val instantWidth = mScreenWidth / 7
        when {
            x <= instantWidth -> doSkip(false)
            x >= mScreenWidth - instantWidth -> doSkip(true)
            else -> togglePlayPause()
        }
    }

    private fun resumeVideo() {
        binding.bottomVideoTimeHolder.videoTogglePlayPause.setImageResource(R.drawable.ic_pause_outline_vector)
        if (mExoPlayer == null) {
            return
        }

        val wasEnded = didVideoEnd()
        if (wasEnded) {
            setPosition(0)
        }

        mWasVideoStarted = true
        mIsPlaying = true
        mExoPlayer?.playWhenReady = true
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun pauseVideo() {
        binding.bottomVideoTimeHolder.videoTogglePlayPause.setImageResource(R.drawable.ic_play_outline_vector)
        if (mExoPlayer == null) {
            return
        }

        mIsPlaying = false
        if (!didVideoEnd()) {
            mExoPlayer?.playWhenReady = false
        }

        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun togglePlayPause() {
        mIsPlaying = !mIsPlaying
        if (mIsPlaying) {
            resumeVideo()
        } else {
            pauseVideo()
        }
    }

    private fun setPosition(seconds: Int) {
        mExoPlayer?.seekTo(seconds * 1000L)
        binding.bottomVideoTimeHolder.videoSeekbar.progress = seconds
        binding.bottomVideoTimeHolder.videoCurrTime.text = seconds.getFormattedDuration()
    }

    private fun setLastVideoSavedPosition() {
        val pos = config.getLastVideoPosition(mUri.toString())
        if (pos > 0) {
            setPosition(pos)
        }
    }

    private fun videoCompleted() {
        if (mExoPlayer == null) {
            return
        }

        clearLastVideoSavedProgress()
        mCurrTime = (mExoPlayer!!.duration / 1000).toInt()
        binding.bottomVideoTimeHolder.videoSeekbar.progress =
            binding.bottomVideoTimeHolder.videoSeekbar.max
        binding.bottomVideoTimeHolder.videoCurrTime.text = mDuration.getFormattedDuration()
        pauseVideo()
    }

    private fun didVideoEnd(): Boolean {
        val currentPos = mExoPlayer?.currentPosition ?: 0
        val duration = mExoPlayer?.duration ?: 0
        return currentPos != 0L && currentPos >= duration
    }

    private fun saveVideoProgress() {
        if (!didVideoEnd()) {
            config.saveLastVideoPosition(
                mUri.toString(),
                mExoPlayer!!.currentPosition.toInt() / 1000
            )
        }
    }

    private fun clearLastVideoSavedProgress() {
        config.removeLastVideoPosition(mUri.toString())
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun setVideoSize() {
        val videoProportion = mVideoSize.x.toFloat() / mVideoSize.y.toFloat()
        val display = windowManager.defaultDisplay
        val screenWidth: Int
        val screenHeight: Int

        val realMetrics = DisplayMetrics()
        display.getRealMetrics(realMetrics)
        screenWidth = realMetrics.widthPixels
        screenHeight = realMetrics.heightPixels

        val screenProportion = screenWidth.toFloat() / screenHeight.toFloat()

        binding.videoSurface.layoutParams.apply {
            if (videoProportion > screenProportion) {
                width = screenWidth
                height = (screenWidth.toFloat() / videoProportion).toInt()
            } else {
                width = (videoProportion * screenHeight.toFloat()).toInt()
                height = screenHeight
            }
            binding.videoSurface.layoutParams = this
        }

        val multiplier = if (screenWidth > screenHeight) 0.5 else 0.8
        mScreenWidth = (screenWidth * multiplier).toInt()

        if (config.screenRotation == ROTATE_BY_ASPECT_RATIO) {
            if (mVideoSize.x > mVideoSize.y) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else if (mVideoSize.x < mVideoSize.y) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
    }

    private fun changeOrientation() {
        mIsOrientationLocked = true
        requestedOrientation =
            if (resources.configuration.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
    }

    private fun toggleFullscreen() {
        fullscreenToggled(!mIsFullscreen)
    }

    private fun fullscreenToggled(isFullScreen: Boolean) {
        mIsFullscreen = isFullScreen
        if (isFullScreen) {
            hideSystemUI()
        } else {
            showSystemUI()
        }

        val newAlpha = if (isFullScreen) 0f else 1f
        arrayOf(
            binding.bottomVideoTimeHolder.videoPrevFile,
            binding.bottomVideoTimeHolder.videoTogglePlayPause,
            binding.bottomVideoTimeHolder.videoNextFile,
            binding.bottomVideoTimeHolder.videoCurrTime,
            binding.bottomVideoTimeHolder.videoSeekbar,
            binding.bottomVideoTimeHolder.videoDuration,
            binding.topShadow,
            binding.videoBottomGradient
        ).forEach {
            it.animate().alpha(newAlpha).start()
        }
        binding.bottomVideoTimeHolder.videoSeekbar.setOnSeekBarChangeListener(if (mIsFullscreen) null else this)
        arrayOf(
            binding.bottomVideoTimeHolder.videoPrevFile,
            binding.bottomVideoTimeHolder.videoNextFile,
            binding.bottomVideoTimeHolder.videoCurrTime,
            binding.bottomVideoTimeHolder.videoDuration,
        ).forEach {
            it.isClickable = !mIsFullscreen
        }

        binding.videoAppbar.animate().alpha(newAlpha).withStartAction {
            binding.videoAppbar.beVisible()
        }.withEndAction {
            binding.videoAppbar.beVisibleIf(newAlpha == 1f)
        }.start()
    }

    private fun initTimeHolder() {
        var right = 0
        var bottom = 0

        if (hasNavBar()) {
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                bottom += navigationBarHeight
            } else {
                right += navigationBarWidth
                bottom += navigationBarHeight
            }
        }

        binding.bottomVideoTimeHolder.videoTimeHolder.setPadding(0, 0, right, bottom)
        binding.bottomVideoTimeHolder.videoSeekbar.setOnSeekBarChangeListener(this)
        binding.bottomVideoTimeHolder.videoSeekbar.max = mDuration
        binding.bottomVideoTimeHolder.videoDuration.text = mDuration.getFormattedDuration()
        binding.bottomVideoTimeHolder.videoCurrTime.text = mCurrTime.getFormattedDuration()
        setupTimer()
    }

    private fun setupTimer() {
        runOnUiThread(object : Runnable {
            override fun run() {
                if (mExoPlayer != null && !mIsDragged && mIsPlaying) {
                    mCurrTime = (mExoPlayer!!.currentPosition / 1000).toInt()
                    binding.bottomVideoTimeHolder.videoSeekbar.progress = mCurrTime
                    binding.bottomVideoTimeHolder.videoCurrTime.text =
                        mCurrTime.getFormattedDuration()
                }

                mTimerHandler.postDelayed(this, 1000)
            }
        })
    }

    private fun doSkip(forward: Boolean) {
        if (mExoPlayer == null) {
            return
        }

        val curr = mExoPlayer!!.currentPosition
        val newProgress =
            if (forward) curr + FAST_FORWARD_VIDEO_MS else curr - FAST_FORWARD_VIDEO_MS
        val roundProgress = Math.round(newProgress / 1000f)
        val limitedProgress =
            (mExoPlayer!!.duration.toInt() / 1000).coerceAtMost(roundProgress).coerceAtLeast(0)
        setPosition(limitedProgress)
        if (!mIsPlaying) {
            togglePlayPause()
        }
    }

    private fun handleEvent(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mTouchDownX = event.rawX
                mTouchDownY = event.rawY
                mTouchDownTime = System.currentTimeMillis()
                mProgressAtDown = mExoPlayer!!.currentPosition
            }

            MotionEvent.ACTION_POINTER_DOWN -> mIgnoreCloseDown = true
            MotionEvent.ACTION_MOVE -> {
                val diffX = event.rawX - mTouchDownX
                val diffY = event.rawY - mTouchDownY

                if (mIsDragged || (abs(diffX) > mDragThreshold && abs(diffX) > abs(
                        diffY
                    )) && binding.videoSurfaceFrame.controller.state.zoom == 1f
                ) {
                    if (!mIsDragged) {
                        arrayOf(
                            binding.bottomVideoTimeHolder.videoCurrTime,
                            binding.bottomVideoTimeHolder.videoSeekbar,
                            binding.bottomVideoTimeHolder.videoDuration,
                        ).forEach {
                            it.animate().alpha(1f).start()
                        }
                    }
                    mIgnoreCloseDown = true
                    mIsDragged = true
                    var percent = ((diffX / mScreenWidth) * 100).toInt()
                    percent = 100.coerceAtMost((-100).coerceAtLeast(percent))

                    val skipLength = (mDuration * 1000f) * (percent / 100f)
                    var newProgress = mProgressAtDown + skipLength
                    newProgress =
                        mExoPlayer!!.duration.toFloat().coerceAtMost(newProgress).coerceAtLeast(0f)
                    val newSeconds = (newProgress / 1000).toInt()
                    setPosition(newSeconds)
                    resetPlayWhenReady()
                }
            }

            MotionEvent.ACTION_UP -> {
                val diffX = mTouchDownX - event.rawX
                val diffY = mTouchDownY - event.rawY

                val downGestureDuration = System.currentTimeMillis() - mTouchDownTime
                if (config.allowDownGesture && !mIgnoreCloseDown && abs(diffY) > abs(diffX) && diffY < -mCloseDownThreshold &&
                    downGestureDuration < MAX_CLOSE_DOWN_GESTURE_DURATION &&
                    binding.videoSurfaceFrame.controller.state.zoom == 1f
                ) {
                    supportFinishAfterTransition()
                }

                mIgnoreCloseDown = false
                if (mIsDragged) {
                    if (mIsFullscreen) {
                        arrayOf(
                            binding.bottomVideoTimeHolder.videoCurrTime,
                            binding.bottomVideoTimeHolder.videoSeekbar,
                            binding.bottomVideoTimeHolder.videoDuration,
                        ).forEach {
                            it.animate().alpha(0f).start()
                        }
                    }

                    if (!mIsPlaying) {
                        togglePlayPause()
                    }
                }
                mIsDragged = false
            }
        }
    }

    private fun handleNextFile() {
        Intent().apply {
            putExtra(GO_TO_NEXT_ITEM, true)
            setResult(Activity.RESULT_OK, this)
        }
        finish()
    }

    private fun handlePrevFile() {
        Intent().apply {
            putExtra(GO_TO_PREV_ITEM, true)
            setResult(Activity.RESULT_OK, this)
        }
        finish()
    }

    private fun resetPlayWhenReady() {
        mExoPlayer?.playWhenReady = false
        mPlayWhenReadyHandler.removeCallbacksAndMessages(null)
        mPlayWhenReadyHandler.postDelayed({
            mExoPlayer?.playWhenReady = true
        }, PLAY_WHEN_READY_DRAG_DELAY)
    }

    private fun releaseExoPlayer() {
        mExoPlayer?.apply {
            stop()
            release()
        }
        mExoPlayer = null
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (mExoPlayer != null && fromUser) {
            setPosition(progress)
            resetPlayWhenReady()
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        mIsDragged = true
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        if (mExoPlayer == null)
            return

        if (mIsPlaying) {
            mExoPlayer!!.playWhenReady = true
        } else {
            togglePlayPause()
        }

        mIsDragged = false
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture) = false

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        mExoPlayer?.setVideoSurface(Surface(binding.videoSurface.surfaceTexture))
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

    companion object {
        private const val PLAY_WHEN_READY_DRAG_DELAY = 100L
    }
}
