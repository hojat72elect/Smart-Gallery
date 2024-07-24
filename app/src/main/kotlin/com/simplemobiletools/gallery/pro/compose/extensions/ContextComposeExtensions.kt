package com.simplemobiletools.gallery.pro.compose.extensions

import android.app.Activity
import android.content.Context
import com.simplemobiletools.commons.R
import com.simplemobiletools.gallery.pro.extensions.baseConfig
import com.simplemobiletools.gallery.pro.extensions.redirectToRateUs
import com.simplemobiletools.gallery.pro.extensions.toast
import com.simplemobiletools.gallery.pro.helpers.BaseConfig

val Context.config: BaseConfig get() = BaseConfig.newInstance(applicationContext)

fun Activity.rateStarsRedirectAndThankYou(stars: Int) {
    if (stars == 5) {
        redirectToRateUs()
    }
    toast(R.string.thank_you)
    baseConfig.wasAppRated = true
}
