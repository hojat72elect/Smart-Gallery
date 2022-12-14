package ca.on.sudbury.hojat.smartgallery.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig
import ca.on.sudbury.hojat.smartgallery.extensions.getContrastColor
import ca.on.sudbury.hojat.smartgallery.extensions.getMyContentProviderCursorLoader
import ca.on.sudbury.hojat.smartgallery.extensions.getProperTextColor
import ca.on.sudbury.hojat.smartgallery.extensions.getSharedThemeSync
import ca.on.sudbury.hojat.smartgallery.extensions.getThemeId
import ca.on.sudbury.hojat.smartgallery.extensions.isUsingSystemDarkTheme
import ca.on.sudbury.hojat.smartgallery.extensions.fillWithColor
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.ActivityCustomizationBinding
import ca.on.sudbury.hojat.smartgallery.dialogs.ColorPickerDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.ConfirmationAdvancedDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.ConfirmationDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.LineColorPickerDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.RadioGroupDialogFragment
import ca.on.sudbury.hojat.smartgallery.helpers.APP_ICON_IDS
import ca.on.sudbury.hojat.smartgallery.helpers.APP_LAUNCHER_NAME
import ca.on.sudbury.hojat.smartgallery.helpers.DARK_GREY
import ca.on.sudbury.hojat.smartgallery.helpers.INVALID_NAVIGATION_BAR_COLOR
import ca.on.sudbury.hojat.smartgallery.helpers.BaseContentProvider
import ca.on.sudbury.hojat.smartgallery.helpers.SAVE_DISCARD_PROMPT_INTERVAL
import ca.on.sudbury.hojat.smartgallery.models.MyTheme
import ca.on.sudbury.hojat.smartgallery.models.RadioItem
import ca.on.sudbury.hojat.smartgallery.models.SharedTheme
import ca.on.sudbury.hojat.smartgallery.usecases.IsSPlusUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.ApplyColorFilterUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.BeVisibleOrGoneUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.CheckAppIconColorUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.RunOnBackgroundThreadUseCase
import kotlin.math.abs

private const val THEME_LIGHT = 0
private const val THEME_DARK = 1
private const val THEME_DARK_RED = 3
private const val THEME_BLACK_WHITE = 4
private const val THEME_CUSTOM = 5
private const val THEME_SHARED = 6
private const val THEME_WHITE = 7
private const val THEME_AUTO = 8
private const val THEME_SYSTEM = 9    // Material You

class CustomizationActivity : BaseSimpleActivity() {

    private lateinit var binding: ActivityCustomizationBinding

    private var curTextColor = 0
    private var curBackgroundColor = 0
    private var curPrimaryColor = 0
    private var curAccentColor = 0
    private var curAppIconColor = 0
    private var curSelectedThemeId = 0
    private var originalAppIconColor = 0
    private var lastSavePromptTS = 0L
    private var curNavigationBarColor = INVALID_NAVIGATION_BAR_COLOR
    private var hasUnsavedChanges = false
    private var predefinedThemes = LinkedHashMap<Int, MyTheme>()
    private var storedSharedTheme: SharedTheme? = null
    private var menu: Menu? = null

    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()
    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomizationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (baseConfig.defaultNavigationBarColor == INVALID_NAVIGATION_BAR_COLOR && baseConfig.navigationBarColor == INVALID_NAVIGATION_BAR_COLOR) {
            baseConfig.defaultNavigationBarColor = window.navigationBarColor
            baseConfig.navigationBarColor = window.navigationBarColor
        }

        initColorVariables()


        val cursorLoader = getMyContentProviderCursorLoader()
        RunOnBackgroundThreadUseCase {

            try {
                storedSharedTheme = getSharedThemeSync(cursorLoader)
                if (storedSharedTheme == null) {
                    baseConfig.isUsingSharedTheme = false
                } else {
                    baseConfig.wasSharedThemeEverActivated = true
                }

                runOnUiThread {
                    setupThemes()
                    val hideGoogleRelations = resources.getBoolean(R.bool.hide_google_relations)
                    BeVisibleOrGoneUseCase(
                        binding.applyToAllHolder,
                        storedSharedTheme == null && curSelectedThemeId != THEME_AUTO && curSelectedThemeId != THEME_SYSTEM && !hideGoogleRelations
                    )
                }
            } catch (e: Exception) {
                Toast.makeText(this, R.string.update_thank_you, Toast.LENGTH_LONG).show()
                finish()
            }
        }


