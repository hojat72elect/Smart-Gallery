package com.simplemobiletools.gallery.pro.new_architecture.shared.activities

import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.new_architecture.feature_home.HomeActivity
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.baseConfig
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.checkAppIconColor
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.checkAppSideloading
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.config
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.favoritesDB
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getFavoriteFromPath
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getSharedTheme
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.isUsingSystemDarkTheme
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.mediaDB
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.showSideloadingDialog
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.SIDELOADING_TRUE
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.SIDELOADING_UNCHECKED
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.ensureBackgroundThread
import com.simplemobiletools.gallery.pro.new_architecture.shared.data.domain.Favorite

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