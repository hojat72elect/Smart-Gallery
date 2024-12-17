package ca.hojat.smart.gallery.shared.ui.dialogs

import androidx.appcompat.app.AlertDialog
import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.databinding.DialogSlideshowBinding
import ca.hojat.smart.gallery.shared.extensions.config
import ca.hojat.smart.gallery.shared.extensions.getAlertDialogBuilder
import ca.hojat.smart.gallery.shared.extensions.hideKeyboard
import ca.hojat.smart.gallery.shared.extensions.setupDialogStuff
import ca.hojat.smart.gallery.shared.extensions.value
import ca.hojat.smart.gallery.shared.helpers.SLIDESHOW_ANIMATION_FADE
import ca.hojat.smart.gallery.shared.helpers.SLIDESHOW_ANIMATION_NONE
import ca.hojat.smart.gallery.shared.helpers.SLIDESHOW_ANIMATION_SLIDE
import ca.hojat.smart.gallery.shared.helpers.SLIDESHOW_DEFAULT_INTERVAL
import ca.hojat.smart.gallery.shared.data.domain.RadioItem
import ca.hojat.smart.gallery.shared.activities.BaseActivity

class SlideshowDialog(val activity: BaseActivity, val callback: () -> Unit) {
    private val binding: DialogSlideshowBinding

    init {
        binding = DialogSlideshowBinding.inflate(activity.layoutInflater).apply {
            intervalHint.hint =
                activity.getString(R.string.seconds_raw)
                    .replaceFirstChar { it.uppercaseChar() }
            intervalValue.setOnClickListener {
                intervalValue.selectAll()
            }

            intervalValue.setOnFocusChangeListener { v, hasFocus ->
                if (!hasFocus)
                    activity.hideKeyboard(v)
            }

            animationHolder.setOnClickListener {
                val items = arrayListOf(
                    RadioItem(SLIDESHOW_ANIMATION_NONE, activity.getString(R.string.no_animation)),
                    RadioItem(SLIDESHOW_ANIMATION_SLIDE, activity.getString(R.string.slide)),
                    RadioItem(SLIDESHOW_ANIMATION_FADE, activity.getString(R.string.fade))
                )

                RadioGroupDialog(activity, items, activity.config.slideshowAnimation) {
                    activity.config.slideshowAnimation = it as Int
                    animationValue.text = getAnimationText()
                }
            }

            includeVideosHolder.setOnClickListener {
                intervalValue.clearFocus()
                includeVideos.toggle()
            }

            includeGifsHolder.setOnClickListener {
                intervalValue.clearFocus()
                includeGifs.toggle()
            }

            randomOrderHolder.setOnClickListener {
                intervalValue.clearFocus()
                randomOrder.toggle()
            }

            moveBackwardsHolder.setOnClickListener {
                intervalValue.clearFocus()
                moveBackwards.toggle()
            }

            loopSlideshowHolder.setOnClickListener {
                intervalValue.clearFocus()
                loopSlideshow.toggle()
            }
        }
        setupValues()

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this) { alertDialog ->
                    alertDialog.hideKeyboard()
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        storeValues()
                        callback()
                        alertDialog.dismiss()
                    }
                }
            }
    }

    private fun setupValues() {
        val config = activity.config
        binding.apply {
            intervalValue.setText(config.slideshowInterval.toString())
            animationValue.text = getAnimationText()
            includeVideos.isChecked = config.slideshowIncludeVideos
            includeGifs.isChecked = config.slideshowIncludeGIFs
            randomOrder.isChecked = config.slideshowRandomOrder
            moveBackwards.isChecked = config.slideshowMoveBackwards
            loopSlideshow.isChecked = config.loopSlideshow
        }
    }

    private fun storeValues() {
        var interval = binding.intervalValue.text.toString()
        if (interval.trim('0').isEmpty())
            interval = SLIDESHOW_DEFAULT_INTERVAL.toString()

        activity.config.apply {
            slideshowAnimation = getAnimationValue(binding.animationValue.value)
            slideshowInterval = interval.toInt()
            slideshowIncludeVideos = binding.includeVideos.isChecked
            slideshowIncludeGIFs = binding.includeGifs.isChecked
            slideshowRandomOrder = binding.randomOrder.isChecked
            slideshowMoveBackwards = binding.moveBackwards.isChecked
            loopSlideshow = binding.loopSlideshow.isChecked
        }
    }

    private fun getAnimationText(): String {
        return when (activity.config.slideshowAnimation) {
            SLIDESHOW_ANIMATION_SLIDE -> activity.getString(R.string.slide)
            SLIDESHOW_ANIMATION_FADE -> activity.getString(R.string.fade)
            else -> activity.getString(R.string.no_animation)
        }
    }

    private fun getAnimationValue(text: String): Int {
        return when (text) {
            activity.getString(R.string.slide) -> SLIDESHOW_ANIMATION_SLIDE
            activity.getString(R.string.fade) -> SLIDESHOW_ANIMATION_FADE
            else -> SLIDESHOW_ANIMATION_NONE
        }
    }
}