        val textColor = if (baseConfig.isUsingSystemTheme) {
            getProperTextColor()
        } else {
            baseConfig.textColor
        }

        updateLabelColors(textColor)
        originalAppIconColor = baseConfig.appIconColor

        if (resources.getBoolean(R.bool.hide_google_relations)) {
            binding.applyToAllHolder.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        setTheme(getThemeId(getCurrentPrimaryColor()))

        if (!baseConfig.isUsingSystemTheme) {
            updateBackgroundColor(getCurrentBackgroundColor())
            updateActionbarColor(getCurrentStatusBarColor())
            updateNavigationBarColor(curNavigationBarColor)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_customization, menu)
        menu.findItem(R.id.save).isVisible = hasUnsavedChanges
        updateMenuItemColors(menu, true, getCurrentStatusBarColor())
        this.menu = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save -> saveChanges(true)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onBackPressed() {
        if (hasUnsavedChanges && System.currentTimeMillis() - lastSavePromptTS > SAVE_DISCARD_PROMPT_INTERVAL) {
            promptSaveDiscard()
        } else {
            super.onBackPressed()
        }
    }

    private fun setupThemes() {
        predefinedThemes.apply {
            if (IsSPlusUseCase()) {
                put(THEME_SYSTEM, getSystemThemeColors())
            }

            put(THEME_AUTO, getAutoThemeColors())
            put(
                THEME_LIGHT,
                MyTheme(
                    R.string.light_theme,
                    R.color.theme_light_text_color,
                    R.color.theme_light_background_color,
                    R.color.color_primary,
                    R.color.color_primary
                )
            )
            put(
                THEME_DARK,
                MyTheme(
                    R.string.dark_theme,
                    R.color.theme_dark_text_color,
                    R.color.theme_dark_background_color,
                    R.color.color_primary,
                    R.color.color_primary
                )
            )
            put(
                THEME_DARK_RED,
                MyTheme(
                    R.string.dark_red,
                    R.color.theme_dark_text_color,
                    R.color.theme_dark_background_color,
                    R.color.theme_dark_red_primary_color,
                    R.color.md_red_700
                )
            )
            put(
                THEME_WHITE,
                MyTheme(
                    R.string.white,
                    R.color.dark_grey,
                    android.R.color.white,
                    android.R.color.white,
                    R.color.color_primary
                )
            )
            put(
                THEME_BLACK_WHITE,
                MyTheme(
                    R.string.black_white,
                    android.R.color.white,
                    android.R.color.black,
                    android.R.color.black,
                    R.color.md_grey_black
                )
            )
            put(THEME_CUSTOM, MyTheme(R.string.custom, 0, 0, 0, 0))

            if (storedSharedTheme != null) {
                put(THEME_SHARED, MyTheme(R.string.shared, 0, 0, 0, 0))
            }
        }
        setupThemePicker()
        setupColorsPickers()
    }

    private fun setupThemePicker() {
        curSelectedThemeId = getCurrentThemeId()
        binding.customizationTheme.text = getThemeText()
        updateAutoThemeFields()
        handleAccentColorLayout()
        binding.customizationThemeHolder.setOnClickListener {
            if (baseConfig.wasAppIconCustomizationWarningShown) {
                themePickerClicked()
            } else {
                val callback: () -> Unit = {
                    baseConfig.wasAppIconCustomizationWarningShown = true
                    themePickerClicked()
                }
                ConfirmationDialogFragment(
                    message = "",
                    messageId = R.string.app_icon_color_warning,
                    positive = R.string.ok,
                    negative = 0,
                    callbackAfterDialogConfirmed = callback
                ).show(supportFragmentManager, ConfirmationDialogFragment.TAG)
            }
        }

        if (binding.customizationTheme.text.toString()
                .trim() == getString(R.string.system_default)
        ) {
            binding.applyToAllHolder.visibility = View.GONE
        }
    }

    private fun themePickerClicked() {
        val items = arrayListOf<RadioItem>()
        for ((key, value) in predefinedThemes) {
            items.add(RadioItem(key, getString(value.nameId)))
        }

        val callback: (Any) -> Unit = { newValue ->
            updateColorTheme(newValue as Int, true)
            if (
                newValue != THEME_CUSTOM &&
                newValue != THEME_SHARED &&
                newValue != THEME_AUTO &&
                newValue != THEME_SYSTEM &&
                !baseConfig.wasCustomThemeSwitchDescriptionShown
            ) {
                baseConfig.wasCustomThemeSwitchDescriptionShown = true
                Toast.makeText(this, R.string.changing_color_description, Toast.LENGTH_LONG).show()
            }

            val hideGoogleRelations = resources.getBoolean(R.bool.hide_google_relations)
            BeVisibleOrGoneUseCase(
                binding.applyToAllHolder,
                curSelectedThemeId != THEME_AUTO && curSelectedThemeId != THEME_SYSTEM && curSelectedThemeId != THEME_SHARED && !hideGoogleRelations
            )
            updateMenuItemColors(menu, true, getCurrentStatusBarColor())
        }
        RadioGroupDialogFragment(
            items = items,
            checkedItemId = curSelectedThemeId,
            callback = callback
        ).show(supportFragmentManager, RadioGroupDialogFragment.TAG)
    }

    private fun updateColorTheme(themeId: Int, useStored: Boolean = false) {
        curSelectedThemeId = themeId
        binding.customizationTheme.text = getThemeText()

        resources.apply {
            if (curSelectedThemeId == THEME_CUSTOM) {
                if (useStored) {
                    curTextColor = baseConfig.customTextColor
                    curBackgroundColor = baseConfig.customBackgroundColor
                    curPrimaryColor = baseConfig.customPrimaryColor
                    curAccentColor = baseConfig.customAccentColor
                    curNavigationBarColor = baseConfig.customNavigationBarColor
                    curAppIconColor = baseConfig.customAppIconColor
                    setTheme(getThemeId(curPrimaryColor))
                    updateMenuItemColors(menu, true, curPrimaryColor)
                    setupColorsPickers()
                } else {
                    baseConfig.customPrimaryColor = curPrimaryColor
                    baseConfig.customAccentColor = curAccentColor
                    baseConfig.customBackgroundColor = curBackgroundColor
                    baseConfig.customTextColor = curTextColor
                    baseConfig.customNavigationBarColor = curNavigationBarColor
                    baseConfig.customAppIconColor = curAppIconColor
                }
            } else if (curSelectedThemeId == THEME_SHARED) {
                if (useStored) {
                    storedSharedTheme?.apply {
                        curTextColor = textColor
                        curBackgroundColor = backgroundColor
                        curPrimaryColor = primaryColor
                        curAccentColor = accentColor
                        curAppIconColor = appIconColor
                        curNavigationBarColor = navigationBarColor
                    }
                    setTheme(getThemeId(curPrimaryColor))
                    setupColorsPickers()
                    updateMenuItemColors(menu, true, curPrimaryColor)
                }
            } else {
                val theme = predefinedThemes[curSelectedThemeId]!!
                curTextColor = getColor(theme.textColorId)
                curBackgroundColor = getColor(theme.backgroundColorId)

                if (curSelectedThemeId != THEME_AUTO && curSelectedThemeId != THEME_SYSTEM) {
                    curPrimaryColor = getColor(theme.primaryColorId)
                    curAccentColor = getColor(R.color.color_primary)
                    curAppIconColor = getColor(theme.appIconColorId)
                }

                curNavigationBarColor = getThemeNavigationColor(curSelectedThemeId)
                setTheme(getThemeId(getCurrentPrimaryColor()))
                colorChanged()
                updateMenuItemColors(menu, true, getCurrentStatusBarColor())
            }
        }

        hasUnsavedChanges = true
        invalidateOptionsMenu()
        updateLabelColors(getCurrentTextColor())
        updateBackgroundColor(getCurrentBackgroundColor())
        updateActionbarColor(getCurrentStatusBarColor())
        updateNavigationBarColor(curNavigationBarColor)
        updateAutoThemeFields()
        updateApplyToAllColors(getCurrentPrimaryColor())
        handleAccentColorLayout()
    }

    private fun getAutoThemeColors(): MyTheme {
        val isUsingSystemDarkTheme = isUsingSystemDarkTheme()
        val textColor =
            if (isUsingSystemDarkTheme) R.color.theme_dark_text_color else R.color.theme_light_text_color
        val backgroundColor =
            if (isUsingSystemDarkTheme) R.color.theme_dark_background_color else R.color.theme_light_background_color
        return MyTheme(
            R.string.auto_light_dark_theme,
            textColor,
            backgroundColor,
            R.color.color_primary,
            R.color.color_primary
        )
    }

    // doesn't really matter what colors we use here, everything will be taken from the system. Use the default dark theme values here.
    private fun getSystemThemeColors(): MyTheme {
        return MyTheme(
            R.string.system_default,
            R.color.theme_dark_text_color,
            R.color.theme_dark_background_color,
            R.color.color_primary,
            R.color.color_primary
        )
    }

    private fun getCurrentThemeId(): Int {
        if (baseConfig.isUsingSharedTheme) {
            return THEME_SHARED
        } else if ((baseConfig.isUsingSystemTheme && !hasUnsavedChanges) || curSelectedThemeId == THEME_SYSTEM) {
            return THEME_SYSTEM
        } else if (baseConfig.isUsingAutoTheme || curSelectedThemeId == THEME_AUTO) {
            return THEME_AUTO
        }

        var themeId = THEME_CUSTOM
        resources.apply {
            for ((key, value) in predefinedThemes.filter { it.key != THEME_CUSTOM && it.key != THEME_SHARED && it.key != THEME_AUTO && it.key != THEME_SYSTEM }) {
                if (curTextColor == getColor(value.textColorId) &&
                    curBackgroundColor == getColor(value.backgroundColorId) &&
                    curPrimaryColor == getColor(value.primaryColorId) &&
                    curAppIconColor == getColor(value.appIconColorId) &&
                    (curNavigationBarColor == baseConfig.defaultNavigationBarColor || curNavigationBarColor == -2)
                ) {
                    themeId = key
                }
            }
        }

        return themeId
    }

    private fun getThemeText(): String {
        var nameId = R.string.custom
        for ((key, value) in predefinedThemes) {
            if (key == curSelectedThemeId) {
                nameId = value.nameId
            }
        }
        return getString(nameId)
    }

    private fun getThemeNavigationColor(themeId: Int) = when (themeId) {
        THEME_BLACK_WHITE -> Color.BLACK
        THEME_WHITE -> Color.WHITE
        THEME_AUTO -> if (isUsingSystemDarkTheme()) Color.BLACK else -2
        THEME_LIGHT -> Color.WHITE
        THEME_DARK -> Color.BLACK
        else -> baseConfig.defaultNavigationBarColor
    }

    private fun updateAutoThemeFields() {
        arrayOf(
            binding.customizationTextColorHolder,
            binding.customizationBackgroundColorHolder,
            binding.customizationNavigationBarColorHolder
        ).forEach {
            BeVisibleOrGoneUseCase(
                it,
                curSelectedThemeId != THEME_AUTO && curSelectedThemeId != THEME_SYSTEM
            )
        }

        BeVisibleOrGoneUseCase(
            binding.customizationPrimaryColorHolder,
            curSelectedThemeId != THEME_SYSTEM
        )
    }

    private fun promptSaveDiscard() {
        lastSavePromptTS = System.currentTimeMillis()
        val callback: (Boolean) -> Unit = { result ->
            if (result) {
                saveChanges(true)
            } else {
                resetColors()
                finish()
            }
        }
        ConfirmationAdvancedDialogFragment(
            message = "",
            messageId = R.string.save_before_closing,
            positive = R.string.save,
            negative = R.string.discard,
            callback = callback
        ).show(
            supportFragmentManager,
            ConfirmationAdvancedDialogFragment.TAG
        )
    }

    private fun saveChanges(finishAfterSave: Boolean) {
        val didAppIconColorChange = curAppIconColor != originalAppIconColor
        baseConfig.apply {
            textColor = curTextColor
            backgroundColor = curBackgroundColor
            primaryColor = curPrimaryColor
            accentColor = curAccentColor
            appIconColor = curAppIconColor

            // -1 is used as an invalid value, lets make use of it for white
            navigationBarColor = if (curNavigationBarColor == INVALID_NAVIGATION_BAR_COLOR) {
                -2
            } else {
                curNavigationBarColor
            }
        }

        if (didAppIconColorChange) {
            CheckAppIconColorUseCase(this)
        }

        if (curSelectedThemeId == THEME_SHARED) {
            val newSharedTheme = SharedTheme(
                curTextColor,
                curBackgroundColor,
                curPrimaryColor,
                curAppIconColor,
                curNavigationBarColor,
                0,
                curAccentColor
            )
            updateSharedTheme(newSharedTheme)
            Intent().apply {
                action = BaseContentProvider.SHARED_THEME_UPDATED
                sendBroadcast(this)
            }
        }

        baseConfig.isUsingSharedTheme = curSelectedThemeId == THEME_SHARED
        baseConfig.shouldUseSharedTheme = curSelectedThemeId == THEME_SHARED
        baseConfig.isUsingAutoTheme = curSelectedThemeId == THEME_AUTO
        baseConfig.isUsingSystemTheme = curSelectedThemeId == THEME_SYSTEM

        hasUnsavedChanges = false
        if (finishAfterSave) {
            finish()
        } else {
            invalidateOptionsMenu()
        }
    }

    private fun resetColors() {
        hasUnsavedChanges = false
        invalidateOptionsMenu()
        initColorVariables()
        setupColorsPickers()
        updateBackgroundColor()
        updateActionbarColor()
        updateNavigationBarColor()
        invalidateOptionsMenu()
        updateLabelColors(getCurrentTextColor())
    }

    private fun initColorVariables() {
        curTextColor = baseConfig.textColor
        curBackgroundColor = baseConfig.backgroundColor
        curPrimaryColor = baseConfig.primaryColor
        curAccentColor = baseConfig.accentColor
        curAppIconColor = baseConfig.appIconColor
        curNavigationBarColor = baseConfig.navigationBarColor
    }

    private fun setupColorsPickers() {
        val textColor = getCurrentTextColor()
        val backgroundColor = getCurrentBackgroundColor()
        val primaryColor = getCurrentPrimaryColor()
        with(binding) {
            customizationTextColor.fillWithColor(textColor, backgroundColor)
            customizationPrimaryColor.fillWithColor(primaryColor, backgroundColor)
            customizationAccentColor.fillWithColor(curAccentColor, backgroundColor)
            customizationBackgroundColor.fillWithColor(backgroundColor, backgroundColor)
            customizationAppIconColor.fillWithColor(curAppIconColor, backgroundColor)
            customizationNavigationBarColor.fillWithColor(
                curNavigationBarColor,
                backgroundColor
            )
            applyToAll.setTextColor(primaryColor.getContrastColor())

            customizationTextColorHolder.setOnClickListener { pickTextColor() }
            customizationBackgroundColorHolder.setOnClickListener { pickBackgroundColor() }
            customizationPrimaryColorHolder.setOnClickListener { pickPrimaryColor() }
            customizationAccentColorHolder.setOnClickListener { pickAccentColor() }

            handleAccentColorLayout()
            customizationNavigationBarColorHolder.setOnClickListener { pickNavigationBarColor() }
            applyToAll.setOnClickListener {
                applyToAll()
            }

            customizationAppIconColorHolder.setOnClickListener {
                if (baseConfig.wasAppIconCustomizationWarningShown) {
                    pickAppIconColor()
                } else {
                    val callback = {
                        baseConfig.wasAppIconCustomizationWarningShown = true
                        pickAppIconColor()
                    }
                    ConfirmationDialogFragment(
                        message = "",
                        messageId = R.string.app_icon_color_warning,
                        positive = R.string.ok,
                        negative = 0,
                        callbackAfterDialogConfirmed = callback
                    ).show(
                        supportFragmentManager,
                        ConfirmationDialogFragment.TAG
                    )
                }
            }
        }
    }

    private fun hasColorChanged(old: Int, new: Int) = abs(old - new) > 1

    private fun colorChanged() {
        hasUnsavedChanges = true
        setupColorsPickers()
        invalidateOptionsMenu()
    }

    private fun setCurrentTextColor(color: Int) {
        curTextColor = color
        updateLabelColors(color)
    }

    private fun setCurrentBackgroundColor(color: Int) {
        curBackgroundColor = color
        updateBackgroundColor(color)
    }

    private fun setCurrentPrimaryColor(color: Int) {
        curPrimaryColor = color
        updateActionbarColor(color)
        updateApplyToAllColors(color)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun updateApplyToAllColors(newColor: Int) {
        if (newColor == baseConfig.primaryColor && !baseConfig.isUsingSystemTheme) {
            binding.applyToAll.setBackgroundResource(R.drawable.button_background_rounded)
        } else {
            val applyBackground =
                resources.getDrawable(R.drawable.button_background_rounded, theme) as RippleDrawable
            ApplyColorFilterUseCase(
                (applyBackground as LayerDrawable).findDrawableByLayerId(R.id.button_background_holder),
                newColor
            )
            binding.applyToAll.background = applyBackground
        }
    }

    private fun setCurrentNavigationBarColor(color: Int) {
        curNavigationBarColor = color
        updateNavigationBarColor(color)
    }

    private fun handleAccentColorLayout() {
        BeVisibleOrGoneUseCase(
            binding.customizationAccentColorHolder,
            curSelectedThemeId == THEME_WHITE || isCurrentWhiteTheme() || curSelectedThemeId == THEME_BLACK_WHITE || isCurrentBlackAndWhiteTheme()
        )

        binding.customizationAccentColorLabel.text = getString(
            if (curSelectedThemeId == THEME_WHITE || isCurrentWhiteTheme()) {
                R.string.accent_color_white
            } else {
                R.string.accent_color_black_and_white
            }
        )
    }

    private fun isCurrentWhiteTheme() =
        curTextColor == DARK_GREY && curPrimaryColor == Color.WHITE && curBackgroundColor == Color.WHITE

    private fun isCurrentBlackAndWhiteTheme() =
        curTextColor == Color.WHITE && curPrimaryColor == Color.BLACK && curBackgroundColor == Color.BLACK

    private fun pickTextColor() {
        val callback: (wasPositivePressed: Boolean, color: Int) -> Unit =
            { wasPositivePressed, color ->
                if (wasPositivePressed) {
                    if (hasColorChanged(curTextColor, color)) {
                        setCurrentTextColor(color)
                        colorChanged()
                        updateColorTheme(getUpdatedTheme())
                    }
                }
            }
        ColorPickerDialogFragment(
            color = curTextColor,
            callback = callback
        ).show(
            supportFragmentManager,
            ColorPickerDialogFragment.TAG
        )
    }

    private fun pickBackgroundColor() {
        val callback: (wasPositivePressed: Boolean, color: Int) -> Unit =
            { wasPositivePressed, color ->
                if (wasPositivePressed) {
                    if (hasColorChanged(curBackgroundColor, color)) {
                        setCurrentBackgroundColor(color)
                        colorChanged()
                        updateColorTheme(getUpdatedTheme())
                    }
                }
            }
        ColorPickerDialogFragment(color = curBackgroundColor, callback = callback).show(
            supportFragmentManager,
            ColorPickerDialogFragment.TAG
        )
    }

    private fun pickPrimaryColor() {
        if (!packageName.startsWith(
                "ca.on.",
                true
            ) && baseConfig.appRunCount > 50
        ) {
            finish()
            return
        }


        val callback: (Boolean, Int) -> Unit = { wasPositivePressed, color ->
            if (wasPositivePressed) {
                if (hasColorChanged(curPrimaryColor, color)) {
                    setCurrentPrimaryColor(color)
                    colorChanged()
                    updateColorTheme(getUpdatedTheme())
                    setTheme(getThemeId(color))
                }
                updateMenuItemColors(menu, true, color)
            } else {
                updateActionbarColor(curPrimaryColor)
                setTheme(getThemeId(curPrimaryColor))
                updateMenuItemColors(menu, true, curPrimaryColor)
            }
        }

        LineColorPickerDialogFragment(
            color = curPrimaryColor,
            isPrimaryColorPicker = true,
            menu = menu,
            callback = callback
        ).show(
            supportFragmentManager,
            LineColorPickerDialogFragment.TAG
        )
    }

    private fun pickAccentColor() {
        val callback: (wasPositivePressed: Boolean, color: Int) -> Unit =
            { wasPositivePressed, color ->
                if (wasPositivePressed) {
                    if (hasColorChanged(curAccentColor, color)) {
                        curAccentColor = color
                        colorChanged()

                        if (isCurrentWhiteTheme() || isCurrentBlackAndWhiteTheme()) {
                            updateActionbarColor(getCurrentStatusBarColor())
                        }
                    }
                }
            }
        ColorPickerDialogFragment(color = curAccentColor, callback = callback).show(
            supportFragmentManager,
            ColorPickerDialogFragment.TAG
        )
    }

    private fun pickNavigationBarColor() {
        ColorPickerDialogFragment(
            curNavigationBarColor,
            true,
            showUseDefaultButton = true,
            currentColorCallback = {
                updateNavigationBarColor(it)
            },
            callback = { wasPositivePressed, color ->
                if (wasPositivePressed) {
                    setCurrentNavigationBarColor(color)
                    colorChanged()
                    updateColorTheme(getUpdatedTheme())
                } else {
                    updateNavigationBarColor(curNavigationBarColor)
                }
            }
        ).show(supportFragmentManager, ColorPickerDialogFragment.TAG)
    }

    private fun pickAppIconColor() {
        val callback: (Boolean, Int) -> Unit = { wasPositivePressed, color ->
            if (wasPositivePressed) {
                if (hasColorChanged(curAppIconColor, color)) {
                    curAppIconColor = color
                    colorChanged()
                    updateColorTheme(getUpdatedTheme())
                }
            }
        }
        LineColorPickerDialogFragment(
            color = curAppIconColor,
            isPrimaryColorPicker = false,
            primaryColors = R.array.md_app_icon_colors,
            appIconIDs = getAppIconIDs(),
            callback = callback
        ).show(supportFragmentManager, LineColorPickerDialogFragment.TAG)

    }

    private fun getUpdatedTheme() =
        if (curSelectedThemeId == THEME_SHARED) THEME_SHARED else getCurrentThemeId()

    private fun applyToAll() {
        val callback: () -> Unit = {
            Intent().apply {
                action = BaseContentProvider.SHARED_THEME_ACTIVATED
                sendBroadcast(this)
            }

            if (!predefinedThemes.containsKey(THEME_SHARED)) {
                predefinedThemes[THEME_SHARED] = MyTheme(R.string.shared, 0, 0, 0, 0)
            }
            baseConfig.wasSharedThemeEverActivated = true
            binding.applyToAllHolder.visibility = View.GONE
            updateColorTheme(THEME_SHARED)
            saveChanges(false)
        }
        ConfirmationDialogFragment(
            message = "",
            messageId = R.string.share_colors_success,
            positive = R.string.ok,
            negative = 0,
            callbackAfterDialogConfirmed = callback
        ).show(supportFragmentManager, ConfirmationDialogFragment.TAG)
    }

    private fun updateLabelColors(textColor: Int) {
        arrayListOf(
            binding.customizationThemeLabel,
            binding.customizationTheme,
            binding.customizationTextColorLabel,
            binding.customizationBackgroundColorLabel,
            binding.customizationPrimaryColorLabel,
            binding.customizationAccentColorLabel,
            binding.customizationAppIconColorLabel,
            binding.customizationNavigationBarColorLabel
        ).forEach {
            it.setTextColor(textColor)
        }

        val primaryColor = getCurrentPrimaryColor()
        binding.applyToAll.setTextColor(primaryColor.getContrastColor())
        updateApplyToAllColors(primaryColor)
    }

    private fun getCurrentTextColor() =
        if (binding.customizationTheme.text.toString()
                .trim() == getString(R.string.system_default)
        ) {
            resources.getColor(R.color.you_neutral_text_color)
        } else {
            curTextColor
        }

    private fun getCurrentBackgroundColor() =
        if (binding.customizationTheme.text.toString()
                .trim() == getString(R.string.system_default)
        ) {
            resources.getColor(R.color.you_background_color)
        } else {
            curBackgroundColor
        }

    private fun getCurrentPrimaryColor() =
        if (binding.customizationTheme.text.toString()
                .trim() == getString(R.string.system_default)
        ) {
            resources.getColor(R.color.you_primary_color)
        } else {
            curPrimaryColor
        }

    private fun getCurrentStatusBarColor() =
        if (binding.customizationTheme.text.toString()
                .trim() == getString(R.string.system_default)
        ) {
            resources.getColor(R.color.you_status_bar_color)
        } else {
            curPrimaryColor
        }

    private fun updateSharedTheme(sharedTheme: SharedTheme) {
        try {
            val contentValues = BaseContentProvider.fillThemeContentValues(sharedTheme)
            applicationContext.contentResolver.update(
                BaseContentProvider.MY_CONTENT_URI,
                contentValues,
                null,
                null
            )
        } catch (e: Exception) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
        }
    }
}
