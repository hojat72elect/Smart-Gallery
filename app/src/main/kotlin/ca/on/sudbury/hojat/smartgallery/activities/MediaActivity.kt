package ca.on.sudbury.hojat.smartgallery.activities

import android.app.SearchManager
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.on.sudbury.hojat.smartgallery.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import ca.on.sudbury.hojat.smartgallery.helpers.FAVORITES
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_BY_RANDOM
import ca.on.sudbury.hojat.smartgallery.helpers.NavigationIcon
import ca.on.sudbury.hojat.smartgallery.helpers.PERMISSION_WRITE_STORAGE
import ca.on.sudbury.hojat.smartgallery.helpers.REQUEST_EDIT_IMAGE
import ca.on.sudbury.hojat.smartgallery.helpers.IS_FROM_GALLERY
import ca.on.sudbury.hojat.smartgallery.models.FileDirItem
import ca.on.hojat.palette.views.MyGridLayoutManager
import ca.on.sudbury.hojat.smartgallery.views.MyRecyclerView
import ca.on.sudbury.hojat.smartgallery.adapters.MediaAdapter
import ca.on.sudbury.hojat.smartgallery.asynctasks.GetMediaAsynctask
import ca.on.sudbury.hojat.smartgallery.base.SimpleActivity
import ca.on.sudbury.hojat.smartgallery.databases.GalleryDatabase
import ca.on.sudbury.hojat.smartgallery.extensions.isVideoFast
import ca.on.sudbury.hojat.smartgallery.database.MediaOperationsListener
import ca.on.sudbury.hojat.smartgallery.databinding.ActivityMediaBinding
import ca.on.sudbury.hojat.smartgallery.dialogs.ChangeGroupingDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.ChangeSortingDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.ChangeViewTypeDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.CreateNewFolderDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.FilterMediaDialogFragment
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.extensions.launchAbout
import ca.on.sudbury.hojat.smartgallery.extensions.restoreRecycleBinPaths
import ca.on.sudbury.hojat.smartgallery.extensions.showRecycleBinEmptyingDialog
import ca.on.sudbury.hojat.smartgallery.extensions.emptyAndDisableTheRecycleBin
import ca.on.sudbury.hojat.smartgallery.extensions.isDownloadsFolder
import ca.on.sudbury.hojat.smartgallery.extensions.tryDeleteFileDirItem
import ca.on.sudbury.hojat.smartgallery.extensions.movePathsInRecycleBin
import ca.on.sudbury.hojat.smartgallery.extensions.deleteDBPath
import ca.on.sudbury.hojat.smartgallery.extensions.mediaDB
import ca.on.sudbury.hojat.smartgallery.extensions.getCachedMedia
import ca.on.sudbury.hojat.smartgallery.extensions.openPath
import ca.on.sudbury.hojat.smartgallery.extensions.updateWidgets
import ca.on.sudbury.hojat.smartgallery.helpers.RECYCLE_BIN
import ca.on.sudbury.hojat.smartgallery.helpers.SHOW_ALL
import ca.on.sudbury.hojat.smartgallery.helpers.MAX_COLUMN_COUNT
import ca.on.sudbury.hojat.smartgallery.helpers.SKIP_AUTHENTICATION
import ca.on.sudbury.hojat.smartgallery.helpers.PATH
import ca.on.sudbury.hojat.smartgallery.helpers.SLIDESHOW_START_ON_ENTER
import ca.on.sudbury.hojat.smartgallery.helpers.GridSpacingItemDecoration
import ca.on.sudbury.hojat.smartgallery.helpers.SET_WALLPAPER_INTENT
import ca.on.sudbury.hojat.smartgallery.helpers.SHOW_FAVORITES
import ca.on.sudbury.hojat.smartgallery.helpers.SHOW_RECYCLE_BIN
import ca.on.sudbury.hojat.smartgallery.helpers.IS_IN_RECYCLE_BIN
import ca.on.sudbury.hojat.smartgallery.helpers.PICKED_PATHS
import ca.on.sudbury.hojat.smartgallery.helpers.MediaFetcher
import ca.on.sudbury.hojat.smartgallery.helpers.GET_ANY_INTENT
import ca.on.sudbury.hojat.smartgallery.helpers.GET_VIDEO_INTENT
import ca.on.sudbury.hojat.smartgallery.helpers.GET_IMAGE_INTENT
import ca.on.sudbury.hojat.smartgallery.helpers.SHOW_TEMP_HIDDEN_DURATION
import ca.on.sudbury.hojat.smartgallery.helpers.DIRECTORY
import ca.on.sudbury.hojat.smartgallery.models.Medium
import ca.on.sudbury.hojat.smartgallery.models.ThumbnailItem
import ca.on.sudbury.hojat.smartgallery.models.ThumbnailSection
import ca.on.sudbury.hojat.smartgallery.extensions.areSystemAnimationsEnabled
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig
import ca.on.sudbury.hojat.smartgallery.extensions.handleHiddenFolderPasswordProtection
import ca.on.sudbury.hojat.smartgallery.extensions.getLatestMediaByDateId
import ca.on.sudbury.hojat.smartgallery.extensions.getLatestMediaId
import ca.on.sudbury.hojat.smartgallery.extensions.getIsPathDirectory
import ca.on.sudbury.hojat.smartgallery.extensions.isMediaFile
import ca.on.sudbury.hojat.smartgallery.extensions.recycleBinPath
import ca.on.sudbury.hojat.smartgallery.extensions.deleteFiles
import ca.on.sudbury.hojat.smartgallery.extensions.getProperTextColor
import ca.on.sudbury.hojat.smartgallery.extensions.handleLockedFolderOpening
import ca.on.sudbury.hojat.smartgallery.extensions.isExternalStorageManager
import ca.on.sudbury.hojat.smartgallery.extensions.getProperPrimaryColor
import ca.on.sudbury.hojat.smartgallery.extensions.getFilenameFromPath
import ca.on.sudbury.hojat.smartgallery.extensions.getDoesFilePathExist
import ca.on.sudbury.hojat.smartgallery.extensions.humanizePath
import ca.on.sudbury.hojat.smartgallery.helpers.SmartGalleryTimeFormat
import ca.on.sudbury.hojat.smartgallery.helpers.ViewType
import ca.on.sudbury.hojat.smartgallery.usecases.IsRPlusUseCase
import ca.on.sudbury.hojat.smartgallery.settings.SettingsActivity
import ca.on.sudbury.hojat.smartgallery.usecases.BeVisibleOrGoneUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.EmptyTheRecycleBinUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.HideKeyboardUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.LaunchCameraUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.RunOnBackgroundThreadUseCase
import java.io.File
import java.io.IOException

