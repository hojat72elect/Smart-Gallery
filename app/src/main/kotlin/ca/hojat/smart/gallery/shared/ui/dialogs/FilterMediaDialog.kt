package ca.hojat.smart.gallery.shared.ui.dialogs

import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.databinding.DialogFilterMediaBinding
import ca.hojat.smart.gallery.shared.extensions.config
import ca.hojat.smart.gallery.shared.extensions.getAlertDialogBuilder
import ca.hojat.smart.gallery.shared.extensions.setupDialogStuff
import ca.hojat.smart.gallery.shared.helpers.TYPE_GIFS
import ca.hojat.smart.gallery.shared.helpers.TYPE_IMAGES
import ca.hojat.smart.gallery.shared.helpers.TYPE_PORTRAITS
import ca.hojat.smart.gallery.shared.helpers.TYPE_RAWS
import ca.hojat.smart.gallery.shared.helpers.TYPE_SVGS
import ca.hojat.smart.gallery.shared.helpers.TYPE_VIDEOS
import ca.hojat.smart.gallery.shared.helpers.getDefaultFileFilter
import ca.hojat.smart.gallery.shared.activities.BaseActivity

class FilterMediaDialog(val activity: BaseActivity, val callback: (result: Int) -> Unit) {
    private val binding = DialogFilterMediaBinding.inflate(activity.layoutInflater)

    init {
        val filterMedia = activity.config.filterMedia
        binding.apply {
            filterMediaImages.isChecked = filterMedia and TYPE_IMAGES != 0
            filterMediaVideos.isChecked = filterMedia and TYPE_VIDEOS != 0
            filterMediaGifs.isChecked = filterMedia and TYPE_GIFS != 0
            filterMediaRaws.isChecked = filterMedia and TYPE_RAWS != 0
            filterMediaSvgs.isChecked = filterMedia and TYPE_SVGS != 0
            filterMediaPortraits.isChecked = filterMedia and TYPE_PORTRAITS != 0
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { _, _ -> dialogConfirmed() }
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this, R.string.filter_media)
            }
    }

    private fun dialogConfirmed() {
        var result = 0
        if (binding.filterMediaImages.isChecked)
            result += TYPE_IMAGES
        if (binding.filterMediaVideos.isChecked)
            result += TYPE_VIDEOS
        if (binding.filterMediaGifs.isChecked)
            result += TYPE_GIFS
        if (binding.filterMediaRaws.isChecked)
            result += TYPE_RAWS
        if (binding.filterMediaSvgs.isChecked)
            result += TYPE_SVGS
        if (binding.filterMediaPortraits.isChecked)
            result += TYPE_PORTRAITS

        if (result == 0) {
            result = getDefaultFileFilter()
        }

        if (activity.config.filterMedia != result) {
            activity.config.filterMedia = result
            callback(result)
        }
    }
}
