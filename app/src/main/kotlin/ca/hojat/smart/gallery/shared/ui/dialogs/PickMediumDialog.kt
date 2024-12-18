package ca.hojat.smart.gallery.shared.ui.dialogs

import androidx.appcompat.app.AlertDialog
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.databinding.DialogMediumPickerBinding
import ca.hojat.smart.gallery.shared.activities.BaseActivity
import ca.hojat.smart.gallery.shared.data.domain.Medium
import ca.hojat.smart.gallery.shared.data.domain.ThumbnailItem
import ca.hojat.smart.gallery.shared.data.domain.ThumbnailSection
import ca.hojat.smart.gallery.shared.data.repository.GetMediaAsyncTask
import ca.hojat.smart.gallery.shared.extensions.config
import ca.hojat.smart.gallery.shared.extensions.getAlertDialogBuilder
import ca.hojat.smart.gallery.shared.extensions.getCachedMedia
import ca.hojat.smart.gallery.shared.extensions.getProperPrimaryColor
import ca.hojat.smart.gallery.shared.extensions.setupDialogStuff
import ca.hojat.smart.gallery.shared.helpers.GridSpacingItemDecoration
import ca.hojat.smart.gallery.shared.helpers.SHOW_ALL
import ca.hojat.smart.gallery.shared.helpers.VIEW_TYPE_GRID
import ca.hojat.smart.gallery.shared.ui.adapters.MediaAdapter
import ca.hojat.smart.gallery.shared.ui.views.MyGridLayoutManager

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
