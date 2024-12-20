package ca.hojat.smart.gallery.shared.ui.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Icon
import android.text.TextUtils
import android.view.Menu
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.databinding.DirectoryItemGridRoundedCornersBinding
import ca.hojat.smart.gallery.databinding.DirectoryItemGridSquareBinding
import ca.hojat.smart.gallery.databinding.DirectoryItemListBinding
import ca.hojat.smart.gallery.feature_media_viewer.MediaActivity
import ca.hojat.smart.gallery.shared.activities.BaseActivity
import ca.hojat.smart.gallery.shared.data.domain.AlbumCover
import ca.hojat.smart.gallery.shared.data.domain.Directory
import ca.hojat.smart.gallery.shared.data.domain.FileDirItem
import ca.hojat.smart.gallery.shared.extensions.applyColorFilter
import ca.hojat.smart.gallery.shared.extensions.beGone
import ca.hojat.smart.gallery.shared.extensions.beVisible
import ca.hojat.smart.gallery.shared.extensions.beVisibleIf
import ca.hojat.smart.gallery.shared.extensions.checkAppendingHidden
import ca.hojat.smart.gallery.shared.extensions.config
import ca.hojat.smart.gallery.shared.extensions.containsNoMedia
import ca.hojat.smart.gallery.shared.extensions.convertToBitmap
import ca.hojat.smart.gallery.shared.extensions.directoryDB
import ca.hojat.smart.gallery.shared.extensions.doesThisOrParentHaveNoMedia
import ca.hojat.smart.gallery.shared.extensions.favoritesDB
import ca.hojat.smart.gallery.shared.extensions.fixDateTaken
import ca.hojat.smart.gallery.shared.extensions.getContrastColor
import ca.hojat.smart.gallery.shared.extensions.getFilenameFromPath
import ca.hojat.smart.gallery.shared.extensions.getProperBackgroundColor
import ca.hojat.smart.gallery.shared.extensions.getShortcutImage
import ca.hojat.smart.gallery.shared.extensions.getTimeFormat
import ca.hojat.smart.gallery.shared.extensions.isAStorageRootFolder
import ca.hojat.smart.gallery.shared.extensions.isGif
import ca.hojat.smart.gallery.shared.extensions.isImageFast
import ca.hojat.smart.gallery.shared.extensions.isMediaFile
import ca.hojat.smart.gallery.shared.extensions.isRawFast
import ca.hojat.smart.gallery.shared.extensions.isSvg
import ca.hojat.smart.gallery.shared.extensions.isVideoFast
import ca.hojat.smart.gallery.shared.extensions.isVisible
import ca.hojat.smart.gallery.shared.extensions.loadImage
import ca.hojat.smart.gallery.shared.extensions.mediaDB
import ca.hojat.smart.gallery.shared.extensions.rescanPaths
import ca.hojat.smart.gallery.shared.extensions.showErrorToast
import ca.hojat.smart.gallery.shared.extensions.toast
import ca.hojat.smart.gallery.shared.helpers.DIRECTORY
import ca.hojat.smart.gallery.shared.helpers.FAVORITES
import ca.hojat.smart.gallery.shared.helpers.FOLDER_MEDIA_CNT_BRACKETS
import ca.hojat.smart.gallery.shared.helpers.FOLDER_MEDIA_CNT_LINE
import ca.hojat.smart.gallery.shared.helpers.FOLDER_STYLE_ROUNDED_CORNERS
import ca.hojat.smart.gallery.shared.helpers.FOLDER_STYLE_SQUARE
import ca.hojat.smart.gallery.shared.helpers.LOCATION_INTERNAL
import ca.hojat.smart.gallery.shared.helpers.LOCATION_SD
import ca.hojat.smart.gallery.shared.helpers.RECYCLE_BIN
import ca.hojat.smart.gallery.shared.helpers.ROUNDED_CORNERS_BIG
import ca.hojat.smart.gallery.shared.helpers.ROUNDED_CORNERS_NONE
import ca.hojat.smart.gallery.shared.helpers.ROUNDED_CORNERS_SMALL
import ca.hojat.smart.gallery.shared.helpers.SORT_BY_CUSTOM
import ca.hojat.smart.gallery.shared.helpers.TYPE_GIFS
import ca.hojat.smart.gallery.shared.helpers.TYPE_IMAGES
import ca.hojat.smart.gallery.shared.helpers.TYPE_RAWS
import ca.hojat.smart.gallery.shared.helpers.TYPE_SVGS
import ca.hojat.smart.gallery.shared.helpers.TYPE_VIDEOS
import ca.hojat.smart.gallery.shared.helpers.VIEW_TYPE_LIST
import ca.hojat.smart.gallery.shared.helpers.ensureBackgroundThread
import ca.hojat.smart.gallery.shared.ui.dialogs.ConfirmDeleteFolderDialog
import ca.hojat.smart.gallery.shared.ui.dialogs.ConfirmationDialog
import ca.hojat.smart.gallery.shared.ui.dialogs.PickMediumDialog
import ca.hojat.smart.gallery.shared.ui.dialogs.PropertiesDialog
import ca.hojat.smart.gallery.shared.ui.dialogs.RenameItemDialog
import ca.hojat.smart.gallery.shared.ui.dialogs.RenameItemsDialog
import ca.hojat.smart.gallery.shared.ui.views.MyRecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.qtalk.recyclerviewfastscroller.RecyclerViewFastScroller
import java.io.File
import java.util.Collections

