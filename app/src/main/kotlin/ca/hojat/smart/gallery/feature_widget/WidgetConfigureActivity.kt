package ca.hojat.smart.gallery.feature_widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.media3.common.util.UnstableApi
import com.bumptech.glide.signature.ObjectKey
import ca.hojat.smart.gallery.shared.ui.dialogs.ColorPickerDialog
import ca.hojat.smart.gallery.shared.helpers.ensureBackgroundThread
import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.databinding.ActivityWidgetConfigBinding
import ca.hojat.smart.gallery.shared.ui.dialogs.PickDirectoryDialog
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
import ca.hojat.smart.gallery.shared.data.domain.Directory
import ca.hojat.smart.gallery.shared.data.domain.Widget
import ca.hojat.smart.gallery.shared.activities.BaseActivity

@UnstableApi
class WidgetConfigureActivity : BaseActivity() {
    private var mBgAlpha = 0f
    private var mWidgetId = 0
    private var mBgColor = 0
    private var mBgColorWithoutTransparency = 0
    private var mTextColor = 0
    private var mFolderPath = ""
    private var mDirectories = ArrayList<Directory>()

    private val binding by viewBinding(ActivityWidgetConfigBinding::inflate)

    public override fun onCreate(savedInstanceState: Bundle?) {
        useDynamicTheme = false
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        setContentView(binding.root)
        initVariables()

        mWidgetId = intent.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (mWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
        }

        binding.configSave.setOnClickListener { saveConfig() }
        binding.configBgColor.setOnClickListener { pickBackgroundColor() }
        binding.configTextColor.setOnClickListener { pickTextColor() }
        binding.folderPickerValue.setOnClickListener { changeSelectedFolder() }
        binding.configImageHolder.setOnClickListener { changeSelectedFolder() }

        updateTextColors(binding.folderPickerHolder)
        val primaryColor = getProperPrimaryColor()
        binding.configBgSeekbar.setColors(primaryColor)
        binding.folderPickerHolder.background = ColorDrawable(getProperBackgroundColor())

        binding.folderPickerShowFolderName.isChecked = config.showWidgetFolderName
        handleFolderNameDisplay()
        binding.folderPickerShowFolderNameHolder.setOnClickListener {
            binding.folderPickerShowFolderName.toggle()
            handleFolderNameDisplay()
        }

        getCachedDirectories(getVideosOnly = false, getImagesOnly = false) {
            mDirectories = it
            val path = it.firstOrNull()?.path
            if (path != null) {
                updateFolderImage(path)
            }
        }
    }

    private fun initVariables() {
        mBgColor = config.widgetBgColor
        mBgAlpha = Color.alpha(mBgColor) / 255f

        mBgColorWithoutTransparency =
            Color.rgb(Color.red(mBgColor), Color.green(mBgColor), Color.blue(mBgColor))
        binding.configBgSeekbar.apply {
            progress = (mBgAlpha * 100).toInt()

            onSeekBarChangeListener { progress ->
                mBgAlpha = progress / 100f
                updateBackgroundColor()
            }
        }
        updateBackgroundColor()

        mTextColor = config.widgetTextColor
        if (mTextColor == resources.getColor(R.color.default_widget_text_color) && config.isUsingSystemTheme) {
            mTextColor =
                resources.getColor(R.color.you_primary_color, theme)
        }

        updateTextColor()
    }

    private fun saveConfig() {
        val views = RemoteViews(packageName, R.layout.widget)
        views.setBackgroundColor(R.id.widget_holder, mBgColor)
        AppWidgetManager.getInstance(this)?.updateAppWidget(mWidgetId, views) ?: return
        config.showWidgetFolderName = binding.folderPickerShowFolderName.isChecked
        val widget = Widget(null, mWidgetId, mFolderPath)
        ensureBackgroundThread {
            widgetsDB.insertOrUpdate(widget)
        }

        storeWidgetColors()
        requestWidgetUpdate()

        Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId)
            setResult(Activity.RESULT_OK, this)
        }
        finish()
    }

    private fun storeWidgetColors() {
        config.apply {
            widgetBgColor = mBgColor
            widgetTextColor = mTextColor
        }
    }

    private fun requestWidgetUpdate() {
        Intent(
            AppWidgetManager.ACTION_APPWIDGET_UPDATE,
            null,
            this,
            MyWidgetProvider::class.java
        ).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(mWidgetId))
            sendBroadcast(this)
        }
    }

    private fun updateTextColor() {
        binding.configFolderName.setTextColor(mTextColor)
        binding.configTextColor.setFillWithStroke(mTextColor, mTextColor)
        binding.configSave.setTextColor(getProperPrimaryColor().getContrastColor())
    }

    private fun updateBackgroundColor() {
        mBgColor = mBgColorWithoutTransparency.adjustAlpha(mBgAlpha)
        binding.configImageHolder.background.applyColorFilter(mBgColor)
        binding.configBgColor.setFillWithStroke(mBgColor, mBgColor)
        binding.configSave.backgroundTintList = ColorStateList.valueOf(getProperPrimaryColor())
    }

    private fun pickBackgroundColor() {
        ColorPickerDialog(this, mBgColorWithoutTransparency) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                mBgColorWithoutTransparency = color
                updateBackgroundColor()
            }
        }
    }

    private fun pickTextColor() {
        ColorPickerDialog(this, mTextColor) { wasPositivePressed, color ->
            if (wasPositivePressed) {
                mTextColor = color
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
        mFolderPath = folderPath
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
