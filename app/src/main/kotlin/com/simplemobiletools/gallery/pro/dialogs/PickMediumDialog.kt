package com.simplemobiletools.gallery.pro.dialogs

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getAlertDialogBuilder
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getProperPrimaryColor
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.setupDialogStuff
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.VIEW_TYPE_GRID
import com.simplemobiletools.gallery.pro.views.MyGridLayoutManager
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.adapters.MediaAdapter
import com.simplemobiletools.gallery.pro.asynctasks.GetMediaAsyncTask
import com.simplemobiletools.gallery.pro.databinding.DialogMediumPickerBinding
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.config
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getCachedMedia
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.GridSpacingItemDecoration
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.SHOW_ALL
import com.simplemobiletools.gallery.pro.models.Medium
import com.simplemobiletools.gallery.pro.models.ThumbnailItem
import com.simplemobiletools.gallery.pro.models.ThumbnailSection
import com.simplemobiletools.gallery.pro.new_architecture.shared.BaseActivity

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@UnstableApi
class PickMediumDialog(
    val activity: BaseActivity,
    val path: String,
    val callback: (path: String) -> Unit
) {
    private var dialog: AlertDialog? = null
    private var shownMedia = ArrayList<ThumbnailItem>()
    private val binding = DialogMediumPickerBinding.inflate(activity.layoutInflater)
    private val config = activity.config
    private val viewType = config.getFolderViewType(if (config.showAll) SHOW_ALL else path)
    private var isGridViewType = viewType == VIEW_TYPE_GRID

    init {
        (binding.mediaGrid.layoutManager as MyGridLayoutManager).apply {
            orientation =
                if (config.scrollHorizontally && isGridViewType) RecyclerView.HORIZONTAL else RecyclerView.VERTICAL
            spanCount = if (isGridViewType) config.mediaColumnCnt else 1
        }

        binding.mediaFastscroller.updateColors(activity.getProperPrimaryColor())

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .setNeutralButton(R.string.other_folder) { _, _ -> showOtherFolder() }
            .apply {
                activity.setupDialogStuff(
                    binding.root,
                    this,
                    R.string.select_photo
                ) { alertDialog ->
                    dialog = alertDialog
                }
            }

        activity.getCachedMedia(path) {
            val media = it.toList() as ArrayList
            if (media.isNotEmpty()) {
                activity.runOnUiThread {
                    gotMedia(media)
                }
            }
        }

        GetMediaAsyncTask(
            context = activity,
            mPath = path,
            isPickImage = false,
            isPickVideo = false,
            showAll = false
        ) {
            gotMedia(it)
        }.execute()
    }



    private fun showOtherFolder() {
        PickDirectoryDialog(
            activity = activity,
            sourcePath = path,
            showOtherFolderButton = true,
            showFavoritesBin = true,
            isPickingCopyMoveDestination = false,
            isPickingFolderForWidget = false
        ) {
            callback(it)
            dialog?.dismiss()
        }
    }

    private fun gotMedia(media: ArrayList<ThumbnailItem>) {
        if (media.hashCode() == shownMedia.hashCode())
            return

        shownMedia = media
        val adapter = MediaAdapter(
            activity = activity,
            media = shownMedia.clone() as ArrayList<ThumbnailItem>,
            listener = null,
            isAGetIntent = true,
            allowMultiplePicks = false,
            path = path,
            recyclerView = binding.mediaGrid
        ) {
            if (it is Medium) {
                callback(it.path)
                dialog?.dismiss()
            }
        }

        val scrollHorizontally = config.scrollHorizontally && isGridViewType
        binding.apply {
            mediaGrid.adapter = adapter
            mediaFastscroller.setScrollVertically(!scrollHorizontally)
        }
        handleGridSpacing(media)
    }

    private fun handleGridSpacing(media: ArrayList<ThumbnailItem>) {
        if (isGridViewType) {
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
}
