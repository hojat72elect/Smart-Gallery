package ca.hojat.smart.gallery.feature_about

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.ACTION_SENDTO
import android.content.Intent.EXTRA_EMAIL
import android.content.Intent.EXTRA_SUBJECT
import android.content.Intent.EXTRA_TEXT
import android.content.Intent.createChooser
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.shared.ui.dialogs.RateStarsAlertDialog
import ca.hojat.smart.gallery.shared.ui.dialogs.alert_dialog.rememberAlertDialogState
import ca.hojat.smart.gallery.shared.ui.extensions.enableEdgeToEdgeSimple
import ca.hojat.smart.gallery.shared.ui.extensions.rateStarsRedirectAndThankYou
import ca.hojat.smart.gallery.shared.ui.theme.AppThemeSurface
import ca.hojat.smart.gallery.shared.ui.dialogs.ConfirmationAdvancedAlertDialog
import ca.hojat.smart.gallery.shared.extensions.baseConfig
import ca.hojat.smart.gallery.shared.extensions.getStoreUrl
import ca.hojat.smart.gallery.shared.extensions.launchMoreAppsFromUsIntent
import ca.hojat.smart.gallery.shared.extensions.launchViewIntent
import ca.hojat.smart.gallery.shared.extensions.redirectToRateUs
import ca.hojat.smart.gallery.shared.extensions.showErrorToast
import ca.hojat.smart.gallery.shared.extensions.toast
import ca.hojat.smart.gallery.shared.helpers.APP_FAQ
import ca.hojat.smart.gallery.shared.helpers.APP_ICON_IDS
import ca.hojat.smart.gallery.shared.helpers.APP_LAUNCHER_NAME
import ca.hojat.smart.gallery.shared.helpers.APP_LICENSES
import ca.hojat.smart.gallery.shared.helpers.APP_NAME
import ca.hojat.smart.gallery.shared.helpers.APP_VERSION_NAME
import ca.hojat.smart.gallery.shared.helpers.SHOW_FAQ_BEFORE_MAIL
import ca.hojat.smart.gallery.shared.data.domain.FAQItem

class AboutActivity : ComponentActivity() {
    private val appName get() = intent.getStringExtra(APP_NAME) ?: ""

    private var firstVersionClickTS = 0L
    private var clicksSinceFirstClick = 0

    companion object {
        private const val EASTER_EGG_TIME_LIMIT = 3000L
        private const val EASTER_EGG_REQUIRED_CLICKS = 7
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdgeSimple()
        setContent {
            val context = LocalContext.current
            val resources = context.resources
            AppThemeSurface {
                val showExternalLinks =
                    remember { !resources.getBoolean(R.bool.hide_all_external_links) }
                val showGoogleRelations =
                    remember { !resources.getBoolean(R.bool.hide_google_relations) }
                val onEmailClickAlertDialogState = getOnEmailClickAlertDialogState()
                val rateStarsAlertDialogState = getRateStarsAlertDialogState()
                val onRateUsClickAlertDialogState =
                    getOnRateUsClickAlertDialogState(rateStarsAlertDialogState::show)
                AboutScreen(
                    goBack = ::finish,
                    helpUsSection = {
                        val showHelpUsSection =
                            remember { showGoogleRelations || !showExternalLinks }
                        HelpUsSection(
                            onRateUsClick = {
                                onRateUsClick(
                                    showConfirmationAdvancedDialog = onRateUsClickAlertDialogState::show,
                                    showRateStarsDialog = rateStarsAlertDialogState::show
                                )
                            },
                            onInviteClick = ::onInviteClick,
                            onContributorsClick = ::onContributorsClick,
                            showDonate = resources.getBoolean(R.bool.show_donate_in_about) && showExternalLinks,
                            onDonateClick = ::onDonateClick,
                            showInvite = showHelpUsSection,
                            showRateUs = showHelpUsSection
                        )
                    },
                    aboutSection = {
                        val setupFAQ = rememberFAQ()
                        if (!showExternalLinks || setupFAQ) {
                            AboutSection(
                                setupFAQ = setupFAQ,
                                onFAQClick = ::launchFAQActivity,
                                onEmailClick = {
                                    onEmailClick(onEmailClickAlertDialogState::show)
                                })
                        }
                    },
                    socialSection = {
                        if (showExternalLinks) {
                            SocialSection(
                                onFacebookClick = ::onFacebookClick,
                                onGithubClick = ::onGithubClick,
                                onRedditClick = ::onRedditClick,
                                onTelegramClick = ::onTelegramClick
                            )
                        }
                    }
                ) {
                    val (showWebsite, fullVersion) = showWebsiteAndFullVersion(
                        resources,
                        showExternalLinks
                    )
                    OtherSection(
                        showMoreApps = showGoogleRelations,
                        onMoreAppsClick = ::launchMoreAppsFromUsIntent,
                        showWebsite = showWebsite,
                        onWebsiteClick = ::onWebsiteClick,
                        showPrivacyPolicy = showExternalLinks,
                        onPrivacyPolicyClick = ::onPrivacyPolicyClick,
                        onLicenseClick = ::onLicenseClick,
                        version = fullVersion,
                        onVersionClick = ::onVersionClick
                    )
                }
            }
        }
    }

