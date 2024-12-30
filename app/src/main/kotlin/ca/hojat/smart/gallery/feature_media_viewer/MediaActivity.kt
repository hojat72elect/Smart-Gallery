package ca.hojat.smart.gallery.feature_media_viewer

import android.app.Activity
import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.databinding.ActivityMediaBinding
import ca.hojat.smart.gallery.feature_home.HomeActivity
import ca.hojat.smart.gallery.shared.activities.BaseActivity
import ca.hojat.smart.gallery.shared.data.db.GalleryDatabase
import ca.hojat.smart.gallery.shared.data.domain.FileDirItem
import ca.hojat.smart.gallery.shared.data.domain.Medium
import ca.hojat.smart.gallery.shared.data.domain.RadioItem
import ca.hojat.smart.gallery.shared.data.domain.ThumbnailItem
import ca.hojat.smart.gallery.shared.data.domain.ThumbnailSection
import ca.hojat.smart.gallery.shared.data.repository.GetMediaAsyncTask
import ca.hojat.smart.gallery.shared.extensions.areSystemAnimationsEnabled
import ca.hojat.smart.gallery.shared.extensions.beGone
import ca.hojat.smart.gallery.shared.extensions.beVisible
import ca.hojat.smart.gallery.shared.extensions.beVisibleIf
import ca.hojat.smart.gallery.shared.extensions.config
import ca.hojat.smart.gallery.shared.extensions.deleteDBPath
import ca.hojat.smart.gallery.shared.extensions.directoryDB
import ca.hojat.smart.gallery.shared.extensions.favoritesDB
import ca.hojat.smart.gallery.shared.extensions.getCachedMedia
import ca.hojat.smart.gallery.shared.extensions.getDoesFilePathExist
import ca.hojat.smart.gallery.shared.extensions.getFilenameFromPath
import ca.hojat.smart.gallery.shared.extensions.getHumanizedFilename
import ca.hojat.smart.gallery.shared.extensions.getIsPathDirectory
import ca.hojat.smart.gallery.shared.extensions.getLatestMediaByDateId
import ca.hojat.smart.gallery.shared.extensions.getLatestMediaId
import ca.hojat.smart.gallery.shared.extensions.getProperBackgroundColor
import ca.hojat.smart.gallery.shared.extensions.getProperPrimaryColor
import ca.hojat.smart.gallery.shared.extensions.getProperTextColor
import ca.hojat.smart.gallery.shared.extensions.getTimeFormat
import ca.hojat.smart.gallery.shared.extensions.hideKeyboard
import ca.hojat.smart.gallery.shared.extensions.isDownloadsFolder
import ca.hojat.smart.gallery.shared.extensions.isExternalStorageManager
import ca.hojat.smart.gallery.shared.extensions.isGone
import ca.hojat.smart.gallery.shared.extensions.isMediaFile
import ca.hojat.smart.gallery.shared.extensions.isVideoFast
import ca.hojat.smart.gallery.shared.extensions.isVisible
import ca.hojat.smart.gallery.shared.extensions.launchCamera
import ca.hojat.smart.gallery.shared.extensions.mediaDB
import ca.hojat.smart.gallery.shared.extensions.openPath
import ca.hojat.smart.gallery.shared.extensions.openRecycleBin
import ca.hojat.smart.gallery.shared.extensions.recycleBinPath
import ca.hojat.smart.gallery.shared.extensions.updateWidgets
import ca.hojat.smart.gallery.shared.extensions.viewBinding
import ca.hojat.smart.gallery.shared.helpers.DIRECTORY
import ca.hojat.smart.gallery.shared.helpers.FAVORITES
import ca.hojat.smart.gallery.shared.helpers.GET_ANY_INTENT
import ca.hojat.smart.gallery.shared.helpers.GET_IMAGE_INTENT
import ca.hojat.smart.gallery.shared.helpers.GET_VIDEO_INTENT
import ca.hojat.smart.gallery.shared.helpers.GridSpacingItemDecoration
import ca.hojat.smart.gallery.shared.helpers.IS_FROM_GALLERY
import ca.hojat.smart.gallery.shared.helpers.IS_IN_RECYCLE_BIN
import ca.hojat.smart.gallery.shared.helpers.MAX_COLUMN_COUNT
import ca.hojat.smart.gallery.shared.helpers.MediaFetcher
import ca.hojat.smart.gallery.shared.helpers.PATH
import ca.hojat.smart.gallery.shared.helpers.PERMISSION_READ_MEDIA_IMAGES
import ca.hojat.smart.gallery.shared.helpers.PICKED_PATHS
import ca.hojat.smart.gallery.shared.helpers.RECYCLE_BIN
import ca.hojat.smart.gallery.shared.helpers.REQUEST_EDIT_IMAGE
import ca.hojat.smart.gallery.shared.helpers.SET_WALLPAPER_INTENT
import ca.hojat.smart.gallery.shared.helpers.SHOW_ALL
import ca.hojat.smart.gallery.shared.helpers.SHOW_FAVORITES
import ca.hojat.smart.gallery.shared.helpers.SHOW_RECYCLE_BIN
import ca.hojat.smart.gallery.shared.helpers.SHOW_TEMP_HIDDEN_DURATION
import ca.hojat.smart.gallery.shared.helpers.SKIP_AUTHENTICATION
import ca.hojat.smart.gallery.shared.helpers.SLIDESHOW_START_ON_ENTER
import ca.hojat.smart.gallery.shared.helpers.SORT_BY_RANDOM
import ca.hojat.smart.gallery.shared.helpers.VIEW_TYPE_GRID
import ca.hojat.smart.gallery.shared.helpers.VIEW_TYPE_LIST
import ca.hojat.smart.gallery.shared.helpers.ensureBackgroundThread
import ca.hojat.smart.gallery.shared.ui.adapters.MediaAdapter
import ca.hojat.smart.gallery.shared.ui.adapters.MediaOperationsListener
import ca.hojat.smart.gallery.shared.ui.dialogs.ChangeGroupingDialog
import ca.hojat.smart.gallery.shared.ui.dialogs.ChangeSortingDialog
import ca.hojat.smart.gallery.shared.ui.dialogs.ChangeViewTypeDialog
import ca.hojat.smart.gallery.shared.ui.dialogs.CreateNewFolderDialog
import ca.hojat.smart.gallery.shared.ui.dialogs.FilterMediaDialog
import ca.hojat.smart.gallery.shared.ui.dialogs.GrantAllFilesDialog
import ca.hojat.smart.gallery.shared.ui.dialogs.RadioGroupDialog
import ca.hojat.smart.gallery.shared.ui.views.MyGridLayoutManager
import ca.hojat.smart.gallery.shared.ui.views.MyRecyclerView
import ca.hojat.smart.gallery.shared.usecases.ShowToastUseCase
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import java.io.File
import java.io.IOException

