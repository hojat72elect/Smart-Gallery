package ca.hojat.smart.gallery.feature_lock

import androidx.biometric.auth.AuthPromptHost
import ca.hojat.smart.gallery.shared.ui.views.MyScrollView

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