    @Composable
    private fun rememberFAQ() =
        remember { !(intent.getSerializableExtra(APP_FAQ) as? ArrayList<FAQItem>).isNullOrEmpty() }

    @SuppressLint("StringFormatMatches")
    @Composable
    private fun showWebsiteAndFullVersion(
        resources: Resources,
        showExternalLinks: Boolean
    ): Pair<Boolean, String> {
        val showWebsite =
            remember { resources.getBoolean(R.bool.show_donate_in_about) && !showExternalLinks }
        var version = intent.getStringExtra(APP_VERSION_NAME) ?: ""
        if (baseConfig.appId.removeSuffix(".debug").endsWith(".pro")) {
            version += " ${getString(R.string.pro)}"
        }
        val fullVersion =
            remember { String.format(getString(R.string.version_placeholder, version)) }
        return Pair(showWebsite, fullVersion)
    }

    @Composable
    private fun getRateStarsAlertDialogState() =
        rememberAlertDialogState().apply {
            DialogMember {
                RateStarsAlertDialog(
                    alertDialogState = this,
                    onRating = ::rateStarsRedirectAndThankYou
                )
            }
        }

    @Composable
    private fun getOnEmailClickAlertDialogState() =
        rememberAlertDialogState().apply {
            DialogMember {
                ConfirmationAdvancedAlertDialog(
                    alertDialogState = this,
                    message = "${getString(R.string.before_asking_question_read_faq)}\n\n${
                        getString(
                            R.string.make_sure_latest
                        )
                    }",
                    messageId = null,
                    positive = R.string.read_faq,
                    negative = R.string.skip
                ) { success ->
                    if (success) {
                        launchFAQActivity()
                    } else {
                        launchEmailIntent()
                    }
                }
            }
        }

    @Composable
    private fun getOnRateUsClickAlertDialogState(showRateStarsDialog: () -> Unit) =
        rememberAlertDialogState().apply {
            DialogMember {
                ConfirmationAdvancedAlertDialog(
                    alertDialogState = this,
                    message = "${getString(R.string.before_asking_question_read_faq)}\n\n${
                        getString(
                            R.string.make_sure_latest
                        )
                    }",
                    messageId = null,
                    positive = R.string.read_faq,
                    negative = R.string.skip
                ) { success ->
                    if (success) {
                        launchFAQActivity()
                    } else {
                        launchRateUsPrompt(showRateStarsDialog)
                    }
                }
            }
        }

    private fun onEmailClick(
        showConfirmationAdvancedDialog: () -> Unit
    ) {
        if (intent.getBooleanExtra(
                SHOW_FAQ_BEFORE_MAIL,
                false
            ) && !baseConfig.wasBeforeAskingShown
        ) {
            baseConfig.wasBeforeAskingShown = true
            showConfirmationAdvancedDialog()
        } else {
            launchEmailIntent()
        }
    }

    private fun launchFAQActivity() {
        val faqItems = intent.getSerializableExtra(APP_FAQ) as ArrayList<FAQItem>
        Intent(applicationContext, FAQActivity::class.java).apply {
            putExtra(
                APP_ICON_IDS,
                intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList<String>()
            )
            putExtra(APP_LAUNCHER_NAME, intent.getStringExtra(APP_LAUNCHER_NAME) ?: "")
            putExtra(APP_FAQ, faqItems)
            startActivity(this)
        }
    }

    @SuppressLint("StringFormatMatches")
    private fun launchEmailIntent() {
        val appVersion =
            String.format(getString(R.string.app_version, intent.getStringExtra(APP_VERSION_NAME)))
        val deviceOS = String.format(getString(R.string.device_os), Build.VERSION.RELEASE)
        val newline = "\n"
        val separator = "------------------------------"
        val body = "$appVersion$newline$deviceOS$newline$separator$newline$newline"

        val address = if (packageName.startsWith("ca.hojat.smart.gallery")) {
            getString(R.string.my_email)
        } else {
            getString(R.string.my_fake_email)
        }

        val selectorIntent = Intent(ACTION_SENDTO)
            .setData("mailto:$address".toUri())
        val emailIntent = Intent(ACTION_SEND).apply {
            putExtra(EXTRA_EMAIL, arrayOf(address))
            putExtra(EXTRA_SUBJECT, appName)
            putExtra(EXTRA_TEXT, body)
            selector = selectorIntent
        }

        try {
            startActivity(emailIntent)
        } catch (e: ActivityNotFoundException) {
            val chooser = createChooser(emailIntent, getString(R.string.send_email))
            try {
                startActivity(chooser)
            } catch (e: Exception) {
                toast(R.string.no_email_client_found)
            }
        } catch (e: Exception) {
            showErrorToast(e)
        }
    }

    private fun onRateUsClick(
        showConfirmationAdvancedDialog: () -> Unit,
        showRateStarsDialog: () -> Unit
    ) {
        if (baseConfig.wasBeforeRateShown) {
            launchRateUsPrompt(showRateStarsDialog)
        } else {
            baseConfig.wasBeforeRateShown = true
            showConfirmationAdvancedDialog()
        }
    }

    private fun launchRateUsPrompt(
        showRateStarsDialog: () -> Unit
    ) {
        if (baseConfig.wasAppRated) {
            redirectToRateUs()
        } else {
            showRateStarsDialog()
        }
    }

    private fun onInviteClick() {
        val text = String.format(getString(R.string.share_text), appName, getStoreUrl())
        Intent().apply {
            action = ACTION_SEND
            putExtra(EXTRA_SUBJECT, appName)
            putExtra(EXTRA_TEXT, text)
            type = "text/plain"
            startActivity(createChooser(this, getString(R.string.invite_via)))
        }
    }

    private fun onContributorsClick() {
        val intent = Intent(applicationContext, ContributorsActivity::class.java)
        startActivity(intent)
    }


    private fun onDonateClick() {
       // User wants to donate to us
    }

    private fun onFacebookClick() {
       // user wants to follow us on FB.
    }

    private fun onGithubClick() {
        launchViewIntent("https://github.com/hojat72elect")
    }

    private fun onRedditClick() {
       // user wants to follow us on reddit
    }


    private fun onTelegramClick() {
        launchViewIntent("https://t.me/hojat72elect")
    }


    private fun onWebsiteClick() {
        launchViewIntent("https://hojat72elect.github.io/")
    }

    private fun onPrivacyPolicyClick() {
       // User wants to see our privacy policy
    }

    private fun onLicenseClick() {
        Intent(applicationContext, LicenseActivity::class.java).apply {
            putExtra(
                APP_ICON_IDS,
                intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList<String>()
            )
            putExtra(APP_LAUNCHER_NAME, intent.getStringExtra(APP_LAUNCHER_NAME) ?: "")
            putExtra(APP_LICENSES, intent.getLongExtra(APP_LICENSES, 0))
            startActivity(this)
        }
    }

    private fun onVersionClick() {
        if (firstVersionClickTS == 0L) {
            firstVersionClickTS = System.currentTimeMillis()
            Handler(Looper.getMainLooper()).postDelayed({
                firstVersionClickTS = 0L
                clicksSinceFirstClick = 0
            }, EASTER_EGG_TIME_LIMIT)
        }

        clicksSinceFirstClick++
        if (clicksSinceFirstClick >= EASTER_EGG_REQUIRED_CLICKS) {
            toast(R.string.hello)
            firstVersionClickTS = 0L
            clicksSinceFirstClick = 0
        }
    }
}
