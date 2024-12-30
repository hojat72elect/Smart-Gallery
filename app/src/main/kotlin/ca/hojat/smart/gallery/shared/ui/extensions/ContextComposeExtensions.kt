package ca.hojat.smart.gallery.shared.ui.extensions

import android.app.Activity
import android.content.Context
import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.shared.extensions.baseConfig
import ca.hojat.smart.gallery.shared.extensions.redirectToRateUs
import ca.hojat.smart.gallery.shared.helpers.BaseConfig
import ca.hojat.smart.gallery.shared.usecases.ShowToastUseCase

val Context.config: BaseConfig get() = BaseConfig.newInstance(applicationContext)

fun Activity.rateStarsRedirectAndThankYou(stars: Int) {
    if (stars == 5) {
        redirectToRateUs()
    }
    ShowToastUseCase(this, R.string.thank_you)
    baseConfig.wasAppRated = true
}
