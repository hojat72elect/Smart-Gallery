package ca.hojat.smart.gallery.feature_lock

import android.content.Context
import android.util.AttributeSet
import androidx.biometric.auth.AuthPromptHost
import androidx.constraintlayout.widget.ConstraintLayout
import ca.hojat.smart.gallery.databinding.TabBiometricIdBinding
import ca.hojat.smart.gallery.shared.extensions.getContrastColor
import ca.hojat.smart.gallery.shared.extensions.getProperPrimaryColor
import ca.hojat.smart.gallery.shared.extensions.isWhiteTheme
import ca.hojat.smart.gallery.shared.extensions.showBiometricPrompt
import ca.hojat.smart.gallery.shared.extensions.updateTextColors
import ca.hojat.smart.gallery.shared.helpers.DARK_GREY
import ca.hojat.smart.gallery.shared.ui.views.MyScrollView

class BiometricIdTab(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs),
    SecurityTab {
    private lateinit var hashListener: HashListener
    private lateinit var biometricPromptHost: AuthPromptHost
    private lateinit var binding: TabBiometricIdBinding
    override fun onFinishInflate() {
        super.onFinishInflate()
        binding = TabBiometricIdBinding.bind(this)
        context.updateTextColors(binding.biometricLockHolder)
        val textColor = if (context.isWhiteTheme()) {
            DARK_GREY
        } else {
            context.getProperPrimaryColor().getContrastColor()
        }

        binding.openBiometricDialog.setTextColor(textColor)
        binding.openBiometricDialog.setOnClickListener {
            biometricPromptHost.activity?.showBiometricPrompt(successCallback = hashListener::receivedHash)
        }
    }

    override fun initTab(
        requiredHash: String,
        listener: HashListener,
        scrollView: MyScrollView,
        biometricPromptHost: AuthPromptHost,
        showBiometricAuthentication: Boolean
    ) {
        this.biometricPromptHost = biometricPromptHost
        hashListener = listener
        if (showBiometricAuthentication) {
            binding.openBiometricDialog.performClick()
        }
    }

    override fun visibilityChanged(isVisible: Boolean) {}
}
