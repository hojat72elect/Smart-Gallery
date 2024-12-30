package ca.hojat.smart.gallery.shared.ui.dialogs

import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.viewpager.widget.ViewPager
import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.databinding.DialogRenameBinding
import ca.hojat.smart.gallery.shared.activities.BaseActivity
import ca.hojat.smart.gallery.shared.extensions.baseConfig
import ca.hojat.smart.gallery.shared.extensions.getAlertDialogBuilder
import ca.hojat.smart.gallery.shared.extensions.getProperBackgroundColor
import ca.hojat.smart.gallery.shared.extensions.getProperPrimaryColor
import ca.hojat.smart.gallery.shared.extensions.getProperTextColor
import ca.hojat.smart.gallery.shared.extensions.setupDialogStuff
import ca.hojat.smart.gallery.shared.helpers.RENAME_PATTERN
import ca.hojat.smart.gallery.shared.helpers.RENAME_SIMPLE
import ca.hojat.smart.gallery.shared.ui.adapters.RenameAdapter
import ca.hojat.smart.gallery.shared.ui.views.MyViewPager
import com.google.android.material.tabs.TabLayout

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
            viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

                override fun onPageSelected(position: Int) {
                    dialogTabLayout.getTabAt(position)!!.select()
                }

                override fun onPageScrollStateChanged(state: Int) {}
            })
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

            dialogTabLayout.setOnTabSelectedListener(object: TabLayout.OnTabSelectedListener{
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    viewPager.currentItem = when {
                        tab?.text.toString().equals(
                            root.context.resources.getString(R.string.simple_renaming),
                            true
                        ) -> RENAME_SIMPLE

                        else -> RENAME_PATTERN
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                override fun onTabReselected(tab: TabLayout.Tab?) {
                    viewPager.currentItem = when {
                        tab?.text.toString().equals(
                            root.context.resources.getString(R.string.simple_renaming),
                            true
                        ) -> RENAME_SIMPLE

                        else -> RENAME_PATTERN
                    }
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
