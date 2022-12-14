package ca.on.sudbury.hojat.smartgallery.activities

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore.Images
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.exifinterface.media.ExifInterface
import androidx.print.PrintHelper
import androidx.viewpager.widget.ViewPager
import ca.on.sudbury.hojat.smartgallery.BuildConfig
import ca.on.sudbury.hojat.smartgallery.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import ca.on.sudbury.hojat.smartgallery.helpers.FAVORITES
import ca.on.sudbury.hojat.smartgallery.helpers.NOMEDIA
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_BY_RANDOM
import ca.on.sudbury.hojat.smartgallery.helpers.IS_FROM_GALLERY
import ca.on.sudbury.hojat.smartgallery.helpers.REAL_FILE_PATH
import ca.on.sudbury.hojat.smartgallery.helpers.REQUEST_EDIT_IMAGE
import ca.on.sudbury.hojat.smartgallery.helpers.REQUEST_SET_AS
import ca.on.sudbury.hojat.smartgallery.helpers.PERMISSION_WRITE_STORAGE
import ca.on.sudbury.hojat.smartgallery.models.FileDirItem
import ca.on.sudbury.hojat.smartgallery.adapters.MyPagerAdapter
import ca.on.sudbury.hojat.smartgallery.asynctasks.GetMediaAsynctask
import ca.on.sudbury.hojat.smartgallery.base.SimpleActivity
import ca.on.sudbury.hojat.smartgallery.databases.GalleryDatabase
import ca.on.sudbury.hojat.smartgallery.databinding.ActivityMediumBinding
import ca.on.sudbury.hojat.smartgallery.dialogs.DeleteWithRememberDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.RenameItemDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.ResizeWithPathDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.SaveAsDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.SlideShowDialogFragment
import ca.on.sudbury.hojat.smartgallery.extensions.setAs
import ca.on.sudbury.hojat.smartgallery.extensions.openPath
import ca.on.sudbury.hojat.smartgallery.extensions.openEditor
import ca.on.sudbury.hojat.smartgallery.extensions.showFileOnMap
import ca.on.sudbury.hojat.smartgallery.extensions.updateFavoritePaths
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.extensions.tryCopyMoveFilesTo
import ca.on.sudbury.hojat.smartgallery.extensions.fixDateTaken
import ca.on.sudbury.hojat.smartgallery.extensions.getShortcutImage
import ca.on.sudbury.hojat.smartgallery.extensions.updateFavorite
import ca.on.sudbury.hojat.smartgallery.extensions.movePathsInRecycleBin
import ca.on.sudbury.hojat.smartgallery.extensions.tryDeleteFileDirItem
import ca.on.sudbury.hojat.smartgallery.extensions.mediaDB
import ca.on.sudbury.hojat.smartgallery.extensions.getFavoritePaths
import ca.on.sudbury.hojat.smartgallery.extensions.toggleFileVisibility
import ca.on.sudbury.hojat.smartgallery.extensions.handleMediaManagementPrompt
import ca.on.sudbury.hojat.smartgallery.extensions.isDownloadsFolder
import ca.on.sudbury.hojat.smartgallery.extensions.updateDBMediaPath
import ca.on.sudbury.hojat.smartgallery.photoview.PhotoFragment
import ca.on.sudbury.hojat.smartgallery.video.VideoFragment
import ca.on.sudbury.hojat.smartgallery.fragments.ViewPagerFragment
import ca.on.sudbury.hojat.smartgallery.helpers.PATH
import ca.on.sudbury.hojat.smartgallery.helpers.SHOW_ALL
import ca.on.sudbury.hojat.smartgallery.helpers.SHOW_FAVORITES
import ca.on.sudbury.hojat.smartgallery.helpers.SHOW_RECYCLE_BIN
import ca.on.sudbury.hojat.smartgallery.helpers.RECYCLE_BIN
import ca.on.sudbury.hojat.smartgallery.helpers.MAX_PRINT_SIDE_SIZE
import ca.on.sudbury.hojat.smartgallery.helpers.SLIDESHOW_START_ON_ENTER
import ca.on.sudbury.hojat.smartgallery.helpers.FadePageTransformer
import ca.on.sudbury.hojat.smartgallery.helpers.SHOW_PREV_ITEM
import ca.on.sudbury.hojat.smartgallery.helpers.SHOW_NEXT_ITEM
import ca.on.sudbury.hojat.smartgallery.helpers.GO_TO_NEXT_ITEM
import ca.on.sudbury.hojat.smartgallery.helpers.GO_TO_PREV_ITEM
import ca.on.sudbury.hojat.smartgallery.helpers.IS_VIEW_INTENT
import ca.on.sudbury.hojat.smartgallery.helpers.SKIP_AUTHENTICATION
import ca.on.sudbury.hojat.smartgallery.helpers.PORTRAIT_PATH
import ca.on.sudbury.hojat.smartgallery.helpers.DefaultPageTransformer
import ca.on.sudbury.hojat.smartgallery.helpers.HIDE_SYSTEM_UI_DELAY
import ca.on.sudbury.hojat.smartgallery.helpers.SLIDESHOW_FADE_DURATION
import ca.on.sudbury.hojat.smartgallery.helpers.SLIDESHOW_SLIDE_DURATION
import ca.on.sudbury.hojat.smartgallery.helpers.SLIDESHOW_DEFAULT_INTERVAL
import ca.on.sudbury.hojat.smartgallery.models.Medium
import ca.on.sudbury.hojat.smartgallery.models.ThumbnailItem
import ca.on.sudbury.hojat.smartgallery.extensions.recycleBinPath
import ca.on.sudbury.hojat.smartgallery.extensions.isPortrait
import ca.on.sudbury.hojat.smartgallery.extensions.handleLockedFolderOpening
import ca.on.sudbury.hojat.smartgallery.extensions.getParentPath
import ca.on.sudbury.hojat.smartgallery.extensions.navigationBarHeight
import ca.on.sudbury.hojat.smartgallery.extensions.portrait
import ca.on.sudbury.hojat.smartgallery.extensions.navigationBarRight
import ca.on.sudbury.hojat.smartgallery.extensions.getCompressionFormat
import ca.on.sudbury.hojat.smartgallery.extensions.getFileOutputStream
import ca.on.sudbury.hojat.smartgallery.extensions.getFilenameFromPath
import ca.on.sudbury.hojat.smartgallery.extensions.updateLastModified
import ca.on.sudbury.hojat.smartgallery.extensions.handleDeletePasswordProtection
import ca.on.sudbury.hojat.smartgallery.extensions.isAStorageRootFolder
import ca.on.sudbury.hojat.smartgallery.extensions.isExternalStorageManager
import ca.on.sudbury.hojat.smartgallery.extensions.internalStoragePath
import ca.on.sudbury.hojat.smartgallery.extensions.getDoesFilePathExist
import ca.on.sudbury.hojat.smartgallery.extensions.getIsPathDirectory
import ca.on.sudbury.hojat.smartgallery.extensions.isMediaFile
import ca.on.sudbury.hojat.smartgallery.extensions.getImageResolution
import ca.on.sudbury.hojat.smartgallery.extensions.getUriMimeType
import ca.on.sudbury.hojat.smartgallery.extensions.tryGenericMimeType
import ca.on.sudbury.hojat.smartgallery.extensions.getFinalUriFromPath
import ca.on.sudbury.hojat.smartgallery.extensions.getResolution
import ca.on.sudbury.hojat.smartgallery.extensions.isVideoFast
import ca.on.sudbury.hojat.smartgallery.extensions.isRawFast
import ca.on.sudbury.hojat.smartgallery.extensions.scanPathRecursively
import ca.on.sudbury.hojat.smartgallery.extensions.statusBarHeight
import ca.on.sudbury.hojat.smartgallery.extensions.getColoredDrawableWithColor
import ca.on.sudbury.hojat.smartgallery.extensions.onGlobalLayout
import ca.on.sudbury.hojat.smartgallery.extensions.hasPermission
import ca.on.sudbury.hojat.smartgallery.extensions.getDuration
import ca.on.sudbury.hojat.smartgallery.extensions.getStringValue
import ca.on.sudbury.hojat.smartgallery.extensions.getProperBackgroundColor
import ca.on.sudbury.hojat.smartgallery.extensions.actionBarHeight
import ca.on.sudbury.hojat.smartgallery.extensions.isSDCardSetAsDefaultStorage
import ca.on.sudbury.hojat.smartgallery.extensions.navigationBarSize
import ca.on.sudbury.hojat.smartgallery.extensions.rescanPaths
import ca.on.sudbury.hojat.smartgallery.extensions.restoreRecycleBinPaths
import ca.on.sudbury.hojat.smartgallery.extensions.sharePathIntent
import ca.on.sudbury.hojat.smartgallery.helpers.BottomAction
import ca.on.sudbury.hojat.smartgallery.helpers.MediaType
import ca.on.sudbury.hojat.smartgallery.helpers.RotationRule
import ca.on.sudbury.hojat.smartgallery.helpers.SlideshowAnimation
import ca.on.sudbury.hojat.smartgallery.usecases.IsNougatPlusUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsOreoPlusUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsRPlusUseCase
import ca.on.sudbury.hojat.smartgallery.settings.SettingsActivity
import ca.on.sudbury.hojat.smartgallery.usecases.BeVisibleOrGoneUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.ConvertDrawableToBitmapUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.CopyNonDimensionExifAttributesUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.FormatFileSizeUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.HideKeyboardUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.HideSystemUiUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsGifUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsPathOnOtgUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsPathOnSdUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsSvgUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.RunOnBackgroundThreadUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.SaveRotatedImageUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.ShowSystemUiUseCase
import java.io.File
import java.io.OutputStream
import kotlin.math.min

