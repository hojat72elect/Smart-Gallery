package com.simplemobiletools.gallery.pro.feature_lock

import androidx.biometric.auth.AuthPromptHost
import com.simplemobiletools.gallery.pro.shared.ui.views.MyScrollView

interface SecurityTab {
    fun initTab(
        requiredHash: String,
        listener: HashListener,
        scrollView: MyScrollView,
        biometricPromptHost: AuthPromptHost,
        showBiometricAuthentication: Boolean
    )

    fun visibilityChanged(isVisible: Boolean)
}
