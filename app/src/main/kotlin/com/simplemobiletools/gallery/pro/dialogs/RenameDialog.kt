package com.simplemobiletools.gallery.pro.dialogs

import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.adapters.RenameAdapter
import com.simplemobiletools.gallery.pro.databinding.DialogRenameBinding
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.baseConfig
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getAlertDialogBuilder
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getProperBackgroundColor
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getProperPrimaryColor
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getProperTextColor
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.onPageChangeListener
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.onTabSelectionChanged
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.setupDialogStuff
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.RENAME_PATTERN
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.RENAME_SIMPLE
import com.simplemobiletools.gallery.pro.new_architecture.shared.activities.BaseActivity
import com.simplemobiletools.gallery.pro.views.MyViewPager

class RenameDialog(
    val activity: BaseActivity,
    val paths: ArrayList<String>,
    private val useMediaFileExtension: Boolean,
    val callback: () -> Unit
) {
    var dialog: AlertDialog? = null
    val view = DialogRenameBinding.inflate(LayoutInflater.from(activity), null, false)
    private var tabsAdapter: RenameAdapter
    private var viewPager: MyViewPager

    init {
        view.apply {
            viewPager = dialogTabViewPager
            tabsAdapter = RenameAdapter(activity, paths)
            viewPager.adapter = tabsAdapter
            viewPager.onPageChangeListener {
                dialogTabLayout.getTabAt(it)!!.select()
            }
            viewPager.currentItem = activity.baseConfig.lastRenameUsed

            if (activity.baseConfig.isUsingSystemTheme) {
                dialogTabLayout.setBackgroundColor(activity.resources.getColor(R.color.you_dialog_background_color))
            } else {
                dialogTabLayout.setBackgroundColor(root.context.getProperBackgroundColor())
            }

            val textColor = root.context.getProperTextColor()
            dialogTabLayout.setTabTextColors(textColor, textColor)
            dialogTabLayout.setSelectedTabIndicatorColor(root.context.getProperPrimaryColor())

            if (activity.baseConfig.isUsingSystemTheme) {
                dialogTabLayout.setBackgroundColor(activity.resources.getColor(R.color.you_dialog_background_color))
            }

            dialogTabLayout.onTabSelectionChanged(tabSelectedAction = {
                viewPager.currentItem = when {
                    it.text.toString().equals(
                        root.context.resources.getString(R.string.simple_renaming),
                        true
                    ) -> RENAME_SIMPLE

                    else -> RENAME_PATTERN
                }
            })
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel) { _, _ -> dismissDialog() }
            .apply {
                activity.setupDialogStuff(view.root, this) { alertDialog ->
                    dialog = alertDialog
                    alertDialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        tabsAdapter.dialogConfirmed(useMediaFileExtension, viewPager.currentItem) {
                            dismissDialog()
                            if (it) {
                                activity.baseConfig.lastRenameUsed = viewPager.currentItem
                                callback()
                            }
                        }
                    }
                }
            }
    }

    private fun dismissDialog() {
        dialog?.dismiss()
    }
}
