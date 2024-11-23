package com.simplemobiletools.gallery.pro.new_architecture.shared.ui.adapters

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcelable
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.media3.common.util.UnstableApi
import androidx.viewpager.widget.PagerAdapter
import com.simplemobiletools.gallery.pro.new_architecture.feature_media_viewer.ViewPagerActivity
import com.simplemobiletools.gallery.pro.new_architecture.feature_media_viewer.PhotoFragment
import com.simplemobiletools.gallery.pro.new_architecture.feature_media_viewer.VideoFragment
import com.simplemobiletools.gallery.pro.new_architecture.feature_media_viewer.ViewPagerFragment
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.MEDIUM
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.SHOULD_INIT_FRAGMENT
import com.simplemobiletools.gallery.pro.new_architecture.shared.data.domain.Medium

@SuppressLint("NewApi")
@UnstableApi
class MyPagerAdapter(
    val activity: ViewPagerActivity,
    fm: FragmentManager,
    val media: MutableList<Medium>
) : FragmentStatePagerAdapter(fm) {
    private val fragments = HashMap<Int, ViewPagerFragment>()
    var shouldInitFragment = true

    override fun getCount() = media.size


    override fun getItem(position: Int): Fragment {
        val medium = media[position]
        val bundle = Bundle()
        bundle.putSerializable(MEDIUM, medium)
        bundle.putBoolean(SHOULD_INIT_FRAGMENT, shouldInitFragment)
        val fragment = if (medium.isVideo()) {
            VideoFragment()
        } else {
            PhotoFragment()
        }

        fragment.arguments = bundle
        fragment.listener = activity
        return fragment
    }

    override fun getItemPosition(item: Any) = PagerAdapter.POSITION_NONE

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position) as ViewPagerFragment
        fragments[position] = fragment
        return fragment
    }

    override fun destroyItem(container: ViewGroup, position: Int, any: Any) {
        fragments.remove(position)
        super.destroyItem(container, position, any)
    }

    fun getCurrentFragment(position: Int) = fragments[position]

    fun toggleFullscreen(isFullscreen: Boolean) {
        for ((_, fragment) in fragments) {
            fragment.fullscreenToggled(isFullscreen)
        }
    }

    // try fixing TransactionTooLargeException crash on Android Nougat, tip from https://stackoverflow.com/a/43193425/1967672
    override fun saveState(): Parcelable? {
        val bundle = super.saveState() as Bundle?
        bundle?.putParcelableArray("states", null)
        return bundle
    }
}
