package ca.hojat.smart.gallery.feature_media_viewer

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.SeekBar
import androidx.annotation.RequiresApi
import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.databinding.ActivityPanoramaVideoBinding
import ca.hojat.smart.gallery.shared.extensions.config
import ca.hojat.smart.gallery.shared.extensions.getFormattedDuration
import ca.hojat.smart.gallery.shared.extensions.hasNavBar
import ca.hojat.smart.gallery.shared.extensions.navigationBarHeight
import ca.hojat.smart.gallery.shared.extensions.navigationBarWidth
import ca.hojat.smart.gallery.shared.extensions.onGlobalLayout
import ca.hojat.smart.gallery.shared.extensions.showErrorToast
import ca.hojat.smart.gallery.shared.extensions.toast
import ca.hojat.smart.gallery.shared.extensions.viewBinding
import ca.hojat.smart.gallery.shared.helpers.PATH
import ca.hojat.smart.gallery.shared.helpers.isRPlus
import ca.hojat.smart.gallery.shared.activities.BaseActivity

@RequiresApi(Build.VERSION_CODES.O)
open class PanoramaVideoActivity : BaseActivity(), SeekBar.OnSeekBarChangeListener {


    private var mIsFullscreen = false
    private var mIsExploreEnabled = true
    private var mIsRendering = false
    private var mIsPlaying = false
    private var mIsDragged = false
    private var mPlayOnReady = false
    private var mDuration = 0
    private var mCurrTime = 0

    private var mTimerHandler = Handler()
    private val binding by viewBinding(ActivityPanoramaVideoBinding::inflate)

    public override fun onCreate(savedInstanceState: Bundle?) {
        useDynamicTheme = false
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        checkNotchSupport()
        checkIntent()

        if (isRPlus()) {
            window.insetsController?.setSystemBarsAppearance(
                0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        }
    }


    override fun onResume() {
        super.onResume()
        mIsRendering = true
        if (config.blackBackground) {
            updateStatusbarColor(Color.BLACK)
        }

        window.statusBarColor = resources.getColor(R.color.circle_black_background)

        if (config.maxBrightness) {
            val attributes = window.attributes
            attributes.screenBrightness = 1f
            window.attributes = attributes
        }
    }

    override fun onPause() {
        super.onPause()
        mIsRendering = false
    }

    override fun onDestroy() {
        super.onDestroy()

        if (!isChangingConfigurations) {
            mTimerHandler.removeCallbacksAndMessages(null)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setupButtons()
    }

    private fun checkIntent() {
        val path = intent.getStringExtra(PATH)
        if (path == null) {
            toast(R.string.invalid_image_path)
            finish()
            return
        }

        setupButtons()
        intent.removeExtra(PATH)

        binding.bottomVideoTimeHolder.videoCurrTime.setOnClickListener { skip(false) }
        binding.bottomVideoTimeHolder.videoDuration.setOnClickListener { skip(true) }

        try {
            binding.bottomVideoTimeHolder.videoTogglePlayPause.setOnClickListener {
                togglePlayPause()
            }
        } catch (e: Exception) {
            showErrorToast(e)
        }

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            mIsFullscreen = visibility and View.SYSTEM_UI_FLAG_FULLSCREEN != 0
            toggleButtonVisibility()
        }
    }

    private fun togglePlayPause() {
        mIsPlaying = !mIsPlaying
        if (mIsPlaying) {
            resumeVideo()
        } else {
            pauseVideo()
        }
    }

    private fun resumeVideo() {
        binding.bottomVideoTimeHolder.videoTogglePlayPause.setImageResource(R.drawable.ic_pause_outline_vector)
        if (mCurrTime == mDuration) {
            setVideoProgress(0)
            mPlayOnReady = true
            return
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun pauseVideo() {
        binding.bottomVideoTimeHolder.videoTogglePlayPause.setImageResource(R.drawable.ic_play_outline_vector)
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun setVideoProgress(seconds: Int) {
        binding.bottomVideoTimeHolder.videoSeekbar.progress = seconds
        mCurrTime = seconds
        binding.bottomVideoTimeHolder.videoCurrTime.text = seconds.getFormattedDuration()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setupButtons() {
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

        binding.bottomVideoTimeHolder.root.setPadding(0, 0, right, bottom)
        binding.bottomVideoTimeHolder.root.background =
            resources.getDrawable(R.drawable.gradient_background)
        binding.bottomVideoTimeHolder.root.onGlobalLayout {
            val newBottomMargin =
                binding.bottomVideoTimeHolder.root.height - resources.getDimension(R.dimen.video_player_play_pause_size)
                    .toInt() - resources.getDimension(R.dimen.activity_margin)
                    .toInt()
            (binding.explore.layoutParams as RelativeLayout.LayoutParams).bottomMargin =
                newBottomMargin

            (binding.cardboard.layoutParams as RelativeLayout.LayoutParams).apply {
                bottomMargin = newBottomMargin
                rightMargin = navigationBarWidth
            }
            binding.explore.requestLayout()
        }
        binding.bottomVideoTimeHolder.videoTogglePlayPause.setImageResource(R.drawable.ic_play_outline_vector)

        binding.cardboard.setOnClickListener {

        }

        binding.explore.setOnClickListener {
            mIsExploreEnabled = !mIsExploreEnabled
            binding.explore.setImageResource(if (mIsExploreEnabled) R.drawable.ic_explore_vector else R.drawable.ic_explore_off_vector)
        }
    }

    private fun toggleButtonVisibility() {
        val newAlpha = if (mIsFullscreen) 0f else 1f
        arrayOf(binding.cardboard, binding.explore).forEach {
            it.animate().alpha(newAlpha)
        }

        arrayOf(
            binding.cardboard,
            binding.explore,
            binding.bottomVideoTimeHolder.videoTogglePlayPause,
            binding.bottomVideoTimeHolder.videoCurrTime,
            binding.bottomVideoTimeHolder.videoDuration
        ).forEach {
            it.isClickable = !mIsFullscreen
        }

        binding.bottomVideoTimeHolder.videoSeekbar.setOnSeekBarChangeListener(if (mIsFullscreen) null else this)
        binding.bottomVideoTimeHolder.videoTimeHolder.animate().alpha(newAlpha).start()
    }

    private fun skip(forward: Boolean) {
        if (forward && mCurrTime == mDuration) {
            return
        }

        if (!mIsPlaying) {
            togglePlayPause()
        }
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            setVideoProgress(progress)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        mIsDragged = true
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        mIsPlaying = true
        resumeVideo()
        mIsDragged = false
    }
}
