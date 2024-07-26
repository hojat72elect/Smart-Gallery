package com.simplemobiletools.gallery.pro.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simplemobiletools.commons.R
import com.simplemobiletools.gallery.pro.extensions.baseConfig
import com.simplemobiletools.gallery.pro.extensions.checkAppIconColor
import com.simplemobiletools.gallery.pro.extensions.checkAppSideloading
import com.simplemobiletools.gallery.pro.extensions.getSharedTheme
import com.simplemobiletools.gallery.pro.extensions.isThankYouInstalled
import com.simplemobiletools.gallery.pro.extensions.isUsingSystemDarkTheme
import com.simplemobiletools.gallery.pro.extensions.showSideloadingDialog
import com.simplemobiletools.gallery.pro.helpers.SIDELOADING_TRUE
import com.simplemobiletools.gallery.pro.helpers.SIDELOADING_UNCHECKED

abstract class BaseSplashActivity : AppCompatActivity() {
    abstract fun initActivity()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (baseConfig.appSideloadingStatus == SIDELOADING_UNCHECKED) {
            if (checkAppSideloading()) {
                return
            }
        } else if (baseConfig.appSideloadingStatus == SIDELOADING_TRUE) {
            showSideloadingDialog()
            return
        }

        baseConfig.apply {
            if (isUsingAutoTheme) {
                val isUsingSystemDarkTheme = isUsingSystemDarkTheme()
                isUsingSharedTheme = false
                textColor =
                    resources.getColor(if (isUsingSystemDarkTheme) R.color.theme_dark_text_color else R.color.theme_light_text_color)
                backgroundColor =
                    resources.getColor(if (isUsingSystemDarkTheme) R.color.theme_dark_background_color else R.color.theme_light_background_color)
            }
        }

        if (!baseConfig.isUsingAutoTheme && !baseConfig.isUsingSystemTheme && isThankYouInstalled()) {
            getSharedTheme {
                if (it != null) {
                    baseConfig.apply {
                        wasSharedThemeForced = true
                        isUsingSharedTheme = true
                        wasSharedThemeEverActivated = true

                        textColor = it.textColor
                        backgroundColor = it.backgroundColor
                        primaryColor = it.primaryColor
                        accentColor = it.accentColor
                    }

                    if (baseConfig.appIconColor != it.appIconColor) {
                        baseConfig.appIconColor = it.appIconColor
                        checkAppIconColor()
                    }
                }
                initActivity()
            }
        } else {
            initActivity()
        }
    }
}