@OptIn(UnstableApi::class)
class MediaActivity : BaseActivity(), MediaOperationsListener {

    private var mPath = ""
    private var mIsGetImageIntent = false
    private var mIsGetVideoIntent = false
    private var mIsGetAnyIntent = false
    private var mIsGettingMedia = false
    private var mAllowPickingMultiple = false
    private var mShowAll = false
    private var mLoadedInitialPhotos = false
    private var mWasFullscreenViewOpen = false
    private var mLastSearchedText = ""
    private var mLatestMediaId = 0L
    private var mLatestMediaDateId = 0L
    private var mLastMediaHandler = Handler()
    private var mTempShowHiddenHandler = Handler()
    private var mCurrAsyncTask: GetMediaAsyncTask? = null
    private var mZoomListener: MyRecyclerView.MyZoomListener? = null

    private var mStoredAnimateGifs = true
    private var mStoredCropThumbnails = true
    private var mStoredScrollHorizontally = true
    private var mStoredShowFileTypes = true
    private var mStoredRoundedCorners = false
    private var mStoredMarkFavoriteItems = true
    private var mStoredTextColor = 0
    private var mStoredPrimaryColor = 0
    private var mStoredThumbnailSpacing = 0

    private val binding by viewBinding(ActivityMediaBinding::inflate)

    companion object {
        var mMedia = ArrayList<ThumbnailItem>()
        private const val LAST_MEDIA_CHECK_PERIOD = 3000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        intent.apply {
            mIsGetImageIntent = getBooleanExtra(GET_IMAGE_INTENT, false)
            mIsGetVideoIntent = getBooleanExtra(GET_VIDEO_INTENT, false)
            mIsGetAnyIntent = getBooleanExtra(GET_ANY_INTENT, false)
            mAllowPickingMultiple = getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        }

        binding.mediaRefreshLayout.setOnRefreshListener { getMedia() }
        try {
            mPath = intent.getStringExtra(DIRECTORY) ?: ""
        } catch (e: Exception) {
            ShowToastUseCase(this, "Error : $e")
            finish()
            return
        }

        setupOptionsMenu()
        refreshMenuItems()
        storeStateVariables()
        updateMaterialActivityViews(
            binding.mediaCoordinator,
            binding.mediaGrid,
            useTransparentNavigation = !config.scrollHorizontally,
            useTopSearchMenu = true
        )

        if (mShowAll) {
            registerFileUpdateListener()
        }

        binding.mediaEmptyTextPlaceholder2.setOnClickListener {
            showFilterMediaDialog()
        }

        updateWidgets()
    }

    override fun onStart() {
        super.onStart()
        mTempShowHiddenHandler.removeCallbacksAndMessages(null)
    }

