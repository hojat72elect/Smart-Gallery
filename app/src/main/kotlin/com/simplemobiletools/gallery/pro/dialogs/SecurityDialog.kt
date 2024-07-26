package com.simplemobiletools.gallery.pro.dialogs

import android.app.Activity
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.biometric.auth.AuthPromptHost
import androidx.fragment.app.FragmentActivity
import com.simplemobiletools.commons.R
import com.simplemobiletools.gallery.pro.adapters.PasswordTypesAdapter
import com.simplemobiletools.gallery.pro.databinding.DialogSecurityBinding
import com.simplemobiletools.commons.extensions.isBiometricIdAvailable
import com.simplemobiletools.commons.extensions.onPageChangeListener
import com.simplemobiletools.commons.extensions.onTabSelectionChanged
import com.simplemobiletools.commons.views.MyDialogViewPager
import com.simplemobiletools.gallery.pro.extensions.baseConfig
import com.simplemobiletools.gallery.pro.extensions.beGone
import com.simplemobiletools.gallery.pro.extensions.getAlertDialogBuilder
import com.simplemobiletools.gallery.pro.extensions.getProperBackgroundColor
import com.simplemobiletools.gallery.pro.extensions.getProperPrimaryColor
import com.simplemobiletools.gallery.pro.extensions.getProperTextColor
import com.simplemobiletools.gallery.pro.extensions.isFingerPrintSensorAvailable
import com.simplemobiletools.gallery.pro.extensions.onGlobalLayout
import com.simplemobiletools.gallery.pro.extensions.setupDialogStuff
import com.simplemobiletools.gallery.pro.helpers.PROTECTION_FINGERPRINT
import com.simplemobiletools.gallery.pro.helpers.PROTECTION_PATTERN
import com.simplemobiletools.gallery.pro.helpers.PROTECTION_PIN
import com.simplemobiletools.gallery.pro.helpers.SHOW_ALL_TABS
import com.simplemobiletools.gallery.pro.helpers.isRPlus
import com.simplemobiletools.gallery.pro.interfaces.HashListener

class SecurityDialog(
    private val activity: Activity,
    private val requiredHash: String,
    private val showTabIndex: Int,
    private val callback: (hash: String, type: Int, success: Boolean) -> Unit
) : HashListener {
    private var dialog: AlertDialog? = null
    private val view = DialogSecurityBinding.inflate(LayoutInflater.from(activity), null, false)
    private var tabsAdapter: PasswordTypesAdapter
    private var viewPager: MyDialogViewPager

    init {
        view.apply {
            viewPager = dialogTabViewPager
            viewPager.offscreenPageLimit = 2
            tabsAdapter = PasswordTypesAdapter(
                context = root.context,
                requiredHash = requiredHash,
                hashListener = this@SecurityDialog,
                scrollView = dialogScrollview,
                biometricPromptHost = AuthPromptHost(activity as FragmentActivity),
                showBiometricIdTab = shouldShowBiometricIdTab(),
                showBiometricAuthentication = showTabIndex == PROTECTION_FINGERPRINT && isRPlus()
            )
            viewPager.adapter = tabsAdapter
            viewPager.onPageChangeListener {
                dialogTabLayout.getTabAt(it)?.select()
            }

            viewPager.onGlobalLayout {
                updateTabVisibility()
            }

            if (showTabIndex == SHOW_ALL_TABS) {
                val textColor = root.context.getProperTextColor()

                if (shouldShowBiometricIdTab()) {
                    val tabTitle = if (isRPlus()) R.string.biometrics else R.string.fingerprint
                    dialogTabLayout.addTab(
                        dialogTabLayout.newTab().setText(tabTitle),
                        PROTECTION_FINGERPRINT
                    )
                }

                if (activity.baseConfig.isUsingSystemTheme) {
                    dialogTabLayout.setBackgroundColor(activity.resources.getColor(R.color.you_dialog_background_color))
                } else {
                    dialogTabLayout.setBackgroundColor(root.context.getProperBackgroundColor())
                }

                dialogTabLayout.setTabTextColors(textColor, textColor)
                dialogTabLayout.setSelectedTabIndicatorColor(root.context.getProperPrimaryColor())
                dialogTabLayout.onTabSelectionChanged(tabSelectedAction = {
                    viewPager.currentItem = when {
                        it.text.toString().equals(
                            root.context.resources.getString(R.string.pattern),
                            true
                        ) -> PROTECTION_PATTERN

                        it.text.toString().equals(
                            root.context.resources.getString(R.string.pin),
                            true
                        ) -> PROTECTION_PIN

                        else -> PROTECTION_FINGERPRINT
                    }
                    updateTabVisibility()
                })
            } else {
                dialogTabLayout.beGone()
                viewPager.currentItem = showTabIndex
                viewPager.allowSwiping = false
            }
        }

        activity.getAlertDialogBuilder()
            .setOnCancelListener { onCancelFail() }
            .setNegativeButton(R.string.cancel) { _, _ -> onCancelFail() }
            .apply {
                activity.setupDialogStuff(view.root, this) { alertDialog ->
                    dialog = alertDialog
                }
            }
    }

    private fun onCancelFail() {
        callback("", 0, false)
        dialog?.dismiss()
    }

    override fun receivedHash(hash: String, type: Int) {
        callback(hash, type, true)
        if (!activity.isFinishing) {
            try {
                dialog?.dismiss()
            } catch (ignored: Exception) {
            }
        }
    }

    private fun updateTabVisibility() {
        for (i in 0..2) {
            tabsAdapter.isTabVisible(i, viewPager.currentItem == i)
        }
    }

    private fun shouldShowBiometricIdTab(): Boolean {
        return if (isRPlus()) {
            activity.isBiometricIdAvailable()
        } else {
            activity.isFingerPrintSensorAvailable()
        }
    }
}
