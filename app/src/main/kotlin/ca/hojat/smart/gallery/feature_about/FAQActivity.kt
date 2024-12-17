package ca.hojat.smart.gallery.feature_about

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import ca.hojat.smart.gallery.shared.ui.extensions.enableEdgeToEdgeSimple
import ca.hojat.smart.gallery.shared.ui.theme.AppThemeSurface
import ca.hojat.smart.gallery.shared.helpers.APP_FAQ
import ca.hojat.smart.gallery.shared.data.domain.FAQItem
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
