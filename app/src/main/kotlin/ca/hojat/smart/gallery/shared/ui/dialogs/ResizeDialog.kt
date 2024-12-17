package ca.hojat.smart.gallery.shared.ui.dialogs

import android.graphics.Point
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.databinding.DialogResizeImageBinding
import ca.hojat.smart.gallery.shared.extensions.getAlertDialogBuilder
import ca.hojat.smart.gallery.shared.extensions.onTextChangeListener
import ca.hojat.smart.gallery.shared.extensions.setupDialogStuff
import ca.hojat.smart.gallery.shared.extensions.showKeyboard
import ca.hojat.smart.gallery.shared.extensions.toInt
import ca.hojat.smart.gallery.shared.extensions.toast
import ca.hojat.smart.gallery.shared.extensions.value
import ca.hojat.smart.gallery.shared.activities.BaseActivity

class ResizeDialog(
    val activity: BaseActivity,
    val size: Point,
    val callback: (newSize: Point) -> Unit
) {
    init {
        val binding = DialogResizeImageBinding.inflate(activity.layoutInflater)
        val widthView = binding.resizeImageWidth
        val heightView = binding.resizeImageHeight

        widthView.setText(size.x.toString())
        heightView.setText(size.y.toString())

        val ratio = size.x / size.y.toFloat()

        widthView.onTextChangeListener {
            if (widthView.hasFocus()) {
                var width = getViewValue(widthView)
                if (width > size.x) {
                    widthView.setText(size.x.toString())
                    width = size.x
                }

                if (binding.keepAspectRatio.isChecked) {
                    heightView.setText((width / ratio).toInt().toString())
                }
            }
        }

        heightView.onTextChangeListener {
            if (heightView.hasFocus()) {
                var height = getViewValue(heightView)
                if (height > size.y) {
                    heightView.setText(size.y.toString())
                    height = size.y
                }

                if (binding.keepAspectRatio.isChecked) {
                    widthView.setText((height * ratio).toInt().toString())
                }
            }
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(
                    binding.root,
                    this,
                    R.string.resize_and_save
                ) { alertDialog ->
                    alertDialog.showKeyboard(binding.resizeImageWidth)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val width = getViewValue(widthView)
                        val height = getViewValue(heightView)
                        if (width <= 0 || height <= 0) {
                            activity.toast(R.string.invalid_values)
                            return@setOnClickListener
                        }

                        val newSize = Point(getViewValue(widthView), getViewValue(heightView))
                        callback(newSize)
                        alertDialog.dismiss()
                    }
                }
            }
    }

    private fun getViewValue(view: EditText): Int {
        val textValue = view.value
        return if (textValue.isEmpty()) 0 else textValue.toInt()
    }
}
