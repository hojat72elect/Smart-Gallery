package ca.hojat.smart.gallery.feature_search

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.databinding.ActivitySearchBinding
import ca.hojat.smart.gallery.feature_media_viewer.ViewPagerActivity
import ca.hojat.smart.gallery.shared.activities.BaseActivity
import ca.hojat.smart.gallery.shared.data.domain.FileDirItem
import ca.hojat.smart.gallery.shared.data.domain.Medium
import ca.hojat.smart.gallery.shared.data.domain.ThumbnailItem
import ca.hojat.smart.gallery.shared.data.repository.GetMediaAsyncTask
import ca.hojat.smart.gallery.shared.extensions.beGone
import ca.hojat.smart.gallery.shared.extensions.beVisible
import ca.hojat.smart.gallery.shared.extensions.config
import ca.hojat.smart.gallery.shared.extensions.deleteDBPath
import ca.hojat.smart.gallery.shared.extensions.getCachedMedia
import ca.hojat.smart.gallery.shared.extensions.getProperBackgroundColor
import ca.hojat.smart.gallery.shared.extensions.getProperPrimaryColor
import ca.hojat.smart.gallery.shared.extensions.getProperTextColor
import ca.hojat.smart.gallery.shared.extensions.isMediaFile
import ca.hojat.smart.gallery.shared.extensions.isVideoFast
import ca.hojat.smart.gallery.shared.extensions.openPath
import ca.hojat.smart.gallery.shared.extensions.recycleBinPath
import ca.hojat.smart.gallery.shared.extensions.viewBinding
import ca.hojat.smart.gallery.shared.helpers.GridSpacingItemDecoration
import ca.hojat.smart.gallery.shared.helpers.MediaFetcher
import ca.hojat.smart.gallery.shared.helpers.PATH
import ca.hojat.smart.gallery.shared.helpers.SHOW_ALL
import ca.hojat.smart.gallery.shared.helpers.VIEW_TYPE_GRID
import ca.hojat.smart.gallery.shared.helpers.ensureBackgroundThread
import ca.hojat.smart.gallery.shared.ui.adapters.MediaAdapter
import ca.hojat.smart.gallery.shared.ui.adapters.MediaOperationsListener
import ca.hojat.smart.gallery.shared.ui.views.MyGridLayoutManager
import ca.hojat.smart.gallery.shared.usecases.ShowToastUseCase
import java.io.File

@UnstableApi
class SearchActivity : BaseActivity(), MediaOperationsListener {
    private var mLastSearchedText = ""

    private var mCurrAsyncTask: GetMediaAsyncTask? = null
    private var mAllMedia = ArrayList<ThumbnailItem>()

    private val binding by viewBinding(ActivitySearchBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupOptionsMenu()
        updateMaterialActivityViews(
            binding.searchCoordinator,
            binding.searchGrid,
            useTransparentNavigation = true,
            useTopSearchMenu = true
        )
        binding.searchEmptyTextPlaceholder.setTextColor(getProperTextColor())
        getAllMedia()
        binding.searchFastscroller.updateColors(getProperPrimaryColor())
    }

    override fun onResume() {
        super.onResume()
        updateMenuColors()
    }

    override fun onDestroy() {
        super.onDestroy()
        mCurrAsyncTask?.stopFetching()
    }

