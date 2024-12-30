package ca.hojat.smart.gallery.shared.usecases

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.MediaStore
import ca.hojat.smart.gallery.R

object LaunchCameraUseCase {
    operator fun invoke(activity:Activity){
        try {
            activity.startActivity( Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA))
        } catch (e: ActivityNotFoundException) {
            ShowToastUseCase(activity, R.string.no_app_found)
        } catch (e: Exception) {
            ShowToastUseCase(activity, "Error : $e")
        }
    }
}