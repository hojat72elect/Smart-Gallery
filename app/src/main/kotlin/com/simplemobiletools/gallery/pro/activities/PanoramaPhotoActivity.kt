package com.simplemobiletools.gallery.pro.activities

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import android.widget.RelativeLayout
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.databinding.ActivityPanoramaPhotoBinding
import com.simplemobiletools.gallery.pro.extensions.config
import com.simplemobiletools.gallery.pro.extensions.hideSystemUI
import com.simplemobiletools.gallery.pro.extensions.navigationBarHeight
import com.simplemobiletools.gallery.pro.extensions.navigationBarWidth
import com.simplemobiletools.gallery.pro.extensions.onGlobalLayout
import com.simplemobiletools.gallery.pro.extensions.showSystemUI
import com.simplemobiletools.gallery.pro.extensions.toast
import com.simplemobiletools.gallery.pro.extensions.viewBinding
import com.simplemobiletools.gallery.pro.helpers.PATH
import com.simplemobiletools.gallery.pro.helpers.isRPlus
import com.simplemobiletools.gallery.pro.new_architecture.BaseActivity

open class PanoramaPhotoActivity : BaseActivity() {


    private var isFullscreen = false
    private var isExploreEnabled = true
    private var isRendering = false

    private val binding by viewBinding(ActivityPanoramaPhotoBinding::inflate)

    public override fun onCreate(savedInstanceState: Bundle?) {
        useDynamicTheme = false
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        checkNotchSupport()
        setupButtonMargins()

        binding.cardboard.setOnClickListener {
            toast("This feature is not implemented yet.")
        }

        binding.explore.setOnClickListener {
            isExploreEnabled = !isExploreEnabled
            binding.explore.setImageResource(if (isExploreEnabled) R.drawable.ic_explore_vector else R.drawable.ic_explore_off_vector)
        }

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
        isRendering = true
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
        isRendering = false
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setupButtonMargins()
    }

    private fun checkIntent() {
        val path = intent.getStringExtra(PATH)
        if (path == null) {
            toast(R.string.invalid_image_path)
            finish()
            return
        }

        intent.removeExtra(PATH)

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            isFullscreen = visibility and View.SYSTEM_UI_FLAG_FULLSCREEN != 0
            toggleButtonVisibility()
        }
    }

    private fun getBitmapToLoad(path: String): Bitmap? {
        val options = BitmapFactory.Options()
        options.inSampleSize = 1
        var bitmap: Bitmap? = null

        for (i in 0..10) {
            try {
                bitmap = if (path.startsWith("content://")) {
                    val inputStream = contentResolver.openInputStream(Uri.parse(path))
                    BitmapFactory.decodeStream(inputStream)
                } else {
                    BitmapFactory.decodeFile(path, options)
                }
                break
            } catch (e: OutOfMemoryError) {
                options.inSampleSize *= 2
            }
        }

        return bitmap
    }

    private fun setupButtonMargins() {
        val navBarHeight = navigationBarHeight
        (binding.cardboard.layoutParams as RelativeLayout.LayoutParams).apply {
            bottomMargin = navBarHeight
            rightMargin = navigationBarWidth
        }

        (binding.explore.layoutParams as RelativeLayout.LayoutParams).bottomMargin =
            navigationBarHeight

        binding.cardboard.onGlobalLayout {
            binding.panoramaGradientBackground.layoutParams.height =
                navBarHeight + binding.cardboard.height
        }
    }

    private fun toggleButtonVisibility() {
        arrayOf(binding.cardboard, binding.explore, binding.panoramaGradientBackground).forEach {
            it.animate().alpha(if (isFullscreen) 0f else 1f)
            it.isClickable = !isFullscreen
        }
    }

    private fun handleClick() {
        isFullscreen = !isFullscreen
        toggleButtonVisibility()
        if (isFullscreen) {
            hideSystemUI()
        } else {
            showSystemUI()
        }
    }

    companion object{
        private const val CARDBOARD_DISPLAY_MODE = 3
    }
}
