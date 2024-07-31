package com.simplemobiletools.gallery.pro.new_architecture.about

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.compose.extensions.enableEdgeToEdgeSimple
import com.simplemobiletools.gallery.pro.compose.theme.AppThemeSurface
import com.simplemobiletools.gallery.pro.extensions.launchViewIntent
import com.simplemobiletools.gallery.pro.helpers.APP_LICENSES
import com.simplemobiletools.gallery.pro.helpers.LICENSE_ANDROID_LAME
import com.simplemobiletools.gallery.pro.helpers.LICENSE_APNG
import com.simplemobiletools.gallery.pro.helpers.LICENSE_AUDIO_RECORD_VIEW
import com.simplemobiletools.gallery.pro.helpers.LICENSE_AUTOFITTEXTVIEW
import com.simplemobiletools.gallery.pro.helpers.LICENSE_CROPPER
import com.simplemobiletools.gallery.pro.helpers.LICENSE_ESPRESSO
import com.simplemobiletools.gallery.pro.helpers.LICENSE_EVENT_BUS
import com.simplemobiletools.gallery.pro.helpers.LICENSE_EXOPLAYER
import com.simplemobiletools.gallery.pro.helpers.LICENSE_FILTERS
import com.simplemobiletools.gallery.pro.helpers.LICENSE_GESTURE_VIEWS
import com.simplemobiletools.gallery.pro.helpers.LICENSE_GIF_DRAWABLE
import com.simplemobiletools.gallery.pro.helpers.LICENSE_GLIDE
import com.simplemobiletools.gallery.pro.helpers.LICENSE_GSON
import com.simplemobiletools.gallery.pro.helpers.LICENSE_INDICATOR_FAST_SCROLL
import com.simplemobiletools.gallery.pro.helpers.LICENSE_JODA
import com.simplemobiletools.gallery.pro.helpers.LICENSE_LEAK_CANARY
import com.simplemobiletools.gallery.pro.helpers.LICENSE_M3U_PARSER
import com.simplemobiletools.gallery.pro.helpers.LICENSE_NUMBER_PICKER
import com.simplemobiletools.gallery.pro.helpers.LICENSE_OTTO
import com.simplemobiletools.gallery.pro.helpers.LICENSE_PANORAMA_VIEW
import com.simplemobiletools.gallery.pro.helpers.LICENSE_PATTERN
import com.simplemobiletools.gallery.pro.helpers.LICENSE_PDF_VIEWER
import com.simplemobiletools.gallery.pro.helpers.LICENSE_PDF_VIEW_PAGER
import com.simplemobiletools.gallery.pro.helpers.LICENSE_PHOTOVIEW
import com.simplemobiletools.gallery.pro.helpers.LICENSE_PICASSO
import com.simplemobiletools.gallery.pro.helpers.LICENSE_REPRINT
import com.simplemobiletools.gallery.pro.helpers.LICENSE_ROBOLECTRIC
import com.simplemobiletools.gallery.pro.helpers.LICENSE_RTL
import com.simplemobiletools.gallery.pro.helpers.LICENSE_SANSELAN
import com.simplemobiletools.gallery.pro.helpers.LICENSE_SMS_MMS
import com.simplemobiletools.gallery.pro.helpers.LICENSE_STETHO
import com.simplemobiletools.gallery.pro.helpers.LICENSE_SUBSAMPLING
import com.simplemobiletools.gallery.pro.helpers.LICENSE_ZIP4J
import com.simplemobiletools.gallery.pro.models.License
import com.simplemobiletools.gallery.pro.helpers.LICENSE_KOTLIN
import kotlinx.collections.immutable.toImmutableList

class LicenseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdgeSimple()
        setContent {
            val licenseMask = remember { intent.getLongExtra(APP_LICENSES, 0) or LICENSE_KOTLIN }
            val thirdPartyLicenses by remember {
                derivedStateOf {
                    initLicenses().filter { licenseMask and it.id != 0L }.toImmutableList()
                }
            }
            AppThemeSurface {
                LicenseScreen(
                    goBack = ::finish,
                    thirdPartyLicenses = thirdPartyLicenses,
                    onLicenseClick = ::launchViewIntent
                )
            }
        }
    }

    private fun initLicenses() = listOf(
        License(LICENSE_KOTLIN, R.string.kotlin_title, R.string.kotlin_text, R.string.kotlin_url),
        License(
            LICENSE_SUBSAMPLING,
            R.string.subsampling_title,
            R.string.subsampling_text,
            R.string.subsampling_url
        ),
        License(LICENSE_GLIDE, R.string.glide_title, R.string.glide_text, R.string.glide_url),
        License(
            LICENSE_CROPPER,
            R.string.cropper_title,
            R.string.cropper_text,
            R.string.cropper_url
        ),
        License(
            LICENSE_RTL,
            R.string.rtl_viewpager_title,
            R.string.rtl_viewpager_text,
            R.string.rtl_viewpager_url
        ),
        License(LICENSE_JODA, R.string.joda_title, R.string.joda_text, R.string.joda_url),
        License(LICENSE_STETHO, R.string.stetho_title, R.string.stetho_text, R.string.stetho_url),
        License(LICENSE_OTTO, R.string.otto_title, R.string.otto_text, R.string.otto_url),
        License(
            LICENSE_PHOTOVIEW,
            R.string.photoview_title,
            R.string.photoview_text,
            R.string.photoview_url
        ),
        License(
            LICENSE_PICASSO,
            R.string.picasso_title,
            R.string.picasso_text,
            R.string.picasso_url
        ),
        License(
            LICENSE_PATTERN,
            R.string.pattern_title,
            R.string.pattern_text,
            R.string.pattern_url
        ),
        License(
            LICENSE_REPRINT,
            R.string.reprint_title,
            R.string.reprint_text,
            R.string.reprint_url
        ),
        License(
            LICENSE_GIF_DRAWABLE,
            R.string.gif_drawable_title,
            R.string.gif_drawable_text,
            R.string.gif_drawable_url
        ),
        License(
            LICENSE_AUTOFITTEXTVIEW,
            R.string.autofittextview_title,
            R.string.autofittextview_text,
            R.string.autofittextview_url
        ),
        License(
            LICENSE_ROBOLECTRIC,
            R.string.robolectric_title,
            R.string.robolectric_text,
            R.string.robolectric_url
        ),
        License(
            LICENSE_ESPRESSO,
            R.string.espresso_title,
            R.string.espresso_text,
            R.string.espresso_url
        ),
        License(LICENSE_GSON, R.string.gson_title, R.string.gson_text, R.string.gson_url),
        License(
            LICENSE_LEAK_CANARY,
            R.string.leak_canary_title,
            R.string.leakcanary_text,
            R.string.leakcanary_url
        ),
        License(
            LICENSE_NUMBER_PICKER,
            R.string.number_picker_title,
            R.string.number_picker_text,
            R.string.number_picker_url
        ),
        License(
            LICENSE_EXOPLAYER,
            R.string.exoplayer_title,
            R.string.exoplayer_text,
            R.string.exoplayer_url
        ),
        License(
            LICENSE_PANORAMA_VIEW,
            R.string.panorama_view_title,
            R.string.panorama_view_text,
            R.string.panorama_view_url
        ),
        License(
            LICENSE_SANSELAN,
            R.string.sanselan_title,
            R.string.sanselan_text,
            R.string.sanselan_url
        ),
        License(
            LICENSE_FILTERS,
            R.string.filters_title,
            R.string.filters_text,
            R.string.filters_url
        ),
        License(
            LICENSE_GESTURE_VIEWS,
            R.string.gesture_views_title,
            R.string.gesture_views_text,
            R.string.gesture_views_url
        ),
        License(
            LICENSE_INDICATOR_FAST_SCROLL,
            R.string.indicator_fast_scroll_title,
            R.string.indicator_fast_scroll_text,
            R.string.indicator_fast_scroll_url
        ),
        License(
            LICENSE_EVENT_BUS,
            R.string.event_bus_title,
            R.string.event_bus_text,
            R.string.event_bus_url
        ),
        License(
            LICENSE_AUDIO_RECORD_VIEW,
            R.string.audio_record_view_title,
            R.string.audio_record_view_text,
            R.string.audio_record_view_url
        ),
        License(
            LICENSE_SMS_MMS,
            R.string.sms_mms_title,
            R.string.sms_mms_text,
            R.string.sms_mms_url
        ),
        License(LICENSE_APNG, R.string.apng_title, R.string.apng_text, R.string.apng_url),
        License(
            LICENSE_PDF_VIEW_PAGER,
            R.string.pdf_view_pager_title,
            R.string.pdf_view_pager_text,
            R.string.pdf_view_pager_url
        ),
        License(
            LICENSE_M3U_PARSER,
            R.string.m3u_parser_title,
            R.string.m3u_parser_text,
            R.string.m3u_parser_url
        ),
        License(
            LICENSE_ANDROID_LAME,
            R.string.android_lame_title,
            R.string.android_lame_text,
            R.string.android_lame_url
        ),
        License(
            LICENSE_PDF_VIEWER,
            R.string.pdf_viewer_title,
            R.string.pdf_viewer_text,
            R.string.pdf_viewer_url
        ),
        License(LICENSE_ZIP4J, R.string.zip4j_title, R.string.zip4j_text, R.string.zip4j_url)
    )
}
