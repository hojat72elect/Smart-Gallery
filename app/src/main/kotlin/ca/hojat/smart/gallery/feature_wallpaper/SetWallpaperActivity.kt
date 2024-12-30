package ca.hojat.smart.gallery.feature_wallpaper

import android.app.Activity
import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.databinding.ActivitySetWallpaperBinding
import ca.hojat.smart.gallery.feature_home.HomeActivity
import ca.hojat.smart.gallery.shared.activities.BaseActivity
import ca.hojat.smart.gallery.shared.data.domain.RadioItem
import ca.hojat.smart.gallery.shared.extensions.checkAppSideloading
import ca.hojat.smart.gallery.shared.extensions.viewBinding
import ca.hojat.smart.gallery.shared.helpers.NavigationIcon
import ca.hojat.smart.gallery.shared.helpers.ensureBackgroundThread
import ca.hojat.smart.gallery.shared.ui.dialogs.RadioGroupDialog
import ca.hojat.smart.gallery.shared.usecases.ShowToastUseCase
import com.canhub.cropper.CropImageView

@OptIn(UnstableApi::class)
class SetWallpaperActivity : BaseActivity(), CropImageView.OnCropImageCompleteListener {

    private var aspectRatio = RATIO_PORTRAIT
    private var wallpaperFlag = -1

    lateinit var uri: Uri
    private lateinit var wallpaperManager: WallpaperManager

    private val binding by viewBinding(ActivitySetWallpaperBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupBottomActions()

        if (checkAppSideloading()) {
            return
        }

        setupOptionsMenu()
        if (intent.data == null) {
            val pickIntent = Intent(applicationContext, HomeActivity::class.java)
            pickIntent.action = Intent.ACTION_PICK
            pickIntent.type = "image/*"
            startActivityForResult(pickIntent, PICK_IMAGE)
            return
        }

        handleImage(intent)
    }


    override fun onResume() {
        super.onResume()
        setupToolbar(binding.setWallpaperToolbar, NavigationIcon.Arrow)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (requestCode == PICK_IMAGE) {
            if (resultCode == Activity.RESULT_OK && resultData != null) {
                handleImage(resultData)
            } else {
                finish()
            }
        }
        super.onActivityResult(requestCode, resultCode, resultData)
    }

    private fun setupOptionsMenu() {
        binding.setWallpaperToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.save -> confirmWallpaper()
                R.id.allow_changing_aspect_ratio -> binding.cropImageView.clearAspectRatio()
                else -> return@setOnMenuItemClickListener false
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun handleImage(intent: Intent) {
        uri = intent.data!!
        if (uri.scheme != "file" && uri.scheme != "content") {
            ShowToastUseCase(this, R.string.unknown_file_location)
            finish()
            return
        }

        wallpaperManager = WallpaperManager.getInstance(applicationContext)
        binding.cropImageView.apply {
            setOnCropImageCompleteListener(this@SetWallpaperActivity)
            setImageUriAsync(uri)
        }

        setupAspectRatio()
    }

    private fun setupBottomActions() {
        binding.bottomSetWallpaperActions.bottomSetWallpaperAspectRatio.setOnClickListener {
            changeAspectRatio()
        }

        binding.bottomSetWallpaperActions.bottomSetWallpaperRotate.setOnClickListener {
            binding.cropImageView.rotateImage(90)
        }
    }

    private fun setupAspectRatio() {
        var widthToUse = wallpaperManager.desiredMinimumWidth
        val heightToUse = wallpaperManager.desiredMinimumHeight
        if (widthToUse == heightToUse) {
            widthToUse /= 2
        }

        when (aspectRatio) {
            RATIO_PORTRAIT -> binding.cropImageView.setAspectRatio(heightToUse, widthToUse)
            RATIO_LANDSCAPE -> binding.cropImageView.setAspectRatio(widthToUse, heightToUse)
            else -> binding.cropImageView.setAspectRatio(widthToUse, widthToUse)
        }
    }

    private fun changeAspectRatio() {
        aspectRatio = ++aspectRatio % (RATIO_SQUARE + 1)
        setupAspectRatio()
    }

    private fun confirmWallpaper() {
        val items = arrayListOf(
            RadioItem(WallpaperManager.FLAG_SYSTEM, getString(R.string.home_screen)),
            RadioItem(WallpaperManager.FLAG_LOCK, getString(R.string.lock_screen)),
            RadioItem(
                WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK,
                getString(R.string.home_and_lock_screen)
            )
        )

        RadioGroupDialog(this, items) {
            wallpaperFlag = it as Int
            binding.cropImageView.croppedImageAsync()
        }
    }

    override fun onCropImageComplete(view: CropImageView, result: CropImageView.CropResult) {
        if (isDestroyed)
            return

        if (result.error == null && result.bitmap != null) {
            ShowToastUseCase(this, R.string.setting_wallpaper)
            ensureBackgroundThread {
                val bitmap = result.bitmap!!
                val wantedHeight = wallpaperManager.desiredMinimumHeight
                val ratio = wantedHeight / bitmap.height.toFloat()
                val wantedWidth = (bitmap.width * ratio).toInt()
                try {
                    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, wantedWidth, wantedHeight, true)
                    wallpaperManager.setBitmap(scaledBitmap, null, true, wallpaperFlag)
                    setResult(RESULT_OK)
                } catch (e: OutOfMemoryError) {
                    ShowToastUseCase(this, R.string.out_of_memory_error)
                    setResult(RESULT_CANCELED)
                }
                finish()
            }
        } else {
            ShowToastUseCase(this, "${getString(R.string.image_editing_failed)}: ${result.error?.message}")
        }
    }

    companion object {
        private const val RATIO_PORTRAIT = 0
        private const val RATIO_LANDSCAPE = 1
        private const val RATIO_SQUARE = 2
        private const val PICK_IMAGE = 1
    }
}
