package com.simplemobiletools.gallery.pro.dialogs

import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.databinding.DialogCustomAspectRatioBinding
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getAlertDialogBuilder
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.setupDialogStuff
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.showKeyboard
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.value
import com.simplemobiletools.gallery.pro.new_architecture.BaseActivity

class CustomAspectRatioDialog(
    val activity: BaseActivity,
    private val defaultCustomAspectRatio: Pair<Float, Float>?,
    val callback: (aspectRatio: Pair<Float, Float>) -> Unit
) {
    init {
        val binding = DialogCustomAspectRatioBinding.inflate(activity.layoutInflater).apply {
            aspectRatioWidth.setText(defaultCustomAspectRatio?.first?.toInt()?.toString() ?: "")
            aspectRatioHeight.setText(defaultCustomAspectRatio?.second?.toInt()?.toString() ?: "")
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this) { alertDialog ->
                    alertDialog.showKeyboard(binding.aspectRatioWidth)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val width = getViewValue(binding.aspectRatioWidth)
                        val height = getViewValue(binding.aspectRatioHeight)
                        callback(Pair(width, height))
                        alertDialog.dismiss()
                    }
                }
            }
    }

    private fun getViewValue(view: EditText): Float {
        val textValue = view.value
        return if (textValue.isEmpty()) 0f else textValue.toFloat()
    }
}
