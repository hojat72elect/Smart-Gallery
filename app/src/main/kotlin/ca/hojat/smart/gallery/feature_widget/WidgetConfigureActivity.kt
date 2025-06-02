package ca.hojat.smart.gallery.feature_widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.RemoteViews
import androidx.core.graphics.drawable.toDrawable
import androidx.media3.common.util.UnstableApi
import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.databinding.ActivityWidgetConfigBinding
import ca.hojat.smart.gallery.shared.activities.BaseActivity
import ca.hojat.smart.gallery.shared.data.domain.Directory
import ca.hojat.smart.gallery.shared.data.domain.Widget
import ca.hojat.smart.gallery.shared.extensions.adjustAlpha
import ca.hojat.smart.gallery.shared.extensions.applyColorFilter
import ca.hojat.smart.gallery.shared.extensions.beVisibleIf
import ca.hojat.smart.gallery.shared.extensions.config
import ca.hojat.smart.gallery.shared.extensions.directoryDB
import ca.hojat.smart.gallery.shared.extensions.getCachedDirectories
import ca.hojat.smart.gallery.shared.extensions.getContrastColor
import ca.hojat.smart.gallery.shared.extensions.getFolderNameFromPath
import ca.hojat.smart.gallery.shared.extensions.getProperBackgroundColor
import ca.hojat.smart.gallery.shared.extensions.getProperPrimaryColor
import ca.hojat.smart.gallery.shared.extensions.loadImageBase
import ca.hojat.smart.gallery.shared.extensions.onSeekBarChangeListener
import ca.hojat.smart.gallery.shared.extensions.setBackgroundColor
import ca.hojat.smart.gallery.shared.extensions.setFillWithStroke
import ca.hojat.smart.gallery.shared.extensions.updateTextColors
import ca.hojat.smart.gallery.shared.extensions.viewBinding
import ca.hojat.smart.gallery.shared.extensions.widgetsDB
import ca.hojat.smart.gallery.shared.helpers.MyWidgetProvider
import ca.hojat.smart.gallery.shared.helpers.ROUNDED_CORNERS_NONE
import ca.hojat.smart.gallery.shared.helpers.ensureBackgroundThread
import ca.hojat.smart.gallery.shared.ui.dialogs.ColorPickerDialog
import ca.hojat.smart.gallery.shared.ui.dialogs.PickDirectoryDialog
import com.bumptech.glide.signature.ObjectKey

/**
 * This activity is for configuring various properties of the app's widget.
 * This activity is available only for widgets.
 */
@UnstableApi
class WidgetConfigureActivity : BaseActivity() {
    private var backgroundAlpha = 0F
    private var appWidgetId = 0
    private var backgroundColor = 0
    private var backgroundColorWithoutTransparency = 0
    private var textColor = 0
    private var folderPath = ""
    private var directories = ArrayList<Directory>()

    private val binding by viewBinding(ActivityWidgetConfigBinding::inflate)

    public override fun onCreate(savedInstanceState: Bundle?) {
        useDynamicTheme = false
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        setContentView(binding.root)
        initializeVariables()

        appWidgetId = intent.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID)
            finish()


        binding.configSave.setOnClickListener { saveConfig() }
        binding.configBackgroundColor.setOnClickListener { pickBackgroundColor() }
        binding.configTextColor.setOnClickListener { pickTextColor() }
        binding.folderPickerValue.setOnClickListener { changeSelectedFolder() }
        binding.configImageHolder.setOnClickListener { changeSelectedFolder() }

        updateTextColors(binding.folderPickerHolder)
        val primaryColor = getProperPrimaryColor()
        binding.configBackgroundSeekbar.setColors(primaryColor)
        binding.folderPickerHolder.background = getProperBackgroundColor().toDrawable()

        binding.folderPickerShowFolderName.isChecked = config.showWidgetFolderName
        handleFolderNameDisplay()
        binding.folderPickerShowFolderNameHolder.setOnClickListener {
            binding.folderPickerShowFolderName.toggle()
            handleFolderNameDisplay()
        }

