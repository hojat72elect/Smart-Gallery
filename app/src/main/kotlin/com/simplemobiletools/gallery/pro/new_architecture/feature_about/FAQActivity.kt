package com.simplemobiletools.gallery.pro.new_architecture.feature_about

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import com.simplemobiletools.gallery.pro.new_architecture.shared.ui.extensions.enableEdgeToEdgeSimple
import com.simplemobiletools.gallery.pro.new_architecture.shared.ui.theme.AppThemeSurface
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.APP_FAQ
import com.simplemobiletools.gallery.pro.new_architecture.shared.data.domain.FAQItem
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
