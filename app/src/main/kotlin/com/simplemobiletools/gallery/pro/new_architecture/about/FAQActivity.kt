package com.simplemobiletools.gallery.pro.new_architecture.about

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import com.simplemobiletools.gallery.pro.compose.extensions.enableEdgeToEdgeSimple
import com.simplemobiletools.gallery.pro.compose.theme.AppThemeSurface
import com.simplemobiletools.gallery.pro.helpers.APP_FAQ
import com.simplemobiletools.gallery.pro.models.FAQItem
import kotlinx.collections.immutable.toImmutableList

class FAQActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdgeSimple()
        setContent {
            AppThemeSurface {
                val faqItems =
                    remember { intent.getSerializableExtra(APP_FAQ) as ArrayList<FAQItem> }
                FAQScreen(
                    goBack = ::finish,
                    faqItems = faqItems.toImmutableList()
                )
            }
        }
    }
}