    private fun setupOptionsMenu() {
        binding.searchMenu.getToolbar().inflateMenu(R.menu.menu_search)
        binding.searchMenu.toggleHideOnScroll(true)
        binding.searchMenu.setupMenu()
        binding.searchMenu.toggleForceArrowBackIcon(true)
        binding.searchMenu.focusView()
        binding.searchMenu.updateHintText(getString(R.string.search_files))

        binding.searchMenu.onNavigateBackClickListener = {
            if (binding.searchMenu.getCurrentQuery().isEmpty()) {
                finish()
            } else {
                binding.searchMenu.closeSearch()
            }
        }

        binding.searchMenu.onSearchTextChangedListener = { text ->
            mLastSearchedText = text
            textChanged(text)
        }

        binding.searchMenu.getToolbar().setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.toggle_filename -> toggleFilenameVisibility()
                else -> return@setOnMenuItemClickListener false
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun updateMenuColors() {
        updateStatusbarColor(getProperBackgroundColor())
        binding.searchMenu.updateColors()
    }

    @SuppressLint("NewApi")
    private fun textChanged(text: String) {
        ensureBackgroundThread {
            try {
                val filtered =
                    mAllMedia.filter { it is Medium && it.name.contains(text, true) } as ArrayList
                filtered.sortBy { it is Medium && !it.name.startsWith(text, true) }
                val grouped =
                    MediaFetcher(applicationContext).groupMedia(filtered as ArrayList<Medium>, "")
                runOnUiThread {
                    if (grouped.isEmpty()) {
                        binding.searchEmptyTextPlaceholder.text =
                            getString(R.string.no_items_found)
                        binding.searchEmptyTextPlaceholder.beVisible()
                    } else {
                        binding.searchEmptyTextPlaceholder.beGone()
                    }

                    handleGridSpacing(grouped)
                    getMediaAdapter()?.updateMedia(grouped)
                }
            } catch (ignored: Exception) {
            }
        }
    }

    @SuppressLint("NewApi")
    private fun setupAdapter() {
        val currAdapter = binding.searchGrid.adapter
        if (currAdapter == null) {
            MediaAdapter(
                activity = this,
                media = mAllMedia,
                listener = this,
                isAGetIntent = false,
                allowMultiplePicks = false,
                path = "",
                recyclerView = binding.searchGrid
            ) {
                if (it is Medium) {
                    itemClicked(it.path)
                }
            }.apply {
                binding.searchGrid.adapter = this
            }
            setupLayoutManager()
            handleGridSpacing(mAllMedia)
        } else if (mLastSearchedText.isEmpty()) {
            (currAdapter as MediaAdapter).updateMedia(mAllMedia)
            handleGridSpacing(mAllMedia)
        } else {
            textChanged(mLastSearchedText)
        }

        setupScrollDirection()
    }

    private fun handleGridSpacing(media: ArrayList<ThumbnailItem>) {
        val viewType = config.getFolderViewType(SHOW_ALL)
        if (viewType == VIEW_TYPE_GRID) {
            if (binding.searchGrid.itemDecorationCount > 0) {
                binding.searchGrid.removeItemDecorationAt(0)
            }

            val spanCount = config.mediaColumnCnt
            val spacing = config.thumbnailSpacing
            val decoration = GridSpacingItemDecoration(
                spanCount,
                spacing,
                config.scrollHorizontally,
                config.fileRoundedCorners,
                media,
                true
            )
            binding.searchGrid.addItemDecoration(decoration)
        }
    }

    private fun getMediaAdapter() = binding.searchGrid.adapter as? MediaAdapter

    @SuppressLint("NewApi")
    private fun toggleFilenameVisibility() {
        config.displayFileNames = !config.displayFileNames
        getMediaAdapter()?.updateDisplayFilenames(config.displayFileNames)
    }

    private fun itemClicked(path: String) {
        val isVideo = path.isVideoFast()
        if (isVideo) {
            openPath(path, false)
        } else {
            Intent(this, ViewPagerActivity::class.java).apply {
                putExtra(PATH, path)
                putExtra(SHOW_ALL, false)
                startActivity(this)
            }
        }
    }

    private fun setupLayoutManager() {
        val viewType = config.getFolderViewType(SHOW_ALL)
        if (viewType == VIEW_TYPE_GRID) {
            setupGridLayoutManager()
        } else {
            setupListLayoutManager()
        }
    }

    private fun setupGridLayoutManager() {
        val layoutManager = binding.searchGrid.layoutManager as MyGridLayoutManager
        if (config.scrollHorizontally) {
            layoutManager.orientation = RecyclerView.HORIZONTAL
            binding.searchGrid.layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        } else {
            layoutManager.orientation = RecyclerView.VERTICAL
            binding.searchGrid.layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        layoutManager.spanCount = config.mediaColumnCnt
        val adapter = getMediaAdapter()
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            @SuppressLint("NewApi")
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
        val layoutManager = binding.searchGrid.layoutManager as MyGridLayoutManager
        layoutManager.spanCount = 1
        layoutManager.orientation = RecyclerView.VERTICAL
    }

    private fun setupScrollDirection() {
        val viewType = config.getFolderViewType(SHOW_ALL)
        val scrollHorizontally = config.scrollHorizontally && viewType == VIEW_TYPE_GRID
        binding.searchFastscroller.setScrollVertically(!scrollHorizontally)
    }

    private fun getAllMedia() {
        getCachedMedia("") {
            if (it.isNotEmpty()) {
                mAllMedia = it.clone() as ArrayList<ThumbnailItem>
            }
            runOnUiThread {
                setupAdapter()
            }
            startAsyncTask(false)
        }
    }

    private fun startAsyncTask(updateItems: Boolean) {
        mCurrAsyncTask?.stopFetching()
        mCurrAsyncTask = GetMediaAsyncTask(applicationContext, "", showAll = true) {
            mAllMedia = it.clone() as ArrayList<ThumbnailItem>
            if (updateItems) {
                textChanged(mLastSearchedText)
            }
        }

        mCurrAsyncTask!!.execute()
    }

    override fun refreshItems() {
        startAsyncTask(true)
    }

    override fun tryDeleteFiles(fileDirItems: ArrayList<FileDirItem>, skipRecycleBin: Boolean) {
        val filtered =
            fileDirItems.filter { File(it.path).isFile && it.path.isMediaFile() } as ArrayList
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

    private fun deleteFilteredFiles(filtered: ArrayList<FileDirItem>) {
        deleteFiles(filtered) {
            if (!it) {
                ShowToastUseCase(this, R.string.unknown_error_occurred)
                return@deleteFiles
            }

            mAllMedia.removeAll { filtered.map { it.path }.contains((it as? Medium)?.path) }

            ensureBackgroundThread {
                val useRecycleBin = config.useRecycleBin
                filtered.forEach { filteredFileDirectory ->
                    if (filteredFileDirectory.path.startsWith(recycleBinPath) || !useRecycleBin) {
                        deleteDBPath(filteredFileDirectory.path)
                    }
                }
            }
        }
    }

    override fun selectedPaths(paths: ArrayList<String>) {}

    override fun updateMediaGridDecoration(media: ArrayList<ThumbnailItem>) {}
}
