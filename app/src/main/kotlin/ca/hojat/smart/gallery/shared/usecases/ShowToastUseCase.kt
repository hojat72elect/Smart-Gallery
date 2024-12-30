package ca.hojat.smart.gallery.shared.usecases

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.StringRes
import ca.hojat.smart.gallery.shared.helpers.isOnMainThread

object ShowToastUseCase {

    private fun doToast(context: Context, message: String, length: Int) {
        if (context is Activity) {
            if (context.isFinishing.not() && context.isDestroyed.not()) {
                Toast.makeText(context, message, length).show()
            }
        } else {
            Toast.makeText(context, message, length).show()
        }
    }

    operator fun invoke(context: Context, @StringRes id: Int, length: Int = Toast.LENGTH_SHORT) {
        this.invoke(context, context.getString(id), length)
    }

    operator fun invoke(context: Context, msg: String, length: Int = Toast.LENGTH_SHORT) {
        try {
            if (isOnMainThread()) {
                doToast(context, msg, length)
            } else {
                Handler(Looper.getMainLooper()).post {
                    doToast(context, msg, length)
                }
            }
        } catch (_: Exception) {
        }
    }
}