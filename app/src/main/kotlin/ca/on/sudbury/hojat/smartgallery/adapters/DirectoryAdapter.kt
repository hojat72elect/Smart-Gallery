package ca.on.sudbury.hojat.smartgallery.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Icon
import android.os.Build
import android.text.TextUtils
import android.view.Menu
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import ca.on.sudbury.hojat.smartgallery.R
import com.bumptech.glide.Glide
import com.google.gson.Gson
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.extensions.isVideoFast
import ca.on.sudbury.hojat.smartgallery.extensions.isRawFast
import ca.on.sudbury.hojat.smartgallery.extensions.getFilenameFromPath
import ca.on.sudbury.hojat.smartgallery.extensions.rescanPaths
import ca.on.sudbury.hojat.smartgallery.extensions.handleLockedFolderOpening
import ca.on.sudbury.hojat.smartgallery.extensions.getProperBackgroundColor
import ca.on.sudbury.hojat.smartgallery.extensions.isExternalStorageManager
import ca.on.sudbury.hojat.smartgallery.extensions.doesThisOrParentHaveNoMedia
import ca.on.sudbury.hojat.smartgallery.extensions.isMediaFile
import ca.on.sudbury.hojat.smartgallery.extensions.getContrastColor
import ca.on.sudbury.hojat.smartgallery.extensions.isAStorageRootFolder
import ca.on.sudbury.hojat.smartgallery.extensions.handleDeletePasswordProtection
import ca.on.sudbury.hojat.smartgallery.helpers.FAVORITES
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_BY_CUSTOM
import ca.on.sudbury.hojat.smartgallery.helpers.SHOW_ALL_TABS
import ca.on.sudbury.hojat.smartgallery.interfaces.ItemMoveCallback
import ca.on.sudbury.hojat.smartgallery.interfaces.ItemTouchHelperContract
import ca.on.sudbury.hojat.smartgallery.interfaces.StartReorderDragListener
import ca.on.sudbury.hojat.smartgallery.models.FileDirItem
import ca.on.sudbury.hojat.smartgallery.views.MyRecyclerView
import ca.on.sudbury.hojat.smartgallery.activities.MediaActivity
import ca.on.sudbury.hojat.smartgallery.database.DirectoryOperationsListener
import ca.on.sudbury.hojat.smartgallery.extensions.showRecycleBinEmptyingDialog
import ca.on.sudbury.hojat.smartgallery.extensions.emptyAndDisableTheRecycleBin
import ca.on.sudbury.hojat.smartgallery.extensions.checkAppendingHidden
import ca.on.sudbury.hojat.smartgallery.extensions.addNoMedia
import ca.on.sudbury.hojat.smartgallery.extensions.tryCopyMoveFilesTo
import ca.on.sudbury.hojat.smartgallery.extensions.fixDateTaken
import ca.on.sudbury.hojat.smartgallery.extensions.getShortcutImage
import ca.on.sudbury.hojat.smartgallery.extensions.mediaDB
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.extensions.loadImage
import ca.on.sudbury.hojat.smartgallery.extensions.removeNoMedia
import ca.on.sudbury.hojat.smartgallery.helpers.RECYCLE_BIN
import ca.on.sudbury.hojat.smartgallery.helpers.DIRECTORY
import ca.on.sudbury.hojat.smartgallery.helpers.ROUNDED_CORNERS_SMALL
import ca.on.sudbury.hojat.smartgallery.helpers.ROUNDED_CORNERS_NONE
import ca.on.sudbury.hojat.smartgallery.helpers.ROUNDED_CORNERS_BIG
import ca.on.sudbury.hojat.smartgallery.helpers.PATH
import ca.on.sudbury.hojat.smartgallery.models.AlbumCover
import ca.on.sudbury.hojat.smartgallery.models.Directory
import ca.on.hojat.palette.recyclerviewfastscroller.RecyclerViewFastScroller
import ca.on.sudbury.hojat.smartgallery.databases.GalleryDatabase
import ca.on.sudbury.hojat.smartgallery.dialogs.ConfirmationDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.DeleteFolderDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.ExcludeFolderDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.FolderLockingNoticeDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.PickMediumDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.RenameItemDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.RenameItemsDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.SecurityDialogFragment
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig
import ca.on.sudbury.hojat.smartgallery.helpers.FileLocation
import ca.on.sudbury.hojat.smartgallery.helpers.FolderMediaCount
import ca.on.sudbury.hojat.smartgallery.helpers.FolderStyle
import ca.on.sudbury.hojat.smartgallery.helpers.MediaType
import ca.on.sudbury.hojat.smartgallery.helpers.SmartGalleryTimeFormat
import ca.on.sudbury.hojat.smartgallery.helpers.ViewType
import ca.on.sudbury.hojat.smartgallery.usecases.IsOreoPlusUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsRPlusUseCase
import ca.on.sudbury.hojat.smartgallery.repositories.SupportedExtensionsRepository
import ca.on.sudbury.hojat.smartgallery.usecases.ApplyColorFilterUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.BeVisibleOrGoneUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.ConvertDrawableToBitmapUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.DoesContainNoMediaUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.EmptyTheRecycleBinUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsGifUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsSvgUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.RunOnBackgroundThreadUseCase
import kotlinx.android.synthetic.main.directory_item_grid_square.view.*
import kotlinx.android.synthetic.main.directory_item_grid_square.view.dir_check
import kotlinx.android.synthetic.main.directory_item_grid_square.view.dir_location
import kotlinx.android.synthetic.main.directory_item_grid_square.view.dir_lock
import kotlinx.android.synthetic.main.directory_item_grid_square.view.dir_name
import kotlinx.android.synthetic.main.directory_item_grid_square.view.dir_pin
import kotlinx.android.synthetic.main.directory_item_grid_square.view.dir_thumbnail
import kotlinx.android.synthetic.main.directory_item_list.view.*
import kotlinx.android.synthetic.main.directory_item_list.view.dir_drag_handle
import kotlinx.android.synthetic.main.directory_item_list.view.dir_holder
import kotlinx.android.synthetic.main.directory_item_list.view.photo_cnt
import java.io.File
import java.util.Collections
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class DirectoryAdapter(
    activity: BaseSimpleActivity,
    var dirs: ArrayList<Directory>,
    val listener: DirectoryOperationsListener?,
    recyclerView: MyRecyclerView,
    private val isPickIntent: Boolean,
    private val swipeRefreshLayout: SwipeRefreshLayout? = null,
    itemClick: (Any) -> Unit
) :
    MyRecyclerViewAdapter(activity, recyclerView, itemClick), ItemTouchHelperContract,
    RecyclerViewFastScroller.OnPopupTextUpdate {

    private val config = activity.config
    private val isListViewType = config.viewTypeFolders == ViewType.List.id
    private var pinnedFolders = config.pinnedFolders
    private var scrollHorizontally = config.scrollHorizontally
    private var animateGifs = config.animateGifs
    private var cropThumbnails = config.cropThumbnails
    private var groupDirectSubfolders = config.groupDirectSubfolders
    private var currentDirectoriesHash = dirs.hashCode()
    private var lockedFolderPaths = ArrayList<String>()
    private var isDragAndDropping = false
    private var startReorderDragListener: StartReorderDragListener? = null

    private var showMediaCount = config.showFolderMediaCount
    private var folderStyle = config.folderStyle
    private var limitFolderTitle = config.limitFolderTitle
    var directorySorting = config.directorySorting
    var dateFormat = config.dateFormat
    var timeFormat =
        with(activity) { if (baseConfig.use24HourFormat) SmartGalleryTimeFormat.FullDay.format else SmartGalleryTimeFormat.HalfDay.format }

    init {
        setupDragListener(true)
        fillLockedFolders()
    }

    override fun getActionMenuId() = R.menu.cab_directories

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutType = when {
            isListViewType -> R.layout.directory_item_list
            folderStyle == FolderStyle.Square.id -> R.layout.directory_item_grid_square
            else -> R.layout.directory_item_grid_rounded_corners
        }

        return createViewHolder(layoutType, parent)
    }

    override fun onBindViewHolder(holder: MyRecyclerViewAdapter.ViewHolder, position: Int) {
        val dir = dirs.getOrNull(position) ?: return
        holder.bindView(dir, true, !isPickIntent) { itemView, _ ->
            setupView(itemView, dir, holder)
        }
        bindViewHolder(holder)
    }

    override fun getItemCount() = dirs.size

    override fun prepareActionMode(menu: Menu) {
        val selectedPaths = getSelectedPaths()
        if (selectedPaths.isEmpty()) {
            return
        }

        val isOneItemSelected = isOneItemSelected()
        menu.apply {
            findItem(R.id.cab_move_to_top).isVisible = isDragAndDropping
            findItem(R.id.cab_move_to_bottom).isVisible = isDragAndDropping

            findItem(R.id.cab_rename).isVisible =
                !selectedPaths.contains(FAVORITES) && !selectedPaths.contains(RECYCLE_BIN)
            findItem(R.id.cab_change_cover_image).isVisible = isOneItemSelected

            findItem(R.id.cab_lock).isVisible = selectedPaths.any { !config.isFolderProtected(it) }
            findItem(R.id.cab_unlock).isVisible = selectedPaths.any { config.isFolderProtected(it) }

            findItem(R.id.cab_empty_recycle_bin).isVisible =
                isOneItemSelected && selectedPaths.first() == RECYCLE_BIN
            findItem(R.id.cab_empty_disable_recycle_bin).isVisible =
                isOneItemSelected && selectedPaths.first() == RECYCLE_BIN

            findItem(R.id.cab_create_shortcut).isVisible = IsOreoPlusUseCase() && isOneItemSelected

            checkHideBtnVisibility(this, selectedPaths)
            checkPinBtnVisibility(this, selectedPaths)
        }
    }

    override fun actionItemPressed(id: Int) {
        if (selectedKeys.isEmpty()) {
            return
        }

        when (id) {
            R.id.cab_move_to_top -> moveSelectedItemsToTop()
            R.id.cab_move_to_bottom -> moveSelectedItemsToBottom()
            R.id.cab_properties -> showProperties()
            R.id.cab_rename -> renameDir()
            R.id.cab_pin -> pinFolders(true)
            R.id.cab_unpin -> pinFolders(false)
            R.id.cab_change_order -> changeOrder()
            R.id.cab_empty_recycle_bin -> tryEmptyRecycleBin(true)
            R.id.cab_empty_disable_recycle_bin -> emptyAndDisableRecycleBin()
            R.id.cab_hide -> toggleFoldersVisibility(true)
            R.id.cab_unhide -> toggleFoldersVisibility(false)
            R.id.cab_exclude -> tryExcludeFolder()
            R.id.cab_lock -> tryLockFolder()
            R.id.cab_unlock -> unlockFolder()
            R.id.cab_copy_to -> copyMoveTo(true)
            R.id.cab_move_to -> moveFilesTo()
            R.id.cab_select_all -> selectAll()
            R.id.cab_create_shortcut -> tryCreateShortcut()
            R.id.cab_delete -> askConfirmDelete()
            R.id.cab_select_photo -> tryChangeAlbumCover(false)
            R.id.cab_use_default -> tryChangeAlbumCover(true)
        }
    }

    override fun getSelectableItemCount() = dirs.size

    override fun getIsItemSelectable(position: Int) = true

    override fun getItemSelectionKey(position: Int) = dirs.getOrNull(position)?.path?.hashCode()

    override fun getItemKeyPosition(key: Int) = dirs.indexOfFirst { it.path.hashCode() == key }

    override fun onActionModeCreated() {}

    @SuppressLint("NotifyDataSetChanged")
    override fun onActionModeDestroyed() {
        if (isDragAndDropping) {
            notifyDataSetChanged()

            val reorderedFoldersList = dirs.map { it.path }
            config.customFoldersOrder = TextUtils.join("|||", reorderedFoldersList)
            config.directorySorting = SORT_BY_CUSTOM
        }

        isDragAndDropping = false
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        if (!activity.isDestroyed) {
            Glide.with(activity).clear(holder.itemView.dir_thumbnail!!)
        }
    }

    private fun checkHideBtnVisibility(menu: Menu, selectedPaths: ArrayList<String>) {
        menu.findItem(R.id.cab_hide).isVisible =
            (!IsRPlusUseCase() || isExternalStorageManager()) && selectedPaths.any {
                !it.doesThisOrParentHaveNoMedia(
                    HashMap(),
                    null
                )
            }

        menu.findItem(R.id.cab_unhide).isVisible =
            (!IsRPlusUseCase() || isExternalStorageManager()) && selectedPaths.any {
                it.doesThisOrParentHaveNoMedia(
                    HashMap(),
                    null
                )
            }
    }

    private fun checkPinBtnVisibility(menu: Menu, selectedPaths: ArrayList<String>) {
        val pinnedFolders = config.pinnedFolders
        menu.findItem(R.id.cab_pin).isVisible = selectedPaths.any { !pinnedFolders.contains(it) }
        menu.findItem(R.id.cab_unpin).isVisible = selectedPaths.any { pinnedFolders.contains(it) }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun moveSelectedItemsToTop() {
        selectedKeys.reversed().forEach { key ->
            val position = dirs.indexOfFirst { it.path.hashCode() == key }
            val tempItem = dirs[position]
            dirs.removeAt(position)
            dirs.add(0, tempItem)
        }

        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun moveSelectedItemsToBottom() {
        selectedKeys.forEach { key ->
            val position = dirs.indexOfFirst { it.path.hashCode() == key }
            val tempItem = dirs[position]
            dirs.removeAt(position)
            dirs.add(dirs.size, tempItem)
        }

        notifyDataSetChanged()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun showProperties() {
        if (selectedKeys.size <= 1) {
            val path = getFirstSelectedItemPath() ?: return
            if (path != FAVORITES && path != RECYCLE_BIN) {
                activity.handleLockedFolderOpening(path) { success ->
                    if (success) {
                        Toast.makeText(
                            activity,
                            "I had to remove this feature.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        } else {
            Toast.makeText(
                activity,
                "I had to remove this feature.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun renameDir() {
        if (selectedKeys.size == 1) {
            val firstDir = getFirstSelectedItem() ?: return
            val sourcePath = firstDir.path
            val dir = File(sourcePath)
            if (activity.isAStorageRootFolder(dir.absolutePath)) {
                Toast.makeText(activity, R.string.rename_folder_root, Toast.LENGTH_LONG).show()
                return
            }

            activity.handleLockedFolderOpening(sourcePath) { success ->
                if (success) {
                    val callback: (String) -> Unit = { newPath ->
                        activity.runOnUiThread {
                            firstDir.apply {
                                path = newPath
                                name = newPath.getFilenameFromPath()
                                tmb = File(newPath, tmb.getFilenameFromPath()).absolutePath
                            }
                            updateDirs(dirs)
                            RunOnBackgroundThreadUseCase {
                                try {
                                    GalleryDatabase.getInstance(activity.applicationContext)
                                        .DirectoryDao().updateDirectoryAfterRename(
                                            firstDir.tmb,
                                            firstDir.name,
                                            firstDir.path,
                                            sourcePath
                                        )
                                    listener?.refreshItems()
                                } catch (e: Exception) {
                                    Toast.makeText(activity, e.toString(), Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                    RenameItemDialogFragment(dir.absolutePath, callback).show(
                        activity.supportFragmentManager,
                        RenameItemDialogFragment.TAG
                    )
                }
            }
        } else {
            val paths = getSelectedPaths().filter {
                !activity.isAStorageRootFolder(it) && !config.isFolderProtected(it)
            } as ArrayList<String>
            val callback: () -> Unit = { listener?.refreshItems() }
            RenameItemsDialogFragment(paths, callback).show(
                activity.supportFragmentManager,
                RenameItemsDialogFragment.TAG
            )
        }
    }

    private fun toggleFoldersVisibility(hide: Boolean) {
        val selectedPaths = getSelectedPaths()
        if (hide && selectedPaths.contains(RECYCLE_BIN)) {
            config.showRecycleBinAtFolders = false
            if (selectedPaths.size == 1) {
                listener?.refreshItems()
                finishActMode()
            }
        }

        if (hide) {
            if (config.wasHideFolderTooltipShown) {
                hideFolders(selectedPaths)
            } else {
                config.wasHideFolderTooltipShown = true
                val callback: () -> Unit = {
                    hideFolders(selectedPaths)
                }
                ConfirmationDialogFragment(
                    message = activity.getString(R.string.hide_folder_description),
                    callbackAfterDialogConfirmed = callback
                ).show(activity.supportFragmentManager, ConfirmationDialogFragment.TAG)
            }
        } else {
            selectedPaths.filter {
                it != FAVORITES && it != RECYCLE_BIN && (selectedPaths.size == 1 || !config.isFolderProtected(
                    it
                ))
            }.forEach {
                val path = it
                activity.handleLockedFolderOpening(path) { success ->
                    if (success) {
                        if (DoesContainNoMediaUseCase(path)) {
                            activity.removeNoMedia(path) {
                                if (config.shouldShowHidden) {
                                    updateFolderNames()
                                } else {
                                    activity.runOnUiThread {
                                        listener?.refreshItems()
                                        finishActMode()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun hideFolders(paths: ArrayList<String>) {
        for (path in paths) {
            activity.handleLockedFolderOpening(path) { success ->
                if (success) {
                    hideFolder(path)
                }
            }
        }
    }

    private fun tryEmptyRecycleBin(askConfirmation: Boolean) {
        if (askConfirmation) {
            activity.showRecycleBinEmptyingDialog {
                emptyRecycleBin()
            }
        } else {
            emptyRecycleBin()
        }
    }

    private fun emptyRecycleBin() {
        activity.handleLockedFolderOpening(RECYCLE_BIN) { success ->
            if (success) {
                EmptyTheRecycleBinUseCase(activity) {
                    listener?.refreshItems()
                }
            }
        }
    }

    private fun emptyAndDisableRecycleBin() {
        activity.handleLockedFolderOpening(RECYCLE_BIN) { success ->
            if (success) {
                activity.showRecycleBinEmptyingDialog {
                    activity.emptyAndDisableTheRecycleBin {
                        listener?.refreshItems()
                    }
                }
            }
        }
    }

    private fun updateFolderNames() {
        val includedFolders = config.includedFolders
        val hidden = activity.getString(R.string.hidden)
        dirs.forEach {
            it.name = activity.checkAppendingHidden(it.path, hidden, includedFolders, ArrayList())
        }
        listener?.updateDirectories(dirs.toMutableList() as ArrayList)
        activity.runOnUiThread {
            updateDirs(dirs)
        }
    }

    private fun hideFolder(path: String) {
        activity.addNoMedia(path) {
            if (config.shouldShowHidden) {
                updateFolderNames()
            } else {
                val affectedPositions = ArrayList<Int>()
                val includedFolders = config.includedFolders
                val newDirs = dirs.filterIndexed { index, directory ->
                    val removeDir = directory.path.doesThisOrParentHaveNoMedia(
                        HashMap(),
                        null
                    ) && !includedFolders.contains(directory.path)
                    if (removeDir) {
                        affectedPositions.add(index)
                    }
                    !removeDir
                } as ArrayList<Directory>

                activity.runOnUiThread {
                    affectedPositions.sortedDescending().forEach {
                        notifyItemRemoved(it)
                    }

                    currentDirectoriesHash = newDirs.hashCode()
                    dirs = newDirs

                    finishActMode()
                    listener?.updateDirectories(newDirs)
                }
            }
        }
    }

    private fun tryExcludeFolder() {
        val selectedPaths = getSelectedPaths()
        val paths =
            selectedPaths.filter { it != PATH && it != RECYCLE_BIN && it != FAVORITES }.toSet()
        if (selectedPaths.contains(RECYCLE_BIN)) {
            config.showRecycleBinAtFolders = false
            if (selectedPaths.size == 1) {
                listener?.refreshItems()
                finishActMode()
            }
        }

        if (paths.size == 1) {
            val callbackAfterDialogConfirmed = {
                listener?.refreshItems()
                finishActMode()
            }
            ExcludeFolderDialogFragment(paths.toMutableList(), callbackAfterDialogConfirmed).show(
                activity.supportFragmentManager, ExcludeFolderDialogFragment.TAG
            )
        } else if (paths.size > 1) {
            config.addExcludedFolders(paths)
            listener?.refreshItems()
            finishActMode()
        }
    }

    private fun tryLockFolder() {
        if (config.wasFolderLockingNoticeShown) {
            lockFolder()
        } else {
            val callback = { lockFolder() }
            FolderLockingNoticeDialogFragment(callback).show(
                activity.supportFragmentManager,
                FolderLockingNoticeDialogFragment.TAG
            )
        }
    }

    private fun lockFolder() {
        val callback: (hash: String, type: Int, success: Boolean) -> Unit = { hash, type, success ->
            if (success) {
                getSelectedPaths().filter { !config.isFolderProtected(it) }.forEach {
                    config.addFolderProtection(it, hash, type)
                    lockedFolderPaths.add(it)
                }

                listener?.refreshItems()
                finishActMode()
            }
        }
        SecurityDialogFragment("", SHOW_ALL_TABS, callback).show(
            activity.supportFragmentManager,
            SecurityDialogFragment.TAG
        )
    }

    private fun unlockFolder() {
        val paths = getSelectedPaths()
        val firstPath = paths.first()
        val tabToShow = config.getFolderProtectionType(firstPath)
        val hashToCheck = config.getFolderProtectionHash(firstPath)

        val callback: (hash: String, type: Int, success: Boolean) -> Unit = { _, _, success ->
            if (success) {
                paths.filter {
                    config.isFolderProtected(it) && config.getFolderProtectionType(it) == tabToShow && config.getFolderProtectionHash(
                        it
                    ) == hashToCheck
                }
                    .forEach {
                        config.removeFolderProtection(it)
                        lockedFolderPaths.remove(it)
                    }

                listener?.refreshItems()
                finishActMode()
            }
        }
        SecurityDialogFragment(hashToCheck, tabToShow, callback).show(
            activity.supportFragmentManager,
            SecurityDialogFragment.TAG
        )

    }

    private fun pinFolders(pin: Boolean) {
        if (pin) {
            config.addPinnedFolders(getSelectedPaths().toHashSet())
        } else {
            config.removePinnedFolders(getSelectedPaths().toHashSet())
        }

        currentDirectoriesHash = 0
        pinnedFolders = config.pinnedFolders
        listener?.recheckPinnedFolders()
    }

    private fun changeOrder() {
        isDragAndDropping = true
        notifyDataSetChanged()
        actMode?.invalidate()

        if (startReorderDragListener == null) {
            val touchHelper = ItemTouchHelper(ItemMoveCallback(this, true))
            touchHelper.attachToRecyclerView(recyclerView)

            startReorderDragListener = object : StartReorderDragListener {
                override fun requestDrag(viewHolder: RecyclerView.ViewHolder) {
                    touchHelper.startDrag(viewHolder)
                }
            }
        }
    }

    private fun moveFilesTo() {
        activity.handleDeletePasswordProtection {
            copyMoveTo(false)
        }
    }

    private fun copyMoveTo(isCopyOperation: Boolean) {
        val paths = ArrayList<String>()
        val showHidden = config.shouldShowHidden
        getSelectedPaths().forEach { selectedPathString ->
            val filter = config.filterMedia
            File(selectedPathString).listFiles()?.filter { fileInSelectedPath ->
                !File(fileInSelectedPath.absolutePath).isDirectory &&
                        fileInSelectedPath.absolutePath.isMediaFile() && (showHidden || !fileInSelectedPath.name.startsWith(
                    '.'
                )) &&
                        ((SupportedExtensionsRepository.photoExtensions.any { extension ->
                            fileInSelectedPath.absolutePath.endsWith(extension, true)
                        } && filter and MediaType.Image.id != 0) ||
                                (SupportedExtensionsRepository.videoExtensions.any { extension ->
                                    fileInSelectedPath.absolutePath.endsWith(extension, true)
                                } && filter and MediaType.Video.id != 0) ||
                                (fileInSelectedPath.absolutePath.endsWith(
                                    ".gif",
                                    true
                                ) && filter and MediaType.Gif.id != 0) ||
                                (SupportedExtensionsRepository.rawExtensions.any { extension ->
                                    fileInSelectedPath.absolutePath.endsWith(extension, true)
                                } && filter and MediaType.Raw.id != 0) ||
                                (IsSvgUseCase(fileInSelectedPath.absolutePath) && filter and MediaType.Svg.id != 0))
            }?.mapTo(paths) { it.absolutePath }
        }

        val fileDirItems =
            paths.map { FileDirItem(it, it.getFilenameFromPath()) } as ArrayList<FileDirItem>
        activity.tryCopyMoveFilesTo(fileDirItems, isCopyOperation) { destinationPath ->
            val newPaths = fileDirItems.map { "$destinationPath/${it.name}" }
                .toMutableList() as ArrayList<String>
            activity.applicationContext.rescanPaths(newPaths) {
                activity.fixDateTaken(newPaths, false)
            }

            config.tempFolderPath = ""
            listener?.refreshItems()
            finishActMode()
        }
    }

    private fun tryCreateShortcut() {
        if (!IsOreoPlusUseCase()) {
            return
        }

        activity.handleLockedFolderOpening(getFirstSelectedItemPath() ?: "") { success ->
            if (success) {
                createShortcut()
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createShortcut() {
        val manager = activity.getSystemService(ShortcutManager::class.java)
        if (manager.isRequestPinShortcutSupported) {
            val dir = getFirstSelectedItem() ?: return
            val path = dir.path
            val drawable = resources.getDrawable(R.drawable.shortcut_image).mutate()
            val coverThumbnail =
                config.parseAlbumCovers().firstOrNull { it.thumbnail == dir.path }?.thumbnail
                    ?: dir.tmb
            activity.getShortcutImage(coverThumbnail, drawable) {
                val intent = Intent(activity, MediaActivity::class.java)
                intent.action = Intent.ACTION_VIEW
                intent.flags =
                    intent.flags or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.putExtra(DIRECTORY, path)

                val shortcut = ShortcutInfo.Builder(activity, path)
                    .setShortLabel(dir.name)
                    .setIcon(Icon.createWithBitmap(ConvertDrawableToBitmapUseCase(drawable)))
                    .setIntent(intent)
                    .build()

                manager.requestPinShortcut(shortcut, null)
            }
        }
    }

    private fun askConfirmDelete() {
        when {
            config.isDeletePasswordProtectionOn -> activity.handleDeletePasswordProtection {
                deleteFolders()
            }
            config.skipDeleteConfirmation -> deleteFolders()
            else -> {
                val itemsCnt = selectedKeys.size
                if (itemsCnt == 1 && getSelectedItems().first().isRecycleBin()) {
                    val callback = {
                        deleteFolders()
                    }
                    ConfirmationDialogFragment(
                        message = "",
                        messageId = R.string.empty_recycle_bin_confirmation,
                        positive = R.string.yes,
                        negative = R.string.no,
                        callbackAfterDialogConfirmed = callback
                    ).show(activity.supportFragmentManager, ConfirmationDialogFragment.TAG)
                    return
                }

                val items = if (itemsCnt == 1) {
                    val folder = getSelectedPaths().first().getFilenameFromPath()
                    "\"$folder\""
                } else {
                    resources.getQuantityString(R.plurals.delete_items, itemsCnt, itemsCnt)
                }

                val fileDirItem = getFirstSelectedItem() ?: return
                val baseString =
                    if (!config.useRecycleBin || (isOneItemSelected() && fileDirItem.areFavorites())) {
                        R.string.deletion_confirmation
                    } else {
                        R.string.move_to_recycle_bin_confirmation
                    }

                val question = String.format(resources.getString(baseString), items)
                val warning =
                    resources.getQuantityString(R.plurals.delete_warning, itemsCnt, itemsCnt)

                val callbackAfterConfirmDeleteFolder = { deleteFolders() }
                DeleteFolderDialogFragment(
                    question,
                    warning,
                    callbackAfterConfirmDeleteFolder
                ).show(
                    activity.supportFragmentManager,
                    DeleteFolderDialogFragment.TAG
                )
            }
        }
    }

    private fun deleteFolders() {
        if (selectedKeys.isEmpty()) {
            return
        }

        val SAFPath = getFirstSelectedItemPath() ?: return
        val selectedDirs = getSelectedItems()
        activity.handleSAFDialog(SAFPath) { it ->
            if (!it) {
                return@handleSAFDialog
            }

            activity.handleSAFDialogSdk30(SAFPath) {
                if (!it) {
                    return@handleSAFDialogSdk30
                }

                var foldersToDelete = ArrayList<File>(selectedKeys.size)
                selectedDirs.forEach { specifiedDirectory ->
                    if (specifiedDirectory.areFavorites() || specifiedDirectory.isRecycleBin()) {
                        if (specifiedDirectory.isRecycleBin()) {
                            tryEmptyRecycleBin(false)
                        } else {
                            RunOnBackgroundThreadUseCase {
                                activity.mediaDB.clearFavorites()
                                GalleryDatabase.getInstance(activity.applicationContext)
                                    .FavoritesDao().clearFavorites()
                                listener?.refreshItems()
                            }
                        }

                        if (selectedKeys.size == 1) {
                            finishActMode()
                        }
                    } else {
                        foldersToDelete.add(File(specifiedDirectory.path))
                    }
                }

                if (foldersToDelete.size == 1) {
                    activity.handleLockedFolderOpening(foldersToDelete.first().absolutePath) { success ->
                        if (success) {
                            listener?.deleteFolders(foldersToDelete)
                        }
                    }
                } else {
                    foldersToDelete =
                        foldersToDelete.filter { folderToBeDeleted ->
                            !config.isFolderProtected(
                                folderToBeDeleted.absolutePath
                            )
                        }
                            .toMutableList() as ArrayList<File>
                    listener?.deleteFolders(foldersToDelete)
                }
            }
        }
    }

    private fun tryChangeAlbumCover(useDefault: Boolean) {
        activity.handleLockedFolderOpening(getFirstSelectedItemPath() ?: "") { success ->
            if (success) {
                changeAlbumCover(useDefault)
            }
        }
    }

    private fun changeAlbumCover(useDefault: Boolean) {
        if (selectedKeys.size != 1)
            return

        val path = getFirstSelectedItemPath() ?: return

        if (useDefault) {
            val albumCovers = getAlbumCoversWithout(path)
            storeCovers(albumCovers)
        } else {
            pickMediumFrom(path, path)
        }
    }

    private fun pickMediumFrom(targetFolder: String, path: String) {
        val callback: (String) -> Unit = { selectedPath ->
            if (File(selectedPath).isDirectory) {
                pickMediumFrom(targetFolder, selectedPath)
            } else {
                val albumCovers = getAlbumCoversWithout(selectedPath)
                val cover = AlbumCover(targetFolder, selectedPath)
                albumCovers.add(cover)
                storeCovers(albumCovers)
            }
        }
        PickMediumDialogFragment(path, callback).show(
            activity.supportFragmentManager, PickMediumDialogFragment.TAG
        )
    }

    private fun getAlbumCoversWithout(path: String) =
        config.parseAlbumCovers().filterNot { it.path == path } as ArrayList

    private fun storeCovers(albumCovers: ArrayList<AlbumCover>) {
        config.albumCovers = Gson().toJson(albumCovers)
        finishActMode()
        listener?.refreshItems()
    }

    private fun getSelectedItems() =
        selectedKeys.mapNotNull { getItemWithKey(it) } as ArrayList<Directory>

    private fun getSelectedPaths() = getSelectedItems().map { it.path } as ArrayList<String>

    private fun getFirstSelectedItem() = getItemWithKey(selectedKeys.first())

    private fun getFirstSelectedItemPath() = getFirstSelectedItem()?.path

    private fun getItemWithKey(key: Int): Directory? =
        dirs.firstOrNull { it.path.hashCode() == key }

    private fun fillLockedFolders() {
        lockedFolderPaths.clear()
        dirs.map { it.path }.filter { config.isFolderProtected(it) }.forEach {
            lockedFolderPaths.add(it)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateDirs(newDirs: ArrayList<Directory>) {
        val directories = newDirs.clone() as ArrayList<Directory>
        if (directories.hashCode() != currentDirectoriesHash) {
            currentDirectoriesHash = directories.hashCode()
            dirs = directories
            fillLockedFolders()
            notifyDataSetChanged()
            finishActMode()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAnimateGifs(animateGifs: Boolean) {
        this.animateGifs = animateGifs
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateCropThumbnails(cropThumbnails: Boolean) {
        this.cropThumbnails = cropThumbnails
        notifyDataSetChanged()
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    private fun setupView(view: View, directory: Directory, holder: ViewHolder) {
        val isSelected = selectedKeys.contains(directory.path.hashCode())
        view.apply {
            dir_path?.text = "${directory.path.substringBeforeLast("/")}/"
            val thumbnailType = when {
                directory.tmb.isVideoFast() -> MediaType.Video.id
                IsGifUseCase(directory.tmb) -> MediaType.Gif.id
                directory.tmb.isRawFast() -> MediaType.Raw.id
                IsSvgUseCase(directory.tmb) -> MediaType.Svg.id
                else -> MediaType.Image.id
            }

            BeVisibleOrGoneUseCase(dir_check, isSelected)
            if (isSelected) {
                ApplyColorFilterUseCase(dir_check.background, properPrimaryColor)
                ApplyColorFilterUseCase(dir_check, contrastColor)
            }

            if (isListViewType) {
                dir_holder.isSelected = isSelected
            }

            if (scrollHorizontally && !isListViewType && folderStyle == FolderStyle.RoundedCorners.id) {
                (dir_thumbnail.layoutParams as RelativeLayout.LayoutParams).addRule(
                    RelativeLayout.ABOVE,
                    dir_name.id
                )

                val photoCntParams = (photo_cnt.layoutParams as RelativeLayout.LayoutParams)
                val nameParams = (dir_name.layoutParams as RelativeLayout.LayoutParams)
                nameParams.removeRule(RelativeLayout.BELOW)

                if (config.showFolderMediaCount == FolderMediaCount.SeparateLine.id) {
                    nameParams.addRule(RelativeLayout.ABOVE, photo_cnt.id)
                    nameParams.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM)

                    photoCntParams.removeRule(RelativeLayout.BELOW)
                    photoCntParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                } else {
                    nameParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                }
            }

            if (lockedFolderPaths.contains(directory.path)) {
                dir_lock.visibility = View.VISIBLE
                dir_lock.background = ColorDrawable(context.getProperBackgroundColor())
                ApplyColorFilterUseCase(
                    dir_lock,
                    context.getProperBackgroundColor().getContrastColor()
                )
            } else {
                dir_lock.visibility = View.GONE
                val roundedCorners = when {
                    isListViewType -> ROUNDED_CORNERS_SMALL
                    folderStyle == FolderStyle.Square.id -> ROUNDED_CORNERS_NONE
                    else -> ROUNDED_CORNERS_BIG
                }

                activity.loadImage(
                    thumbnailType,
                    directory.tmb,
                    dir_thumbnail,
                    scrollHorizontally,
                    animateGifs,
                    cropThumbnails,
                    roundedCorners,
                    directory.getKey()
                )
            }

            BeVisibleOrGoneUseCase(dir_pin, pinnedFolders.contains(directory.path))
            BeVisibleOrGoneUseCase(dir_location, directory.location != FileLocation.Internal.id)

            if (dir_location.visibility == View.VISIBLE) {
                dir_location.setImageResource(if (directory.location == FileLocation.SdCard.id) R.drawable.ic_sd_card_vector else R.drawable.ic_usb_vector)
            }

            photo_cnt.text = directory.subfoldersMediaCount.toString()
            BeVisibleOrGoneUseCase(photo_cnt, showMediaCount == FolderMediaCount.SeparateLine.id)

            if (limitFolderTitle) {
                dir_name.setSingleLine()
                dir_name.ellipsize = TextUtils.TruncateAt.MIDDLE
            }

            var nameCount = directory.name
            if (showMediaCount == FolderMediaCount.Brackets.id) {
                nameCount += " (${directory.subfoldersMediaCount})"
            }

            if (groupDirectSubfolders) {
                if (directory.subfoldersCount > 1) {
                    nameCount += " [${directory.subfoldersCount}]"
                }
            }

            dir_name.text = nameCount

            if (isListViewType || folderStyle == FolderStyle.RoundedCorners.id) {
                photo_cnt.setTextColor(textColor)
                dir_name.setTextColor(textColor)
                ApplyColorFilterUseCase(dir_location, textColor)
            }

            if (isListViewType) {
                dir_path.setTextColor(textColor)
                ApplyColorFilterUseCase(dir_pin, textColor)
                ApplyColorFilterUseCase(dir_location, textColor)
                BeVisibleOrGoneUseCase(dir_drag_handle, isDragAndDropping)
            } else {
                BeVisibleOrGoneUseCase(dir_drag_handle_wrapper, isDragAndDropping)
            }

            if (isDragAndDropping) {
                ApplyColorFilterUseCase(dir_drag_handle, textColor)
                dir_drag_handle.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        startReorderDragListener?.requestDrag(holder)
                    }
                    false
                }
            }
        }
    }

    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(dirs, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(dirs, i, i - 1)
            }
        }

        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onRowSelected(myViewHolder: ViewHolder?) {
        swipeRefreshLayout?.isEnabled = false
    }

    override fun onRowClear(myViewHolder: ViewHolder?) {
        swipeRefreshLayout?.isEnabled = activity.config.enablePullToRefresh
    }

    override fun onChange(position: Int) =
        dirs.getOrNull(position)?.getBubbleText(directorySorting, activity, dateFormat, timeFormat)
            ?: ""
}
