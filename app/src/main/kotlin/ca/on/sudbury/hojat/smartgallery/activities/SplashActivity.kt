package ca.on.sudbury.hojat.smartgallery.activities

import android.content.Intent
import ca.on.sudbury.hojat.smartgallery.databases.GalleryDatabase
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.extensions.getFavoriteFromPath
import ca.on.sudbury.hojat.smartgallery.extensions.mediaDB
import ca.on.sudbury.hojat.smartgallery.models.Favorite
import ca.on.sudbury.hojat.smartgallery.usecases.RunOnBackgroundThreadUseCase

class SplashActivity : BaseSplashActivity() {
    override fun initActivity() {

        // check if previously selected favorite items have been properly migrated into the new Favorites table
        if (config.wereFavoritesMigrated) {
            launchActivity()
        } else {
            if (config.appRunCount == 0) {
                config.wereFavoritesMigrated = true
                launchActivity()
            } else {
                config.wereFavoritesMigrated = true
                RunOnBackgroundThreadUseCase {
                    val favorites = ArrayList<Favorite>()
                    val favoritePaths =
                        mediaDB.getFavorites().map { it.path }.toMutableList() as ArrayList<String>
                    favoritePaths.forEach {
                        favorites.add(getFavoriteFromPath(it))
                    }
                    GalleryDatabase.getInstance(applicationContext).FavoritesDao()
                        .insertAll(favorites)

                    runOnUiThread {
                        launchActivity()
                    }
                }
            }
        }
    }

    private fun launchActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