    override fun onResume() {
        super.onResume()
        updateMenuColors()
        if (mStoredAnimateGifs != config.animateGifs) {
            getMediaAdapter()?.updateAnimateGifs(config.animateGifs)
        }

        if (mStoredCropThumbnails != config.cropThumbnails) {
            getMediaAdapter()?.updateCropThumbnails(config.cropThumbnails)
        }

        if (mStoredScrollHorizontally != config.scrollHorizontally) {
            mLoadedInitialPhotos = false
            binding.mediaGrid.adapter = null
            getMedia()
        }

        if (mStoredShowFileTypes != config.showThumbnailFileTypes) {
            getMediaAdapter()?.updateShowFileTypes(config.showThumbnailFileTypes)
        }

        if (mStoredTextColor != getProperTextColor()) {
            getMediaAdapter()?.updateTextColor(getProperTextColor())
        }

        val primaryColor = getProperPrimaryColor()
        if (mStoredPrimaryColor != primaryColor) {
            getMediaAdapter()?.updatePrimaryColor()
        }

        if (
            mStoredThumbnailSpacing != config.thumbnailSpacing
            || mStoredRoundedCorners != config.fileRoundedCorners
            || mStoredMarkFavoriteItems != config.markFavoriteItems
        ) {
            binding.mediaGrid.adapter = null
            setupAdapter()
        }

        refreshMenuItems()

        binding.mediaFastscroller.updateColors(primaryColor)
        binding.mediaRefreshLayout.isEnabled = config.enablePullToRefresh
        getMediaAdapter()?.apply {
            dateFormat = config.dateFormat
            timeFormat = getTimeFormat()
        }

        binding.mediaEmptyTextPlaceholder.setTextColor(getProperTextColor())
        binding.mediaEmptyTextPlaceholder2.setTextColor(getProperPrimaryColor())
        binding.mediaEmptyTextPlaceholder2.bringToFront()

        // do not refresh Random sorted files after opening a fullscreen image and going Back
        val isRandomSorting = config.getFolderSorting(mPath) and SORT_BY_RANDOM != 0
        if (mMedia.isEmpty() || !isRandomSorting || (!mWasFullscreenViewOpen)) {
            if (shouldSkipAuthentication()) {
                tryLoadGallery()
            } else {


                tryLoadGallery()

            }
        }
    }

    override fun onPause() {
        super.onPause()
        mIsGettingMedia = false
        binding.mediaRefreshLayout.isRefreshing = false
        storeStateVariables()
        mLastMediaHandler.removeCallbacksAndMessages(null)

        if (mMedia.isNotEmpty()) {
            mCurrAsyncTask?.stopFetching()
        }
    }

