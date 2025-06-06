package ca.hojat.smart.gallery.feature_settings

import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.databinding.DialogLineColorPickerBinding
import ca.hojat.smart.gallery.shared.activities.BaseActivity
import ca.hojat.smart.gallery.shared.extensions.beGoneIf
import ca.hojat.smart.gallery.shared.extensions.beVisibleIf
import ca.hojat.smart.gallery.shared.extensions.copyToClipboard
import ca.hojat.smart.gallery.shared.extensions.getAlertDialogBuilder
import ca.hojat.smart.gallery.shared.extensions.setupDialogStuff
import ca.hojat.smart.gallery.shared.extensions.toHex
import ca.hojat.smart.gallery.shared.extensions.value
import com.google.android.material.appbar.MaterialToolbar

class LineColorPickerDialog(
    val activity: BaseActivity,
    val color: Int,
    private val isPrimaryColorPicker: Boolean,
    private val primaryColors: Int = R.array.md_primary_colors,
    private val appIconIDs: ArrayList<Int>? = null,
    val toolbar: MaterialToolbar? = null,
    val callback: (wasPositivePressed: Boolean, color: Int) -> Unit
) {

    private val defaultColorValue = activity.resources.getColor(R.color.color_primary)
    private var wasDimmedBackgroundRemoved = false
    private var dialog: AlertDialog? = null
    private var view = DialogLineColorPickerBinding.inflate(activity.layoutInflater, null, false)

    init {
        view.apply {
            hexCode.text = color.toHex()
            hexCode.setOnLongClickListener {
                activity.copyToClipboard(hexCode.value.substring(1))
                true
            }

            lineColorPickerIcon.beGoneIf(isPrimaryColorPicker)
            val indexes = getColorIndexes(color)

            val primaryColorIndex = indexes.first
            primaryColorChanged(primaryColorIndex)
            primaryLineColorPicker.updateColors(getColors(primaryColors), primaryColorIndex)
            primaryLineColorPicker.listener = LineColorPickerListener { index, color ->
                val secondaryColors = getColorsForIndex(index)
                secondaryLineColorPicker.updateColors(secondaryColors)

                val newColor =
                    if (isPrimaryColorPicker) secondaryLineColorPicker.getCurrentColor() else color
                colorUpdated(newColor)

                if (!isPrimaryColorPicker) {
                    primaryColorChanged(index)
                }
            }

            secondaryLineColorPicker.beVisibleIf(isPrimaryColorPicker)
            secondaryLineColorPicker.updateColors(
                getColorsForIndex(primaryColorIndex),
                indexes.second
            )
            secondaryLineColorPicker.listener =
                LineColorPickerListener { _, color -> colorUpdated(color) }
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { _, _ -> dialogConfirmed() }
            .setNegativeButton(R.string.cancel) { _, _ -> dialogDismissed() }
            .setOnCancelListener { dialogDismissed() }
            .apply {
                activity.setupDialogStuff(view.root, this) { alertDialog ->
                    dialog = alertDialog
                }
            }
    }

    fun getSpecificColor() = view.secondaryLineColorPicker.getCurrentColor()

    private fun colorUpdated(color: Int) {
        view.hexCode.text = color.toHex()
        if (isPrimaryColorPicker) {

            if (toolbar != null) {
                activity.updateTopBarColors(toolbar, color)
            }

            if (!wasDimmedBackgroundRemoved) {
                dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                wasDimmedBackgroundRemoved = true
            }
        }
    }

    private fun getColorIndexes(color: Int): Pair<Int, Int> {
        if (color == defaultColorValue) {
            return getDefaultColorPair()
        }

        for (i in 0 until PRIMARY_COLORS_COUNT) {
            getColorsForIndex(i).indexOfFirst { color == it }.apply {
                if (this != -1) {
                    return Pair(i, this)
                }
            }
        }

        return getDefaultColorPair()
    }

    private fun primaryColorChanged(index: Int) {
        view.lineColorPickerIcon.setImageResource(appIconIDs?.getOrNull(index) ?: 0)
    }

    private fun getDefaultColorPair() =
        Pair(DEFAULT_PRIMARY_COLOR_INDEX, DEFAULT_SECONDARY_COLOR_INDEX)

    private fun dialogDismissed() {
        callback(false, 0)
    }

    private fun dialogConfirmed() {
        val targetView =
            if (isPrimaryColorPicker) view.secondaryLineColorPicker else view.primaryLineColorPicker
        val color = targetView.getCurrentColor()
        callback(true, color)
    }

    private fun getColorsForIndex(index: Int) = when (index) {
        0 -> getColors(R.array.md_reds)
        1 -> getColors(R.array.md_pinks)
        2 -> getColors(R.array.md_purples)
        3 -> getColors(R.array.md_deep_purples)
        4 -> getColors(R.array.md_indigos)
        5 -> getColors(R.array.md_blues)
        6 -> getColors(R.array.md_light_blues)
        7 -> getColors(R.array.md_cyans)
        8 -> getColors(R.array.md_teals)
        9 -> getColors(R.array.md_greens)
        10 -> getColors(R.array.md_light_greens)
        11 -> getColors(R.array.md_limes)
        12 -> getColors(R.array.md_yellows)
        13 -> getColors(R.array.md_ambers)
        14 -> getColors(R.array.md_oranges)
        15 -> getColors(R.array.md_deep_oranges)
        16 -> getColors(R.array.md_browns)
        17 -> getColors(R.array.md_blue_greys)
        18 -> getColors(R.array.md_greys)
        else -> throw RuntimeException("Invalid color id $index")
    }

    private fun getColors(id: Int) = activity.resources.getIntArray(id).toCollection(ArrayList())

    companion object {
        private const val PRIMARY_COLORS_COUNT = 19
        private const val DEFAULT_PRIMARY_COLOR_INDEX = 14
        private const val DEFAULT_SECONDARY_COLOR_INDEX = 6
    }
}
