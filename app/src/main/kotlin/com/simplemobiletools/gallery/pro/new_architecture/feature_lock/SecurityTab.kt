package com.simplemobiletools.gallery.pro.new_architecture.feature_lock

import androidx.biometric.auth.AuthPromptHost
import com.simplemobiletools.gallery.pro.new_architecture.shared.ui.views.MyScrollView

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