@UnstableApi
class DirectoryAdapter(
    activity: BaseActivity,
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
    private val isListViewType = config.viewTypeFolders == VIEW_TYPE_LIST
    private var pinnedFolders = config.pinnedFolders
    private var scrollHorizontally = config.scrollHorizontally
    private var animateGifs = config.animateGifs
    private var cropThumbnails = config.cropThumbnails
    private var groupDirectSubFolders = config.groupDirectSubfolders
    private var currentDirectoriesHash = dirs.hashCode()
    private var lockedFolderPaths = ArrayList<String>()
    private var isDragAndDropping = false
    private var startReorderDragListener: StartReorderDragListener? = null
    private var showMediaCount = config.showFolderMediaCount
    private var folderStyle = config.folderStyle
    private var limitFolderTitle = config.limitFolderTitle
    var directorySorting = config.directorySorting
    var dateFormat = config.dateFormat
    var timeFormat = activity.getTimeFormat()

    init {
        setupDragListener()
    }

    override fun getActionMenuId() = R.menu.cab_directories

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = when {
            isListViewType -> DirectoryItemListBinding.inflate(layoutInflater, parent, false)
            folderStyle == FOLDER_STYLE_SQUARE -> DirectoryItemGridSquareBinding.inflate(
                layoutInflater,
                parent,
                false
            )

            else -> DirectoryItemGridRoundedCornersBinding.inflate(layoutInflater, parent, false)
        }

        return createViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
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


            findItem(R.id.cab_empty_recycle_bin).isVisible =
                isOneItemSelected && selectedPaths.first() == RECYCLE_BIN
            findItem(R.id.cab_empty_disable_recycle_bin).isVisible =
                isOneItemSelected && selectedPaths.first() == RECYCLE_BIN

            findItem(R.id.cab_create_shortcut).isVisible = isOneItemSelected
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
            R.id.cab_copy_to -> copyFilesTo()
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
            Glide.with(activity).clear(bindItem(holder.itemView).dirThumbnail)
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


    private fun showProperties() {
        if (selectedKeys.size <= 1) {
            val path = getFirstSelectedItemPath() ?: return
            if (path != FAVORITES && path != RECYCLE_BIN) {
                PropertiesDialog(activity, path, config.shouldShowHidden)
            }
        } else {
            PropertiesDialog(activity, getSelectedPaths().filter {
                it != FAVORITES && it != RECYCLE_BIN
            }.toMutableList(), config.shouldShowHidden)
        }
    }


    private fun renameDir() {
        if (selectedKeys.size == 1) {
            val firstDir = getFirstSelectedItem() ?: return
            val sourcePath = firstDir.path
            val dir = File(sourcePath)
            if (activity.isAStorageRootFolder(dir.absolutePath)) {
                activity.toast(R.string.rename_folder_root)
                return
            }

            RenameItemDialog(activity, dir.absolutePath) {
                activity.runOnUiThread {
                    firstDir.apply {
                        path = it
                        name = it.getFilenameFromPath()
                        tmb = File(it, tmb.getFilenameFromPath()).absolutePath
                    }
                    updateDirs(dirs)
                    ensureBackgroundThread {
                        try {
                            activity.directoryDB.updateDirectoryAfterRename(
                                firstDir.tmb,
                                firstDir.name,
                                firstDir.path,
                                sourcePath
                            )
                            listener?.refreshItems()
                        } catch (e: Exception) {
                            activity.showErrorToast(e)
                        }
                    }
                }
            }

        } else {
            val paths = getSelectedPaths().filter {
                !activity.isAStorageRootFolder(it)
            } as ArrayList<String>
            RenameItemsDialog(activity, paths) {
                listener?.refreshItems()
            }
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

        selectedPaths.filter {
            it != FAVORITES && it != RECYCLE_BIN
        }.forEach {
            val path = it
            if (path.containsNoMedia()) {
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

    private fun hideFolders(paths: ArrayList<String>) {
        for (path in paths) {
            hideFolder(path)
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
        activity.emptyTheRecycleBin {
            listener?.refreshItems()
        }
    }

    private fun emptyAndDisableRecycleBin() {
        activity.showRecycleBinEmptyingDialog {
            activity.emptyAndDisableTheRecycleBin {
                listener?.refreshItems()
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

    @SuppressLint("NotifyDataSetChanged")
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

    private fun copyFilesTo() {
        handleLockedFolderOpeningForFolders(getSelectedPaths()) {
            copyMoveTo(it, true)
        }
    }

    private fun moveFilesTo() {
        handleLockedFolderOpeningForFolders(getSelectedPaths()) {
            copyMoveTo(it, false)
        }
    }

    private fun copyMoveTo(selectedPaths: Collection<String>, isCopyOperation: Boolean) {
        val paths = ArrayList<String>()
        val showHidden = config.shouldShowHidden
        selectedPaths.forEach {
            val filter = config.filterMedia
            File(it).listFiles()?.filter {
                !File(it.absolutePath).isDirectory &&
                        it.absolutePath.isMediaFile() && (showHidden || !it.name.startsWith('.')) &&
                        ((it.isImageFast() && filter and TYPE_IMAGES != 0) ||
                                (it.isVideoFast() && filter and TYPE_VIDEOS != 0) ||
                                (it.isGif() && filter and TYPE_GIFS != 0) ||
                                (it.isRawFast() && filter and TYPE_RAWS != 0) ||
                                (it.isSvg() && filter and TYPE_SVGS != 0))
            }?.mapTo(paths) { it.absolutePath }
        }

        val fileDirItems =
            paths.map { FileDirItem(it, it.getFilenameFromPath()) } as ArrayList<FileDirItem>
        activity.tryCopyMoveFilesTo(fileDirItems, isCopyOperation) {
            val destinationPath = it
            val newPaths = fileDirItems.map { "$destinationPath/${it.name}" }
                .toMutableList() as ArrayList<String>
            activity.rescanPaths(newPaths) {
                activity.fixDateTaken(newPaths, false)
            }

            config.tempFolderPath = ""
            listener?.refreshItems()
            finishActMode()
        }
    }

    private fun tryCreateShortcut() {
        createShortcut()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun createShortcut() {
        val manager = activity.getSystemService(ShortcutManager::class.java)
        if (manager.isRequestPinShortcutSupported) {
            val dir = getFirstSelectedItem() ?: return
            val path = dir.path
            val drawable = resources.getDrawable(R.drawable.shortcut_image).mutate()
            val coverThumbnail =
                config.parseAlbumCovers().firstOrNull { it.tmb == dir.path }?.tmb ?: dir.tmb
            activity.getShortcutImage(coverThumbnail, drawable) {
                val intent = Intent(activity, MediaActivity::class.java)
                intent.action = Intent.ACTION_VIEW
                intent.flags =
                    intent.flags or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.putExtra(DIRECTORY, path)

                val shortcut = ShortcutInfo.Builder(activity, path)
                    .setShortLabel(dir.name)
                    .setIcon(Icon.createWithBitmap(drawable.convertToBitmap()))
                    .setIntent(intent)
                    .build()

                manager.requestPinShortcut(shortcut, null)
            }
        }
    }

    private fun askConfirmDelete() {
        when {

            config.skipDeleteConfirmation -> deleteFolders()
            else -> {
                val itemsCnt = selectedKeys.size
                if (itemsCnt == 1 && getSelectedItems().first().isRecycleBin()) {
                    ConfirmationDialog(
                        activity,
                        "",
                        R.string.empty_recycle_bin_confirmation,
                        R.string.yes,
                        R.string.no
                    ) {
                        deleteFolders()
                    }
                    return
                }

                val items = if (itemsCnt == 1) {
                    val folder = getSelectedPaths().first().getFilenameFromPath()
                    "\"$folder\""
                } else {
                    resources.getQuantityString(
                        R.plurals.delete_items,
                        itemsCnt,
                        itemsCnt
                    )
                }

                val fileDirItem = getFirstSelectedItem() ?: return
                val baseString =
                    if (!config.useRecycleBin || config.tempSkipRecycleBin || (isOneItemSelected() && fileDirItem.areFavorites())) {
                        R.string.deletion_confirmation
                    } else {
                        R.string.move_to_recycle_bin_confirmation
                    }

                val question = String.format(resources.getString(baseString), items)
                val warning = resources.getQuantityString(
                    R.plurals.delete_warning,
                    itemsCnt,
                    itemsCnt
                )
                ConfirmDeleteFolderDialog(activity, question, warning) {
                    deleteFolders()
                }
            }
        }
    }

    private fun deleteFolders() {
        if (selectedKeys.isEmpty()) {
            return
        }

        val sAFPath = getFirstSelectedItemPath() ?: return
        val selectedDirs = getSelectedItems()
        activity.handleSAFDialog {
            if (!it) {
                return@handleSAFDialog
            }

            activity.handleSAFDialogSdk30(sAFPath) {
                if (!it) {
                    return@handleSAFDialogSdk30
                }

                val foldersToDelete = ArrayList<File>(selectedKeys.size)
                selectedDirs.forEach {
                    if (it.areFavorites() || it.isRecycleBin()) {
                        if (it.isRecycleBin()) {
                            tryEmptyRecycleBin(false)
                        } else {
                            ensureBackgroundThread {
                                activity.mediaDB.clearFavorites()
                                activity.favoritesDB.clearFavorites()
                                listener?.refreshItems()
                            }
                        }

                        if (selectedKeys.size == 1) {
                            finishActMode()
                        }
                    } else {
                        foldersToDelete.add(File(it.path))
                    }
                }

                handleLockedFolderOpeningForFolders(foldersToDelete.map { it.absolutePath }) {
                    listener?.deleteFolders(it.map { File(it) }.toMutableList() as ArrayList<File>)
                }
            }
        }
    }

    private fun handleLockedFolderOpeningForFolders(
        folders: Collection<String>,
        callback: (Collection<String>) -> Unit
    ) {
        callback(folders)
    }

    private fun tryChangeAlbumCover(useDefault: Boolean) {

        changeAlbumCover(useDefault)
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
        PickMediumDialog(activity, path) {
            if (File(it).isDirectory) {
                pickMediumFrom(targetFolder, it)
            } else {
                val albumCovers = getAlbumCoversWithout(path)
                val cover = AlbumCover(targetFolder, it)
                albumCovers.add(cover)
                storeCovers(albumCovers)
            }
        }
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

    @SuppressLint("NotifyDataSetChanged")
    fun updateDirs(newDirs: ArrayList<Directory>) {
        val directories = newDirs.clone() as ArrayList<Directory>
        if (directories.hashCode() != currentDirectoriesHash) {
            currentDirectoriesHash = directories.hashCode()
            dirs = directories
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
        bindItem(view).apply {
            dirPath?.text = "${directory.path.substringBeforeLast("/")}/"
            val thumbnailType = when {
                directory.tmb.isVideoFast() -> TYPE_VIDEOS
                directory.tmb.isGif() -> TYPE_GIFS
                directory.tmb.isRawFast() -> TYPE_RAWS
                directory.tmb.isSvg() -> TYPE_SVGS
                else -> TYPE_IMAGES
            }

            dirCheck.beVisibleIf(isSelected)
            if (isSelected) {
                dirCheck.background?.applyColorFilter(properPrimaryColor)
                dirCheck.applyColorFilter(contrastColor)
            }

            if (isListViewType) {
                dirHolder.isSelected = isSelected
            }

            if (scrollHorizontally && !isListViewType && folderStyle == FOLDER_STYLE_ROUNDED_CORNERS) {
                (dirThumbnail.layoutParams as RelativeLayout.LayoutParams).addRule(
                    RelativeLayout.ABOVE,
                    dirName.id
                )

                val photoCntParams = (photoCnt.layoutParams as RelativeLayout.LayoutParams)
                val nameParams = (dirName.layoutParams as RelativeLayout.LayoutParams)
                nameParams.removeRule(RelativeLayout.BELOW)

                if (config.showFolderMediaCount == FOLDER_MEDIA_CNT_LINE) {
                    nameParams.addRule(RelativeLayout.ABOVE, photoCnt.id)
                    nameParams.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM)

                    photoCntParams.removeRule(RelativeLayout.BELOW)
                    photoCntParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                } else {
                    nameParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                }
            }

            if (lockedFolderPaths.contains(directory.path)) {
                dirLock.beVisible()
                dirLock.background = ColorDrawable(root.context.getProperBackgroundColor())
                dirLock.applyColorFilter(root.context.getProperBackgroundColor().getContrastColor())
            } else {
                dirLock.beGone()
                val roundedCorners = when {
                    isListViewType -> ROUNDED_CORNERS_SMALL
                    folderStyle == FOLDER_STYLE_SQUARE -> ROUNDED_CORNERS_NONE
                    else -> ROUNDED_CORNERS_BIG
                }

                activity.loadImage(
                    thumbnailType,
                    directory.tmb,
                    dirThumbnail,
                    scrollHorizontally,
                    animateGifs,
                    cropThumbnails,
                    roundedCorners,
                    directory.getKey()
                )
            }

            dirPin.beVisibleIf(pinnedFolders.contains(directory.path))
            dirLocation.beVisibleIf(directory.location != LOCATION_INTERNAL)
            if (dirLocation.isVisible()) {
                dirLocation.setImageResource(if (directory.location == LOCATION_SD) R.drawable.ic_sd_card_vector else R.drawable.ic_usb_vector)
            }

            photoCnt.text = directory.subfoldersMediaCount.toString()
            photoCnt.beVisibleIf(showMediaCount == FOLDER_MEDIA_CNT_LINE)

            if (limitFolderTitle) {
                dirName.setSingleLine()
                dirName.ellipsize = TextUtils.TruncateAt.MIDDLE
            }

            var nameCount = directory.name
            if (showMediaCount == FOLDER_MEDIA_CNT_BRACKETS) {
                nameCount += " (${directory.subfoldersMediaCount})"
            }

            if (groupDirectSubFolders) {
                if (directory.subfoldersCount > 1) {
                    nameCount += " [${directory.subfoldersCount}]"
                }
            }

            dirName.text = nameCount

            if (isListViewType || folderStyle == FOLDER_STYLE_ROUNDED_CORNERS) {
                photoCnt.setTextColor(textColor)
                dirName.setTextColor(textColor)
                dirLocation.applyColorFilter(textColor)
            }

            if (isListViewType) {
                dirPath?.setTextColor(textColor)
                dirPin.applyColorFilter(textColor)
                dirLocation.applyColorFilter(textColor)
                dirDragHandle.beVisibleIf(isDragAndDropping)
            } else {
                dirDragHandleWrapper?.beVisibleIf(isDragAndDropping)
            }

            if (isDragAndDropping) {
                dirDragHandle.applyColorFilter(textColor)
                dirDragHandle.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        startReorderDragListener?.requestDrag(holder)
                    }
                    false
                }
            }
        }
    }

    override fun onRowClear(myViewHolder: ViewHolder?) {
        swipeRefreshLayout?.isEnabled = activity.config.enablePullToRefresh
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

    override fun onChange(position: Int) =
        dirs.getOrNull(position)?.getBubbleText(directorySorting, activity, dateFormat, timeFormat)
            ?: ""

    private fun bindItem(view: View): DirectoryItemBinding {
        return when {
            isListViewType -> DirectoryItemListBinding.bind(view).toItemBinding()
            folderStyle == FOLDER_STYLE_SQUARE -> DirectoryItemGridSquareBinding.bind(view)
                .toItemBinding()

            else -> DirectoryItemGridRoundedCornersBinding.bind(view).toItemBinding()
        }
    }
}
