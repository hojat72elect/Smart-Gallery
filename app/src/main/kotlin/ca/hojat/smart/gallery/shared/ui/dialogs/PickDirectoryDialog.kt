package ca.hojat.smart.gallery.shared.ui.dialogs

import android.graphics.Color
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.databinding.DialogDirectoryPickerBinding
import ca.hojat.smart.gallery.shared.activities.BaseActivity
import ca.hojat.smart.gallery.shared.data.domain.Directory
import ca.hojat.smart.gallery.shared.extensions.addTempFolderIfNeeded
import ca.hojat.smart.gallery.shared.extensions.beGone
import ca.hojat.smart.gallery.shared.extensions.beInvisible
import ca.hojat.smart.gallery.shared.extensions.beVisibleIf
import ca.hojat.smart.gallery.shared.extensions.config
import ca.hojat.smart.gallery.shared.extensions.getAlertDialogBuilder
import ca.hojat.smart.gallery.shared.extensions.getCachedDirectories
import ca.hojat.smart.gallery.shared.extensions.getDefaultCopyDestinationPath
import ca.hojat.smart.gallery.shared.extensions.getDirsToShow
import ca.hojat.smart.gallery.shared.extensions.getDistinctPath
import ca.hojat.smart.gallery.shared.extensions.getProperPrimaryColor
import ca.hojat.smart.gallery.shared.extensions.getSortedDirectories
import ca.hojat.smart.gallery.shared.extensions.handleHiddenFolderPasswordProtection
import ca.hojat.smart.gallery.shared.extensions.handleLockedFolderOpening
import ca.hojat.smart.gallery.shared.extensions.hideKeyboard
import ca.hojat.smart.gallery.shared.extensions.isGone
import ca.hojat.smart.gallery.shared.extensions.isInDownloadDir
import ca.hojat.smart.gallery.shared.extensions.isRestrictedWithSAFSdk30
import ca.hojat.smart.gallery.shared.extensions.setupDialogStuff
import ca.hojat.smart.gallery.shared.extensions.toast
import ca.hojat.smart.gallery.shared.helpers.VIEW_TYPE_GRID
import ca.hojat.smart.gallery.shared.ui.adapters.DirectoryAdapter
import ca.hojat.smart.gallery.shared.ui.views.MyGridLayoutManager
import ca.hojat.smart.gallery.shared.ui.views.MySearchMenu

