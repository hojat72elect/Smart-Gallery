package ca.hojat.smart.gallery.shared.ui.views

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.provider.Settings
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import ca.hojat.smart.gallery.shared.extensions.onGlobalLayout
import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.shared.extensions.audioManager
import ca.hojat.smart.gallery.shared.helpers.DRAG_THRESHOLD
import kotlin.math.abs

// allow horizontal swipes through the layout, else it can cause glitches at zoomed in images
class MediaSideScroll(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

    private var mTouchDownX = 0f
    private var mTouchDownY = 0f
    private var mTouchDownTime = 0L
    private var mTouchDownValue = -1
    private var mTempBrightness = 0
    private var mLastTouchY = 0f
    private var mViewHeight = 0
    private var mIsBrightnessScroll = false
    private var mPassTouches = false
    private var dragThreshold = DRAG_THRESHOLD * context.resources.displayMetrics.density

    private var mSlideInfoText = ""
    private var mSlideInfoFadeHandler = Handler()
    private var mParentView: ViewGroup? = null
    private var activity: Activity? = null
    private var doubleTap: ((Float, Float) -> Unit)? = null

    private lateinit var slideInfoView: TextView
    private lateinit var singleTap: (Float, Float) -> Unit

    fun initialize(
        activity: Activity,
        slideInfoView: TextView,
        isBrightness: Boolean,
        parentView: ViewGroup?,
        singleTap: (x: Float, y: Float) -> Unit,
        doubleTap: ((x: Float, y: Float) -> Unit)? = null
    ) {
        this.activity = activity
        this.slideInfoView = slideInfoView
        this.singleTap = singleTap
        this.doubleTap = doubleTap
        mParentView = parentView
        mIsBrightnessScroll = isBrightness
        mSlideInfoText =
            activity.getString(if (isBrightness) R.string.brightness else R.string.volume)
        onGlobalLayout {
            mViewHeight = height
        }
    }

    private val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                singleTap(e.rawX, e.rawY)
                return true
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                if (doubleTap != null) {
                    doubleTap!!.invoke(e.rawX, e.rawY)
                }
                return true
            }
        })

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (mPassTouches) {
            if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
                mPassTouches = false
            }
            return false
        }
        return super.dispatchTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mPassTouches && activity == null) {
            return false
        }

        gestureDetector.onTouchEvent(event)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mTouchDownX = event.rawX
                mTouchDownY = event.rawY
                mLastTouchY = event.rawY
                mTouchDownTime = System.currentTimeMillis()
                if (mIsBrightnessScroll) {
                    if (mTouchDownValue == -1) {
                        mTouchDownValue = getCurrentBrightness()
                    }
                } else {
                    mTouchDownValue = getCurrentVolume()
                }
            }

            MotionEvent.ACTION_MOVE -> {
                val diffX = mTouchDownX - event.rawX
                val diffY = mTouchDownY - event.rawY

                if (abs(diffY) > dragThreshold && abs(diffY) > abs(diffX)) {
                    var percent = ((diffY / mViewHeight) * 100).toInt() * 3
                    percent = 100.coerceAtMost((-100).coerceAtLeast(percent))

                    if ((percent == 100 && event.rawY > mLastTouchY) || (percent == -100 && event.rawY < mLastTouchY)) {
                        mTouchDownY = event.rawY
                        mTouchDownValue =
                            if (mIsBrightnessScroll) mTempBrightness else getCurrentVolume()
                    }

                    percentChanged(percent)
                } else if (abs(diffX) > dragThreshold || abs(diffY) > dragThreshold) {
                    if (!mPassTouches) {
                        event.action = MotionEvent.ACTION_DOWN
                        event.setLocation(event.rawX, event.rawY)
                        mParentView?.dispatchTouchEvent(event)
                    }
                    mPassTouches = true
                    mParentView?.dispatchTouchEvent(event)
                    return false
                }
                mLastTouchY = event.rawY
            }

            MotionEvent.ACTION_UP -> {
                if (mIsBrightnessScroll) {
                    mTouchDownValue = mTempBrightness
                }
            }
        }
        return true
    }

    private fun getCurrentVolume() =
        activity?.audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0

    private fun getCurrentBrightness(): Int {
        return try {
            Settings.System.getInt(activity!!.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        } catch (e: Settings.SettingNotFoundException) {
            70
        }
    }

    private fun percentChanged(percent: Int) {
        if (mIsBrightnessScroll) {
            brightnessPercentChanged(percent)
        } else {
            volumePercentChanged(percent)
        }
    }

    private fun volumePercentChanged(percent: Int) {
        val stream = AudioManager.STREAM_MUSIC
        val maxVolume = activity!!.audioManager.getStreamMaxVolume(stream)
        val percentPerPoint = 100 / maxVolume
        if (percentPerPoint == 0) {
            return
        }

        val addPoints = percent / percentPerPoint
        val newVolume = maxVolume.coerceAtMost(0.coerceAtLeast(mTouchDownValue + addPoints))
        activity!!.audioManager.setStreamVolume(stream, newVolume, 0)

        val absolutePercent = ((newVolume / maxVolume.toFloat()) * 100).toInt()
        showValue(absolutePercent)

        mSlideInfoFadeHandler.removeCallbacksAndMessages(null)
        mSlideInfoFadeHandler.postDelayed({
            slideInfoView.animate().alpha(0f)
        }, SLIDE_INFO_FADE_DELAY)
    }

    private fun brightnessPercentChanged(percent: Int) {
        val maxBrightness = 255f
        var newBrightness = (mTouchDownValue + 2.55 * percent).toFloat()
        newBrightness = maxBrightness.coerceAtMost(0f.coerceAtLeast(newBrightness))
        mTempBrightness = newBrightness.toInt()

        val absolutePercent = ((newBrightness / maxBrightness) * 100).toInt()
        showValue(absolutePercent)

        val attributes = activity!!.window.attributes
        attributes.screenBrightness = absolutePercent / 100f
        activity!!.window.attributes = attributes

        mSlideInfoFadeHandler.removeCallbacksAndMessages(null)
        mSlideInfoFadeHandler.postDelayed({
            slideInfoView.animate().alpha(0f)
        }, SLIDE_INFO_FADE_DELAY)
    }

    @SuppressLint("SetTextI18n")
    private fun showValue(percent: Int) {
        slideInfoView.apply {
            text = "$mSlideInfoText:\n$percent%"
            alpha = 1f
        }
    }

    companion object {
        private const val SLIDE_INFO_FADE_DELAY = 1000L
    }
}