        getCachedDirectories(getVideosOnly = false, getImagesOnly = false) {
            directories = it
            val path = it.firstOrNull()?.path
            if (path != null) {
                updateFolderImage(path)
            }
        }
    }

    private fun initializeVariables() {
        backgroundColor = config.widgetBackgroundColor
        backgroundAlpha = Color.alpha(backgroundColor) / 255f

        backgroundColorWithoutTransparency =
            Color.rgb(Color.red(backgroundColor), Color.green(backgroundColor), Color.blue(backgroundColor))
        binding.configBackgroundSeekbar.apply {
            progress = (backgroundAlpha * 100).toInt()

            onSeekBarChangeListener { progress ->
                backgroundAlpha = progress / 100f
                updateBackgroundColor()
            }
        }
        updateBackgroundColor()

        textColor = config.widgetTextColor
        if (textColor == resources.getColor(R.color.default_widget_text_color) && config.isUsingSystemTheme) {
            textColor =
                resources.getColor(R.color.you_primary_color, theme)
        }

        updateTextColor()
    }

    /**
     * The button in the bottom for saving widget configuration will call this function.
     */
    private fun saveConfig() {
        val views = RemoteViews(packageName, R.layout.widget)
        views.setBackgroundColor(R.id.widget_holder, backgroundColor)
        AppWidgetManager.getInstance(this)?.updateAppWidget(appWidgetId, views) ?: return
        config.showWidgetFolderName = binding.folderPickerShowFolderName.isChecked
        val widget = Widget(null, appWidgetId, folderPath)
        ensureBackgroundThread {
            widgetsDB.insertOrUpdate(widget)
        }

        storeWidgetColors()
        requestWidgetUpdate()

        Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, this)
        }
        finish()
    }

    private fun storeWidgetColors() {
        config.apply {
            widgetBackgroundColor = this@WidgetConfigureActivity.backgroundColor
            widgetTextColor = this@WidgetConfigureActivity.textColor
        }
    }

    private fun requestWidgetUpdate() {
        Intent(
            AppWidgetManager.ACTION_APPWIDGET_UPDATE,
            null,
            this,
            MyWidgetProvider::class.java
        ).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
            sendBroadcast(this)
        }
    }

    private fun updateTextColor() {
        binding.configFolderName.setTextColor(textColor)
        binding.configTextColor.setFillWithStroke(textColor, textColor)
        binding.configSave.setTextColor(getProperPrimaryColor().getContrastColor())
    }

    private fun updateBackgroundColor() {
        backgroundColor = backgroundColorWithoutTransparency.adjustAlpha(backgroundAlpha)
        binding.configImageHolder.background.applyColorFilter(backgroundColor)
        binding.configBackgroundColor.setFillWithStroke(backgroundColor, backgroundColor)
        binding.configSave.backgroundTintList = ColorStateList.valueOf(getProperPrimaryColor())
    }

    private fun pickBackgroundColor() {
        ColorPickerDialog(this, backgroundColorWithoutTransparency) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                backgroundColorWithoutTransparency = color
                updateBackgroundColor()
            }
        }
    }

    private fun pickTextColor() {
        ColorPickerDialog(this, textColor) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                textColor = color
                updateTextColor()
            }
        }
    }


    private fun changeSelectedFolder() {
        PickDirectoryDialog(
            this, "",
            showOtherFolderButton = false,
            showFavoritesBin = true,
            isPickingCopyMoveDestination = false,
            isPickingFolderForWidget = true
        ) {
            updateFolderImage(it)
        }
    }

    private fun updateFolderImage(folderPath: String) {
        this.folderPath = folderPath
        runOnUiThread {
            binding.folderPickerValue.text = getFolderNameFromPath(folderPath)
            binding.configFolderName.text = getFolderNameFromPath(folderPath)
        }

        ensureBackgroundThread {
            val path = directoryDB.getDirectoryThumbnail(folderPath)
            if (path != null) {
                runOnUiThread {
                    val signature = ObjectKey(System.currentTimeMillis().toString())
                    loadImageBase(
                        path,
                        binding.configImage,
                        config.cropThumbnails,
                        ROUNDED_CORNERS_NONE,
                        signature
                    )
                }
            }
        }
    }

    private fun handleFolderNameDisplay() {
        val showFolderName = binding.folderPickerShowFolderName.isChecked
        binding.configFolderName.beVisibleIf(showFolderName)
    }
}