@UnstableApi
class PickDirectoryDialog(
    val activity: BaseActivity,
    private val sourcePath: String,
    showOtherFolderButton: Boolean,
    val showFavoritesBin: Boolean,
    val isPickingCopyMoveDestination: Boolean,
    val isPickingFolderForWidget: Boolean,
    val callback: (path: String) -> Unit
) {
    private var dialog: AlertDialog? = null
    private var shownDirectories = ArrayList<Directory>()
    private var allDirectories = ArrayList<Directory>()
    private var openedSubfolders = arrayListOf("")
    private var binding = DialogDirectoryPickerBinding.inflate(activity.layoutInflater)
    private var isGridViewType = activity.config.viewTypeFolders == VIEW_TYPE_GRID
    private var showHidden = activity.config.shouldShowHidden
    private var currentPathPrefix = ""
    private val config = activity.config
    private val searchView = binding.folderSearchView
    private val searchEditText = searchView.binding.topToolbarSearch
    private val searchViewAppBarLayout = searchView.binding.topAppBarLayout

    init {
        (binding.directoriesGrid.layoutManager as MyGridLayoutManager).apply {
            orientation =
                if (activity.config.scrollHorizontally && isGridViewType) RecyclerView.HORIZONTAL else RecyclerView.VERTICAL
            spanCount = if (isGridViewType) activity.config.dirColumnCnt else 1
        }

        binding.directoriesFastscroller.updateColors(activity.getProperPrimaryColor())

        configureSearchView()

        val builder = activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .setOnKeyListener { _, i, keyEvent ->
                if (keyEvent.action == KeyEvent.ACTION_UP && i == KeyEvent.KEYCODE_BACK) {
                    backPressed()
                }
                true
            }

        if (showOtherFolderButton) {
            builder.setNeutralButton(R.string.other_folder) { _, _ -> showOtherFolder() }
        }

        builder.apply {
            activity.setupDialogStuff(
                binding.root,
                this,
                R.string.select_destination
            ) { alertDialog ->
                dialog = alertDialog
                binding.directoriesShowHidden.beVisibleIf(!context.config.shouldShowHidden)
                binding.directoriesShowHidden.setOnClickListener {
                    activity.handleHiddenFolderPasswordProtection {
                        binding.directoriesShowHidden.beGone()
                        showHidden = true
                        fetchDirectories(true)
                    }
                }
            }
        }

        fetchDirectories(false)
    }

    private fun configureSearchView() = with(searchView) {
        updateHintText(context.getString(R.string.search_folders))
        searchEditText.imeOptions = EditorInfo.IME_ACTION_DONE

        toggleHideOnScroll(!config.scrollHorizontally)
        setupMenu()
        setSearchViewListeners()
        updateSearchViewUi()
    }

    private fun MySearchMenu.updateSearchViewUi() {
        getToolbar().beInvisible()
        updateColors()
        setBackgroundColor(Color.TRANSPARENT)
        searchViewAppBarLayout.setBackgroundColor(Color.TRANSPARENT)
    }

    private fun MySearchMenu.setSearchViewListeners() {
        onSearchOpenListener = {
            updateSearchViewLeftIcon(R.drawable.ic_cross_vector)
        }

        onSearchClosedListener = {
            searchEditText.clearFocus()
            activity.hideKeyboard(searchEditText)
            updateSearchViewLeftIcon(R.drawable.ic_search_vector)
        }

        onSearchTextChangedListener = { text ->
            filterFolderListBySearchQuery(text)
        }
    }

    private fun updateSearchViewLeftIcon(iconResId: Int) =
        with(searchView.binding.topToolbarSearchIcon) {
            post {
                setImageResource(iconResId)
            }
        }


    private fun filterFolderListBySearchQuery(query: String) {
        val adapter = binding.directoriesGrid.adapter as? DirectoryAdapter
        var dirsToShow = allDirectories
        if (query.isNotEmpty()) {
            dirsToShow =
                dirsToShow.filter { it.name.contains(query, true) }.toMutableList() as ArrayList
        }
        dirsToShow = activity.getSortedDirectories(dirsToShow)
        checkPlaceholderVisibility(dirsToShow)

        val filteredFolderListUpdated = adapter?.dirs != dirsToShow
        if (filteredFolderListUpdated) {
            adapter?.updateDirs(dirsToShow)

            binding.directoriesGrid.apply {
                post {
                    scrollToPosition(0)
                }
            }
        }
    }

    private fun checkPlaceholderVisibility(dirs: ArrayList<Directory>) = with(binding) {
        directoriesEmptyPlaceholder.beVisibleIf(dirs.isEmpty())

        if (folderSearchView.isSearchOpen) {
            directoriesEmptyPlaceholder.text =
                root.context.getString(R.string.no_items_found)
        }

        directoriesFastscroller.beVisibleIf(directoriesEmptyPlaceholder.isGone())
    }

    private fun fetchDirectories(forceShowHiddenAndExcluded: Boolean) {
        activity.getCachedDirectories(
            forceShowHidden = forceShowHiddenAndExcluded,
            forceShowExcluded = forceShowHiddenAndExcluded
        ) {
            if (it.isNotEmpty()) {
                it.forEach {
                    it.subfoldersMediaCount = it.mediaCnt
                }

                activity.runOnUiThread {
                    gotDirectories(activity.addTempFolderIfNeeded(it))
                }
            }
        }
    }


    private fun showOtherFolder() {
        activity.hideKeyboard(searchEditText)
        FilePickerDialog(
            activity,
            activity.getDefaultCopyDestinationPath(showHidden, sourcePath),
            !isPickingCopyMoveDestination && !isPickingFolderForWidget,
            showHidden,
            showFAB = true,
            canAddShowHiddenButton = true
        ) {
            config.lastCopyPath = it
            activity.handleLockedFolderOpening(it) { success ->
                if (success) {
                    callback(it)
                }
            }
        }
    }

    private fun gotDirectories(newDirs: ArrayList<Directory>) {
        if (allDirectories.isEmpty()) {
            allDirectories = newDirs.clone() as ArrayList<Directory>
        }

        val distinctDirs =
            newDirs.filter { showFavoritesBin || (!it.isRecycleBin() && !it.areFavorites()) }
                .distinctBy { it.path.getDistinctPath() }
                .toMutableList() as ArrayList<Directory>
        val sortedDirs = activity.getSortedDirectories(distinctDirs)
        val dirs = activity.getDirsToShow(sortedDirs, allDirectories, currentPathPrefix)
            .clone() as ArrayList<Directory>
        if (dirs.hashCode() == shownDirectories.hashCode()) {
            return
        }

        shownDirectories = dirs
        val adapter = DirectoryAdapter(
            activity,
            dirs.clone() as ArrayList<Directory>,
            null,
            binding.directoriesGrid,
            true
        ) {
            val clickedDir = it as Directory
            val path = clickedDir.path
            if (clickedDir.subfoldersCount == 1 || !activity.config.groupDirectSubfolders) {
                if (isPickingCopyMoveDestination && path.trimEnd('/') == sourcePath) {
                    activity.toast(R.string.source_and_destination_same)
                    return@DirectoryAdapter
                } else if (isPickingCopyMoveDestination && activity.isRestrictedWithSAFSdk30(path) && !activity.isInDownloadDir(
                        path
                    )
                ) {
                    activity.toast(
                        R.string.system_folder_copy_restriction,
                        Toast.LENGTH_LONG
                    )
                    return@DirectoryAdapter
                } else {
                    activity.handleLockedFolderOpening(path) { success ->
                        if (success) {
                            callback(path)
                        }
                    }
                    dialog?.dismiss()
                }
            } else {
                currentPathPrefix = path
                openedSubfolders.add(path)
                gotDirectories(allDirectories)
            }
        }

        val scrollHorizontally = activity.config.scrollHorizontally && isGridViewType
        binding.apply {
            directoriesGrid.adapter = adapter
            directoriesFastscroller.setScrollVertically(!scrollHorizontally)
        }
    }

    private fun backPressed() {
        if (searchView.isSearchOpen) {
            searchView.closeSearch()
        } else if (activity.config.groupDirectSubfolders) {
            if (currentPathPrefix.isEmpty()) {
                dialog?.dismiss()
            } else {
                openedSubfolders.removeLast()
                currentPathPrefix = openedSubfolders.last()
                gotDirectories(allDirectories)
            }
        } else {
            dialog?.dismiss()
        }
    }
}