@Suppress("UNCHECKED_CAST")
class ViewPagerActivity : SimpleActivity(), ViewPager.OnPageChangeListener,
    ViewPagerFragment.FragmentListener {

    private lateinit var binding: ActivityMediumBinding

    private val REQUEST_VIEW_VIDEO = 1

    private var mPath = ""
    private var mDirectory = ""
    private var mIsFullScreen = false
    private var mPos = -1
    private var mShowAll = false
    private var mIsSlideshowActive = false
    private var mPrevHashcode = 0

    private var mSlideshowHandler = Handler()
    private var mSlideshowInterval = SLIDESHOW_DEFAULT_INTERVAL
    private var mSlideshowMoveBackwards = false
    private var mSlideshowMedia = mutableListOf<Medium>()
    private var mAreSlideShowMediaVisible = false

    private var mIsOrientationLocked = false

    private var mMediaFiles = ArrayList<Medium>()
    private var mFavoritePaths = ArrayList<String>()
    private var mIgnoredPaths = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        showTransparentTop = true
        showTransparentNavigation = true

        super.onCreate(savedInstanceState)
        binding = ActivityMediumBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupOptionsMenu()
        refreshMenuItems()

        window.decorView.setBackgroundColor(getProperBackgroundColor())
        binding.topShadow.layoutParams.height = statusBarHeight + actionBarHeight
        checkNotchSupport()
        (MediaActivity.mMedia.clone() as ArrayList<ThumbnailItem>).filterIsInstanceTo(
            mMediaFiles,
            Medium::class.java
        )

        handlePermission(PERMISSION_WRITE_STORAGE) {
            if (it) {
                initViewPager()
            } else {
                Toast.makeText(this, R.string.no_storage_permissions, Toast.LENGTH_LONG).show()
                finish()
            }
        }

        initFavorites()
    }

    override fun onResume() {
        super.onResume()
        if (!hasPermission(PERMISSION_WRITE_STORAGE)) {
            finish()
            return
        }

        if (config.bottomActions) {
            window.navigationBarColor = Color.TRANSPARENT
        } else {
            setTranslucentNavigation()
        }

        initBottomActions()

        if (config.maxBrightness) {
            val attributes = window.attributes
            attributes.screenBrightness = 1f
            window.attributes = attributes
        }

        setupOrientation()
        refreshMenuItems()

        val filename = getCurrentMedium()?.name ?: mPath.getFilenameFromPath()
        binding.mediumViewerToolbar.title = filename
    }

    override fun onPause() {
        super.onPause()
        stopSlideshow()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (intent.extras?.containsKey(IS_VIEW_INTENT) == true) {
            config.temporarilyShowHidden = false
        }

        if (config.isThirdPartyIntent) {
            config.isThirdPartyIntent = false

            if (intent.extras == null || isExternalIntent()) {
                mMediaFiles.clear()
            }
        }
    }

    fun refreshMenuItems() {
        val currentMedium = getCurrentMedium() ?: return
        currentMedium.isFavorite = mFavoritePaths.contains(currentMedium.path)
        val visibleBottomActions = if (config.bottomActions) config.visibleBottomActions else 0

        runOnUiThread {
            val rotationDegrees = getCurrentPhotoFragment()?.mCurrentRotationDegrees ?: 0
            binding.mediumViewerToolbar.menu.apply {
                findItem(R.id.menu_show_on_map).isVisible =
                    visibleBottomActions and BottomAction.ShowOnMap.id == 0
                findItem(R.id.menu_slideshow).isVisible =
                    visibleBottomActions and BottomAction.SlideShow.id == 0
                findItem(R.id.menu_properties).isVisible =
                    visibleBottomActions and BottomAction.Properties.id == 0
                findItem(R.id.menu_delete).isVisible =
                    visibleBottomActions and BottomAction.Delete.id == 0
                findItem(R.id.menu_share).isVisible =
                    visibleBottomActions and BottomAction.Share.id == 0
                findItem(R.id.menu_edit).isVisible =
                    visibleBottomActions and BottomAction.Edit.id == 0 && !currentMedium.isSVG()
                findItem(R.id.menu_rename).isVisible =
                    visibleBottomActions and BottomAction.Rename.id == 0 && !currentMedium.getIsInRecycleBin()
                findItem(R.id.menu_rotate).isVisible =
                    currentMedium.isImage() && visibleBottomActions and BottomAction.Rotate.id == 0
                findItem(R.id.menu_set_as).isVisible =
                    visibleBottomActions and BottomAction.SetAs.id == 0
                findItem(R.id.menu_copy_to).isVisible =
                    visibleBottomActions and BottomAction.Copy.id == 0
                findItem(R.id.menu_move_to).isVisible =
                    visibleBottomActions and BottomAction.Move.id == 0
                findItem(R.id.menu_save_as).isVisible = rotationDegrees != 0
                findItem(R.id.menu_print).isVisible =
                    currentMedium.isImage() || currentMedium.isRaw()
                findItem(R.id.menu_resize).isVisible =
                    visibleBottomActions and BottomAction.Resize.id == 0 && currentMedium.isImage()
                findItem(R.id.menu_hide).isVisible =
                    (!IsRPlusUseCase() || isExternalStorageManager()) && !currentMedium.isHidden() && visibleBottomActions and BottomAction.ToggleVisibility.id == 0 && !currentMedium.getIsInRecycleBin()

                findItem(R.id.menu_unhide).isVisible =
                    (!IsRPlusUseCase() || isExternalStorageManager()) && currentMedium.isHidden() && visibleBottomActions and BottomAction.ToggleVisibility.id == 0 && !currentMedium.getIsInRecycleBin()

                findItem(R.id.menu_add_to_favorites).isVisible =
                    !currentMedium.isFavorite && visibleBottomActions and BottomAction.ToggleFavorite.id == 0 && !currentMedium.getIsInRecycleBin()

                findItem(R.id.menu_remove_from_favorites).isVisible =
                    currentMedium.isFavorite && visibleBottomActions and BottomAction.ToggleFavorite.id == 0 && !currentMedium.getIsInRecycleBin()

                findItem(R.id.menu_restore_file).isVisible =
                    currentMedium.path.startsWith(recycleBinPath)
                findItem(R.id.menu_create_shortcut).isVisible = IsOreoPlusUseCase()
                findItem(R.id.menu_change_orientation).isVisible =
                    rotationDegrees == 0 && visibleBottomActions and BottomAction.ChangeOrientation.id == 0
                findItem(R.id.menu_change_orientation).icon =
                    resources.getDrawable(getChangeOrientationIcon())
                findItem(R.id.menu_rotate).setShowAsAction(
                    if (rotationDegrees != 0) {
                        MenuItem.SHOW_AS_ACTION_ALWAYS
                    } else {
                        MenuItem.SHOW_AS_ACTION_IF_ROOM
                    }
                )
            }

            if (visibleBottomActions != 0) {
                updateBottomActionIcons(currentMedium)
            }
        }
    }

    private fun setupOptionsMenu() {
        (binding.mediumViewerAppbar.layoutParams as RelativeLayout.LayoutParams).topMargin =
            statusBarHeight
        binding.mediumViewerToolbar.apply {
            setTitleTextColor(Color.WHITE)
            overflowIcon =
                resources.getColoredDrawableWithColor(R.drawable.ic_three_dots_vector, Color.WHITE)
            navigationIcon =
                resources.getColoredDrawableWithColor(R.drawable.ic_arrow_left_vector, Color.WHITE)
        }

        updateMenuItemColors(binding.mediumViewerToolbar.menu, forceWhiteIcons = true)
        binding.mediumViewerToolbar.setOnMenuItemClickListener { menuItem ->
            if (getCurrentMedium() == null) {
                return@setOnMenuItemClickListener true
            }

            when (menuItem.itemId) {
                R.id.menu_set_as -> setAs(getCurrentPath())
                R.id.menu_slideshow -> initSlideshow()
                R.id.menu_copy_to -> checkMediaManagementAndCopy(true)
                R.id.menu_move_to -> moveFileTo()
                R.id.menu_open_with -> openPath(getCurrentPath(), true)
                R.id.menu_hide -> toggleFileVisibility(true)
                R.id.menu_unhide -> toggleFileVisibility(false)
                R.id.menu_share -> sharePathIntent(getCurrentPath(), BuildConfig.APPLICATION_ID)
                R.id.menu_delete -> checkDeleteConfirmation()
                R.id.menu_rename -> checkMediaManagementAndRename()
                R.id.menu_print -> printFile()
                R.id.menu_edit -> openEditor(getCurrentPath())
                R.id.menu_properties -> showProperties()
                R.id.menu_show_on_map -> showFileOnMap(getCurrentPath())
                R.id.menu_rotate_right -> rotateImage(90)
                R.id.menu_rotate_left -> rotateImage(-90)
                R.id.menu_rotate_one_eighty -> rotateImage(180)
                R.id.menu_add_to_favorites -> toggleFavorite()
                R.id.menu_remove_from_favorites -> toggleFavorite()
                R.id.menu_restore_file -> restoreFile()
                R.id.menu_force_portrait -> toggleOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                R.id.menu_force_landscape -> toggleOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                R.id.menu_default_orientation -> toggleOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                R.id.menu_save_as -> saveImageAs()
                R.id.menu_create_shortcut -> createShortcut()
                R.id.menu_resize -> resizeImage()
                R.id.menu_settings -> {
                    HideKeyboardUseCase(this)
                    startActivity(Intent(applicationContext, SettingsActivity::class.java))
                }
                else -> return@setOnMenuItemClickListener false
            }
            return@setOnMenuItemClickListener true
        }

        binding.mediumViewerToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (requestCode == REQUEST_EDIT_IMAGE && resultCode == RESULT_OK && resultData != null) {
            mPos = -1
            mPrevHashcode = 0
            refreshViewPager()
        } else if (requestCode == REQUEST_SET_AS && resultCode == RESULT_OK) {
            Toast.makeText(this, R.string.wallpaper_set_successfully, Toast.LENGTH_LONG).show()
        } else if (requestCode == REQUEST_VIEW_VIDEO && resultCode == RESULT_OK && resultData != null) {
            if (resultData.getBooleanExtra(GO_TO_NEXT_ITEM, false)) {
                goToNextItem()
            } else if (resultData.getBooleanExtra(GO_TO_PREV_ITEM, false)) {
                goToPrevItem()
            }
        }
        super.onActivityResult(requestCode, resultCode, resultData)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        initBottomActionsLayout()
        (binding.mediumViewerAppbar.layoutParams as RelativeLayout.LayoutParams).topMargin =
            statusBarHeight
    }

    private fun initViewPager() {
        val uri = intent.data
        if (uri != null) {
            var cursor: Cursor? = null
            try {
                val proj = arrayOf(Images.Media.DATA)
                cursor = contentResolver.query(uri, proj, null, null, null)
                if (cursor?.moveToFirst() == true) {
                    mPath = cursor.getStringValue(Images.Media.DATA)
                }
            } finally {
                cursor?.close()
            }
        } else {
            try {
                mPath = intent.getStringExtra(PATH) ?: ""
                mShowAll = config.showAll
            } catch (e: Exception) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                finish()
                return
            }
        }

        if (intent.extras?.containsKey(REAL_FILE_PATH) == true) {
            mPath = intent.extras!!.getString(REAL_FILE_PATH)!!
        }

        if (mPath.isEmpty()) {
            Toast.makeText(this, R.string.unknown_error_occurred, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        if (mPath.isPortrait() && getPortraitPath() == "") {
            val newIntent = Intent(this, ViewPagerActivity::class.java)
            newIntent.putExtras(intent!!.extras!!)
            newIntent.putExtra(PORTRAIT_PATH, mPath)
            newIntent.putExtra(
                PATH,
                "${mPath.getParentPath().getParentPath()}/${mPath.getFilenameFromPath()}"
            )

            startActivity(newIntent)
            finish()
            return
        }

        if (!getDoesFilePathExist(mPath) && getPortraitPath() == "") {
            finish()
            return
        }

        ShowSystemUiUseCase(this)

        if (intent.getBooleanExtra(SKIP_AUTHENTICATION, false)) {
            initContinue()
        } else {
            handleLockedFolderOpening(mPath.getParentPath()) { success ->
                if (success) {
                    initContinue()
                } else {
                    finish()
                }
            }
        }
    }

    private fun initContinue() {
        if (intent.extras?.containsKey(IS_VIEW_INTENT) == true) {
            if (isShowHiddenFlagNeeded()) {
                if (!config.isHiddenPasswordProtectionOn) {
                    config.temporarilyShowHidden = true
                }
            }

            config.isThirdPartyIntent = true
        }

        val isShowingFavorites = intent.getBooleanExtra(SHOW_FAVORITES, false)
        val isShowingRecycleBin = intent.getBooleanExtra(SHOW_RECYCLE_BIN, false)
        mDirectory = when {
            isShowingFavorites -> FAVORITES
            isShowingRecycleBin -> RECYCLE_BIN
            else -> mPath.getParentPath()
        }
        binding.mediumViewerToolbar.title = mPath.getFilenameFromPath()

        binding.viewPager.onGlobalLayout {
            if (!isDestroyed) {
                if (mMediaFiles.isNotEmpty()) {
                    gotMedia(
                        mMediaFiles as ArrayList<ThumbnailItem>,
                        refetchViewPagerPosition = true
                    )
                    checkSlideshowOnEnter()
                }
            }
        }

        // show the selected image asap, while loading the rest in the background to allow swiping between them. Might be needed at third party intents
        if (mMediaFiles.isEmpty() && mPath.isNotEmpty() && mDirectory != FAVORITES) {
            val filename = mPath.getFilenameFromPath()
            val folder = mPath.getParentPath()
            val type = getTypeFromPath(mPath)
            val medium = Medium(null, filename, mPath, folder, 0, 0, 0, type, 0, false, 0L, 0L)
            mMediaFiles.add(medium)
            gotMedia(mMediaFiles as ArrayList<ThumbnailItem>, refetchViewPagerPosition = true)
        }

        refreshViewPager(true)
        binding.viewPager.offscreenPageLimit = 2

        if (config.blackBackground) {
            binding.viewPager.background = ColorDrawable(Color.BLACK)
        }

        if (config.hideSystemUI) {
            binding.viewPager.onGlobalLayout {
                Handler().postDelayed({
                    fragmentClicked()
                }, HIDE_SYSTEM_UI_DELAY)
            }
        }

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            mIsFullScreen = if (IsNougatPlusUseCase() && isInMultiWindowMode) {
                visibility and View.SYSTEM_UI_FLAG_LOW_PROFILE != 0
            } else if (visibility and View.SYSTEM_UI_FLAG_LOW_PROFILE == 0) {
                false
            } else {
                visibility and View.SYSTEM_UI_FLAG_FULLSCREEN != 0
            }

            checkSystemUI()
            fullscreenToggled()
        }

        if (intent.action == "com.android.camera.action.REVIEW") {
            RunOnBackgroundThreadUseCase {
                if (mediaDB.getMediaFromPath(mPath).isEmpty()) {
                    val filename = mPath.getFilenameFromPath()
                    val parent = mPath.getParentPath()
                    val type = getTypeFromPath(mPath)
                    val isFavorite = GalleryDatabase.getInstance(applicationContext).FavoritesDao()
                        .isFavorite(mPath)
                    val duration = if (type == MediaType.Video.id) getDuration(mPath) ?: 0 else 0
                    val ts = System.currentTimeMillis()
                    val medium = Medium(
                        null,
                        filename,
                        mPath,
                        parent,
                        ts,
                        ts,
                        File(mPath).length(),
                        type,
                        duration,
                        isFavorite,
                        0,
                        0L
                    )
                    mediaDB.insert(medium)
                }
            }
        }
    }

    private fun getTypeFromPath(path: String): Int {
        return when {
            path.isVideoFast() -> MediaType.Video.id
            IsGifUseCase(path) -> MediaType.Gif.id
            IsSvgUseCase(path) -> MediaType.Svg.id
            path.isRawFast() -> MediaType.Raw.id
            path.isPortrait() -> MediaType.Portrait.id
            else -> MediaType.Image.id
        }
    }

    private fun initBottomActions() {
        initBottomActionButtons()
        initBottomActionsLayout()
    }

    private fun initFavorites() {
        RunOnBackgroundThreadUseCase {
            mFavoritePaths = getFavoritePaths()
        }
    }

    private fun setupOrientation() {
        if (!mIsOrientationLocked) {
            if (config.screenRotation == RotationRule.DeviceRotation.id) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
            } else if (config.screenRotation == RotationRule.SystemSetting.id) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    private fun updatePagerItems(media: MutableList<Medium>) {
        val pagerAdapter = MyPagerAdapter(this, supportFragmentManager, media)
        if (!isDestroyed) {
            pagerAdapter.shouldInitFragment = mPos < 5
            binding.viewPager.apply {
                // must remove the listener before changing adapter, otherwise it might cause `mPos` to be set to 0
                removeOnPageChangeListener(this@ViewPagerActivity)
                adapter = pagerAdapter
                pagerAdapter.shouldInitFragment = true
                addOnPageChangeListener(this@ViewPagerActivity)
                currentItem = mPos
            }
        }
    }

    private fun checkSlideshowOnEnter() {
        if (intent.getBooleanExtra(SLIDESHOW_START_ON_ENTER, false)) {
            initSlideshow()
        }
    }

    private fun initSlideshow() {
        SlideShowDialogFragment(callbackAfterDialogConfirmed = { startSlideshow() }).show(
            supportFragmentManager,
            SlideShowDialogFragment.TAG
        )
    }

    private fun startSlideshow() {
        if (getMediaForSlideshow()) {
            binding.viewPager.onGlobalLayout {
                if (!isDestroyed) {
                    if (config.slideshowAnimation == SlideshowAnimation.Fade.id) {
                        binding.viewPager.setPageTransformer(false, FadePageTransformer())
                    }

                    HideSystemUiUseCase(this)
                    mSlideshowInterval = config.slideshowInterval
                    mSlideshowMoveBackwards = config.slideshowMoveBackwards
                    mIsSlideshowActive = true
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    scheduleSwipe()
                }
            }
        }
    }

    private fun goToNextMedium(forward: Boolean) {
        val oldPosition = binding.viewPager.currentItem
        val newPosition = if (forward) oldPosition + 1 else oldPosition - 1
        if (newPosition == -1 || newPosition > binding.viewPager.adapter!!.count - 1) {
            slideshowEnded(forward)
        } else {
            binding.viewPager.setCurrentItem(newPosition, false)
        }
    }

    private fun animatePagerTransition(forward: Boolean) {
        val oldPosition = binding.viewPager.currentItem
        val animator = ValueAnimator.ofInt(0, binding.viewPager.width)
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator) {
            }

            override fun onAnimationEnd(animation: Animator) {
                if (binding.viewPager.isFakeDragging) {
                    try {
                        binding.viewPager.endFakeDrag()
                    } catch (ignored: Exception) {
                        stopSlideshow()
                    }

                    if (binding.viewPager.currentItem == oldPosition) {
                        slideshowEnded(forward)
                    }
                }
            }

            override fun onAnimationCancel(animation: Animator) {
                binding.viewPager.endFakeDrag()
            }

            override fun onAnimationStart(animation: Animator) {
            }
        })

        if (config.slideshowAnimation == SlideshowAnimation.Slide.id) {
            animator.interpolator = DecelerateInterpolator()
            animator.duration = SLIDESHOW_SLIDE_DURATION
        } else {
            animator.duration = SLIDESHOW_FADE_DURATION
        }

        animator.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            var oldDragPosition = 0
            override fun onAnimationUpdate(animation: ValueAnimator) {
                if (binding.viewPager.isFakeDragging) {
                    val dragPosition = animation.animatedValue as Int
                    val dragOffset = dragPosition - oldDragPosition
                    oldDragPosition = dragPosition
                    try {
                        binding.viewPager.fakeDragBy(dragOffset * (if (forward) -1f else 1f))
                    } catch (e: Exception) {
                        stopSlideshow()
                    }
                }
            }
        })

        binding.viewPager.beginFakeDrag()
        animator.start()
    }

    private fun slideshowEnded(forward: Boolean) {
        if (config.loopSlideshow) {
            if (forward) {
                binding.viewPager.setCurrentItem(0, false)
            } else {
                binding.viewPager.setCurrentItem(binding.viewPager.adapter!!.count - 1, false)
            }
        } else {
            stopSlideshow()
            Toast.makeText(this, R.string.slideshow_ended, Toast.LENGTH_LONG).show()
        }
    }

    private fun stopSlideshow() {
        if (mIsSlideshowActive) {
            binding.viewPager.setPageTransformer(false, DefaultPageTransformer())
            mIsSlideshowActive = false
            ShowSystemUiUseCase(this)
            mSlideshowHandler.removeCallbacksAndMessages(null)
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            mAreSlideShowMediaVisible = false
        }
    }

    private fun scheduleSwipe() {
        mSlideshowHandler.removeCallbacksAndMessages(null)
        if (mIsSlideshowActive) {
            if (getCurrentMedium()!!.isImage() || getCurrentMedium()!!.isGIF() || getCurrentMedium()!!.isPortrait()) {
                mSlideshowHandler.postDelayed({
                    if (mIsSlideshowActive && !isDestroyed) {
                        swipeToNextMedium()
                    }
                }, mSlideshowInterval * 1000L)
            } else {
                (getCurrentFragment() as? VideoFragment)!!.playVideo()
            }
        }
    }

    private fun swipeToNextMedium() {
        if (config.slideshowAnimation == SlideshowAnimation.None.id) {
            goToNextMedium(!mSlideshowMoveBackwards)
        } else {
            animatePagerTransition(!mSlideshowMoveBackwards)
        }
    }

    private fun getMediaForSlideshow(): Boolean {
        mSlideshowMedia = mMediaFiles.filter {
            it.isImage() || it.isPortrait() || (config.slideshowIncludeVideos && it.isVideo() || (config.slideshowIncludeGIFs && it.isGIF()))
        }.toMutableList()

        if (config.slideshowRandomOrder) {
            mSlideshowMedia.shuffle()
            mPos = 0
        } else {
            mPath = getCurrentPath()
            mPos = getPositionInList(mSlideshowMedia)
        }

        return if (mSlideshowMedia.isEmpty()) {
            Toast.makeText(this, R.string.no_media_for_slideshow, Toast.LENGTH_LONG).show()
            false
        } else {
            updatePagerItems(mSlideshowMedia)
            mAreSlideShowMediaVisible = true
            true
        }
    }

    private fun moveFileTo() {
        handleDeletePasswordProtection {
            checkMediaManagementAndCopy(false)
        }
    }

    private fun checkMediaManagementAndCopy(isCopyOperation: Boolean) {
        handleMediaManagementPrompt {
            copyMoveTo(isCopyOperation)
        }
    }

    private fun copyMoveTo(isCopyOperation: Boolean) {
        val currPath = getCurrentPath()
        if (!isCopyOperation && currPath.startsWith(recycleBinPath)) {
            Toast.makeText(
                this,
                R.string.moving_recycle_bin_items_disabled,
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val fileDirItems = arrayListOf(FileDirItem(currPath, currPath.getFilenameFromPath()))
        tryCopyMoveFilesTo(fileDirItems, isCopyOperation) {
            val newPath = "$it/${currPath.getFilenameFromPath()}"
            applicationContext.rescanPaths(arrayListOf(newPath)) {
                fixDateTaken(arrayListOf(newPath), false)
            }

            config.tempFolderPath = ""
            if (!isCopyOperation) {
                refreshViewPager()
                updateFavoritePaths(fileDirItems, it)
            }
        }
    }

    private fun toggleFileVisibility(hide: Boolean, callback: (() -> Unit)? = null) {
        toggleFileVisibility(getCurrentPath(), hide) {
            val newFileName = it.getFilenameFromPath()
            binding.mediumViewerToolbar.title = newFileName

            getCurrentMedium()!!.apply {
                name = newFileName
                path = it
                getCurrentMedia()[mPos] = this
            }

            refreshMenuItems()
            callback?.invoke()
        }
    }

    private fun rotateImage(degrees: Int) {
        val currentPath = getCurrentPath()
        if (!IsRPlusUseCase() && (
                    IsPathOnSdUseCase(this, currentPath) ||
                            IsPathOnOtgUseCase(this, currentPath)) && !isSDCardSetAsDefaultStorage()
        ) {
            handleSAFDialog(currentPath) {
                if (it) {
                    rotateBy(degrees)
                }
            }
        } else {
            rotateBy(degrees)
        }
    }

    private fun rotateBy(degrees: Int) {
        getCurrentPhotoFragment()?.rotateImageViewBy(degrees)
        refreshMenuItems()
    }

    private fun toggleOrientation(orientation: Int) {
        requestedOrientation = orientation
        mIsOrientationLocked = orientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        refreshMenuItems()
    }

    private fun getChangeOrientationIcon(): Int {
        return if (mIsOrientationLocked) {
            if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                R.drawable.ic_orientation_portrait_vector
            } else {
                R.drawable.ic_orientation_landscape_vector
            }
        } else {
            R.drawable.ic_orientation_auto_vector
        }
    }

    private fun saveImageAs() {
        val currPath = getCurrentPath()
        val callback: (savePath: String) -> Unit = {
            val newPath = it
            handleSAFDialog(it) {
                if (!it) {
                    return@handleSAFDialog
                }

                Toast.makeText(this, R.string.saving, Toast.LENGTH_LONG).show()
                RunOnBackgroundThreadUseCase {
                    val photoFragment =
                        getCurrentPhotoFragment() ?: return@RunOnBackgroundThreadUseCase
                    SaveRotatedImageUseCase(
                        this,
                        currPath,
                        newPath,
                        photoFragment.mCurrentRotationDegrees,
                        true
                    ) {
                        Toast.makeText(this, R.string.file_saved, Toast.LENGTH_LONG).show()
                        getCurrentPhotoFragment()?.mCurrentRotationDegrees = 0
                        refreshMenuItems()
                    }
                }
            }
        }
        SaveAsDialogFragment(
            path = currPath,
            appendFilename = false,
            callback = callback
        ).show(supportFragmentManager, SaveAsDialogFragment.TAG)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun createShortcut() {
        if (!IsOreoPlusUseCase()) {
            return
        }

        val manager = getSystemService(ShortcutManager::class.java)
        if (manager.isRequestPinShortcutSupported) {
            val medium = getCurrentMedium() ?: return
            val path = medium.path
            val drawable = resources.getDrawable(R.drawable.shortcut_image).mutate()
            getShortcutImage(path, drawable) {
                val intent = Intent(this, ViewPagerActivity::class.java).apply {
                    putExtra(PATH, path)
                    putExtra(SHOW_ALL, config.showAll)
                    putExtra(SHOW_FAVORITES, path == FAVORITES)
                    putExtra(SHOW_RECYCLE_BIN, path == RECYCLE_BIN)
                    action = Intent.ACTION_VIEW
                    flags =
                        flags or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }

                val shortcut = ShortcutInfo.Builder(this, path)
                    .setShortLabel(medium.name)
                    .setIcon(Icon.createWithBitmap(ConvertDrawableToBitmapUseCase(drawable)))
                    .setIntent(intent)
                    .build()

                manager.requestPinShortcut(shortcut, null)
            }
        }
    }

    private fun getCurrentPhotoFragment() = getCurrentFragment() as? PhotoFragment

    private fun getPortraitPath() = intent.getStringExtra(PORTRAIT_PATH) ?: ""

    private fun isShowHiddenFlagNeeded(): Boolean {
        val file = File(mPath)
        if (file.isHidden) {
            return true
        }

        var parent = file.parentFile ?: return false
        while (true) {
            if (parent.isHidden || parent.list()?.any { it.startsWith(NOMEDIA) } == true) {
                return true
            }

            if (parent.absolutePath == "/") {
                break
            }
            parent = parent.parentFile ?: return false
        }

        return false
    }

    private fun getCurrentFragment() =
        (binding.viewPager.adapter as? MyPagerAdapter)?.getCurrentFragment(binding.viewPager.currentItem)

    private fun showProperties() {
        if (getCurrentMedium() != null) {
            Toast.makeText(
                this,
                "I had to remove this feature.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun initBottomActionsLayout() {
        binding.bottomActions.root.layoutParams.height =
            resources.getDimension(R.dimen.bottom_actions_height).toInt() + navigationBarHeight
        if (config.bottomActions) {
            binding.bottomActions.root.visibility = View.VISIBLE
        } else {
            binding.bottomActions.root.visibility = View.GONE
        }

        if (!portrait && navigationBarRight && (if (navigationBarRight) navigationBarSize.x else 0) > 0) {
            binding.mediumViewerToolbar.setPadding(
                0,
                0,
                if (navigationBarRight) navigationBarSize.x else 0,
                0
            )
        } else {
            binding.mediumViewerToolbar.setPadding(0, 0, 0, 0)
        }
    }

    private fun initBottomActionButtons() {
        val currentMedium = getCurrentMedium()
        val visibleBottomActions = if (config.bottomActions) config.visibleBottomActions else 0
        with(binding.bottomActions.bottomFavorite) {
            BeVisibleOrGoneUseCase(
                this,
                visibleBottomActions and BottomAction.ToggleFavorite.id != 0 && currentMedium?.getIsInRecycleBin() == false
            )
            setOnLongClickListener {
                Toast.makeText(
                    this@ViewPagerActivity,
                    R.string.toggle_favorite, Toast.LENGTH_LONG
                ).show(); true
            }
            setOnClickListener {
                toggleFavorite()
            }
        }

        with(binding.bottomActions.bottomEdit) {
            BeVisibleOrGoneUseCase(
                this,
                visibleBottomActions and BottomAction.Edit.id != 0 && currentMedium?.isSVG() == false
            )
            setOnLongClickListener {
                Toast.makeText(
                    this@ViewPagerActivity,
                    R.string.edit, Toast.LENGTH_LONG
                ).show(); true
            }
            setOnClickListener { openEditor(getCurrentPath()) }
        }

        with(binding.bottomActions.bottomShare) {
            BeVisibleOrGoneUseCase(this, visibleBottomActions and BottomAction.Share.id != 0)
            setOnLongClickListener {
                Toast.makeText(
                    this@ViewPagerActivity,
                    R.string.share, Toast.LENGTH_LONG
                ).show(); true
            }
            setOnClickListener {
                sharePathIntent(getCurrentPath(), BuildConfig.APPLICATION_ID)
            }
        }

        with(binding.bottomActions.bottomDelete) {
            BeVisibleOrGoneUseCase(this, visibleBottomActions and BottomAction.Delete.id != 0)
            setOnLongClickListener {
                Toast.makeText(
                    this@ViewPagerActivity,
                    R.string.delete, Toast.LENGTH_LONG
                ).show(); true
            }
            setOnClickListener { checkDeleteConfirmation() }
        }

        with(binding.bottomActions.bottomRotate) {
            BeVisibleOrGoneUseCase(
                this,
                config.visibleBottomActions and BottomAction.Rotate.id != 0 && getCurrentMedium()?.isImage() == true
            )
            setOnLongClickListener {
                Toast.makeText(
                    this@ViewPagerActivity,
                    R.string.rotate, Toast.LENGTH_LONG
                ).show(); true
            }
            setOnClickListener { rotateImage(90) }
        }

        with(binding.bottomActions.bottomProperties) {
            BeVisibleOrGoneUseCase(this, visibleBottomActions and BottomAction.Properties.id != 0)
            setOnLongClickListener {
                Toast.makeText(
                    this@ViewPagerActivity,
                    R.string.properties, Toast.LENGTH_LONG
                ).show(); true
            }
            setOnClickListener { showProperties() }
        }

        with(binding.bottomActions.bottomChangeOrientation) {
            BeVisibleOrGoneUseCase(
                this,
                visibleBottomActions and BottomAction.ChangeOrientation.id != 0
            )
            setOnLongClickListener {
                Toast.makeText(
                    this@ViewPagerActivity,
                    R.string.change_orientation, Toast.LENGTH_LONG
                ).show(); true
            }
            setOnClickListener {
                requestedOrientation = when (requestedOrientation) {
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    else -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
                mIsOrientationLocked =
                    requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                updateBottomActionIcons(currentMedium)
            }
        }

        with(binding.bottomActions.bottomSlideshow) {
            BeVisibleOrGoneUseCase(this, visibleBottomActions and BottomAction.SlideShow.id != 0)
            setOnLongClickListener {
                Toast.makeText(
                    this@ViewPagerActivity,
                    R.string.slideshow, Toast.LENGTH_LONG
                ).show(); true
            }
            setOnClickListener { initSlideshow() }
        }

        with(binding.bottomActions.bottomShowOnMap) {
            BeVisibleOrGoneUseCase(this, visibleBottomActions and BottomAction.ShowOnMap.id != 0)
            setOnLongClickListener {
                Toast.makeText(
                    this@ViewPagerActivity,
                    R.string.show_on_map, Toast.LENGTH_LONG
                ).show(); true
            }
            setOnClickListener { showFileOnMap(getCurrentPath()) }
        }

        with(binding.bottomActions.bottomToggleFileVisibility) {
            BeVisibleOrGoneUseCase(
                this,
                visibleBottomActions and BottomAction.ToggleVisibility.id != 0
            )
            setOnLongClickListener {
                Toast.makeText(
                    this@ViewPagerActivity,
                    if (currentMedium?.isHidden() == true) R.string.unhide else R.string.hide,
                    Toast.LENGTH_LONG
                ).show(); true
            }
            setOnClickListener {
                currentMedium?.apply {
                    toggleFileVisibility(!isHidden()) {
                        updateBottomActionIcons(currentMedium)
                    }
                }
            }
        }

        with(binding.bottomActions.bottomRename) {
            BeVisibleOrGoneUseCase(
                this,
                visibleBottomActions and BottomAction.Rename.id != 0 && currentMedium?.getIsInRecycleBin() == false
            )
            setOnLongClickListener {
                Toast.makeText(
                    this@ViewPagerActivity,
                    R.string.rename, Toast.LENGTH_LONG
                ).show(); true
            }
            setOnClickListener { checkMediaManagementAndRename() }
        }

        with(binding.bottomActions.bottomSetAs) {
            BeVisibleOrGoneUseCase(this, visibleBottomActions and BottomAction.SetAs.id != 0)
            setOnLongClickListener {
                Toast.makeText(
                    this@ViewPagerActivity,
                    R.string.set_as, Toast.LENGTH_LONG
                ).show(); true
            }
            setOnClickListener { setAs(getCurrentPath()) }
        }

        with(binding.bottomActions.bottomCopy) {
            BeVisibleOrGoneUseCase(this, visibleBottomActions and BottomAction.Copy.id != 0)
            setOnLongClickListener {
                Toast.makeText(
                    this@ViewPagerActivity,
                    R.string.copy, Toast.LENGTH_LONG
                ).show(); true
            }
            setOnClickListener { checkMediaManagementAndCopy(true) }
        }

        with(binding.bottomActions.bottomMove) {
            BeVisibleOrGoneUseCase(this, visibleBottomActions and BottomAction.Move.id != 0)
            setOnLongClickListener {
                Toast.makeText(
                    this@ViewPagerActivity,
                    R.string.move, Toast.LENGTH_LONG
                ).show(); true
            }
            setOnClickListener { moveFileTo() }
        }

        with(binding.bottomActions.bottomResize) {
            BeVisibleOrGoneUseCase(
                this,
                visibleBottomActions and BottomAction.Resize.id != 0 && currentMedium?.isImage() == true
            )
            setOnLongClickListener {
                Toast.makeText(
                    this@ViewPagerActivity,
                    R.string.resize, Toast.LENGTH_LONG
                ).show(); true
            }
            setOnClickListener { resizeImage() }
        }
    }

    private fun updateBottomActionIcons(medium: Medium?) {
        if (medium == null) {
            return
        }

        val favoriteIcon =
            if (medium.isFavorite) R.drawable.ic_star_vector else R.drawable.ic_star_outline_vector

        with(binding.bottomActions) {
            bottomFavorite.setImageResource(favoriteIcon)
            val hideIcon =
                if (medium.isHidden()) R.drawable.ic_unhide_vector else R.drawable.ic_hide_vector
            bottomToggleFileVisibility.setImageResource(hideIcon)
            BeVisibleOrGoneUseCase(
                bottomRotate,
                config.visibleBottomActions and BottomAction.Rotate.id != 0 && getCurrentMedium()?.isImage() == true
            )
            bottomChangeOrientation.setImageResource(getChangeOrientationIcon())
        }
    }

    private fun toggleFavorite() {
        val medium = getCurrentMedium() ?: return
        medium.isFavorite = !medium.isFavorite
        RunOnBackgroundThreadUseCase {
            updateFavorite(medium.path, medium.isFavorite)
            if (medium.isFavorite) {
                mFavoritePaths.add(medium.path)
            } else {
                mFavoritePaths.remove(medium.path)
            }
            runOnUiThread {
                refreshMenuItems()
            }
        }
    }

    private fun printFile() {
        sendPrintIntent(getCurrentPath())
    }

    private fun sendPrintIntent(path: String) {
        val printHelper = PrintHelper(this)
        printHelper.scaleMode = PrintHelper.SCALE_MODE_FIT
        printHelper.orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        try {
            val resolution = path.getImageResolution(this)
            if (resolution == null) {
                Toast.makeText(this, R.string.unknown_error_occurred, Toast.LENGTH_LONG).show()
                return
            }

            var requestedWidth = resolution.x
            var requestedHeight = resolution.y

            if (requestedWidth >= MAX_PRINT_SIDE_SIZE) {
                requestedHeight =
                    (requestedHeight / (requestedWidth / MAX_PRINT_SIDE_SIZE.toFloat())).toInt()
                requestedWidth = MAX_PRINT_SIDE_SIZE
            } else if (requestedHeight >= MAX_PRINT_SIDE_SIZE) {
                requestedWidth =
                    (requestedWidth / (requestedHeight / MAX_PRINT_SIDE_SIZE.toFloat())).toInt()
                requestedHeight = MAX_PRINT_SIDE_SIZE
            }

            val options = RequestOptions()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)

            Glide.with(this)
                .asBitmap()
                .load(path)
                .apply(options)
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Toast.makeText(this@ViewPagerActivity, e.toString(), Toast.LENGTH_LONG)
                            .show()
                        return false
                    }

                    override fun onResourceReady(
                        bitmap: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        if (bitmap != null) {
                            printHelper.printBitmap(path.getFilenameFromPath(), bitmap)
                        }

                        return false
                    }
                }).submit(requestedWidth, requestedHeight)
        } catch (_: Exception) {
        }
    }

    private fun restoreFile() {
        restoreRecycleBinPaths(arrayListOf(getCurrentPath())) {
            refreshViewPager()
        }
    }

    @SuppressLint("Recycle")
    @TargetApi(Build.VERSION_CODES.N)
    private fun resizeImage() {
        val oldPath = getCurrentPath()
        val originalSize = oldPath.getImageResolution(this) ?: return
        val callback: (newSize: Point, newPath: String) -> Unit = { newSize, newPath ->
            RunOnBackgroundThreadUseCase {
                try {
                    var oldExif: ExifInterface? = null
                    if (IsNougatPlusUseCase()) {
                        val inputStream =
                            contentResolver.openInputStream(Uri.fromFile(File(oldPath)))
                        oldExif = ExifInterface(inputStream!!)
                    }

                    val newBitmap = Glide.with(applicationContext).asBitmap().load(oldPath)
                        .submit(newSize.x, newSize.y).get()

                    val newFile = File(newPath)
                    val newFileDirItem = FileDirItem(newPath, newPath.getFilenameFromPath())
                    getFileOutputStream(newFileDirItem, true) {
                        if (it != null) {
                            saveBitmap(
                                newFile,
                                newBitmap,
                                it,
                                oldExif,
                                File(oldPath).lastModified()
                            )
                        } else {
                            Toast.makeText(this, R.string.image_editing_failed, Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                } catch (e: OutOfMemoryError) {
                    Toast.makeText(this, R.string.out_of_memory_error, Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }
        ResizeWithPathDialogFragment(originalSize, oldPath, callback).show(
            supportFragmentManager,
            ResizeWithPathDialogFragment.TAG
        )
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun saveBitmap(
        file: File,
        bitmap: Bitmap,
        out: OutputStream,
        oldExif: ExifInterface?,
        lastModified: Long
    ) {
        try {
            bitmap.compress(file.absolutePath.getCompressionFormat(), 90, out)

            if (IsNougatPlusUseCase()) {
                val newExif = ExifInterface(file.absolutePath)

                if (oldExif != null) {
                    CopyNonDimensionExifAttributesUseCase(oldExif, newExif)
                }
                if (oldExif != null) {
                    CopyNonDimensionExifAttributesUseCase(oldExif, newExif)
                }
            }
        } catch (_: Exception) {
        }
        Toast.makeText(this, R.string.file_saved, Toast.LENGTH_LONG).show()
        val paths = arrayListOf(file.absolutePath)
        applicationContext.rescanPaths(paths) {
            fixDateTaken(paths, false)

            if (config.keepLastModified && lastModified != 0L) {
                File(file.absolutePath).setLastModified(lastModified)
                updateLastModified(file.absolutePath, lastModified)
            }
        }
        out.close()
    }

    private fun checkDeleteConfirmation() {
        if (getCurrentMedium() == null) {
            return
        }

        handleMediaManagementPrompt {
            if (config.isDeletePasswordProtectionOn) {
                handleDeletePasswordProtection {
                    deleteConfirmed()
                }
            } else if (config.tempSkipDeleteConfirmation || config.skipDeleteConfirmation) {
                deleteConfirmed()
            } else {
                askConfirmDelete()
            }
        }
    }

    private fun askConfirmDelete() {
        val fileDirItem = getCurrentMedium()?.toFileDirItem() ?: return
        val size = FormatFileSizeUseCase(fileDirItem.getProperSize(this, countHidden = true))
        val filename = "\"${getCurrentPath().getFilenameFromPath()}\""
        val filenameAndSize = "$filename ($size)"

        val baseString = if (config.useRecycleBin && !getCurrentMedium()!!.getIsInRecycleBin()) {
            R.string.move_to_recycle_bin_confirmation
        } else {
            R.string.deletion_confirmation
        }

        val message = String.format(resources.getString(baseString), filenameAndSize)

        val callback: (Boolean) -> Unit = { remember ->
            config.tempSkipDeleteConfirmation = remember
            deleteConfirmed()
        }
        DeleteWithRememberDialogFragment(message, callback).show(
            supportFragmentManager,
            DeleteWithRememberDialogFragment.TAG
        )
    }

    private fun deleteConfirmed() {
        val currentMedium = getCurrentMedium()
        val path = currentMedium?.path ?: return
        if (getIsPathDirectory(path) || !path.isMediaFile()) {
            return
        }

        val fileDirItem = currentMedium.toFileDirItem()
        if (config.useRecycleBin && !getCurrentMedium()!!.getIsInRecycleBin()) {
            checkManageMediaOrHandleSAFDialogSdk30(fileDirItem.path) { it ->
                if (!it) {
                    return@checkManageMediaOrHandleSAFDialogSdk30
                }

                mIgnoredPaths.add(fileDirItem.path)
                val media =
                    mMediaFiles.filter { !mIgnoredPaths.contains(it.path) } as ArrayList<Medium>
                if (media.isNotEmpty()) {
                    runOnUiThread {
                        refreshUI(media, false)
                    }
                }

                if (media.size == 1) {
                    onPageSelected(0)
                }

                movePathsInRecycleBin(arrayListOf(path)) {
                    if (it) {
                        tryDeleteFileDirItem(
                            fileDirItem,
                            allowDeleteFolder = false,
                            deleteFromDatabase = false
                        ) {
                            mIgnoredPaths.remove(fileDirItem.path)
                            if (media.isEmpty()) {
                                deleteDirectoryIfEmpty()
                                finish()
                            }
                        }
                    } else {
                        Toast.makeText(this, R.string.unknown_error_occurred, Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
        } else {
            handleDeletion(fileDirItem)
        }
    }

    private fun handleDeletion(fileDirItem: FileDirItem) {
        checkManageMediaOrHandleSAFDialogSdk30(fileDirItem.path) {
            if (!it) {
                return@checkManageMediaOrHandleSAFDialogSdk30
            }

            mIgnoredPaths.add(fileDirItem.path)
            val media = mMediaFiles.filter { !mIgnoredPaths.contains(it.path) } as ArrayList<Medium>
            if (media.isNotEmpty()) {
                runOnUiThread {
                    refreshUI(media, false)
                }
            }

            if (media.size == 1) {
                onPageSelected(0)
            }

            tryDeleteFileDirItem(
                fileDirItem,
                allowDeleteFolder = false,
                deleteFromDatabase = true
            ) {
                mIgnoredPaths.remove(fileDirItem.path)
                if (media.isEmpty()) {
                    deleteDirectoryIfEmpty()
                    finish()
                }
            }
        }
    }

    private fun isDirEmpty(media: ArrayList<Medium>): Boolean {
        return if (media.isEmpty()) {
            deleteDirectoryIfEmpty()
            finish()
            true
        } else {
            false
        }
    }

    private fun checkMediaManagementAndRename() {
        handleMediaManagementPrompt {
            renameFile()
        }
    }

    private fun renameFile() {
        val oldPath = getCurrentPath()

        val isSDOrOtgRootFolder =
            isAStorageRootFolder(oldPath.getParentPath()) && !oldPath.startsWith(internalStoragePath)
        if (IsRPlusUseCase() && isSDOrOtgRootFolder && !isExternalStorageManager()) {
            Toast.makeText(
                this,
                R.string.rename_in_sd_card_system_restriction,
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val callback: (String) -> Unit = { newPath ->
            getCurrentMedia().getOrNull(mPos)?.apply {
                path = newPath
                name = newPath.getFilenameFromPath()
            }
            RunOnBackgroundThreadUseCase {
                updateDBMediaPath(oldPath, newPath)
            }
            updateActionbarTitle()
        }
        RenameItemDialogFragment(oldPath, callback).show(
            supportFragmentManager,
            RenameItemDialogFragment.TAG
        )

    }

    private fun refreshViewPager(refetchPosition: Boolean = false) {
        val isRandomSorting = config.getFolderSorting(mDirectory) and SORT_BY_RANDOM != 0
        if (!isRandomSorting || isExternalIntent()) {
            GetMediaAsynctask(
                applicationContext,
                mDirectory,
                isPickImage = false,
                isPickVideo = false,
                showAll = mShowAll
            ) {
                gotMedia(it, refetchViewPagerPosition = refetchPosition)
            }.execute()
        }
    }

    private fun gotMedia(
        thumbnailItems: ArrayList<ThumbnailItem>,
        ignorePlayingVideos: Boolean = false,
        refetchViewPagerPosition: Boolean = false
    ) {
        val media = thumbnailItems.asSequence().filter {
            it is Medium && !mIgnoredPaths.contains(it.path)
        }.map { it as Medium }.toMutableList() as ArrayList<Medium>

        if (isDirEmpty(media) || media.hashCode() == mPrevHashcode) {
            return
        }

        val isPlaying = (getCurrentFragment() as? VideoFragment)?.mIsPlaying == true
        if (!ignorePlayingVideos && isPlaying && !isExternalIntent()) {
            return
        }

        refreshUI(media, refetchViewPagerPosition)
    }

    private fun refreshUI(media: ArrayList<Medium>, refetchViewPagerPosition: Boolean) {
        mPrevHashcode = media.hashCode()
        mMediaFiles = media

        if (refetchViewPagerPosition || mPos == -1) {
            mPos = getPositionInList(media)
            if (mPos == -1) {
                min(mPos, media.size - 1)
            }
        }

        updateActionbarTitle()
        updatePagerItems(mMediaFiles.toMutableList())

        refreshMenuItems()
        checkOrientation()
        initBottomActions()
    }

    private fun getPositionInList(items: MutableList<Medium>): Int {
        mPos = 0
        for ((i, medium) in items.withIndex()) {
            val portraitPath = getPortraitPath()
            if (portraitPath != "") {
                val portraitPaths = File(portraitPath).parentFile?.list()
                if (portraitPaths != null) {
                    for (path in portraitPaths) {
                        if (medium.name == path) {
                            return i
                        }
                    }
                }
            } else if (medium.path.equals(mPath, true)) {
                return i
            }
        }
        return mPos
    }

    private fun deleteDirectoryIfEmpty() {
        if (config.deleteEmptyFolders) {
            val fileDirItem = FileDirItem(
                mDirectory,
                mDirectory.getFilenameFromPath(),
                File(mDirectory).isDirectory
            )
            if (!fileDirItem.path.isDownloadsFolder() && fileDirItem.isDirectory) {
                RunOnBackgroundThreadUseCase {
                    if (fileDirItem.getProperFileCount(this, true) == 0) {
                        tryDeleteFileDirItem(
                            fileDirItem,
                            allowDeleteFolder = true,
                            deleteFromDatabase = true
                        )
                        scanPathRecursively(mDirectory)
                    }
                }
            }
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun checkOrientation() {
        if (!mIsOrientationLocked && config.screenRotation == RotationRule.AspectRatio.id) {
            var flipSides = false
            try {
                val pathToLoad = getCurrentPath()
                val exif = ExifInterface(pathToLoad)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1)
                flipSides =
                    orientation == ExifInterface.ORIENTATION_ROTATE_90 || orientation == ExifInterface.ORIENTATION_ROTATE_270
            } catch (_: Exception) {
            }
            val resolution = applicationContext.getResolution(getCurrentPath()) ?: return
            val width = if (flipSides) resolution.y else resolution.x
            val height = if (flipSides) resolution.x else resolution.y
            if (width > height) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else if (width < height) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
    }

    override fun fragmentClicked() {
        mIsFullScreen = !mIsFullScreen
        checkSystemUI()
        fullscreenToggled()
    }

    override fun videoEnded(): Boolean {
        if (mIsSlideshowActive) {
            swipeToNextMedium()
        }
        return mIsSlideshowActive
    }

    override fun isSlideShowActive() = mIsSlideshowActive

    override fun goToPrevItem() {
        binding.viewPager.setCurrentItem(binding.viewPager.currentItem - 1, false)
        checkOrientation()
    }

    override fun goToNextItem() {
        binding.viewPager.setCurrentItem(binding.viewPager.currentItem + 1, false)
        checkOrientation()
    }

    override fun launchViewVideoIntent(path: String) {
        HideKeyboardUseCase(this)
        RunOnBackgroundThreadUseCase {
            val newUri = getFinalUriFromPath(path, BuildConfig.APPLICATION_ID)
                ?: return@RunOnBackgroundThreadUseCase
            val mimeType = getUriMimeType(path, newUri)
            Intent().apply {
                action = Intent.ACTION_VIEW
                setDataAndType(newUri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(IS_FROM_GALLERY, true)
                putExtra(REAL_FILE_PATH, path)
                putExtra(SHOW_PREV_ITEM, binding.viewPager.currentItem != 0)
                putExtra(SHOW_NEXT_ITEM, binding.viewPager.currentItem != mMediaFiles.size - 1)

                try {
                    startActivityForResult(this, REQUEST_VIEW_VIDEO)
                } catch (e: ActivityNotFoundException) {
                    if (!tryGenericMimeType(this, mimeType, newUri)) {
                        Toast.makeText(
                            this@ViewPagerActivity,
                            R.string.no_app_found,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@ViewPagerActivity, e.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun checkSystemUI() {
        if (mIsFullScreen) {
            HideSystemUiUseCase(this)
        } else {
            stopSlideshow()
            ShowSystemUiUseCase(this)
        }
    }

    private fun fullscreenToggled() {
        binding.viewPager.adapter?.let {
            (it as MyPagerAdapter).toggleFullscreen(mIsFullScreen)
            val newAlpha = if (mIsFullScreen) 0f else 1f
            binding.topShadow.animate().alpha(newAlpha).start()
            binding.bottomActions.root.animate().alpha(newAlpha).withStartAction {
                binding.bottomActions.root.visibility = View.VISIBLE
            }.withEndAction {
                BeVisibleOrGoneUseCase(binding.bottomActions.root, newAlpha == 1f)
            }.start()

            binding.mediumViewerAppbar.animate().alpha(newAlpha).withStartAction {
                binding.mediumViewerAppbar.visibility = View.VISIBLE
            }.withEndAction {
                BeVisibleOrGoneUseCase(binding.mediumViewerAppbar, newAlpha == 1f)
            }.start()
        }
    }

    private fun updateActionbarTitle() {
        runOnUiThread {
            if (mPos < getCurrentMedia().size) {
                binding.mediumViewerToolbar.title =
                    getCurrentMedia()[mPos].path.getFilenameFromPath()
            }
        }
    }

    private fun getCurrentMedium(): Medium? {
        return if (getCurrentMedia().isEmpty() || mPos == -1) {
            null
        } else {
            getCurrentMedia()[min(mPos, getCurrentMedia().size - 1)]
        }
    }

    private fun getCurrentMedia() = if (mAreSlideShowMediaVisible) mSlideshowMedia else mMediaFiles

    private fun getCurrentPath() = getCurrentMedium()?.path ?: ""

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {
        if (mPos != position) {
            mPos = position
            updateActionbarTitle()
            refreshMenuItems()
            scheduleSwipe()
        }
    }

    override fun onPageScrollStateChanged(state: Int) {
        if (state == ViewPager.SCROLL_STATE_IDLE && getCurrentMedium() != null) {
            checkOrientation()
        }
    }

    private fun isExternalIntent(): Boolean {
        return !intent.getBooleanExtra(IS_FROM_GALLERY, false)
    }
}