class MediaActivity : SimpleActivity(), MediaOperationsListener {
    private lateinit var binding: ActivityMediaBinding
    private val LAST_MEDIA_CHECK_PERIOD = 3000L

    private var mPath = ""
    private var mIsGetImageIntent = false
    private var mIsGetVideoIntent = false
    private var mIsGetAnyIntent = false
    private var mIsGettingMedia = false
    private var mAllowPickingMultiple = false
    private var mShowAll = false
    private var mLoadedInitialPhotos = false
    private var mIsSearchOpen = false
    private var mWasFullscreenViewOpen = false
    private var mLastSearchedText = ""
    private var mLatestMediaId = 0L
    private var mLatestMediaDateId = 0L
    private var mLastMediaHandler = Handler()
    private var mTempShowHiddenHandler = Handler()
    private var mCurrAsyncTask: GetMediaAsynctask? = null
    private var mZoomListener: MyRecyclerView.MyZoomListener? = null
    private var mSearchMenuItem: MenuItem? = null

    private var mStoredAnimateGifs = true
    private var mStoredCropThumbnails = true
    private var mStoredScrollHorizontally = true
    private var mStoredShowFileTypes = true
    private var mStoredRoundedCorners = false
    private var mStoredMarkFavoriteItems = true
    private var mStoredTextColor = 0
    private var mStoredPrimaryColor = 0
    private var mStoredThumbnailSpacing = 0

