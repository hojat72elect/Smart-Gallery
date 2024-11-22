package com.simplemobiletools.gallery.pro.new_architecture.feature_media_viewer

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import android.widget.RelativeLayout
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.databinding.ActivityPanoramaPhotoBinding
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.config
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.navigationBarHeight
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.navigationBarWidth
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.onGlobalLayout
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.toast
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.viewBinding
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.PATH
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.isRPlus
import com.simplemobiletools.gallery.pro.new_architecture.shared.BaseActivity

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
}
