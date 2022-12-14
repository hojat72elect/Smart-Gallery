package ca.on.sudbury.hojat.smartgallery.activities

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.RemoteViews
import android.widget.SeekBar
import ca.on.sudbury.hojat.smartgallery.R
import com.bumptech.glide.signature.ObjectKey
import ca.on.sudbury.hojat.smartgallery.extensions.getProperPrimaryColor
import ca.on.sudbury.hojat.smartgallery.extensions.fillWithColor
import ca.on.sudbury.hojat.smartgallery.extensions.getContrastColor
import ca.on.sudbury.hojat.smartgallery.extensions.adjustAlpha
import ca.on.sudbury.hojat.smartgallery.extensions.getProperBackgroundColor
import ca.on.sudbury.hojat.smartgallery.extensions.updateTextColors
import ca.on.sudbury.hojat.smartgallery.base.SimpleActivity
import ca.on.sudbury.hojat.smartgallery.databases.GalleryDatabase
import ca.on.sudbury.hojat.smartgallery.databinding.ActivityWidgetConfigBinding
import ca.on.sudbury.hojat.smartgallery.dialogs.ColorPickerDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.PickDirectoryDialogFragment
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.extensions.getFolderNameFromPath
import ca.on.sudbury.hojat.smartgallery.extensions.loadJpg
import ca.on.sudbury.hojat.smartgallery.extensions.getCachedDirectories
import ca.on.sudbury.hojat.smartgallery.extensions.widgetsDB
import ca.on.sudbury.hojat.smartgallery.helpers.SmartGalleryWidgetProvider
import ca.on.sudbury.hojat.smartgallery.helpers.ROUNDED_CORNERS_NONE
import ca.on.sudbury.hojat.smartgallery.models.Directory
import ca.on.sudbury.hojat.smartgallery.models.Widget
import ca.on.sudbury.hojat.smartgallery.usecases.ApplyColorFilterUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.BeVisibleOrGoneUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.RunOnBackgroundThreadUseCase

class WidgetConfigureActivity : SimpleActivity() {

    private lateinit var binding: ActivityWidgetConfigBinding

    private var mBgAlpha = 0f
    private var mWidgetId = 0
    private var mBgColor = 0
    private var mBgColorWithoutTransparency = 0
    private var mTextColor = 0
    private var mFolderPath = ""
    private var mDirectories = ArrayList<Directory>()

    public override fun onCreate(savedInstanceState: Bundle?) {
        useDynamicTheme = false
        super.onCreate(savedInstanceState)
        binding = ActivityWidgetConfigBinding.inflate(layoutInflater)

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

    override fun onResume() {
        super.onResume()
        setupToolbar(binding.configToolbar)
    }

    private fun initVariables() {
        mBgColor = config.widgetBgColor
        mBgAlpha = Color.alpha(mBgColor) / 255f

        mBgColorWithoutTransparency =
            Color.rgb(Color.red(mBgColor), Color.green(mBgColor), Color.blue(mBgColor))
        binding.configBgSeekbar.apply {
            progress = (mBgAlpha * 100).toInt()


            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    mBgAlpha = progress / 100f
                    updateBackgroundColor()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

        }
        updateBackgroundColor()

        mTextColor = config.widgetTextColor
        updateTextColor()
    }

    private fun saveConfig() {
        val views = RemoteViews(packageName, R.layout.widget)
        views.setInt(R.id.widget_holder, "setBackgroundColor", mBgColor)
        AppWidgetManager.getInstance(this)?.updateAppWidget(mWidgetId, views) ?: return
        config.showWidgetFolderName = binding.folderPickerShowFolderName.isChecked
        val widget = Widget(null, mWidgetId, mFolderPath)
        RunOnBackgroundThreadUseCase {
            widgetsDB.insertOrUpdate(widget)
        }

        storeWidgetColors()
        requestWidgetUpdate()

        Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId)
            setResult(RESULT_OK, this)
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
            SmartGalleryWidgetProvider::class.java
        ).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(mWidgetId))
            sendBroadcast(this)
        }
    }

    private fun updateBackgroundColor() {
        mBgColor = mBgColorWithoutTransparency.adjustAlpha(mBgAlpha)
        ApplyColorFilterUseCase(binding.configImageHolder.background, mBgColor)
        binding.configBgColor.fillWithColor(mBgColor, mBgColor)
        binding.configSave.backgroundTintList = ColorStateList.valueOf(getProperPrimaryColor())
    }

    private fun updateTextColor() {
        binding.configFolderName.setTextColor(mTextColor)
        binding.configTextColor.fillWithColor(mTextColor, mTextColor)
        binding.configSave.setTextColor(getProperPrimaryColor().getContrastColor())
    }

    private fun pickBackgroundColor() {
        val callback: (wasPositivePressed: Boolean, color: Int) -> Unit =
            { wasPositivePressed, color ->
                if (wasPositivePressed) {
                    mBgColorWithoutTransparency = color
                    updateBackgroundColor()
                }
            }
        ColorPickerDialogFragment(
            color = mBgColorWithoutTransparency,
            callback = callback
        ).show(
            supportFragmentManager,
            ColorPickerDialogFragment.TAG
        )
    }

    private fun pickTextColor() {
        val callback: (wasPositivePressed: Boolean, color: Int) -> Unit =
            { wasPositivePressed, color ->
                if (wasPositivePressed) {
                    mTextColor = color
                    updateTextColor()
                }
            }
        ColorPickerDialogFragment(color = mTextColor, callback = callback).show(
            supportFragmentManager,
            ColorPickerDialogFragment.TAG
        )
    }

    private fun changeSelectedFolder() {
        val callback: (String) -> Unit = { path ->
            updateFolderImage(path)
        }
        PickDirectoryDialogFragment(
            "",
            showFavoritesBin = true,
            isPickingCopyMoveDestination = false,
            callback = callback
        ).show(supportFragmentManager, PickDirectoryDialogFragment.TAG)
    }

    private fun updateFolderImage(folderPath: String) {
        mFolderPath = folderPath
        runOnUiThread {
            binding.folderPickerValue.text = getFolderNameFromPath(folderPath)
            binding.configFolderName.text = getFolderNameFromPath(folderPath)
        }

        RunOnBackgroundThreadUseCase {
            val path = GalleryDatabase.getInstance(applicationContext).DirectoryDao()
                .getDirectoryThumbnail(folderPath)
            if (path != null) {
                runOnUiThread {
                    val signature = ObjectKey(System.currentTimeMillis().toString())
                    loadJpg(
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
        BeVisibleOrGoneUseCase(binding.configFolderName, showFolderName)
    }
}
