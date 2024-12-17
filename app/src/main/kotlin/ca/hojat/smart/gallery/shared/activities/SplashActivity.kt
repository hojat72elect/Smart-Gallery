package ca.hojat.smart.gallery.shared.activities

import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.feature_home.HomeActivity
import ca.hojat.smart.gallery.shared.extensions.baseConfig
import ca.hojat.smart.gallery.shared.extensions.checkAppIconColor
import ca.hojat.smart.gallery.shared.extensions.checkAppSideloading
import ca.hojat.smart.gallery.shared.extensions.config
import ca.hojat.smart.gallery.shared.extensions.favoritesDB
import ca.hojat.smart.gallery.shared.extensions.getFavoriteFromPath
import ca.hojat.smart.gallery.shared.extensions.getSharedTheme
import ca.hojat.smart.gallery.shared.extensions.isUsingSystemDarkTheme
import ca.hojat.smart.gallery.shared.extensions.mediaDB
import ca.hojat.smart.gallery.shared.extensions.showSideloadingDialog
import ca.hojat.smart.gallery.shared.helpers.SIDELOADING_TRUE
import ca.hojat.smart.gallery.shared.helpers.SIDELOADING_UNCHECKED
import ca.hojat.smart.gallery.shared.helpers.ensureBackgroundThread
import ca.hojat.smart.gallery.shared.data.domain.Favorite

@OptIn(UnstableApi::class)
class SplashActivity : AppCompatActivity() {


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

        if (!baseConfig.isUsingAutoTheme && !baseConfig.isUsingSystemTheme) {
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

    private fun initActivity() {
        // check if previously selected favorite items have been properly migrated into the new Favorites table
        if (config.wereFavoritesMigrated) {
            launchActivity()
        } else {
            config.wereFavoritesMigrated = true
            ensureBackgroundThread {
                val favorites = ArrayList<Favorite>()
                val favoritePaths =
                    mediaDB.getFavorites().map { it.path }.toMutableList() as ArrayList<String>
                favoritePaths.forEach {
                    favorites.add(getFavoriteFromPath(it))
                }
                favoritesDB.insertAll(favorites)

                runOnUiThread {
                    launchActivity()
                }
            }
        }
    }

    private fun launchActivity() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}