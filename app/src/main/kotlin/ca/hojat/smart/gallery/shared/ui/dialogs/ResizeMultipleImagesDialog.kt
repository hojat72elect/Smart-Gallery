package ca.hojat.smart.gallery.shared.ui.dialogs

import android.graphics.Point
import androidx.appcompat.app.AlertDialog
import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.databinding.DialogResizeMultipleImagesBinding
import ca.hojat.smart.gallery.shared.activities.BaseActivity
import ca.hojat.smart.gallery.shared.extensions.getAlertDialogBuilder
import ca.hojat.smart.gallery.shared.extensions.getParentPath
import ca.hojat.smart.gallery.shared.extensions.getProperPrimaryColor
import ca.hojat.smart.gallery.shared.extensions.setupDialogStuff
import ca.hojat.smart.gallery.shared.extensions.showKeyboard
import ca.hojat.smart.gallery.shared.extensions.toInt
import ca.hojat.smart.gallery.shared.helpers.ensureBackgroundThread
import ca.hojat.smart.gallery.shared.usecases.ShowToastUseCase
import java.io.File
import kotlin.math.roundToInt

private const val DEFAULT_RESIZE_FACTOR = "75"

class ResizeMultipleImagesDialog(
    private val activity: BaseActivity,
    private val imagePaths: List<String>,
    private val imageSizes: List<Point>,
    private val callback: () -> Unit
) {

    private var dialog: AlertDialog? = null
    private val binding = DialogResizeMultipleImagesBinding.inflate(activity.layoutInflater)
    private val progressView = binding.resizeProgress
    private val resizeFactorEditText = binding.resizeFactorEditText

    init {
        resizeFactorEditText.setText(DEFAULT_RESIZE_FACTOR)
        progressView.apply {
            max = imagePaths.size
            setIndicatorColor(activity.getProperPrimaryColor())
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(
                    binding.root,
                    this,
                    R.string.resize_multiple_images
                ) { alertDialog ->
                    dialog = alertDialog
                    alertDialog.showKeyboard(resizeFactorEditText)

                    val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    val negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    positiveButton.setOnClickListener {
                        val resizeFactorText = resizeFactorEditText.text?.toString()
                        if (resizeFactorText.isNullOrEmpty() || resizeFactorText.toInt() !in 10..90) {
                            ShowToastUseCase(activity, R.string.resize_factor_error)
                            return@setOnClickListener
                        }

                        val resizeFactor = resizeFactorText.toFloat().div(100)

                        alertDialog.setCanceledOnTouchOutside(false)
                        arrayOf(
                            binding.resizeFactorInputLayout,
                            positiveButton,
                            negativeButton
                        ).forEach {
                            it.isEnabled = false
                            it.alpha = 0.6f
                        }
                        resizeImages(resizeFactor)
                    }
                }
            }
    }


    private fun resizeImages(factor: Float) {
        progressView.show()
        with(activity) {
            val newSizes = imageSizes.map {
                val width = (it.x * factor).roundToInt()
                val height = (it.y * factor).roundToInt()
                Point(width, height)
            }

            val parentPath = imagePaths.first().getParentPath()
            val pathsToRescan = arrayListOf<String>()
            val pathLastModifiedMap = mutableMapOf<String, Long>()

            ensureWriteAccess(parentPath) {
                ensureBackgroundThread {
                    for (i in imagePaths.indices) {
                        val path = imagePaths[i]
                        val size = newSizes[i]
                        val lastModified = File(path).lastModified()

                        try {
                            resizeImage(path, path, size) {
                                if (it) {
                                    pathsToRescan.add(path)
                                    pathLastModifiedMap[path] = lastModified
                                    runOnUiThread {
                                        progressView.progress = i + 1
                                    }
                                }
                            }
                        } catch (e: OutOfMemoryError) {
                            ShowToastUseCase(this, R.string.out_of_memory_error)
                        } catch (e: Exception) {
                            ShowToastUseCase(this, "Error : $e")
                        }
                    }

                    val failureCount = imagePaths.size - pathsToRescan.size
                    if (failureCount > 0) {
                        ShowToastUseCase(
                            this,
                            resources.getQuantityString(
                                R.plurals.failed_to_resize_images,
                                failureCount,
                                failureCount
                            )
                        )
                    } else {
                        ShowToastUseCase(this, R.string.images_resized_successfully)
                    }

                    rescanPathsAndUpdateLastModified(pathsToRescan, pathLastModifiedMap) {
                        activity.runOnUiThread {
                            dialog?.dismiss()
                            callback.invoke()
                        }
                    }
                }
            }
        }
    }
}
