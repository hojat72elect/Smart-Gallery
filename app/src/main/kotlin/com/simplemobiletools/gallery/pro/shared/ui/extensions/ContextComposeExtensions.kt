package com.simplemobiletools.gallery.pro.shared.ui.extensions

import android.app.Activity
import android.content.Context
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.shared.extensions.baseConfig
import com.simplemobiletools.gallery.pro.shared.extensions.redirectToRateUs
import com.simplemobiletools.gallery.pro.shared.extensions.toast
import com.simplemobiletools.gallery.pro.shared.helpers.BaseConfig

val Context.config: BaseConfig get() = BaseConfig.newInstance(applicationContext)

fun Activity.rateStarsRedirectAndThankYou(stars: Int) {
    if (stars == 5) {
        redirectToRateUs()
    }
    toast(R.string.thank_you)
    baseConfig.wasAppRated = true
}