    override fun onStop() {
        super.onStop()

        if (config.temporarilyShowHidden || config.tempSkipDeleteConfirmation) {
            mTempShowHiddenHandler.postDelayed({
                config.temporarilyShowHidden = false
                config.tempSkipDeleteConfirmation = false
                config.tempSkipRecycleBin = false
            }, SHOW_TEMP_HIDDEN_DURATION)
        } else {
            mTempShowHiddenHandler.removeCallbacksAndMessages(null)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (config.showAll && !isChangingConfigurations) {
            config.temporarilyShowHidden = false
            config.tempSkipDeleteConfirmation = false
            config.tempSkipRecycleBin = false
            unregisterFileUpdateListener()
            GalleryDatabase.destroyInstance()
        }

        mTempShowHiddenHandler.removeCallbacksAndMessages(null)
    }

    override fun onBackPressed() {
        if (binding.mediaMenu.isSearchOpen) {
            binding.mediaMenu.closeSearch()
        } else {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (requestCode == REQUEST_EDIT_IMAGE) {
            if (resultCode == Activity.RESULT_OK && resultData != null) {
                mMedia.clear()
                refreshItems()
            }
        }
        super.onActivityResult(requestCode, resultCode, resultData)
    }

    private fun refreshMenuItems() {
        val isDefaultFolder =
            config.defaultFolder.isNotEmpty() && File(config.defaultFolder).compareTo(File(mPath)) == 0

        binding.mediaMenu.getToolbar().menu.apply {
            findItem(R.id.group).isVisible = !config.scrollHorizontally

            findItem(R.id.empty_recycle_bin).isVisible = mPath == RECYCLE_BIN
            findItem(R.id.empty_disable_recycle_bin).isVisible = mPath == RECYCLE_BIN
            findItem(R.id.restore_all_files).isVisible = mPath == RECYCLE_BIN

            findItem(R.id.folder_view).isVisible = mShowAll
            findItem(R.id.open_camera).isVisible = mShowAll
            findItem(R.id.about).isVisible = mShowAll
            findItem(R.id.create_new_folder).isVisible =
                !mShowAll && mPath != RECYCLE_BIN && mPath != FAVORITES
            findItem(R.id.open_recycle_bin).isVisible = config.useRecycleBin && mPath != RECYCLE_BIN

            findItem(R.id.temporarily_show_hidden).isVisible = !config.shouldShowHidden
            findItem(R.id.stop_showing_hidden).isVisible = (isExternalStorageManager()) && config.temporarilyShowHidden

            findItem(R.id.set_as_default_folder).isVisible = !isDefaultFolder
            findItem(R.id.unset_as_default_folder).isVisible = isDefaultFolder

            val viewType = config.getFolderViewType(if (mShowAll) SHOW_ALL else mPath)
            findItem(R.id.column_count).isVisible = viewType == VIEW_TYPE_GRID
            findItem(R.id.toggle_filename).isVisible = viewType == VIEW_TYPE_GRID
        }
    }

    private fun setupOptionsMenu() {
        binding.mediaMenu.getToolbar().inflateMenu(R.menu.menu_media)
        binding.mediaMenu.toggleHideOnScroll(!config.scrollHorizontally)
        binding.mediaMenu.setupMenu()

        binding.mediaMenu.onSearchTextChangedListener = { text ->
            mLastSearchedText = text
            searchQueryChanged(text)
            binding.mediaRefreshLayout.isEnabled = text.isEmpty() && config.enablePullToRefresh
        }

        binding.mediaMenu.getToolbar().setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.sort -> showSortingDialog()
                R.id.filter -> showFilterMediaDialog()
                R.id.empty_recycle_bin -> emptyRecycleBin()
                R.id.empty_disable_recycle_bin -> emptyAndDisableRecycleBin()
                R.id.restore_all_files -> restoreAllFiles()
                R.id.toggle_filename -> toggleFilenameVisibility()
                R.id.open_camera -> launchCamera()
                R.id.folder_view -> switchToFolderView()
                R.id.change_view_type -> changeViewType()
                R.id.group -> showGroupByDialog()
                R.id.create_new_folder -> createNewFolder()
                R.id.open_recycle_bin -> openRecycleBin()
                R.id.temporarily_show_hidden -> tryToggleTemporarilyShowHidden()
                R.id.stop_showing_hidden -> tryToggleTemporarilyShowHidden()
                R.id.column_count -> changeColumnCount()
                R.id.set_as_default_folder -> setAsDefaultFolder()
                R.id.unset_as_default_folder -> unsetAsDefaultFolder()
                R.id.slideshow -> startSlideshow()
                R.id.settings -> launchSettings()
                R.id.about -> launchAbout()
                else -> return@setOnMenuItemClickListener false
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun startSlideshow() {
        if (mMedia.isNotEmpty()) {
            hideKeyboard()
            Intent(this, ViewPagerActivity::class.java).apply {
                val item = mMedia.firstOrNull { it is Medium } as? Medium ?: return
                putExtra(SKIP_AUTHENTICATION, shouldSkipAuthentication())
                putExtra(PATH, item.path)
                putExtra(SHOW_ALL, mShowAll)
                putExtra(SLIDESHOW_START_ON_ENTER, true)
                startActivity(this)
            }
        }
    }

    private fun updateMenuColors() {
        updateStatusbarColor(getProperBackgroundColor())
        binding.mediaMenu.updateColors()
    }

    private fun storeStateVariables() {
        mStoredTextColor = getProperTextColor()
        mStoredPrimaryColor = getProperPrimaryColor()
        config.apply {
            mStoredAnimateGifs = animateGifs
            mStoredCropThumbnails = cropThumbnails
            mStoredScrollHorizontally = scrollHorizontally
            mStoredShowFileTypes = showThumbnailFileTypes
            mStoredMarkFavoriteItems = markFavoriteItems
            mStoredThumbnailSpacing = thumbnailSpacing
            mStoredRoundedCorners = fileRoundedCorners
            mShowAll = showAll && mPath != RECYCLE_BIN
        }
    }

    private fun searchQueryChanged(text: String) {
        ensureBackgroundThread {
            try {
                val filtered =
                    mMedia.filter { it is Medium && it.name.contains(text, true) } as ArrayList
                filtered.sortBy { it is Medium && !it.name.startsWith(text, true) }
                val grouped = MediaFetcher(applicationContext).groupMedia(
                    filtered as ArrayList<Medium>,
                    mPath
                )
                runOnUiThread {
                    if (grouped.isEmpty()) {
                        binding.mediaEmptyTextPlaceholder.text =
                            getString(R.string.no_items_found)
                        binding.mediaEmptyTextPlaceholder.beVisible()
                        binding.mediaFastscroller.beGone()
                    } else {
                        binding.mediaEmptyTextPlaceholder.beGone()
                        binding.mediaFastscroller.beVisible()
                    }

                    handleGridSpacing(grouped)
                    getMediaAdapter()?.updateMedia(grouped)
                }
            } catch (ignored: Exception) {
            }
        }
    }

    private fun tryLoadGallery() {
        handlePermission(PERMISSION_READ_MEDIA_IMAGES) {
            if (it) {
                val dirName = when (mPath) {
                    FAVORITES -> getString(R.string.favorites)
                    RECYCLE_BIN -> getString(R.string.recycle_bin)
                    config.otgPath -> getString(R.string.usb)
                    else -> getHumanizedFilename(mPath)
                }

                val searchHint = if (mShowAll) {
                    getString(R.string.search_files)
                } else {
                    getString(R.string.search_in_placeholder, dirName)
                }

                binding.mediaMenu.updateHintText(searchHint)
                if (!mShowAll) {
                    binding.mediaMenu.toggleForceArrowBackIcon(true)
                    binding.mediaMenu.onNavigateBackClickListener = {
                        onBackPressed()
                    }
                }

                getMedia()
                setupLayoutManager()
            } else {
                ShowToastUseCase(this, R.string.no_storage_permissions)
                finish()
            }
        }
    }

    private fun getMediaAdapter() = binding.mediaGrid.adapter as? MediaAdapter

    private fun setupAdapter() {
        if (!mShowAll && isDirEmpty()) {
            return
        }

        val currAdapter = binding.mediaGrid.adapter
        if (currAdapter == null) {
            initZoomListener()
            MediaAdapter(
                this,
                mMedia.clone() as ArrayList<ThumbnailItem>,
                this,
                mIsGetImageIntent || mIsGetVideoIntent || mIsGetAnyIntent,
                mAllowPickingMultiple,
                mPath,
                binding.mediaGrid
            ) {
                if (it is Medium && !isFinishing) {
                    itemClicked(it.path)
                }
            }.apply {
                setupZoomListener(mZoomListener)
                binding.mediaGrid.adapter = this
            }

            val viewType = config.getFolderViewType(if (mShowAll) SHOW_ALL else mPath)
            if (viewType == VIEW_TYPE_LIST && areSystemAnimationsEnabled) {
                binding.mediaGrid.scheduleLayoutAnimation()
            }

            setupLayoutManager()
            handleGridSpacing()
        } else if (mLastSearchedText.isEmpty()) {
            (currAdapter as MediaAdapter).updateMedia(mMedia)
            handleGridSpacing()
        } else {
            searchQueryChanged(mLastSearchedText)
        }

        setupScrollDirection()
    }

    private fun setupScrollDirection() {
        val viewType = config.getFolderViewType(if (mShowAll) SHOW_ALL else mPath)
        val scrollHorizontally = config.scrollHorizontally && viewType == VIEW_TYPE_GRID
        binding.mediaFastscroller.setScrollVertically(!scrollHorizontally)
    }

    private fun checkLastMediaChanged() {
        if (isDestroyed || config.getFolderSorting(mPath) and SORT_BY_RANDOM != 0) {
            return
        }

        mLastMediaHandler.removeCallbacksAndMessages(null)
        mLastMediaHandler.postDelayed({
            ensureBackgroundThread {
                val mediaId = getLatestMediaId()
                val mediaDateId = getLatestMediaByDateId()
                if (mLatestMediaId != mediaId || mLatestMediaDateId != mediaDateId) {
                    mLatestMediaId = mediaId
                    mLatestMediaDateId = mediaDateId
                    runOnUiThread {
                        getMedia()
                    }
                } else {
                    checkLastMediaChanged()
                }
            }
        }, LAST_MEDIA_CHECK_PERIOD)
    }

    private fun showSortingDialog() {
        ChangeSortingDialog(
            this,
            isDirectorySorting = false,
            showFolderCheckbox = true,
            path = mPath
        ) {
            mLoadedInitialPhotos = false
            binding.mediaGrid.adapter = null
            getMedia()
        }
    }

    private fun showFilterMediaDialog() {
        FilterMediaDialog(this) {
            mLoadedInitialPhotos = false
            binding.mediaRefreshLayout.isRefreshing = true
            binding.mediaGrid.adapter = null
            getMedia()
        }
    }

    private fun emptyRecycleBin() {
        showRecycleBinEmptyingDialog {
            emptyTheRecycleBin {
                finish()
            }
        }
    }

    private fun emptyAndDisableRecycleBin() {
        showRecycleBinEmptyingDialog {
            emptyAndDisableTheRecycleBin {
                finish()
            }
        }
    }

    private fun restoreAllFiles() {
        val paths = mMedia.filterIsInstance<Medium>().map { it.path } as ArrayList<String>
        restoreRecycleBinPaths(paths) {
            ensureBackgroundThread {
                directoryDB.deleteDirPath(RECYCLE_BIN)
            }
            finish()
        }
    }

    private fun toggleFilenameVisibility() {
        config.displayFileNames = !config.displayFileNames
        getMediaAdapter()?.updateDisplayFilenames(config.displayFileNames)
    }

    private fun switchToFolderView() {
        hideKeyboard()
        config.showAll = false
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun changeViewType() {
        ChangeViewTypeDialog(this, false, mPath) {
            refreshMenuItems()
            setupLayoutManager()
            binding.mediaGrid.adapter = null
            setupAdapter()
        }
    }

    private fun showGroupByDialog() {
        ChangeGroupingDialog(this, mPath) {
            mLoadedInitialPhotos = false
            binding.mediaGrid.adapter = null
            getMedia()
        }
    }

    private fun deleteDirectoryIfEmpty() {
        if (config.deleteEmptyFolders) {
            val fileDirItem = FileDirItem(mPath, mPath.getFilenameFromPath(), true)
            if (!fileDirItem.isDownloadsFolder() && fileDirItem.isDirectory) {
                ensureBackgroundThread {
                    if (fileDirItem.getProperFileCount(this, true) == 0) {
                        tryDeleteFileDirItem(
                            fileDirItem,
                            allowDeleteFolder = true,
                            deleteFromDatabase = true
                        )
                    }
                }
            }
        }
    }

    private fun getMedia() {
        if (mIsGettingMedia) {
            return
        }

        mIsGettingMedia = true
        if (mLoadedInitialPhotos) {
            startAsyncTask()
        } else {
            getCachedMedia(mPath, mIsGetVideoIntent, mIsGetImageIntent) {
                if (it.isEmpty()) {
                    runOnUiThread {
                        binding.mediaRefreshLayout.isRefreshing = true
                    }
                } else {
                    gotMedia(it, true)
                }
                startAsyncTask()
            }
        }

        mLoadedInitialPhotos = true
    }

    private fun startAsyncTask() {
        mCurrAsyncTask?.stopFetching()
        mCurrAsyncTask = GetMediaAsyncTask(
            applicationContext,
            mPath,
            mIsGetImageIntent,
            mIsGetVideoIntent,
            mShowAll
        ) {
            ensureBackgroundThread {
                val oldMedia = mMedia.clone() as ArrayList<ThumbnailItem>
                val newMedia = it
                try {
                    gotMedia(newMedia, false)

                    // remove cached files that are no longer valid for whatever reason
                    val newPaths = newMedia.mapNotNull { it as? Medium }.map { it.path }
                    oldMedia.mapNotNull { it as? Medium }.filter { !newPaths.contains(it.path) }
                        .forEach {
                            if (mPath == FAVORITES && getDoesFilePathExist(it.path)) {
                                favoritesDB.deleteFavoritePath(it.path)
                                mediaDB.updateFavorite(it.path, false)
                            } else {
                                mediaDB.deleteMediumPath(it.path)
                            }
                        }
                } catch (_: Exception) {
                }
            }
        }

        mCurrAsyncTask!!.execute()
    }

    private fun isDirEmpty(): Boolean {
        return if (mMedia.size <= 0 && config.filterMedia > 0) {
            if (mPath != FAVORITES && mPath != RECYCLE_BIN) {
                deleteDirectoryIfEmpty()
                deleteDBDirectory()
            }

            if (mPath == FAVORITES) {
                ensureBackgroundThread {
                    directoryDB.deleteDirPath(FAVORITES)
                }
            }

            if (mPath == RECYCLE_BIN) {
                binding.mediaEmptyTextPlaceholder.setText(R.string.no_items_found)
                binding.mediaEmptyTextPlaceholder.beVisible()
                binding.mediaEmptyTextPlaceholder2.beGone()
            } else {
                finish()
            }

            true
        } else {
            false
        }
    }

    private fun deleteDBDirectory() {
        ensureBackgroundThread {
            try {
                directoryDB.deleteDirPath(mPath)
            } catch (ignored: Exception) {
            }
        }
    }

    private fun createNewFolder() {
        CreateNewFolderDialog(this, mPath) {
            config.tempFolderPath = it
        }
    }

    private fun tryToggleTemporarilyShowHidden() {
        if (config.temporarilyShowHidden) {
            toggleTemporarilyShowHidden(false)
        } else {
            if (!isExternalStorageManager()) {
                GrantAllFilesDialog(this)
            } else {
                toggleTemporarilyShowHidden(true)
            }
        }
    }

    private fun toggleTemporarilyShowHidden(show: Boolean) {
        mLoadedInitialPhotos = false
        config.temporarilyShowHidden = show
        getMedia()
        refreshMenuItems()
    }

    private fun setupLayoutManager() {
        val viewType = config.getFolderViewType(if (mShowAll) SHOW_ALL else mPath)
        if (viewType == VIEW_TYPE_GRID) {
            setupGridLayoutManager()
        } else {
            setupListLayoutManager()
        }
    }

    private fun setupGridLayoutManager() {
        val layoutManager = binding.mediaGrid.layoutManager as MyGridLayoutManager
        if (config.scrollHorizontally) {
            layoutManager.orientation = RecyclerView.HORIZONTAL
            binding.mediaRefreshLayout.layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        } else {
            layoutManager.orientation = RecyclerView.VERTICAL
            binding.mediaRefreshLayout.layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        layoutManager.spanCount = config.mediaColumnCnt
        val adapter = getMediaAdapter()
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (adapter?.isASectionTitle(position) == true) {
                    layoutManager.spanCount
                } else {
                    1
                }
            }
        }
    }

    private fun setupListLayoutManager() {
        val layoutManager = binding.mediaGrid.layoutManager as MyGridLayoutManager
        layoutManager.spanCount = 1
        layoutManager.orientation = RecyclerView.VERTICAL
        binding.mediaRefreshLayout.layoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        mZoomListener = null
    }

    private fun handleGridSpacing(media: ArrayList<ThumbnailItem> = mMedia) {
        val viewType = config.getFolderViewType(if (mShowAll) SHOW_ALL else mPath)
        if (viewType == VIEW_TYPE_GRID) {
            val spanCount = config.mediaColumnCnt
            val spacing = config.thumbnailSpacing
            val useGridPosition = media.firstOrNull() is ThumbnailSection

            var currentGridDecoration: GridSpacingItemDecoration? = null
            if (binding.mediaGrid.itemDecorationCount > 0) {
                currentGridDecoration =
                    binding.mediaGrid.getItemDecorationAt(0) as GridSpacingItemDecoration
                currentGridDecoration.items = media
            }

            val newGridDecoration = GridSpacingItemDecoration(
                spanCount,
                spacing,
                config.scrollHorizontally,
                config.fileRoundedCorners,
                media,
                useGridPosition
            )
            if (currentGridDecoration.toString() != newGridDecoration.toString()) {
                if (currentGridDecoration != null) {
                    binding.mediaGrid.removeItemDecoration(currentGridDecoration)
                }
                binding.mediaGrid.addItemDecoration(newGridDecoration)
            }
        }
    }

    private fun initZoomListener() {
        val viewType = config.getFolderViewType(if (mShowAll) SHOW_ALL else mPath)
        if (viewType == VIEW_TYPE_GRID) {
            val layoutManager = binding.mediaGrid.layoutManager as MyGridLayoutManager
            mZoomListener = object : MyRecyclerView.MyZoomListener {
                override fun zoomIn() {
                    if (layoutManager.spanCount > 1) {
                        reduceColumnCount()
                        getMediaAdapter()?.finishActMode()
                    }
                }

                override fun zoomOut() {
                    if (layoutManager.spanCount < MAX_COLUMN_COUNT) {
                        increaseColumnCount()
                        getMediaAdapter()?.finishActMode()
                    }
                }
            }
        } else {
            mZoomListener = null
        }
    }

    private fun changeColumnCount() {
        val items = ArrayList<RadioItem>()
        for (i in 1..MAX_COLUMN_COUNT) {
            items.add(
                RadioItem(
                    i,
                    resources.getQuantityString(
                        R.plurals.column_counts,
                        i,
                        i
                    )
                )
            )
        }

        val currentColumnCount = (binding.mediaGrid.layoutManager as MyGridLayoutManager).spanCount
        RadioGroupDialog(this, items, currentColumnCount) {
            val newColumnCount = it as Int
            if (currentColumnCount != newColumnCount) {
                config.mediaColumnCnt = newColumnCount
                columnCountChanged()
            }
        }
    }

    private fun increaseColumnCount() {
        config.mediaColumnCnt += 1
        columnCountChanged()
    }

    private fun reduceColumnCount() {
        config.mediaColumnCnt -= 1
        columnCountChanged()
    }

    private fun columnCountChanged() {
        (binding.mediaGrid.layoutManager as MyGridLayoutManager).spanCount = config.mediaColumnCnt
        handleGridSpacing()
        refreshMenuItems()
        getMediaAdapter()?.apply {
            notifyItemRangeChanged(0, media.size)
        }
    }

    private fun isSetWallpaperIntent() = intent.getBooleanExtra(SET_WALLPAPER_INTENT, false)

    private fun itemClicked(path: String) {
        hideKeyboard()
        if (isSetWallpaperIntent()) {
            ShowToastUseCase(this, R.string.setting_wallpaper)

            val wantedWidth = wallpaperDesiredMinimumWidth
            val wantedHeight = wallpaperDesiredMinimumHeight
            val ratio = wantedWidth.toFloat() / wantedHeight

            val options = RequestOptions()
                .override((wantedWidth * ratio).toInt(), wantedHeight)
                .fitCenter()

            Glide.with(this)
                .asBitmap()
                .load(File(path))
                .apply(options)
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        try {
                            WallpaperManager.getInstance(applicationContext).setBitmap(resource)
                            setResult(Activity.RESULT_OK)
                        } catch (ignored: IOException) {
                        }

                        finish()
                    }
                })
        } else if (mIsGetImageIntent || mIsGetVideoIntent || mIsGetAnyIntent) {
            Intent().apply {
                data = Uri.parse(path)
                setResult(Activity.RESULT_OK, this)
            }
            finish()
        } else {
            mWasFullscreenViewOpen = true
            val isVideo = path.isVideoFast()
            if (isVideo) {
                val extras = HashMap<String, Boolean>()
                extras[SHOW_FAVORITES] = mPath == FAVORITES
                if (path.startsWith(recycleBinPath)) {
                    extras[IS_IN_RECYCLE_BIN] = true
                }

                if (shouldSkipAuthentication()) {
                    extras[SKIP_AUTHENTICATION] = true
                }
                openPath(path, false, extras)
            } else {
                Intent(this, ViewPagerActivity::class.java).apply {
                    putExtra(SKIP_AUTHENTICATION, shouldSkipAuthentication())
                    putExtra(PATH, path)
                    putExtra(SHOW_ALL, mShowAll)
                    putExtra(SHOW_FAVORITES, mPath == FAVORITES)
                    putExtra(SHOW_RECYCLE_BIN, mPath == RECYCLE_BIN)
                    putExtra(IS_FROM_GALLERY, true)
                    startActivity(this)
                }
            }
        }
    }

