package ca.on.sudbury.hojat.smartgallery.wallpaper

import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import ca.on.hojat.renderer.cropper.CropImageView
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.extensions.checkAppSideloading
import ca.on.sudbury.hojat.smartgallery.helpers.NavigationIcon
import ca.on.sudbury.hojat.smartgallery.models.RadioItem
import ca.on.sudbury.hojat.smartgallery.activities.MainActivity
import ca.on.sudbury.hojat.smartgallery.base.SimpleActivity
import ca.on.sudbury.hojat.smartgallery.databinding.ActivitySetWallpaperBinding
import ca.on.sudbury.hojat.smartgallery.dialogs.RadioGroupDialogFragment
import ca.on.sudbury.hojat.smartgallery.usecases.IsNougatPlusUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.RunOnBackgroundThreadUseCase

private const val RATIO_PORTRAIT = 0
private const val RATIO_LANDSCAPE = 1
private const val RATIO_SQUARE = 2
private const val PICK_IMAGE = 1


class SetWallpaperActivity : SimpleActivity(), CropImageView.OnCropImageCompleteListener {

    private lateinit var binding: ActivitySetWallpaperBinding

    private var aspectRatio = RATIO_PORTRAIT
    private var wallpaperFlag = -1
    lateinit var uri: Uri
    private lateinit var wallpaperManager: WallpaperManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetWallpaperBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBottomActions()

        if (checkAppSideloading()) {
            return
        }

        setupOptionsMenu()
        if (intent.data == null) {
            val pickIntent = Intent(applicationContext, MainActivity::class.java)
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
            if (resultCode == RESULT_OK && resultData != null) {
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
            Toast.makeText(this, R.string.unknown_file_location, Toast.LENGTH_LONG).show()
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
        if (IsNougatPlusUseCase()) {
            val items = arrayListOf(
                RadioItem(WallpaperManager.FLAG_SYSTEM, getString(R.string.home_screen)),
                RadioItem(WallpaperManager.FLAG_LOCK, getString(R.string.lock_screen)),
                RadioItem(
                    WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK,
                    getString(R.string.home_and_lock_screen)
                )
            )
            val callback: (Any) -> Unit = { newValue ->
                wallpaperFlag = newValue as Int
                binding.cropImageView.getCroppedImageAsync()
            }
            RadioGroupDialogFragment(
                items = items,
                callback = callback
            ).show(supportFragmentManager, RadioGroupDialogFragment.TAG)
        } else {
            binding.cropImageView.getCroppedImageAsync()
        }
    }

    override fun onCropImageComplete(view: CropImageView?, result: CropImageView.CropResult) {
        if (isDestroyed)
            return

        if (result.error == null) {
            Toast.makeText(this, R.string.setting_wallpaper, Toast.LENGTH_LONG).show()
            RunOnBackgroundThreadUseCase {
                val bitmap = result.bitmap
                val wantedHeight = wallpaperManager.desiredMinimumHeight
                val ratio = wantedHeight / bitmap.height.toFloat()
                val wantedWidth = (bitmap.width * ratio).toInt()
                try {
                    val scaledBitmap =
                        Bitmap.createScaledBitmap(bitmap, wantedWidth, wantedHeight, true)
                    if (IsNougatPlusUseCase()) {
                        wallpaperManager.setBitmap(scaledBitmap, null, true, wallpaperFlag)
                    } else {
                        wallpaperManager.setBitmap(scaledBitmap)
                    }
                    setResult(RESULT_OK)
                } catch (e: OutOfMemoryError) {
                    Toast.makeText(this, R.string.out_of_memory_error, Toast.LENGTH_LONG).show()
                    setResult(RESULT_CANCELED)
                }
                finish()
            }
        } else {
            Toast.makeText(
                this,
                "${getString(R.string.image_editing_failed)}: ${result.error.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