    companion object {
        var mMedia = ArrayList<ThumbnailItem>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMediaBinding.inflate(layoutInflater)
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
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupOptionsMenu()
        refreshMenuItems()
        storeStateVariables()

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

        val navigation = if (mShowAll) {
            NavigationIcon.None
        } else {
            NavigationIcon.Arrow
        }

        setupToolbar(binding.mediaToolbar, navigation, searchMenuItem = mSearchMenuItem)
        refreshMenuItems()

        binding.mediaFastscroller.updateColors(primaryColor)
        binding.mediaRefreshLayout.isEnabled = config.enablePullToRefresh
        getMediaAdapter()?.apply {
            dateFormat = config.dateFormat
            timeFormat =
                if (baseConfig.use24HourFormat) SmartGalleryTimeFormat.FullDay.format else SmartGalleryTimeFormat.HalfDay.format
        }

        binding.mediaEmptyTextPlaceholder.setTextColor(getProperTextColor())
        binding.mediaEmptyTextPlaceholder2.setTextColor(getProperPrimaryColor())
        binding.mediaEmptyTextPlaceholder2.bringToFront()

        // do not refresh Random sorted files after opening a fullscreen image and going Back
        val isRandomSorting = config.getFolderSorting(mPath) and SORT_BY_RANDOM != 0
        if (mMedia.isEmpty() || !isRandomSorting || (isRandomSorting && !mWasFullscreenViewOpen)) {
            if (shouldSkipAuthentication()) {
                tryLoadGallery()
            } else {
                handleLockedFolderOpening(mPath) { success ->
                    if (success) {
                        tryLoadGallery()
                    } else {
                        finish()
                    }
                }
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
            unregisterFileUpdateListener()
            GalleryDatabase.destroyInstance()
        }

        mTempShowHiddenHandler.removeCallbacksAndMessages(null)
    }

    override fun onBackPressed() {
        if (mIsSearchOpen && mSearchMenuItem != null) {
            mSearchMenuItem!!.collapseActionView()
        } else {
            super.onBackPressed()
        }
    }

    private fun refreshMenuItems() {
        val isDefaultFolder =
            config.defaultFolder.isNotEmpty() && File(config.defaultFolder).compareTo(File(mPath)) == 0

        binding.mediaToolbar.menu.apply {
            findItem(R.id.group).isVisible = !config.scrollHorizontally

            findItem(R.id.empty_recycle_bin).isVisible = mPath == RECYCLE_BIN
            findItem(R.id.empty_disable_recycle_bin).isVisible = mPath == RECYCLE_BIN
            findItem(R.id.restore_all_files).isVisible = mPath == RECYCLE_BIN

            findItem(R.id.folder_view).isVisible = mShowAll
            findItem(R.id.open_camera).isVisible = mShowAll
            findItem(R.id.about).isVisible = mShowAll
            findItem(R.id.create_new_folder).isVisible =
                !mShowAll && mPath != RECYCLE_BIN && mPath != FAVORITES

            findItem(R.id.temporarily_show_hidden).isVisible =
                (!IsRPlusUseCase() || isExternalStorageManager()) && !config.shouldShowHidden
            findItem(R.id.stop_showing_hidden).isVisible =
                (!IsRPlusUseCase() || isExternalStorageManager()) && config.temporarilyShowHidden

            findItem(R.id.set_as_default_folder).isVisible = !isDefaultFolder
            findItem(R.id.unset_as_default_folder).isVisible = isDefaultFolder

            val viewType = config.getFolderViewType(if (mShowAll) SHOW_ALL else mPath)
            findItem(R.id.increase_column_count).isVisible =
                viewType == ViewType.Grid.id && config.mediaColumnCnt < MAX_COLUMN_COUNT
            findItem(R.id.reduce_column_count).isVisible =
                viewType == ViewType.Grid.id && config.mediaColumnCnt > 1
            findItem(R.id.toggle_filename).isVisible = viewType == ViewType.Grid.id
        }
    }

    private fun setupOptionsMenu() {
        setupSearch(binding.mediaToolbar.menu)
        binding.mediaToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.sort -> showSortingDialog()
                R.id.filter -> showFilterMediaDialog()
                R.id.empty_recycle_bin -> emptyRecycleBin()
                R.id.empty_disable_recycle_bin -> emptyAndDisableRecycleBin()
                R.id.restore_all_files -> restoreAllFiles()
                R.id.toggle_filename -> toggleFilenameVisibility()
                R.id.open_camera -> LaunchCameraUseCase(this)
                R.id.folder_view -> switchToFolderView()
                R.id.change_view_type -> changeViewType()
                R.id.group -> showGroupByDialog()
                R.id.create_new_folder -> createNewFolder()
                R.id.temporarily_show_hidden -> tryToggleTemporarilyShowHidden()
                R.id.stop_showing_hidden -> tryToggleTemporarilyShowHidden()
                R.id.increase_column_count -> increaseColumnCount()
                R.id.reduce_column_count -> reduceColumnCount()
                R.id.set_as_default_folder -> setAsDefaultFolder()
                R.id.unset_as_default_folder -> unsetAsDefaultFolder()
                R.id.slideshow -> startSlideshow()
                R.id.settings -> {
                    // start settings page
                    HideKeyboardUseCase(this)
                    startActivity(Intent(applicationContext, SettingsActivity::class.java))
                }
                R.id.about -> launchAbout()
                else -> return@setOnMenuItemClickListener false
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun startSlideshow() {
        if (mMedia.isNotEmpty()) {
            HideKeyboardUseCase(this)
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
            mShowAll = showAll
        }
    }

    private fun setupSearch(menu: Menu) {
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        mSearchMenuItem = menu.findItem(R.id.search)
        (mSearchMenuItem?.actionView as? SearchView)?.apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            isSubmitButtonEnabled = false
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String) = false

                override fun onQueryTextChange(newText: String): Boolean {
                    if (mIsSearchOpen) {
                        mLastSearchedText = newText
                        searchQueryChanged(newText)
                    }
                    return true
                }
            })
        }

        MenuItemCompat.setOnActionExpandListener(
            mSearchMenuItem,
            object : MenuItemCompat.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                    mIsSearchOpen = true
                    binding.mediaRefreshLayout.isEnabled = false
                    return true
                }

                // this triggers on device rotation too, avoid doing anything
                override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                    if (mIsSearchOpen) {
                        mIsSearchOpen = false
                        mLastSearchedText = ""

                        binding.mediaRefreshLayout.isEnabled = config.enablePullToRefresh
                        searchQueryChanged("")
                    }
                    return true
                }
            })
    }

    private fun searchQueryChanged(text: String) {
        RunOnBackgroundThreadUseCase {
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
                        binding.mediaEmptyTextPlaceholder.text = getString(R.string.no_items_found)
                        binding.mediaEmptyTextPlaceholder.visibility = View.VISIBLE
                        binding.mediaFastscroller.visibility = View.GONE
                    } else {
                        binding.mediaEmptyTextPlaceholder.visibility = View.GONE
                        binding.mediaFastscroller.visibility = View.VISIBLE
                    }

                    handleGridSpacing(grouped)
                    getMediaAdapter()?.updateMedia(grouped)
                }
            } catch (ignored: Exception) {
            }
        }
    }

    private fun tryLoadGallery() {
        handlePermission(PERMISSION_WRITE_STORAGE) {
            if (it) {
                val dirName = when {
                    mPath == FAVORITES -> getString(R.string.favorites)
                    mPath == RECYCLE_BIN -> getString(R.string.recycle_bin)
                    mPath == config.otgPath -> getString(R.string.usb)
                    else -> getHumanizedFilename(mPath)
                }

                binding.mediaToolbar.title =
                    if (mShowAll) resources.getString(R.string.all_folders) else dirName
                getMedia()
                setupLayoutManager()
            } else {
                Toast.makeText(this, R.string.no_storage_permissions, Toast.LENGTH_LONG).show()
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
            if (viewType == ViewType.List.id && areSystemAnimationsEnabled) {
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
        val scrollHorizontally = config.scrollHorizontally && viewType == ViewType.Grid.id
        binding.mediaFastscroller.setScrollVertically(!scrollHorizontally)
    }

    private fun checkLastMediaChanged() {
        if (isDestroyed || config.getFolderSorting(mPath) and SORT_BY_RANDOM != 0) {
            return
        }

        mLastMediaHandler.removeCallbacksAndMessages(null)
        mLastMediaHandler.postDelayed({
            RunOnBackgroundThreadUseCase {
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
        val callback = {
            mLoadedInitialPhotos = false
            binding.mediaGrid.adapter = null
            getMedia()
        }
        ChangeSortingDialogFragment(
            isDirectorySorting = false,
            showFolderCheckbox = true,
            path = mPath, callback = callback
        ).show(supportFragmentManager, ChangeSortingDialogFragment.TAG)
    }

    private fun showFilterMediaDialog() {
        val callbackAfterDialogConfirmed: (Int) -> Unit = {
            mLoadedInitialPhotos = false
            binding.mediaRefreshLayout.isRefreshing = true
            binding.mediaGrid.adapter = null
            getMedia()
        }
        FilterMediaDialogFragment(callbackAfterDialogConfirmed).show(
            supportFragmentManager,
            FilterMediaDialogFragment.TAG
        )
    }

    private fun emptyRecycleBin() {
        showRecycleBinEmptyingDialog {
            EmptyTheRecycleBinUseCase(this) {
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
        val paths = mMedia.filter { it is Medium }.map { (it as Medium).path } as ArrayList<String>
        restoreRecycleBinPaths(paths) {
            RunOnBackgroundThreadUseCase {
                GalleryDatabase.getInstance(applicationContext).DirectoryDao()
                    .deleteDirPath(RECYCLE_BIN)
            }
            finish()
        }
    }

    private fun toggleFilenameVisibility() {
        config.displayFileNames = !config.displayFileNames
        getMediaAdapter()?.updateDisplayFilenames(config.displayFileNames)
    }

    private fun switchToFolderView() {
        HideKeyboardUseCase(this)
        config.showAll = false
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun changeViewType() {
        val callback = {
            refreshMenuItems()
            setupLayoutManager()
            binding.mediaGrid.adapter = null
            setupAdapter()
        }
        ChangeViewTypeDialogFragment(
            fromFoldersView = false,
            path = mPath,
            callback = callback
        ).show(
            supportFragmentManager,
            ChangeViewTypeDialogFragment.TAG
        )
    }

    private fun showGroupByDialog() {
        val callback = {
            mLoadedInitialPhotos = false
            binding.mediaGrid.adapter = null
            getMedia()
        }
        ChangeGroupingDialogFragment(mPath, callback).show(
            supportFragmentManager,
            ChangeGroupingDialogFragment.TAG
        )
    }

    private fun deleteDirectoryIfEmpty() {
        if (config.deleteEmptyFolders) {
            val fileDirItem = FileDirItem(mPath, mPath.getFilenameFromPath(), true)
            if (!fileDirItem.path.isDownloadsFolder() && fileDirItem.isDirectory) {
                RunOnBackgroundThreadUseCase {
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
        mCurrAsyncTask = GetMediaAsynctask(
            applicationContext,
            mPath,
            mIsGetImageIntent,
            mIsGetVideoIntent,
            mShowAll
        ) {
            RunOnBackgroundThreadUseCase {

                val oldMedia = mMedia.clone() as ArrayList<ThumbnailItem>
                val newMedia = it
                try {
                    gotMedia(newMedia, false)

                    // remove cached files that are no longer valid for whatever reason
                    val newPaths = newMedia.mapNotNull { it as? Medium }.map { it.path }
                    oldMedia.mapNotNull { it as? Medium }.filter { !newPaths.contains(it.path) }
                        .forEach {
                            if (mPath == FAVORITES && getDoesFilePathExist(it.path)) {
                                GalleryDatabase.getInstance(applicationContext).FavoritesDao()
                                    .deleteFavoritePath(it.path)
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
                RunOnBackgroundThreadUseCase {
                    GalleryDatabase.getInstance(applicationContext).DirectoryDao()
                        .deleteDirPath(FAVORITES)
                }
            }

            finish()
            true
        } else {
            false
        }
    }

    private fun deleteDBDirectory() {
        RunOnBackgroundThreadUseCase {
            try {
                GalleryDatabase.getInstance(applicationContext).DirectoryDao().deleteDirPath(mPath)
            } catch (ignored: Exception) {
            }
        }
    }

    private fun createNewFolder() {
        val callback: (String) -> Unit = { path ->
            config.tempFolderPath = path
        }
        CreateNewFolderDialogFragment(mPath, callback).show(
            supportFragmentManager,
            CreateNewFolderDialogFragment.TAG
        )
    }

    private fun tryToggleTemporarilyShowHidden() {
        if (config.temporarilyShowHidden) {
            toggleTemporarilyShowHidden(false)
        } else {
            handleHiddenFolderPasswordProtection {
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
        if (viewType == ViewType.Grid.id) {
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
        if (viewType == ViewType.Grid.id) {
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
        if (viewType == ViewType.Grid.id) {
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

    private fun increaseColumnCount() {
        config.mediaColumnCnt = ++(binding.mediaGrid.layoutManager as MyGridLayoutManager).spanCount
        columnCountChanged()
    }

    private fun reduceColumnCount() {
        config.mediaColumnCnt = --(binding.mediaGrid.layoutManager as MyGridLayoutManager).spanCount
        columnCountChanged()
    }

    private fun columnCountChanged() {
        handleGridSpacing()
        refreshMenuItems()
        getMediaAdapter()?.apply {
            notifyItemRangeChanged(0, media.size)
        }
    }

    private fun isSetWallpaperIntent() = intent.getBooleanExtra(SET_WALLPAPER_INTENT, false)

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (requestCode == REQUEST_EDIT_IMAGE) {
            if (resultCode == RESULT_OK && resultData != null) {
                mMedia.clear()
                refreshItems()
            }
        }
        super.onActivityResult(requestCode, resultCode, resultData)
    }

    private fun itemClicked(path: String) {
        HideKeyboardUseCase(this)
        if (isSetWallpaperIntent()) {
            Toast.makeText(this, R.string.setting_wallpaper, Toast.LENGTH_LONG).show()

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
                            setResult(RESULT_OK)
                        } catch (ignored: IOException) {
                        }

                        finish()
                    }
                })
        } else if (mIsGetImageIntent || mIsGetVideoIntent || mIsGetAnyIntent) {
            Intent().apply {
                data = Uri.parse(path)
                setResult(RESULT_OK, this)
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
            BeVisibleOrGoneUseCase(
                binding.mediaEmptyTextPlaceholder,
                media.isEmpty() && !isFromCache
            )
            BeVisibleOrGoneUseCase(
                binding.mediaEmptyTextPlaceholder2,
                media.isEmpty() && !isFromCache
            )

            if (binding.mediaEmptyTextPlaceholder.visibility == View.VISIBLE) {
                binding.mediaEmptyTextPlaceholder.text = getString(R.string.no_media_with_filters)
            }
            BeVisibleOrGoneUseCase(
                binding.mediaFastscroller,
                binding.mediaEmptyTextPlaceholder.visibility == View.GONE
            )
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

    override fun tryDeleteFiles(fileDirItems: ArrayList<FileDirItem>) {
        val filtered =
            fileDirItems.filter { !getIsPathDirectory(it.path) && it.path.isMediaFile() } as ArrayList
        if (filtered.isEmpty()) {
            return
        }

        if (config.useRecycleBin && !filtered.first().path.startsWith(recycleBinPath)) {
            val movingItems = resources.getQuantityString(
                R.plurals.moving_items_into_bin,
                filtered.size,
                filtered.size
            )
            Toast.makeText(this, movingItems, Toast.LENGTH_LONG).show()

            movePathsInRecycleBin(filtered.map { it.path } as ArrayList<String>) {
                if (it) {
                    deleteFilteredFiles(filtered)
                } else {
                    Toast.makeText(this, R.string.unknown_error_occurred, Toast.LENGTH_LONG).show()
                }
            }
        } else {
            val deletingItems =
                resources.getQuantityString(R.plurals.deleting_items, filtered.size, filtered.size)
            Toast.makeText(this, deletingItems, Toast.LENGTH_LONG).show()
            deleteFilteredFiles(filtered)
        }
    }

    private fun shouldSkipAuthentication() = intent.getBooleanExtra(SKIP_AUTHENTICATION, false)

    private fun deleteFilteredFiles(filtered: ArrayList<FileDirItem>) {
        deleteFiles(filtered) {
            if (!it) {
                Toast.makeText(this, R.string.unknown_error_occurred, Toast.LENGTH_LONG).show()
                return@deleteFiles
            }

            mMedia.removeAll { filtered.map { it.path }.contains((it as? Medium)?.path) }

            RunOnBackgroundThreadUseCase {
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
            setResult(RESULT_OK, this)
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

    private fun getHumanizedFilename(path: String): String {
        val humanized = humanizePath(path)
        return humanized.substring(humanized.lastIndexOf("/") + 1)
    }

}