    private fun gotMedia(media: ArrayList<ThumbnailItem>, isFromCache: Boolean) {
        mIsGettingMedia = false
        checkLastMediaChanged()
        mMedia = media

        runOnUiThread {
            binding.mediaRefreshLayout.isRefreshing = false
            binding.mediaEmptyTextPlaceholder.beVisibleIf(media.isEmpty() && !isFromCache)
            binding.mediaEmptyTextPlaceholder2.beVisibleIf(media.isEmpty() && !isFromCache)

            if (binding.mediaEmptyTextPlaceholder.isVisible()) {
                binding.mediaEmptyTextPlaceholder.text = getString(R.string.no_media_with_filters)
            }
            binding.mediaFastscroller.beVisibleIf(binding.mediaEmptyTextPlaceholder.isGone())
            setupAdapter()
        }

        mLatestMediaId = getLatestMediaId()
        mLatestMediaDateId = getLatestMediaByDateId()
        if (!isFromCache) {
            val mediaToInsert =
                (mMedia).filter { it is Medium && it.deletedTS == 0L }.map { it as Medium }
            Thread {
                try {
                    mediaDB.insertAll(mediaToInsert)
                } catch (_: Exception) {
                }
            }.start()
        }
    }

    override fun tryDeleteFiles(fileDirItems: ArrayList<FileDirItem>, skipRecycleBin: Boolean) {
        val filtered =
            fileDirItems.filter { !getIsPathDirectory(it.path) && it.path.isMediaFile() } as ArrayList
        if (filtered.isEmpty()) {
            return
        }

        if (config.useRecycleBin && !skipRecycleBin && !filtered.first().path.startsWith(
                recycleBinPath
            )
        ) {
            val movingItems = resources.getQuantityString(
                R.plurals.moving_items_into_bin,
                filtered.size,
                filtered.size
            )
            ShowToastUseCase(this, movingItems)

            movePathsInRecycleBin(filtered.map { it.path } as ArrayList<String>) {
                if (it) {
                    deleteFilteredFiles(filtered)
                } else {
                    ShowToastUseCase(this, R.string.unknown_error_occurred)
                }
            }
        } else {
            val deletingItems = resources.getQuantityString(
                R.plurals.deleting_items,
                filtered.size,
                filtered.size
            )
            ShowToastUseCase(this, deletingItems)
            deleteFilteredFiles(filtered)
        }
    }

