package com.simplemobiletools.gallery.pro.new_architecture.shared.ui.dialogs

import android.annotation.SuppressLint
import android.content.DialogInterface
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.databinding.DialogChangeFileThumbnailStyleBinding
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.config
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getAlertDialogBuilder
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.setupDialogStuff
import com.simplemobiletools.gallery.pro.new_architecture.shared.data.domain.RadioItem
import com.simplemobiletools.gallery.pro.new_architecture.shared.activities.BaseActivity

class ChangeFileThumbnailStyleDialog(val activity: BaseActivity) :
    DialogInterface.OnClickListener {
    private var config = activity.config
    private val binding: DialogChangeFileThumbnailStyleBinding
    private var thumbnailSpacing = config.thumbnailSpacing

    init {
        binding = DialogChangeFileThumbnailStyleBinding.inflate(activity.layoutInflater).apply {
            dialogFileStyleRoundedCorners.isChecked = config.fileRoundedCorners
            dialogFileStyleShowThumbnailVideoDuration.isChecked = config.showThumbnailVideoDuration
            dialogFileStyleShowThumbnailFileTypes.isChecked = config.showThumbnailFileTypes
            dialogFileStyleMarkFavoriteItems.isChecked = config.markFavoriteItems

            dialogFileStyleRoundedCornersHolder.setOnClickListener { dialogFileStyleRoundedCorners.toggle() }
            dialogFileStyleShowThumbnailVideoDurationHolder.setOnClickListener { dialogFileStyleShowThumbnailVideoDuration.toggle() }
            dialogFileStyleShowThumbnailFileTypesHolder.setOnClickListener { dialogFileStyleShowThumbnailFileTypes.toggle() }
            dialogFileStyleMarkFavoriteItemsHolder.setOnClickListener { dialogFileStyleMarkFavoriteItems.toggle() }

            dialogFileStyleSpacingHolder.setOnClickListener {
                val items = arrayListOf(
                    RadioItem(0, "0x"),
                    RadioItem(1, "1x"),
                    RadioItem(2, "2x"),
                    RadioItem(4, "4x"),
                    RadioItem(8, "8x"),
                    RadioItem(16, "16x"),
                    RadioItem(32, "32x"),
                    RadioItem(64, "64x")
                )

                RadioGroupDialog(activity, items, thumbnailSpacing) {
                    thumbnailSpacing = it as Int
                    updateThumbnailSpacingText()
                }
            }
        }

        updateThumbnailSpacingText()

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, this)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this)
            }
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        config.fileRoundedCorners = binding.dialogFileStyleRoundedCorners.isChecked
        config.showThumbnailVideoDuration =
            binding.dialogFileStyleShowThumbnailVideoDuration.isChecked
        config.showThumbnailFileTypes = binding.dialogFileStyleShowThumbnailFileTypes.isChecked
        config.markFavoriteItems = binding.dialogFileStyleMarkFavoriteItems.isChecked
        config.thumbnailSpacing = thumbnailSpacing
    }

    @SuppressLint("SetTextI18n")
    private fun updateThumbnailSpacingText() {
        binding.dialogFileStyleSpacing.text = "${thumbnailSpacing}x"
    }
}
