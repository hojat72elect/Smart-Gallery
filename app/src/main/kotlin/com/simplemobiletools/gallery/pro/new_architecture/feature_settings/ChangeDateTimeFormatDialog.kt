package com.simplemobiletools.gallery.pro.new_architecture.feature_settings

import android.app.Activity
import android.text.format.DateFormat
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.databinding.DialogChangeDateTimeFormatBinding
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.baseConfig
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getAlertDialogBuilder
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.setupDialogStuff
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.DATE_FORMAT_EIGHT
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.DATE_FORMAT_FIVE
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.DATE_FORMAT_FOUR
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.DATE_FORMAT_ONE
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.DATE_FORMAT_SEVEN
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.DATE_FORMAT_SIX
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.DATE_FORMAT_THREE
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.DATE_FORMAT_TWO
import java.util.Calendar
import java.util.Locale

class ChangeDateTimeFormatDialog(val activity: Activity, val callback: () -> Unit) {
    private val view =
        DialogChangeDateTimeFormatBinding.inflate(activity.layoutInflater, null, false)

    init {
        view.apply {
            changeDateTimeDialogRadioOne.text = formatDateSample(DATE_FORMAT_ONE)
            changeDateTimeDialogRadioTwo.text = formatDateSample(DATE_FORMAT_TWO)
            changeDateTimeDialogRadioThree.text = formatDateSample(DATE_FORMAT_THREE)
            changeDateTimeDialogRadioFour.text = formatDateSample(DATE_FORMAT_FOUR)
            changeDateTimeDialogRadioFive.text = formatDateSample(DATE_FORMAT_FIVE)
            changeDateTimeDialogRadioSix.text = formatDateSample(DATE_FORMAT_SIX)
            changeDateTimeDialogRadioSeven.text = formatDateSample(DATE_FORMAT_SEVEN)
            changeDateTimeDialogRadioEight.text = formatDateSample(DATE_FORMAT_EIGHT)

            changeDateTimeDialog24Hour.isChecked = activity.baseConfig.use24HourFormat

            val formatButton = when (activity.baseConfig.dateFormat) {
                DATE_FORMAT_ONE -> changeDateTimeDialogRadioOne
                DATE_FORMAT_TWO -> changeDateTimeDialogRadioTwo
                DATE_FORMAT_THREE -> changeDateTimeDialogRadioThree
                DATE_FORMAT_FOUR -> changeDateTimeDialogRadioFour
                DATE_FORMAT_FIVE -> changeDateTimeDialogRadioFive
                DATE_FORMAT_SIX -> changeDateTimeDialogRadioSix
                DATE_FORMAT_SEVEN -> changeDateTimeDialogRadioSeven
                else -> changeDateTimeDialogRadioEight
            }
            formatButton.isChecked = true
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { _, _ -> dialogConfirmed() }
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view.root, this)
            }
    }

    private fun dialogConfirmed() {
        activity.baseConfig.dateFormat =
            when (view.changeDateTimeDialogRadioGroup.checkedRadioButtonId) {
                view.changeDateTimeDialogRadioOne.id -> DATE_FORMAT_ONE
                view.changeDateTimeDialogRadioTwo.id -> DATE_FORMAT_TWO
                view.changeDateTimeDialogRadioThree.id -> DATE_FORMAT_THREE
                view.changeDateTimeDialogRadioFour.id -> DATE_FORMAT_FOUR
                view.changeDateTimeDialogRadioFive.id -> DATE_FORMAT_FIVE
                view.changeDateTimeDialogRadioSix.id -> DATE_FORMAT_SIX
                view.changeDateTimeDialogRadioSeven.id -> DATE_FORMAT_SEVEN
                else -> DATE_FORMAT_EIGHT
            }

        activity.baseConfig.use24HourFormat = view.changeDateTimeDialog24Hour.isChecked
        callback()
    }

    private fun formatDateSample(format: String): String {
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = timeSample
        return DateFormat.format(format, cal).toString()
    }
}

private const val timeSample = 1676419200000    // February 15, 2023