    private fun shouldSkipAuthentication() = intent.getBooleanExtra(SKIP_AUTHENTICATION, false)

    private fun deleteFilteredFiles(filtered: ArrayList<FileDirItem>) {
        deleteFiles(filtered) {
            if (!it) {
                ShowToastUseCase(this, R.string.unknown_error_occurred)
                return@deleteFiles
            }

            mMedia.removeAll { filtered.map { it.path }.contains((it as? Medium)?.path) }

            ensureBackgroundThread {
                val useRecycleBin = config.useRecycleBin
                filtered.forEach {
                    if (it.path.startsWith(recycleBinPath) || !useRecycleBin) {
                        deleteDBPath(it.path)
                    }
                }
            }

            if (mMedia.isEmpty()) {
                deleteDirectoryIfEmpty()
                deleteDBDirectory()
                finish()
            }
        }
    }

    override fun refreshItems() {
        getMedia()
    }

    override fun selectedPaths(paths: ArrayList<String>) {
        Intent().apply {
            putExtra(PICKED_PATHS, paths)
            setResult(Activity.RESULT_OK, this)
        }
        finish()
    }

    override fun updateMediaGridDecoration(media: ArrayList<ThumbnailItem>) {
        var currentGridPosition = 0
        media.forEach {
            if (it is Medium) {
                it.gridPosition = currentGridPosition++
            } else if (it is ThumbnailSection) {
                currentGridPosition = 0
            }
        }

        if (binding.mediaGrid.itemDecorationCount > 0) {
            val currentGridDecoration =
                binding.mediaGrid.getItemDecorationAt(0) as GridSpacingItemDecoration
            currentGridDecoration.items = media
        }
    }

    private fun setAsDefaultFolder() {
        config.defaultFolder = mPath
        refreshMenuItems()
    }

    private fun unsetAsDefaultFolder() {
        config.defaultFolder = ""
        refreshMenuItems()
    }
}
