package com.simplemobiletools.gallery.pro.new_architecture.shared.activities

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.RecoverableSecurityException
import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.database.ContentObserver
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Point
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.MediaStore.Files
import android.provider.MediaStore.Images
import android.provider.MediaStore.Video
import android.provider.Settings
import android.telecom.TelecomManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.util.Pair
import androidx.core.view.ScrollingView
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.exifinterface.media.ExifInterface
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.simplemobiletools.gallery.pro.BuildConfig
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.new_architecture.feature_settings.CustomizationActivity
import com.simplemobiletools.gallery.pro.asynctasks.CopyMoveTask
import com.simplemobiletools.gallery.pro.new_architecture.shared.ui.dialogs.AllFilesPermissionDialog
import com.simplemobiletools.gallery.pro.new_architecture.shared.ui.dialogs.ConfirmationAdvancedDialog
import com.simplemobiletools.gallery.pro.new_architecture.shared.ui.dialogs.ConfirmationDialog
import com.simplemobiletools.gallery.pro.new_architecture.shared.ui.dialogs.ExportSettingsDialog
import com.simplemobiletools.gallery.pro.new_architecture.shared.ui.dialogs.FileConflictDialog
import com.simplemobiletools.gallery.pro.new_architecture.shared.ui.dialogs.FilePickerDialog
import com.simplemobiletools.gallery.pro.new_architecture.shared.ui.dialogs.PermissionRequiredDialog
import com.simplemobiletools.gallery.pro.new_architecture.shared.ui.dialogs.PickDirectoryDialog
import com.simplemobiletools.gallery.pro.new_architecture.shared.ui.dialogs.ResizeMultipleImagesDialog
import com.simplemobiletools.gallery.pro.new_architecture.shared.ui.dialogs.ResizeWithPathDialog
import com.simplemobiletools.gallery.pro.new_architecture.shared.ui.dialogs.WhatsNewDialog
import com.simplemobiletools.gallery.pro.new_architecture.shared.ui.dialogs.WritePermissionDialog
import com.simplemobiletools.gallery.pro.interfaces.CopyMoveListener
import com.simplemobiletools.gallery.pro.new_architecture.shared.data.domain.Android30RenameFormat
import com.simplemobiletools.gallery.pro.new_architecture.shared.data.domain.FAQItem
import com.simplemobiletools.gallery.pro.new_architecture.shared.data.domain.FileDirItem
import com.simplemobiletools.gallery.pro.new_architecture.shared.data.domain.Release
import com.simplemobiletools.gallery.pro.new_architecture.feature_about.AboutActivity
import com.simplemobiletools.gallery.pro.new_architecture.feature_settings.SettingsActivity
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.addBit
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.addPathToDB
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.adjustAlpha
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.applyColorFilter
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.baseConfig
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.buildDocumentUriSdk30
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.canManageMedia
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.config
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.copyNonDimensionAttributesTo
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.createAndroidDataOrObbPath
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.createAndroidDataOrObbUri
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.createAndroidSAFFile
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.createDirectorySync
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.createDocumentUriUsingFirstParentTreeUri
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.createFirstParentTreeUri
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.createFirstParentTreeUriUsingRootTree
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.createSAFFileSdk30
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.deleteAndroidSAFDirectory
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.deleteDBPath
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.deleteDocumentWithSAFSdk30
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.deleteFromMediaStore
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.deleteRecursively
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.directoryDB
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.doesThisOrParentHaveNoMedia
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.fixDateTaken
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.formatSize
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getAndroidSAFUri
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getAndroidTreeUri
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getAppIconColors
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getAvailableStorageB
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getColoredDrawableWithColor
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getColoredMaterialStatusBarColor
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getCompressionFormat
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getContrastColor
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getCurrentFormattedDateTime
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getDocumentFile
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getDoesFilePathExist
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getFileInputStreamSync
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getFileKey
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getFileOutputStreamSync
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getFileUrisFromFileDirItems
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getFilenameFromPath
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getFirstParentLevel
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getFirstParentPath
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getImageResolution
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getIntValue
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getIsPathDirectory
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getItemSize
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getLongValue
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getMimeType
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getParentPath
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getPermissionString
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getPicturesDirectoryPath
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getProperBackgroundColor
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getProperStatusBarColor
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getRealPathFromURI
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getSomeDocumentFile
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getThemeId
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.hasAllPermissions
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.hasPermission
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.hasProperStoredAndroidTreeUri
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.hasProperStoredDocumentUriSdk30
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.hasProperStoredFirstParentUri
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.hasProperStoredTreeUri
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.hideKeyboard
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.humanizePath
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.internalStoragePath
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.isAccessibleWithSAFSdk30
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.isAppInstalledOnSDCard
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.isExternalStorageManager
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.isInDownloadDir
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.isJpg
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.isPathOnInternalStorage
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.isPathOnOTG
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.isPathOnSD
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.isRecycleBinPath
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.isRestrictedSAFOnlyRoot
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.isRestrictedWithSAFSdk30
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.isSDCardSetAsDefaultStorage
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.isUsingGestureNavigation
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.mediaDB
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.navigationBarHeight
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.needsStupidWritePermissions
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.openNotificationSettings
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.recycleBin
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.recycleBinPath
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.removeBit
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.renameAndroidSAFDocument
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.renameDocumentSdk30
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.rescanAndDeletePath
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.rescanFolderMedia
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.rescanPath
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.rescanPaths
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.saveExifRotation
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.saveFile
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.saveImageRotation
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.scanPathRecursively
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.scanPathsRecursively
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.showErrorToast
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.showFileCreateError
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.statusBarHeight
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.storeAndroidTreeUri
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.toFileDirItem
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.toast
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.trySAFFileDelete
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.updateDBMediaPath
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.updateDirectoryPath
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.updateInMediaStore
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.updateLastModified
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.updateOTGPathFromPartition
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.writeLn
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.APP_FAQ
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.APP_ICON_IDS
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.APP_LAUNCHER_NAME
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.APP_LICENSES
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.APP_NAME
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.APP_VERSION_NAME
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.CONFLICT_KEEP_BOTH
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.CONFLICT_SKIP
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.CREATE_DOCUMENT_SDK_30
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.DARK_GREY
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.EXTERNAL_STORAGE_PROVIDER_AUTHORITY
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.EXTRA_SHOW_ADVANCED
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.HIGHER_ALPHA
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.LICENSE_APNG
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.LICENSE_CROPPER
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.LICENSE_EXOPLAYER
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.LICENSE_FILTERS
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.LICENSE_GESTURE_VIEWS
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.LICENSE_GIF_DRAWABLE
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.LICENSE_GLIDE
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.LICENSE_PANORAMA_VIEW
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.LICENSE_PATTERN
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.LICENSE_PICASSO
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.LICENSE_REPRINT
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.LICENSE_RTL
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.LICENSE_SANSELAN
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.LICENSE_SUBSAMPLING
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.MEDIUM_ALPHA
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.MyContextWrapper
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.NOMEDIA
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.NavigationIcon
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.OPEN_DOCUMENT_TREE_FOR_ANDROID_DATA_OR_OBB
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.OPEN_DOCUMENT_TREE_FOR_SDK_30
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.OPEN_DOCUMENT_TREE_OTG
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.OPEN_DOCUMENT_TREE_SD
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.PERMISSION_POST_NOTIFICATIONS
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.PERMISSION_READ_MEDIA_VISUAL_USER_SELECTED
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.PERMISSION_WRITE_STORAGE
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.RECYCLE_BIN
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.REQUEST_CODE_SET_DEFAULT_CALLER_ID
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.REQUEST_CODE_SET_DEFAULT_DIALER
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.SD_OTG_SHORT
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.SELECT_EXPORT_SETTINGS_FILE_INTENT
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.SHOW_FAQ_BEFORE_MAIL
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.ensureBackgroundThread
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.getConflictResolution
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.isNougatPlus
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.isOreoPlus
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.isPiePlus
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.isQPlus
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.isRPlus
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.isSPlus
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.isTiramisuPlus
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.isUpsideDownCakePlus
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.sumByLong
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.regex.Pattern

open class BaseActivity : AppCompatActivity() {

    private val observer = object : ContentObserver(null) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)
            if (uri != null) {
                val path = getRealPathFromURI(uri)
                if (path != null) {
                    updateDirectoryPath(path.getParentPath())
                    addPathToDB(path)
                }
            }
        }
    }
    private var materialScrollColorAnimation: ValueAnimator? = null
    var copyMoveCallback: ((destinationPath: String) -> Unit)? = null
    private var actionOnPermission: ((granted: Boolean) -> Unit)? = null
    private var isAskingPermissions = false
    var useDynamicTheme = true
    var showTransparentTop = false
    var isMaterialActivity =
        false      // by material activity we mean translucent navigation bar and opaque status and action bars
    private var checkedDocumentPath = ""
    private var currentScrollY = 0
    private var configItemsToExport = LinkedHashMap<String, Any>()

    private var mainCoordinatorLayout: CoordinatorLayout? = null
    private var nestedView: View? = null
    private var scrollingView: ScrollingView? = null
    private var toolbar: Toolbar? = null
    private var useTransparentNavigation = false
    private var useTopSearchMenu = false


    companion object {
        var funAfterSAFPermission: ((success: Boolean) -> Unit)? = null
        var funAfterSdk30Action: ((success: Boolean) -> Unit)? = null
        var funAfterUpdate30File: ((success: Boolean) -> Unit)? = null
        var funAfterTrash30File: ((success: Boolean) -> Unit)? = null
        var funRecoverableSecurity: ((success: Boolean) -> Unit)? = null
        var funAfterManageMediaPermission: (() -> Unit)? = null

        private const val GENERIC_PERM_HANDLER = 100
        private const val DELETE_FILE_SDK_30_HANDLER = 300
        private const val RECOVERABLE_SECURITY_HANDLER = 301
        private const val UPDATE_FILE_SDK_30_HANDLER = 302
        private const val MANAGE_MEDIA_RC = 303
        private const val TRASH_FILE_SDK_30_HANDLER = 304
    }

    fun getAppIconIDs(): ArrayList<Int> = arrayListOf(
        R.mipmap.ic_launcher_red,
        R.mipmap.ic_launcher_pink,
        R.mipmap.ic_launcher_purple,
        R.mipmap.ic_launcher_deep_purple,
        R.mipmap.ic_launcher_indigo,
        R.mipmap.ic_launcher_blue,
        R.mipmap.ic_launcher_light_blue,
        R.mipmap.ic_launcher_cyan,
        R.mipmap.ic_launcher_teal,
        R.mipmap.ic_launcher_green,
        R.mipmap.ic_launcher_light_green,
        R.mipmap.ic_launcher_lime,
        R.mipmap.ic_launcher_yellow,
        R.mipmap.ic_launcher_amber,
        R.mipmap.ic_launcher,
        R.mipmap.ic_launcher_deep_orange,
        R.mipmap.ic_launcher_brown,
        R.mipmap.ic_launcher_blue_grey,
        R.mipmap.ic_launcher_grey_black
    )

    private fun getAppLauncherName(): String = getString(R.string.app_launcher_name)

    override fun onCreate(savedInstanceState: Bundle?) {
        if (useDynamicTheme) {
            setTheme(getThemeId(showTransparentTop = showTransparentTop))
        }

        super.onCreate(savedInstanceState)
    }

    @SuppressLint("NewApi")
    override fun onResume() {
        super.onResume()
        if (useDynamicTheme) {
            setTheme(getThemeId(showTransparentTop = showTransparentTop))

            val backgroundColor = if (baseConfig.isUsingSystemTheme) {
                resources.getColor(R.color.you_background_color, theme)
            } else {
                baseConfig.backgroundColor
            }

            updateBackgroundColor(backgroundColor)
        }

        if (showTransparentTop) {
            window.statusBarColor = Color.TRANSPARENT
        } else if (!isMaterialActivity) {
            val color = if (baseConfig.isUsingSystemTheme) {
                resources.getColor(R.color.you_status_bar_color)
            } else {
                getProperStatusBarColor()
            }

            updateActionbarColor(color)
        }

        updateRecentsAppIcon()

        var navBarColor = getProperBackgroundColor()
        if (isMaterialActivity) {
            navBarColor = navBarColor.adjustAlpha(HIGHER_ALPHA)
        }

        updateNavigationBarColor(navBarColor)
    }

    override fun onDestroy() {
        super.onDestroy()
        funAfterSAFPermission = null
        actionOnPermission = null
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        handleNavigationAndScrolling()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                hideKeyboard()
                finish()
            }

            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun attachBaseContext(newBase: Context) {
        if (newBase.baseConfig.useEnglish && !isTiramisuPlus()) {
            super.attachBaseContext(MyContextWrapper(newBase).wrap(newBase, "en"))
        } else {
            super.attachBaseContext(newBase)
        }
    }

    protected fun checkNotchSupport() {
        if (isPiePlus()) {
            val cutoutMode = when {
                config.showNotch -> WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                else -> WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER
            }

            window.attributes.layoutInDisplayCutoutMode = cutoutMode
            if (config.showNotch) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            }
        }
    }

    protected fun registerFileUpdateListener() {
        try {
            contentResolver.registerContentObserver(
                Images.Media.EXTERNAL_CONTENT_URI,
                true,
                observer
            )
            contentResolver.registerContentObserver(
                Video.Media.EXTERNAL_CONTENT_URI,
                true,
                observer
            )
        } catch (ignored: Exception) {
        }
    }

    protected fun unregisterFileUpdateListener() {
        try {
            contentResolver.unregisterContentObserver(observer)
        } catch (ignored: Exception) {
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    protected fun showAddIncludedFolderDialog(callback: () -> Unit) {
        FilePickerDialog(
            activity = this,
            currPath = config.lastFilePickerPath,
            pickFile = false,
            showHidden = config.shouldShowHidden,
            showFAB = false,
            canAddShowHiddenButton = true
        ) {
            config.lastFilePickerPath = it
            config.addIncludedFolder(it)
            callback()
            ensureBackgroundThread {
                scanPathRecursively(it)
            }
        }
    }

    fun launchSettings() {
        hideKeyboard()
        startActivity(Intent(applicationContext, SettingsActivity::class.java))
    }

    fun launchAbout() {
        val licenses =
            LICENSE_GLIDE or LICENSE_CROPPER or LICENSE_RTL or LICENSE_SUBSAMPLING or LICENSE_PATTERN or LICENSE_REPRINT or LICENSE_GIF_DRAWABLE or
                    LICENSE_PICASSO or LICENSE_EXOPLAYER or LICENSE_PANORAMA_VIEW or LICENSE_SANSELAN or LICENSE_FILTERS or LICENSE_GESTURE_VIEWS or LICENSE_APNG

        val faqItems = arrayListOf(
            FAQItem(R.string.faq_3_title, R.string.faq_3_text),
            FAQItem(R.string.faq_12_title, R.string.faq_12_text),
            FAQItem(R.string.faq_7_title, R.string.faq_7_text),
            FAQItem(R.string.faq_14_title, R.string.faq_14_text),
            FAQItem(R.string.faq_1_title, R.string.faq_1_text),
            FAQItem(
                R.string.faq_5_title_commons,
                R.string.faq_5_text_commons
            ),
            FAQItem(R.string.faq_5_title, R.string.faq_5_text),
            FAQItem(R.string.faq_4_title, R.string.faq_4_text),
            FAQItem(R.string.faq_6_title, R.string.faq_6_text),
            FAQItem(R.string.faq_8_title, R.string.faq_8_text),
            FAQItem(R.string.faq_10_title, R.string.faq_10_text),
            FAQItem(R.string.faq_11_title, R.string.faq_11_text),
            FAQItem(R.string.faq_13_title, R.string.faq_13_text),
            FAQItem(R.string.faq_15_title, R.string.faq_15_text),
            FAQItem(R.string.faq_2_title, R.string.faq_2_text),
            FAQItem(R.string.faq_18_title, R.string.faq_18_text),
            FAQItem(
                R.string.faq_9_title_commons,
                R.string.faq_9_text_commons
            ),
        )

        if (!resources.getBoolean(R.bool.hide_google_relations)) {
            faqItems.add(
                FAQItem(
                    R.string.faq_2_title_commons,
                    R.string.faq_2_text_commons
                )
            )
            faqItems.add(
                FAQItem(
                    R.string.faq_6_title_commons,
                    R.string.faq_6_text_commons
                )
            )
            faqItems.add(
                FAQItem(
                    R.string.faq_7_title_commons,
                    R.string.faq_7_text_commons
                )
            )
            faqItems.add(
                FAQItem(
                    R.string.faq_10_title_commons,
                    R.string.faq_10_text_commons
                )
            )
        }

        if (isRPlus() && !isExternalStorageManager()) {
            faqItems.add(
                0,
                FAQItem(
                    R.string.faq_16_title,
                    "${getString(R.string.faq_16_text)} ${getString(R.string.faq_16_text_extra)}"
                )
            )
            faqItems.add(1, FAQItem(R.string.faq_17_title, R.string.faq_17_text))
            faqItems.removeIf { it.text == R.string.faq_7_text }
            faqItems.removeIf { it.text == R.string.faq_14_text }
            faqItems.removeIf { it.text == R.string.faq_8_text }
        }

        startAboutActivity(R.string.app_name, licenses, BuildConfig.VERSION_NAME, faqItems, true)
    }


    fun updateBackgroundColor(color: Int = baseConfig.backgroundColor) {
        window.decorView.setBackgroundColor(color)
    }

    fun updateStatusbarColor(color: Int) {
        window.statusBarColor = color

        if (color.getContrastColor() == DARK_GREY) {
            window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility.addBit(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        } else {
            window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility.removeBit(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        }
    }

    fun updateActionbarColor(color: Int = getProperStatusBarColor()) {
        updateStatusbarColor(color)
        setTaskDescription(ActivityManager.TaskDescription(null, null, color))
    }

    private fun updateNavigationBarColor(color: Int) {
        window.navigationBarColor = color
        updateNavigationBarButtons(color)
    }

    private fun updateNavigationBarButtons(color: Int) {
        if (isOreoPlus()) {
            if (color.getContrastColor() == DARK_GREY) {
                window.decorView.systemUiVisibility =
                    window.decorView.systemUiVisibility.addBit(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
            } else {
                window.decorView.systemUiVisibility =
                    window.decorView.systemUiVisibility.removeBit(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
            }
        }
    }

    // use translucent navigation bar, set the background color to action and status bars
    fun updateMaterialActivityViews(
        mainCoordinatorLayout: CoordinatorLayout?,
        nestedView: View?,
        useTransparentNavigation: Boolean,
        useTopSearchMenu: Boolean,
    ) {
        this.mainCoordinatorLayout = mainCoordinatorLayout
        this.nestedView = nestedView
        this.useTransparentNavigation = useTransparentNavigation
        this.useTopSearchMenu = useTopSearchMenu
        handleNavigationAndScrolling()

        val backgroundColor = getProperBackgroundColor()
        updateStatusbarColor(backgroundColor)
        updateActionbarColor(backgroundColor)
    }

    private fun handleNavigationAndScrolling() {
        if (useTransparentNavigation) {
            if (navigationBarHeight > 0 || isUsingGestureNavigation()) {
                window.decorView.systemUiVisibility =
                    window.decorView.systemUiVisibility.addBit(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
                updateTopBottomInsets(statusBarHeight, navigationBarHeight)
                // Don't touch this. Window Inset API often has a domino effect and things will most likely break.
                onApplyWindowInsets {
                    val insets =
                        it.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
                    updateTopBottomInsets(insets.top, insets.bottom)
                }
            } else {
                window.decorView.systemUiVisibility =
                    window.decorView.systemUiVisibility.removeBit(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
                updateTopBottomInsets(0, 0)
            }
        }
    }

    private fun updateTopBottomInsets(top: Int, bottom: Int) {
        nestedView?.run {
            setPadding(paddingLeft, paddingTop, paddingRight, bottom)
        }
        (mainCoordinatorLayout?.layoutParams as? FrameLayout.LayoutParams)?.topMargin = top
    }

    // colorize the top toolbar and statusbar at scrolling down a bit
    fun setupMaterialScrollListener(scrollingView: ScrollingView?, toolbar: Toolbar) {
        this.scrollingView = scrollingView
        this.toolbar = toolbar
        if (scrollingView is RecyclerView) {
            scrollingView.setOnScrollChangeListener { _, _, _, _, _ ->
                val newScrollY = scrollingView.computeVerticalScrollOffset()
                scrollingChanged(newScrollY, currentScrollY)
                currentScrollY = newScrollY
            }
        } else if (scrollingView is NestedScrollView) {
            scrollingView.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                scrollingChanged(scrollY, oldScrollY)
            }
        }
    }

    private fun scrollingChanged(newScrollY: Int, oldScrollY: Int) {
        if (newScrollY > 0 && oldScrollY == 0) {
            val colorFrom = window.statusBarColor
            val colorTo = getColoredMaterialStatusBarColor()
            animateTopBarColors(colorFrom, colorTo)
        } else if (newScrollY == 0 && oldScrollY > 0) {
            val colorFrom = window.statusBarColor
            val colorTo = getRequiredStatusBarColor()
            animateTopBarColors(colorFrom, colorTo)
        }
    }

    private fun animateTopBarColors(colorFrom: Int, colorTo: Int) {
        if (toolbar == null) {
            return
        }

        materialScrollColorAnimation?.end()
        materialScrollColorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        materialScrollColorAnimation!!.addUpdateListener { animator ->
            val color = animator.animatedValue as Int
            if (toolbar != null) {
                updateTopBarColors(toolbar!!, color)
            }
        }

        materialScrollColorAnimation!!.start()
    }

    private fun getRequiredStatusBarColor(): Int {
        return if ((scrollingView is RecyclerView || scrollingView is NestedScrollView) && scrollingView?.computeVerticalScrollOffset() == 0) {
            getProperBackgroundColor()
        } else {
            getColoredMaterialStatusBarColor()
        }
    }

    fun updateTopBarColors(toolbar: Toolbar, color: Int) {
        val contrastColor = if (useTopSearchMenu) {
            getProperBackgroundColor().getContrastColor()
        } else {
            color.getContrastColor()
        }

        if (!useTopSearchMenu) {
            updateStatusbarColor(color)
            toolbar.setBackgroundColor(color)
            toolbar.setTitleTextColor(contrastColor)
            toolbar.navigationIcon?.applyColorFilter(contrastColor)
            toolbar.collapseIcon = resources.getColoredDrawableWithColor(
                R.drawable.ic_arrow_left_vector,
                contrastColor
            )
        }

        toolbar.overflowIcon =
            resources.getColoredDrawableWithColor(R.drawable.ic_three_dots_vector, contrastColor)

        val menu = toolbar.menu
        for (i in 0 until menu.size()) {
            try {
                menu.getItem(i)?.icon?.setTint(contrastColor)
            } catch (ignored: Exception) {
            }
        }
    }

    fun updateStatusBarOnPageChange() {
        if (scrollingView is RecyclerView || scrollingView is NestedScrollView) {
            val scrollY = scrollingView!!.computeVerticalScrollOffset()
            val colorFrom = window.statusBarColor
            val colorTo = if (scrollY > 0) {
                getColoredMaterialStatusBarColor()
            } else {
                getRequiredStatusBarColor()
            }
            animateTopBarColors(colorFrom, colorTo)
            currentScrollY = scrollY
        }
    }

    fun setupToolbar(
        toolbar: Toolbar,
        toolbarNavigationIcon: NavigationIcon = NavigationIcon.None,
        statusBarColor: Int = getRequiredStatusBarColor(),
        searchMenuItem: MenuItem? = null
    ) {
        val contrastColor = statusBarColor.getContrastColor()
        if (toolbarNavigationIcon != NavigationIcon.None) {
            val drawableId =
                if (toolbarNavigationIcon == NavigationIcon.Cross) R.drawable.ic_cross_vector else R.drawable.ic_arrow_left_vector
            toolbar.navigationIcon =
                resources.getColoredDrawableWithColor(drawableId, contrastColor)
            toolbar.setNavigationContentDescription(toolbarNavigationIcon.accessibilityResId)
        }

        toolbar.setNavigationOnClickListener {
            hideKeyboard()
            finish()
        }

        updateTopBarColors(toolbar, statusBarColor)

        if (!useTopSearchMenu) {
            searchMenuItem?.actionView?.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
                ?.apply {
                    applyColorFilter(contrastColor)
                }

            searchMenuItem?.actionView?.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
                ?.apply {
                    setTextColor(contrastColor)
                    setHintTextColor(contrastColor.adjustAlpha(MEDIUM_ALPHA))
                    hint = "${getString(R.string.search)}…"

                    if (isQPlus()) {
                        textCursorDrawable = null
                    }
                }

            // search underline
            searchMenuItem?.actionView?.findViewById<View>(androidx.appcompat.R.id.search_plate)
                ?.apply {
                    background.setColorFilter(contrastColor, PorterDuff.Mode.MULTIPLY)
                }
        }
    }

    private fun updateRecentsAppIcon() {
        if (baseConfig.isUsingModifiedAppIcon) {
            val appIconIDs = getAppIconIDs()
            val currentAppIconColorIndex = getCurrentAppIconColorIndex()
            if (appIconIDs.size - 1 < currentAppIconColorIndex) {
                return
            }

            val recentsIcon =
                BitmapFactory.decodeResource(resources, appIconIDs[currentAppIconColorIndex])
            val title = getAppLauncherName()
            val color = baseConfig.primaryColor

            val description = ActivityManager.TaskDescription(title, recentsIcon, color)
            setTaskDescription(description)
        }
    }

    fun updateMenuItemColors(
        menu: Menu?,
        baseColor: Int = getProperStatusBarColor(),
        forceWhiteIcons: Boolean = false
    ) {
        if (menu == null) {
            return
        }

        var color = baseColor.getContrastColor()
        if (forceWhiteIcons) {
            color = Color.WHITE
        }

        for (i in 0 until menu.size()) {
            try {
                menu.getItem(i)?.icon?.setTint(color)
            } catch (ignored: Exception) {
            }
        }
    }

    private fun getCurrentAppIconColorIndex(): Int {
        val appIconColor = baseConfig.appIconColor
        getAppIconColors().forEachIndexed { index, color ->
            if (color == appIconColor) {
                return index
            }
        }
        return 0
    }

    fun setTranslucentNavigation() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        val partition = try {
            checkedDocumentPath.substring(9, 18)
        } catch (e: Exception) {
            ""
        }

        val sdOtgPattern = Pattern.compile(SD_OTG_SHORT)
        if (requestCode == CREATE_DOCUMENT_SDK_30) {
            if (resultCode == Activity.RESULT_OK && resultData != null && resultData.data != null) {

                val treeUri = resultData.data
                val checkedUri = buildDocumentUriSdk30(checkedDocumentPath)

                if (treeUri != checkedUri) {
                    toast(getString(R.string.wrong_folder_selected, checkedDocumentPath))
                    return
                }

                val takeFlags =
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                applicationContext.contentResolver.takePersistableUriPermission(treeUri, takeFlags)
                val funAfter = funAfterSdk30Action
                funAfterSdk30Action = null
                funAfter?.invoke(true)
            } else {
                funAfterSdk30Action?.invoke(false)
            }

        } else if (requestCode == OPEN_DOCUMENT_TREE_FOR_SDK_30) {
            if (resultCode == Activity.RESULT_OK && resultData != null && resultData.data != null) {
                val treeUri = resultData.data
                val checkedUri = createFirstParentTreeUri(checkedDocumentPath)

                if (treeUri != checkedUri) {
                    val level = getFirstParentLevel(checkedDocumentPath)
                    val firstParentPath = checkedDocumentPath.getFirstParentPath(this, level)
                    toast(getString(R.string.wrong_folder_selected, humanizePath(firstParentPath)))
                    return
                }

                val takeFlags =
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                applicationContext.contentResolver.takePersistableUriPermission(treeUri, takeFlags)
                val funAfter = funAfterSdk30Action
                funAfterSdk30Action = null
                funAfter?.invoke(true)
            } else {
                funAfterSdk30Action?.invoke(false)
            }

        } else if (requestCode == OPEN_DOCUMENT_TREE_FOR_ANDROID_DATA_OR_OBB) {
            if (resultCode == Activity.RESULT_OK && resultData != null && resultData.data != null) {
                if (isProperAndroidRoot(checkedDocumentPath, resultData.data!!)) {
                    if (resultData.dataString == baseConfig.otgTreeUri || resultData.dataString == baseConfig.sdTreeUri) {
                        val pathToSelect = createAndroidDataOrObbPath(checkedDocumentPath)
                        toast(getString(R.string.wrong_folder_selected, pathToSelect))
                        return
                    }

                    val treeUri = resultData.data
                    storeAndroidTreeUri(checkedDocumentPath, treeUri.toString())

                    val takeFlags =
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    applicationContext.contentResolver.takePersistableUriPermission(
                        treeUri!!,
                        takeFlags
                    )
                    funAfterSAFPermission?.invoke(true)
                    funAfterSAFPermission = null
                } else {
                    toast(
                        getString(
                            R.string.wrong_folder_selected,
                            createAndroidDataOrObbPath(checkedDocumentPath)
                        )
                    )
                    Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                        if (isRPlus()) {
                            putExtra(
                                DocumentsContract.EXTRA_INITIAL_URI,
                                createAndroidDataOrObbUri(checkedDocumentPath)
                            )
                        }

                        try {
                            startActivityForResult(this, requestCode)
                        } catch (e: Exception) {
                            showErrorToast(e)
                        }
                    }
                }
            } else {
                funAfterSAFPermission?.invoke(false)
            }
        } else if (requestCode == OPEN_DOCUMENT_TREE_SD) {
            if (resultCode == Activity.RESULT_OK && resultData != null && resultData.data != null) {
                val isProperPartition = partition.isEmpty() || !sdOtgPattern.matcher(partition)
                    .matches() || (sdOtgPattern.matcher(partition)
                    .matches() && resultData.dataString!!.contains(partition))
                if (isProperSDRootFolder(resultData.data!!) && isProperPartition) {
                    if (resultData.dataString == baseConfig.otgTreeUri) {
                        toast(R.string.sd_card_usb_same)
                        return
                    }

                    saveTreeUri(resultData)
                    funAfterSAFPermission?.invoke(true)
                    funAfterSAFPermission = null
                } else {
                    toast(R.string.wrong_root_selected)
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)

                    try {
                        startActivityForResult(intent, requestCode)
                    } catch (e: Exception) {
                        showErrorToast(e)
                    }
                }
            } else {
                funAfterSAFPermission?.invoke(false)
            }
        } else if (requestCode == OPEN_DOCUMENT_TREE_OTG) {
            if (resultCode == Activity.RESULT_OK && resultData != null && resultData.data != null) {
                val isProperPartition = partition.isEmpty() || !sdOtgPattern.matcher(partition)
                    .matches() || (sdOtgPattern.matcher(partition)
                    .matches() && resultData.dataString!!.contains(partition))
                if (isProperOTGRootFolder(resultData.data!!) && isProperPartition) {
                    if (resultData.dataString == baseConfig.sdTreeUri) {
                        funAfterSAFPermission?.invoke(false)
                        toast(R.string.sd_card_usb_same)
                        return
                    }
                    baseConfig.otgTreeUri = resultData.dataString!!
                    baseConfig.otgPartition =
                        baseConfig.otgTreeUri.removeSuffix("%3A").substringAfterLast('/')
                            .trimEnd('/')
                    updateOTGPathFromPartition()

                    val takeFlags =
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    applicationContext.contentResolver.takePersistableUriPermission(
                        resultData.data!!,
                        takeFlags
                    )

                    funAfterSAFPermission?.invoke(true)
                    funAfterSAFPermission = null
                } else {
                    toast(R.string.wrong_root_selected_usb)
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)

                    try {
                        startActivityForResult(intent, requestCode)
                    } catch (e: Exception) {
                        showErrorToast(e)
                    }
                }
            } else {
                funAfterSAFPermission?.invoke(false)
            }
        } else if (requestCode == SELECT_EXPORT_SETTINGS_FILE_INTENT && resultCode == Activity.RESULT_OK && resultData != null && resultData.data != null) {
            val outputStream = contentResolver.openOutputStream(resultData.data!!)
            exportSettingsTo(outputStream, configItemsToExport)
        } else if (requestCode == DELETE_FILE_SDK_30_HANDLER) {
            funAfterSdk30Action?.invoke(resultCode == Activity.RESULT_OK)
        } else if (requestCode == RECOVERABLE_SECURITY_HANDLER) {
            funRecoverableSecurity?.invoke(resultCode == Activity.RESULT_OK)
            funRecoverableSecurity = null
        } else if (requestCode == UPDATE_FILE_SDK_30_HANDLER) {
            funAfterUpdate30File?.invoke(resultCode == Activity.RESULT_OK)
        } else if (requestCode == MANAGE_MEDIA_RC) {
            funAfterManageMediaPermission?.invoke()
        } else if (requestCode == TRASH_FILE_SDK_30_HANDLER) {
            funAfterTrash30File?.invoke(resultCode == Activity.RESULT_OK)
        }
    }

    private fun saveTreeUri(resultData: Intent) {
        val treeUri = resultData.data
        baseConfig.sdTreeUri = treeUri.toString()

        val takeFlags =
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        applicationContext.contentResolver.takePersistableUriPermission(treeUri!!, takeFlags)
    }

    private fun isProperSDRootFolder(uri: Uri) =
        isExternalStorageDocument(uri) && isRootUri(uri) && !isInternalStorage(uri)

    private fun isProperSDFolder(uri: Uri) =
        isExternalStorageDocument(uri) && !isInternalStorage(uri)

    private fun isProperOTGRootFolder(uri: Uri) =
        isExternalStorageDocument(uri) && isRootUri(uri) && !isInternalStorage(uri)

    private fun isProperOTGFolder(uri: Uri) =
        isExternalStorageDocument(uri) && !isInternalStorage(uri)

    private fun isRootUri(uri: Uri) = uri.lastPathSegment?.endsWith(":") ?: false

    private fun isInternalStorage(uri: Uri) =
        isExternalStorageDocument(uri) && DocumentsContract.getTreeDocumentId(uri)
            .contains("primary")

    private fun isAndroidDir(uri: Uri) =
        isExternalStorageDocument(uri) && DocumentsContract.getTreeDocumentId(uri)
            .contains(":Android")

    private fun isInternalStorageAndroidDir(uri: Uri) = isInternalStorage(uri) && isAndroidDir(uri)
    private fun isOTGAndroidDir(uri: Uri) = isProperOTGFolder(uri) && isAndroidDir(uri)
    private fun isSDAndroidDir(uri: Uri) = isProperSDFolder(uri) && isAndroidDir(uri)
    private fun isExternalStorageDocument(uri: Uri) =
        EXTERNAL_STORAGE_PROVIDER_AUTHORITY == uri.authority

    private fun isProperAndroidRoot(path: String, uri: Uri): Boolean {
        return when {
            isPathOnOTG(path) -> isOTGAndroidDir(uri)
            isPathOnSD(path) -> isSDAndroidDir(uri)
            else -> isInternalStorageAndroidDir(uri)
        }
    }

    private fun startAboutActivity(
        appNameId: Int,
        licenseMask: Long,
        versionName: String,
        faqItems: ArrayList<FAQItem>,
        showFAQBeforeMail: Boolean
    ) {
        hideKeyboard()
        Intent(applicationContext, AboutActivity::class.java).apply {
            putExtra(APP_ICON_IDS, getAppIconIDs())
            putExtra(APP_LAUNCHER_NAME, getAppLauncherName())
            putExtra(APP_NAME, getString(appNameId))
            putExtra(APP_LICENSES, licenseMask)
            putExtra(APP_VERSION_NAME, versionName)
            putExtra(APP_FAQ, faqItems)
            putExtra(SHOW_FAQ_BEFORE_MAIL, showFAQBeforeMail)
            startActivity(this)
        }
    }

    fun startCustomizationActivity() {

        Intent(applicationContext, CustomizationActivity::class.java).apply {
            putExtra(APP_ICON_IDS, getAppIconIDs())
            putExtra(APP_LAUNCHER_NAME, getAppLauncherName())
            startActivity(this)
        }
    }

    fun handleCustomizeColorsClick() {
        startCustomizationActivity()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun launchCustomizeNotificationsIntent() {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            startActivity(this)
        }
    }


    fun launchChangeAppLanguageIntent() {
        Toast.makeText(this, "This feature has not yet been added!!!", Toast.LENGTH_SHORT).show()
    }

    // synchronous return value determines only if we are showing the SAF dialog, callback result tells if the SD or OTG permission has been granted

    @RequiresApi(Build.VERSION_CODES.O)
    fun handleSAFDialog(path: String, callback: (success: Boolean) -> Unit): Boolean {
        hideKeyboard()
        return if (!packageName.startsWith("com.simplemobiletools")) {
            callback(true)
            false
        } else if (isShowingSAFDialog(path) || isShowingOTGDialog(path)) {
            funAfterSAFPermission = callback
            true
        } else {
            callback(true)
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun handleSAFDialogSdk30(path: String, callback: (success: Boolean) -> Unit): Boolean {
        hideKeyboard()
        return if (!packageName.startsWith("com.simplemobiletools")) {
            callback(true)
            false
        } else if (isShowingSAFDialogSdk30(path)) {
            funAfterSdk30Action = callback
            true
        } else {
            callback(true)
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun checkManageMediaOrHandleSAFDialogSdk30(
        path: String,
        callback: (success: Boolean) -> Unit
    ): Boolean {
        hideKeyboard()
        return if (canManageMedia()) {
            callback(true)
            false
        } else {
            handleSAFDialogSdk30(path, callback)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun handleSAFCreateDocumentDialogSdk30(
        path: String,
        callback: (success: Boolean) -> Unit
    ): Boolean {
        hideKeyboard()
        return if (!packageName.startsWith("com.simplemobiletools")) {
            callback(true)
            false
        } else if (isShowingSAFCreateDocumentDialogSdk30(path)) {
            funAfterSdk30Action = callback
            true
        } else {
            callback(true)
            false
        }
    }

    fun handleAndroidSAFDialog(path: String, callback: (success: Boolean) -> Unit): Boolean {
        hideKeyboard()
        return if (!packageName.startsWith("com.simplemobiletools")) {
            callback(true)
            false
        } else if (isShowingAndroidSAFDialog(path)) {
            funAfterSAFPermission = callback
            true
        } else {
            callback(true)
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun handleOTGPermission(callback: (success: Boolean) -> Unit) {
        hideKeyboard()
        if (baseConfig.otgTreeUri.isNotEmpty()) {
            callback(true)
            return
        }

        funAfterSAFPermission = callback
        WritePermissionDialog(this, WritePermissionDialog.Mode.Otg) {
            Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                try {
                    startActivityForResult(this, OPEN_DOCUMENT_TREE_OTG)
                    return@apply
                } catch (e: Exception) {
                    type = "*/*"
                }

                try {
                    startActivityForResult(this, OPEN_DOCUMENT_TREE_OTG)
                } catch (e: ActivityNotFoundException) {
                    toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
                } catch (e: Exception) {
                    toast(R.string.unknown_error_occurred)
                }
            }
        }
    }

    @SuppressLint("NewApi")
    fun deleteSDK30Uris(uris: List<Uri>, callback: (success: Boolean) -> Unit) {
        hideKeyboard()
        if (isRPlus()) {
            funAfterSdk30Action = callback
            try {
                val deleteRequest =
                    MediaStore.createDeleteRequest(contentResolver, uris).intentSender
                startIntentSenderForResult(deleteRequest, DELETE_FILE_SDK_30_HANDLER, null, 0, 0, 0)
            } catch (e: Exception) {
                showErrorToast(e)
            }
        } else {
            callback(false)
        }
    }

    @SuppressLint("NewApi")
    fun trashSDK30Uris(uris: List<Uri>, toTrash: Boolean, callback: (success: Boolean) -> Unit) {
        hideKeyboard()
        if (isRPlus()) {
            funAfterTrash30File = callback
            try {
                val trashRequest =
                    MediaStore.createTrashRequest(contentResolver, uris, toTrash).intentSender
                startIntentSenderForResult(trashRequest, TRASH_FILE_SDK_30_HANDLER, null, 0, 0, 0)
            } catch (e: Exception) {
                showErrorToast(e)
            }
        } else {
            callback(false)
        }
    }

    @SuppressLint("NewApi")
    fun updateSDK30Uris(uris: List<Uri>, callback: (success: Boolean) -> Unit) {
        hideKeyboard()
        if (isRPlus()) {
            funAfterUpdate30File = callback
            try {
                val writeRequest = MediaStore.createWriteRequest(contentResolver, uris).intentSender
                startIntentSenderForResult(writeRequest, UPDATE_FILE_SDK_30_HANDLER, null, 0, 0, 0)
            } catch (e: Exception) {
                showErrorToast(e)
            }
        } else {
            callback(false)
        }
    }

    @SuppressLint("NewApi")
    fun handleRecoverableSecurityException(callback: (success: Boolean) -> Unit) {
        try {
            callback.invoke(true)
        } catch (securityException: SecurityException) {
            if (isQPlus()) {
                funRecoverableSecurity = callback
                val recoverableSecurityException =
                    securityException as? RecoverableSecurityException ?: throw securityException
                val intentSender = recoverableSecurityException.userAction.actionIntent.intentSender
                startIntentSenderForResult(
                    intentSender,
                    RECOVERABLE_SECURITY_HANDLER,
                    null,
                    0,
                    0,
                    0
                )
            } else {
                callback(false)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun launchMediaManagementIntent(callback: () -> Unit) {
        Intent(Settings.ACTION_REQUEST_MANAGE_MEDIA).apply {
            data = Uri.parse("package:$packageName")
            try {
                startActivityForResult(this, MANAGE_MEDIA_RC)
            } catch (e: Exception) {
                showErrorToast(e)
            }
        }
        funAfterManageMediaPermission = callback
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun copyMoveFilesTo(
        fileDirItems: ArrayList<FileDirItem>,
        source: String,
        destination: String,
        isCopyOperation: Boolean,
        copyHidden: Boolean,
        callback: (destinationPath: String) -> Unit
    ) {
        if (source == destination) {
            toast(R.string.source_and_destination_same)
            return
        }

        if (!getDoesFilePathExist(destination)) {
            toast(R.string.invalid_destination)
            return
        }

        handleSAFDialog(destination) {
            if (!it) {
                copyMoveListener.copyFailed()
                return@handleSAFDialog
            }

            handleSAFDialogSdk30(destination) {
                if (!it) {
                    copyMoveListener.copyFailed()
                    return@handleSAFDialogSdk30
                }

                copyMoveCallback = callback
                var fileCountToCopy = fileDirItems.size
                if (isCopyOperation) {
                    val recycleBinPath = fileDirItems.first().isRecycleBinPath(this)
                    if (canManageMedia() && !recycleBinPath) {
                        val fileUris = getFileUrisFromFileDirItems(fileDirItems)
                        updateSDK30Uris(fileUris) { sdk30UriSuccess ->
                            if (sdk30UriSuccess) {
                                startCopyMove(
                                    fileDirItems,
                                    destination,
                                    true,
                                    copyHidden
                                )
                            }
                        }
                    } else {
                        startCopyMove(
                            fileDirItems,
                            destination,
                            true,
                            copyHidden
                        )
                    }
                } else {
                    if (isPathOnOTG(source) || isPathOnOTG(destination) || isPathOnSD(source) || isPathOnSD(
                            destination
                        ) ||
                        isRestrictedSAFOnlyRoot(source) || isRestrictedSAFOnlyRoot(destination) ||
                        isAccessibleWithSAFSdk30(source) || isAccessibleWithSAFSdk30(destination) ||
                        fileDirItems.first().isDirectory
                    ) {
                        handleSAFDialog(source) { safSuccess ->
                            if (safSuccess) {
                                val recycleBinPath = fileDirItems.first().isRecycleBinPath(this)
                                if (canManageMedia() && !recycleBinPath) {
                                    val fileUris = getFileUrisFromFileDirItems(fileDirItems)
                                    updateSDK30Uris(fileUris) { sdk30UriSuccess ->
                                        if (sdk30UriSuccess) {
                                            startCopyMove(
                                                fileDirItems,
                                                destination,
                                                false,
                                                copyHidden
                                            )
                                        }
                                    }
                                } else {
                                    startCopyMove(
                                        fileDirItems,
                                        destination,
                                        false,
                                        copyHidden
                                    )
                                }
                            }
                        }
                    } else {
                        try {
                            checkConflicts(fileDirItems, destination, 0, LinkedHashMap()) {
                                toast(R.string.moving)
                                ensureBackgroundThread {
                                    val updatedPaths = ArrayList<String>(fileDirItems.size)
                                    val destinationFolder = File(destination)
                                    for (oldFileDirItem in fileDirItems) {
                                        var newFile = File(destinationFolder, oldFileDirItem.name)
                                        if (newFile.exists()) {
                                            when {
                                                getConflictResolution(
                                                    it,
                                                    newFile.absolutePath
                                                ) == CONFLICT_SKIP -> fileCountToCopy--

                                                getConflictResolution(
                                                    it,
                                                    newFile.absolutePath
                                                ) == CONFLICT_KEEP_BOTH -> newFile =
                                                    getAlternativeFile(newFile)

                                                else ->
                                                    // this file is guaranteed to be on the internal storage, so just delete it this way
                                                    newFile.delete()
                                            }
                                        }

                                        if (!newFile.exists() && File(oldFileDirItem.path).renameTo(
                                                newFile
                                            )
                                        ) {
                                            if (!baseConfig.keepLastModified) {
                                                newFile.setLastModified(System.currentTimeMillis())
                                            }
                                            updatedPaths.add(newFile.absolutePath)
                                            deleteFromMediaStore(oldFileDirItem.path)
                                        }
                                    }

                                    runOnUiThread {
                                        if (updatedPaths.isEmpty()) {
                                            copyMoveListener.copySucceeded(
                                                false,
                                                fileCountToCopy == 0,
                                                destination,
                                                false
                                            )
                                        } else {
                                            copyMoveListener.copySucceeded(
                                                false,
                                                fileCountToCopy <= updatedPaths.size,
                                                destination,
                                                updatedPaths.size == 1
                                            )
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            showErrorToast(e)
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    fun getAlternativeFile(file: File): File {
        var fileIndex = 1
        var newFile: File?
        do {
            val newName =
                String.format("%s(%d).%s", file.nameWithoutExtension, fileIndex, file.extension)
            newFile = File(file.parent, newName)
            fileIndex++
        } while (getDoesFilePathExist(newFile!!.absolutePath))
        return newFile
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun startCopyMove(
        files: ArrayList<FileDirItem>,
        destinationPath: String,
        isCopyOperation: Boolean,
        copyHidden: Boolean
    ) {
        val availableSpace = destinationPath.getAvailableStorageB()
        val sumToCopy = files.sumByLong { it.getProperSize(applicationContext, copyHidden) }
        if (availableSpace == -1L || sumToCopy < availableSpace) {
            checkConflicts(files, destinationPath, 0, LinkedHashMap()) {
                toast(if (isCopyOperation) R.string.copying else R.string.moving)
                val pair = Pair(files, destinationPath)
                handleNotificationPermission { granted ->
                    if (granted) {
                        CopyMoveTask(
                            this,
                            isCopyOperation,
                            true,
                            it,
                            copyMoveListener,
                            copyHidden
                        ).execute(pair)
                    } else {
                        PermissionRequiredDialog(
                            this,
                            R.string.allow_notifications_files,
                            { openNotificationSettings() })
                    }
                }
            }
        } else {
            val text = String.format(
                getString(R.string.no_space),
                sumToCopy.formatSize(),
                availableSpace.formatSize()
            )
            toast(text, Toast.LENGTH_LONG)
        }
    }

    private fun checkConflicts(
        files: ArrayList<FileDirItem>,
        destinationPath: String,
        index: Int,
        conflictResolutions: LinkedHashMap<String, Int>,
        callback: (resolutions: LinkedHashMap<String, Int>) -> Unit
    ) {
        if (index == files.size) {
            callback(conflictResolutions)
            return
        }

        val file = files[index]
        val newFileDirItem =
            FileDirItem("$destinationPath/${file.name}", file.name, file.isDirectory)
        ensureBackgroundThread {
            if (getDoesFilePathExist(newFileDirItem.path)) {
                runOnUiThread {
                    FileConflictDialog(
                        this,
                        newFileDirItem,
                        files.size > 1
                    ) { resolution, applyForAll ->
                        if (applyForAll) {
                            conflictResolutions.clear()
                            conflictResolutions[""] = resolution
                            checkConflicts(
                                files,
                                destinationPath,
                                files.size,
                                conflictResolutions,
                                callback
                            )
                        } else {
                            conflictResolutions[newFileDirItem.path] = resolution
                            checkConflicts(
                                files,
                                destinationPath,
                                index + 1,
                                conflictResolutions,
                                callback
                            )
                        }
                    }
                }
            } else {
                runOnUiThread {
                    checkConflicts(files, destinationPath, index + 1, conflictResolutions, callback)
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun handlePermission(permissionId: Int, callback: (granted: Boolean) -> Unit) {
        actionOnPermission = null
        if (hasPermission(permissionId)) {
            callback(true)
        } else {
            isAskingPermissions = true
            actionOnPermission = callback
            ActivityCompat.requestPermissions(
                this,
                arrayOf(getPermissionString(permissionId)),
                GENERIC_PERM_HANDLER
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun handlePartialMediaPermissions(
        permissionIds: Collection<Int>,
        force: Boolean = false,
        callback: (granted: Boolean) -> Unit
    ) {
        actionOnPermission = null
        if (isUpsideDownCakePlus()) {
            if (hasPermission(PERMISSION_READ_MEDIA_VISUAL_USER_SELECTED) && !force) {
                callback(true)
            } else {
                isAskingPermissions = true
                actionOnPermission = callback
                ActivityCompat.requestPermissions(
                    this,
                    permissionIds.map { getPermissionString(it) }.toTypedArray(),
                    GENERIC_PERM_HANDLER
                )
            }
        } else {
            if (hasAllPermissions(permissionIds)) {
                callback(true)
            } else {
                isAskingPermissions = true
                actionOnPermission = callback
                ActivityCompat.requestPermissions(
                    this,
                    permissionIds.map { getPermissionString(it) }.toTypedArray(),
                    GENERIC_PERM_HANDLER
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun handleNotificationPermission(callback: (granted: Boolean) -> Unit) {
        if (!isTiramisuPlus()) {
            callback(true)
        } else {
            handlePermission(PERMISSION_POST_NOTIFICATIONS) { granted ->
                callback(granted)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        isAskingPermissions = false
        if (requestCode == GENERIC_PERM_HANDLER && grantResults.isNotEmpty()) {
            actionOnPermission?.invoke(grantResults[0] == 0)
        }
    }

    private val copyMoveListener = object : CopyMoveListener {
        override fun copySucceeded(
            copyOnly: Boolean,
            copiedAll: Boolean,
            destinationPath: String,
            wasCopyingOneFileOnly: Boolean
        ) {
            if (copyOnly) {
                toast(
                    if (copiedAll) {
                        if (wasCopyingOneFileOnly) {
                            R.string.copying_success_one
                        } else {
                            R.string.copying_success
                        }
                    } else {
                        R.string.copying_success_partial
                    }
                )
            } else {
                toast(
                    if (copiedAll) {
                        if (wasCopyingOneFileOnly) {
                            R.string.moving_success_one
                        } else {
                            R.string.moving_success
                        }
                    } else {
                        R.string.moving_success_partial
                    }
                )
            }

            copyMoveCallback?.invoke(destinationPath)
            copyMoveCallback = null
        }

        override fun copyFailed() {
            toast(R.string.copy_move_failed)
            copyMoveCallback = null
        }
    }

    fun checkAppOnSDCard() {
        if (!baseConfig.wasAppOnSDShown && isAppInstalledOnSDCard()) {
            baseConfig.wasAppOnSDShown = true
            ConfirmationDialog(this, "", R.string.app_on_sd_card, R.string.ok, 0) {}
        }
    }


    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun exportSettings(configItems: LinkedHashMap<String, Any>) {
        if (isQPlus()) {
            configItemsToExport = configItems
            ExportSettingsDialog(this, getExportSettingsFilename(), true) { _, filename ->
                Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TITLE, filename)
                    addCategory(Intent.CATEGORY_OPENABLE)

                    try {
                        startActivityForResult(this, SELECT_EXPORT_SETTINGS_FILE_INTENT)
                    } catch (e: ActivityNotFoundException) {
                        toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
                    } catch (e: Exception) {
                        showErrorToast(e)
                    }
                }
            }
        } else {
            handlePermission(PERMISSION_WRITE_STORAGE) {
                if (it) {
                    ExportSettingsDialog(
                        this,
                        getExportSettingsFilename(),
                        false
                    ) { path, _ ->
                        val file = File(path)
                        getFileOutputStream(file.toFileDirItem(this), true) {
                            exportSettingsTo(it, configItems)
                        }
                    }
                }
            }
        }
    }

    private fun exportSettingsTo(
        outputStream: OutputStream?,
        configItems: LinkedHashMap<String, Any>
    ) {
        if (outputStream == null) {
            toast(R.string.unknown_error_occurred)
            return
        }

        ensureBackgroundThread {
            outputStream.bufferedWriter().use { out ->
                for ((key, value) in configItems) {
                    out.writeLn("$key=$value")
                }
            }

            toast(R.string.settings_exported_successfully)
        }
    }

    private fun getExportSettingsFilename(): String {
        val appName = baseConfig.appId.removeSuffix(".debug").removeSuffix(".pro")
            .removePrefix("com.simplemobiletools.")
        return "$appName-settings_${getCurrentFormattedDateTime()}"
    }

    @SuppressLint("InlinedApi")
    protected fun launchSetDefaultDialerIntent() {
        if (isQPlus()) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager!!.isRoleAvailable(RoleManager.ROLE_DIALER) && !roleManager.isRoleHeld(
                    RoleManager.ROLE_DIALER
                )
            ) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_DIALER)
            }
        } else {
            Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).putExtra(
                TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
                packageName
            ).apply {
                try {
                    startActivityForResult(this, REQUEST_CODE_SET_DEFAULT_DIALER)
                } catch (e: ActivityNotFoundException) {
                    toast(R.string.no_app_found)
                } catch (e: Exception) {
                    showErrorToast(e)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun setDefaultCallerIdApp() {
        val roleManager = getSystemService(RoleManager::class.java)
        if (roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) && !roleManager.isRoleHeld(
                RoleManager.ROLE_CALL_SCREENING
            )
        ) {
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
            startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_CALLER_ID)
        }
    }

    fun handleMediaManagementPrompt(callback: () -> Unit) {

        if (canManageMedia() || isExternalStorageManager()) {
            callback()
        } else if (isRPlus() && resources.getBoolean(R.bool.require_all_files_access) && !config.avoidShowingAllFilesPrompt) {
            if (Environment.isExternalStorageManager()) {
                callback()
            } else {
                var messagePrompt =
                    getString(R.string.access_storage_prompt)
                messagePrompt += if (isSPlus()) {
                    "\n\n${getString(R.string.media_management_alternative)}"
                } else {
                    "\n\n${getString(R.string.alternative_media_access)}"
                }

                AllFilesPermissionDialog(this, messagePrompt, callback = { success ->
                    if (success) {
                        launchGrantAllFilesIntent()
                    }
                }, neutralPressed = {
                    if (isSPlus()) {
                        launchMediaManagementIntent(callback)
                    } else {
                        config.avoidShowingAllFilesPrompt = true
                    }
                })
            }
        } else {
            callback()
        }
    }

     fun launchGrantAllFilesIntent() {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.addCategory("android.intent.category.DEFAULT")
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent()
            intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
            try {
                startActivity(intent)
            } catch (e: Exception) {
                showErrorToast(e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addNoMedia(path: String, callback: () -> Unit) {
        val file = File(path, NOMEDIA)
        if (getDoesFilePathExist(file.absolutePath)) {
            callback()
            return
        }

        if (needsStupidWritePermissions(path)) {
            handleSAFDialog(file.absolutePath) {
                if (!it) {
                    return@handleSAFDialog
                }

                val fileDocument = getDocumentFile(path)
                if (fileDocument?.exists() == true && fileDocument.isDirectory) {
                    fileDocument.createFile("", NOMEDIA)
                    addNoMediaIntoMediaStore(file.absolutePath)
                    callback()
                } else {
                    toast(R.string.unknown_error_occurred)
                    callback()
                }
            }
        } else {
            try {
                if (file.createNewFile()) {
                    ensureBackgroundThread {
                        addNoMediaIntoMediaStore(file.absolutePath)
                    }
                } else {
                    toast(R.string.unknown_error_occurred)
                }
            } catch (e: Exception) {
                showErrorToast(e)
            }
            callback()
        }
    }

    private fun addNoMediaIntoMediaStore(path: String) {
        try {
            val content = ContentValues().apply {
                put(Files.FileColumns.TITLE, NOMEDIA)
                put(Files.FileColumns.DATA, path)
                put(Files.FileColumns.MEDIA_TYPE, Files.FileColumns.MEDIA_TYPE_NONE)
            }
            contentResolver.insert(Files.getContentUri("external"), content)
        } catch (e: Exception) {
            showErrorToast(e)
        }
    }

    fun removeNoMedia(path: String, callback: (() -> Unit)? = null) {
        val file = File(path, NOMEDIA)
        if (!getDoesFilePathExist(file.absolutePath)) {
            callback?.invoke()
            return
        }

        tryDeleteFileDirItem(
            fileDirItem = file.toFileDirItem(applicationContext),
            allowDeleteFolder = false,
            deleteFromDatabase = false
        ) {
            callback?.invoke()
            deleteFromMediaStore(file.absolutePath) { needsRescan ->
                if (needsRescan) {
                    rescanAndDeletePath(path) {
                        rescanFolderMedia(path)
                    }
                } else {
                    rescanFolderMedia(path)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun toggleFileVisibility(
        oldPath: String,
        hide: Boolean,
        callback: ((newPath: String) -> Unit)? = null
    ) {

        val path = oldPath.getParentPath()
        var filename = oldPath.getFilenameFromPath()
        if ((hide && filename.startsWith('.')) || (!hide && !filename.startsWith('.'))) {
            callback?.invoke(oldPath)
            return
        }

        filename = if (hide) {
            ".${filename.trimStart('.')}"
        } else {
            filename.substring(1, filename.length)
        }

        val newPath = "$path/$filename"
        renameFile(oldPath, newPath, false) { _, _ ->
            runOnUiThread {
                callback?.invoke(newPath)
            }

            ensureBackgroundThread {
                updateDBMediaPath(oldPath, newPath)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @SuppressLint("UnsafeOptInUsageError")
    fun tryCopyMoveFilesTo(
        fileDirItems: ArrayList<FileDirItem>,
        isCopyOperation: Boolean,
        callback: (destinationPath: String) -> Unit
    ) {

        if (fileDirItems.isEmpty()) {
            toast(R.string.unknown_error_occurred)
            return
        }

        val source = fileDirItems[0].getParentPath()
        PickDirectoryDialog(
            activity = this,
            sourcePath = source,
            showOtherFolderButton = true,
            showFavoritesBin = false,
            isPickingCopyMoveDestination = true,
            isPickingFolderForWidget = false
        ) {
            val destination = it
            handleSAFDialog(source) {
                if (it) {
                    copyMoveFilesTo(
                        fileDirItems,
                        source.trimEnd('/'),
                        destination,
                        isCopyOperation,
                        config.shouldShowHidden,
                        callback
                    )
                }
            }
        }
    }

    fun tryDeleteFileDirItem(
        fileDirItem: FileDirItem,
        allowDeleteFolder: Boolean = false,
        deleteFromDatabase: Boolean,
        callback: ((
            wasSuccess: Boolean
        ) -> Unit)? = null
    ) {

        deleteFile(fileDirItem, allowDeleteFolder, isDeletingMultipleFiles = false) {
            if (deleteFromDatabase) {
                ensureBackgroundThread {
                    deleteDBPath(fileDirItem.path)
                    runOnUiThread {
                        callback?.invoke(it)
                    }
                }
            } else {
                callback?.invoke(it)
            }
        }
    }

    fun movePathsInRecycleBin(
        paths: ArrayList<String>,
        callback: ((wasSuccess: Boolean) -> Unit)?
    ) {
        ensureBackgroundThread {
            var pathsCnt = paths.size
            val otgPath = config.otgPath

            for (source in paths) {
                if (otgPath.isNotEmpty() && source.startsWith(otgPath)) {
                    var inputStream: InputStream? = null
                    var out: OutputStream? = null
                    try {
                        val destination = "$recycleBinPath/$source"
                        val fileDocument = getSomeDocumentFile(source)
                        inputStream =
                            applicationContext.contentResolver.openInputStream(fileDocument?.uri!!)
                        out = getFileOutputStreamSync(destination, source.getMimeType())

                        var copiedSize = 0L
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var bytes = inputStream!!.read(buffer)
                        while (bytes >= 0) {
                            out!!.write(buffer, 0, bytes)
                            copiedSize += bytes
                            bytes = inputStream.read(buffer)
                        }

                        out?.flush()

                        if (fileDocument.getItemSize(true) == copiedSize && getDoesFilePathExist(
                                destination
                            )
                        ) {
                            mediaDB.updateDeleted(
                                "$RECYCLE_BIN$source",
                                System.currentTimeMillis(),
                                source
                            )
                            pathsCnt--
                        }
                    } catch (e: Exception) {
                        showErrorToast(e)
                        return@ensureBackgroundThread
                    } finally {
                        inputStream?.close()
                        out?.close()
                    }
                } else {
                    val file = File(source)
                    val internalFile = File(recycleBinPath, source)
                    val lastModified = file.lastModified()
                    try {
                        if (file.copyRecursively(internalFile, true)) {
                            mediaDB.updateDeleted(
                                "$RECYCLE_BIN$source",
                                System.currentTimeMillis(),
                                source
                            )
                            pathsCnt--

                            if (config.keepLastModified && lastModified != 0L) {
                                internalFile.setLastModified(lastModified)
                            }
                        }
                    } catch (e: Exception) {
                        showErrorToast(e)
                        return@ensureBackgroundThread
                    }
                }
            }
            callback?.invoke(pathsCnt == 0)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun restoreRecycleBinPath(path: String, callback: () -> Unit) {
        restoreRecycleBinPaths(arrayListOf(path), callback)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun restoreRecycleBinPaths(paths: ArrayList<String>, callback: () -> Unit) {
        ensureBackgroundThread {
            val newPaths = ArrayList<String>()
            var shownRestoringToPictures = false
            for (source in paths) {
                var destination = source.removePrefix(recycleBinPath)

                val destinationParent = destination.getParentPath()
                if (isRestrictedWithSAFSdk30(destinationParent) && !isInDownloadDir(
                        destinationParent
                    )
                ) {
                    // if the file is not writeable on SDK30+, change it to Pictures
                    val picturesDirectory = getPicturesDirectoryPath(destination)
                    destination = File(picturesDirectory, destination.getFilenameFromPath()).path
                    if (!shownRestoringToPictures) {
                        toast(getString(R.string.restore_to_path, humanizePath(picturesDirectory)))
                        shownRestoringToPictures = true
                    }
                }

                val lastModified = File(source).lastModified()

                val isShowingSAF = handleSAFDialog(destination) {}
                if (isShowingSAF) {
                    return@ensureBackgroundThread
                }

                val isShowingSAFSdk30 = handleSAFDialogSdk30(destination) {}
                if (isShowingSAFSdk30) {
                    return@ensureBackgroundThread
                }

                if (getDoesFilePathExist(destination)) {
                    val newFile = getAlternativeFile(File(destination))
                    destination = newFile.path
                }

                var inputStream: InputStream? = null
                var out: OutputStream? = null
                try {
                    out = getFileOutputStreamSync(destination, source.getMimeType())
                    inputStream = getFileInputStreamSync(source)

                    var copiedSize = 0L
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var bytes = inputStream!!.read(buffer)
                    while (bytes >= 0) {
                        out!!.write(buffer, 0, bytes)
                        copiedSize += bytes
                        bytes = inputStream.read(buffer)
                    }

                    out?.flush()

                    if (File(source).length() == copiedSize) {
                        mediaDB.updateDeleted(
                            destination.removePrefix(recycleBinPath),
                            0,
                            "$RECYCLE_BIN${source.removePrefix(recycleBinPath)}"
                        )
                    }
                    newPaths.add(destination)

                    if (config.keepLastModified && lastModified != 0L) {
                        File(destination).setLastModified(lastModified)
                    }
                } catch (e: Exception) {
                    showErrorToast(e)
                } finally {
                    inputStream?.close()
                    out?.close()
                }
            }

            runOnUiThread {
                callback()
            }

            rescanPaths(newPaths) {
                fixDateTaken(newPaths, false)
            }
        }
    }

    fun emptyTheRecycleBin(callback: (() -> Unit)? = null) {
        ensureBackgroundThread {
            try {
                recycleBin.deleteRecursively()
                mediaDB.clearRecycleBin()
                directoryDB.deleteRecycleBin()
                toast(R.string.recycle_bin_emptied)
                callback?.invoke()
            } catch (e: Exception) {
                toast(R.string.unknown_error_occurred)
            }
        }
    }

    fun emptyAndDisableTheRecycleBin(callback: () -> Unit) {
        ensureBackgroundThread {
            emptyTheRecycleBin {
                config.useRecycleBin = false
                callback()
            }
        }
    }

    fun showRecycleBinEmptyingDialog(callback: () -> Unit) {
        ConfirmationDialog(
            this,
            "",
            R.string.empty_recycle_bin_confirmation,
            R.string.yes,
            R.string.no
        ) {
            callback()
        }
    }

    fun updateFavoritePaths(
        fileDirItems: ArrayList<FileDirItem>,
        destination: String
    ) {
        ensureBackgroundThread {
            fileDirItems.forEach {
                val newPath = "$destination/${it.name}"
                updateDBMediaPath(it.path, newPath)
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun saveRotatedImageToFile(
        oldPath: String,
        newPath: String,
        degrees: Int,
        showToasts: Boolean,
        callback: () -> Unit
    ) {
        var newDegrees = degrees
        if (newDegrees < 0) {
            newDegrees += 360
        }

        if (oldPath == newPath && oldPath.isJpg()) {
            if (tryRotateByExif(oldPath, newDegrees, showToasts, callback)) {
                return
            }
        }

        val tmpPath = "$recycleBinPath/.tmp_${newPath.getFilenameFromPath()}"
        val tmpFileDirItem = FileDirItem(tmpPath, tmpPath.getFilenameFromPath())
        try {
            getFileOutputStream(tmpFileDirItem) {
                if (it == null) {
                    if (showToasts) {
                        toast(R.string.unknown_error_occurred)
                    }
                    return@getFileOutputStream
                }

                val oldLastModified = File(oldPath).lastModified()
                if (oldPath.isJpg()) {
                    copyFile(oldPath, tmpPath)
                    saveExifRotation(ExifInterface(tmpPath), newDegrees)
                } else {
                    val inputstream = getFileInputStreamSync(oldPath)
                    val bitmap = BitmapFactory.decodeStream(inputstream)
                    saveFile(tmpPath, bitmap, it as FileOutputStream, newDegrees)
                }

                copyFile(tmpPath, newPath)
                rescanPaths(arrayListOf(newPath))
                fileRotatedSuccessfully(newPath, oldLastModified)

                it.flush()
                it.close()
                callback.invoke()
            }
        } catch (e: OutOfMemoryError) {
            if (showToasts) {
                toast(R.string.out_of_memory_error)
            }
        } catch (e: Exception) {
            if (showToasts) {
                showErrorToast(e)
            }
        } finally {
            tryDeleteFileDirItem(
                fileDirItem = tmpFileDirItem,
                allowDeleteFolder = false,
                deleteFromDatabase = true
            )
        }
    }


    private fun copyFile(source: String, destination: String) {
        var inputStream: InputStream? = null
        var out: OutputStream? = null
        try {
            out = getFileOutputStreamSync(destination, source.getMimeType())
            inputStream = getFileInputStreamSync(source)
            inputStream!!.copyTo(out!!)
        } catch (e: Exception) {
            showErrorToast(e)
        } finally {
            inputStream?.close()
            out?.close()
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun ensureWriteAccess(path: String, callback: () -> Unit) {
        when {
            isRestrictedSAFOnlyRoot(path) -> {
                handleAndroidSAFDialog(path) {
                    if (!it) {
                        return@handleAndroidSAFDialog
                    }
                    callback.invoke()
                }
            }

            needsStupidWritePermissions(path) -> {
                handleSAFDialog(path) {
                    if (!it) {
                        return@handleSAFDialog
                    }
                    callback()
                }
            }

            isAccessibleWithSAFSdk30(path) -> {
                handleSAFDialogSdk30(path) {
                    if (!it) {
                        return@handleSAFDialogSdk30
                    }
                    callback()
                }
            }

            else -> {
                callback()
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun launchResizeMultipleImagesDialog(
        paths: List<String>,
        callback: (() -> Unit)? = null
    ) {
        ensureBackgroundThread {
            val imagePaths = mutableListOf<String>()
            val imageSizes = mutableListOf<Point>()
            for (path in paths) {
                val size = path.getImageResolution(this)
                if (size != null) {
                    imagePaths.add(path)
                    imageSizes.add(size)
                }
            }

            runOnUiThread {
                ResizeMultipleImagesDialog(this, imagePaths, imageSizes) {
                    callback?.invoke()
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun launchResizeImageDialog(path: String, callback: (() -> Unit)? = null) {
        val originalSize = path.getImageResolution(this) ?: return
        ResizeWithPathDialog(this, originalSize, path) { newSize, newPath ->
            ensureBackgroundThread {
                val file = File(newPath)
                val pathLastModifiedMap = mapOf(file.absolutePath to file.lastModified())
                try {
                    resizeImage(path, newPath, newSize) { success ->
                        if (success) {
                            toast(R.string.file_saved)

                            val paths = arrayListOf(file.absolutePath)
                            rescanPathsAndUpdateLastModified(paths, pathLastModifiedMap) {
                                runOnUiThread {
                                    callback?.invoke()
                                }
                            }
                        } else {
                            toast(R.string.image_editing_failed)
                        }
                    }
                } catch (e: OutOfMemoryError) {
                    toast(R.string.out_of_memory_error)
                } catch (e: Exception) {
                    showErrorToast(e)
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun deleteFiles(
        files: List<FileDirItem>,
        allowDeleteFolder: Boolean = false,
        callback: ((wasSuccess: Boolean) -> Unit)? = null
    ) {
        ensureBackgroundThread {
            deleteFilesBg(files, allowDeleteFolder, callback)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun deleteFilesBg(
        files: List<FileDirItem>,
        allowDeleteFolder: Boolean = false,
        callback: ((wasSuccess: Boolean) -> Unit)? = null
    ) {
        if (files.isEmpty()) {
            runOnUiThread {
                callback?.invoke(true)
            }
            return
        }

        val firstFile = files.first()
        val firstFilePath = firstFile.path
        handleSAFDialog(firstFilePath) {
            if (!it) {
                return@handleSAFDialog
            }

            checkManageMediaOrHandleSAFDialogSdk30(firstFilePath) {
                if (!it) {
                    return@checkManageMediaOrHandleSAFDialogSdk30
                }

                val recycleBinPath = firstFile.isRecycleBinPath(this)
                if (canManageMedia() && !recycleBinPath && !firstFilePath.doesThisOrParentHaveNoMedia(
                        java.util.HashMap(), null
                    )
                ) {
                    val fileUris = getFileUrisFromFileDirItems(files)

                    deleteSDK30Uris(fileUris) { success ->
                        runOnUiThread {
                            callback?.invoke(success)
                        }
                    }
                } else {
                    deleteFilesCasual(files, allowDeleteFolder, callback)
                }
            }
        }
    }

    private fun deleteFilesCasual(
        files: List<FileDirItem>,
        allowDeleteFolder: Boolean = false,
        callback: ((wasSuccess: Boolean) -> Unit)? = null
    ) {
        var wasSuccess = false
        val failedFileDirItems = java.util.ArrayList<FileDirItem>()
        files.forEachIndexed { index, file ->
            deleteFileBg(file, allowDeleteFolder, true) {
                if (it) {
                    wasSuccess = true
                } else {
                    failedFileDirItems.add(file)
                }

                if (index == files.lastIndex) {
                    if (isRPlus() && failedFileDirItems.isNotEmpty()) {
                        val fileUris = getFileUrisFromFileDirItems(failedFileDirItems)
                        deleteSDK30Uris(fileUris) { success ->
                            runOnUiThread {
                                callback?.invoke(success)
                            }
                        }
                    } else {
                        runOnUiThread {
                            callback?.invoke(wasSuccess)
                        }
                    }
                }
            }
        }
    }

    fun deleteFileBg(
        fileDirItem: FileDirItem,
        allowDeleteFolder: Boolean = false,
        isDeletingMultipleFiles: Boolean,
        callback: ((wasSuccess: Boolean) -> Unit)? = null,
    ) {
        val path = fileDirItem.path
        if (isRestrictedSAFOnlyRoot(path)) {
            deleteAndroidSAFDirectory(path, allowDeleteFolder, callback)
        } else {
            val file = File(path)
            if (!isRPlus() && file.absolutePath.startsWith(internalStoragePath) && !file.canWrite()) {
                callback?.invoke(false)
                return
            }

            var fileDeleted =
                !isPathOnOTG(path) && ((!file.exists() && file.length() == 0L) || file.delete())
            if (fileDeleted) {
                deleteFromMediaStore(path) { needsRescan ->
                    if (needsRescan) {
                        rescanAndDeletePath(path) {
                            runOnUiThread {
                                callback?.invoke(true)
                            }
                        }
                    } else {
                        runOnUiThread {
                            callback?.invoke(true)
                        }
                    }
                }
            } else {
                if (getIsPathDirectory(file.absolutePath) && allowDeleteFolder) {
                    fileDeleted = deleteRecursively(file, this)
                }

                if (!fileDeleted) {
                    if (needsStupidWritePermissions(path)) {
                        handleSAFDialog(path) {
                            if (it) {
                                trySAFFileDelete(fileDirItem, allowDeleteFolder, callback)
                            }
                        }
                    } else if (isAccessibleWithSAFSdk30(path)) {
                        if (canManageMedia()) {
                            deleteSdk30(fileDirItem, callback)
                        } else {
                            handleSAFDialogSdk30(path) {
                                if (it) {
                                    deleteDocumentWithSAFSdk30(
                                        fileDirItem,
                                        allowDeleteFolder,
                                        callback
                                    )
                                }
                            }
                        }
                    } else if (isRPlus() && !isDeletingMultipleFiles) {
                        deleteSdk30(fileDirItem, callback)
                    } else {
                        callback?.invoke(false)
                    }
                }
            }
        }
    }


    fun checkWhatsNew(releases: List<Release>, currVersion: Int) {
        if (baseConfig.lastVersion == 0) {
            baseConfig.lastVersion = currVersion
            return
        }

        val newReleases = arrayListOf<Release>()
        releases.filterTo(newReleases) { it.id > baseConfig.lastVersion }

        if (newReleases.isNotEmpty()) {
            WhatsNewDialog(this, newReleases)
        }

        baseConfig.lastVersion = currVersion
    }


    private fun deleteSdk30(
        fileDirItem: FileDirItem,
        callback: ((wasSuccess: Boolean) -> Unit)?
    ) {
        val fileUris = getFileUrisFromFileDirItems(arrayListOf(fileDirItem))
        deleteSDK30Uris(fileUris) { success ->
            runOnUiThread {
                callback?.invoke(success)
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun renameFile(
        oldPath: String,
        newPath: String,
        isRenamingMultipleFiles: Boolean,
        callback: ((success: Boolean, android30RenameFormat: Android30RenameFormat) -> Unit)? = null
    ) {
        if (isRestrictedSAFOnlyRoot(oldPath)) {
            handleAndroidSAFDialog(oldPath) {
                if (!it) {
                    runOnUiThread {
                        callback?.invoke(false, Android30RenameFormat.NONE)
                    }
                    return@handleAndroidSAFDialog
                }

                try {
                    ensureBackgroundThread {
                        val success = renameAndroidSAFDocument(oldPath, newPath)
                        runOnUiThread {
                            callback?.invoke(success, Android30RenameFormat.NONE)
                        }
                    }
                } catch (e: Exception) {
                    showErrorToast(e)
                    runOnUiThread {
                        callback?.invoke(false, Android30RenameFormat.NONE)
                    }
                }
            }
        } else if (isAccessibleWithSAFSdk30(oldPath)) {
            if (canManageMedia() && !File(oldPath).isDirectory && isPathOnInternalStorage(oldPath)) {
                renameCasually(oldPath, newPath, isRenamingMultipleFiles, callback)
            } else {
                handleSAFDialogSdk30(oldPath) {
                    if (!it) {
                        return@handleSAFDialogSdk30
                    }

                    try {
                        ensureBackgroundThread {
                            val success = renameDocumentSdk30(oldPath, newPath)
                            if (success) {
                                updateInMediaStore(oldPath, newPath)
                                rescanPath(newPath) {
                                    runOnUiThread {
                                        callback?.invoke(true, Android30RenameFormat.NONE)
                                    }
                                    if (!oldPath.equals(newPath, true)) {
                                        deleteFromMediaStore(oldPath)
                                    }
                                    scanPathRecursively(newPath)
                                }
                            } else {
                                runOnUiThread {
                                    callback?.invoke(false, Android30RenameFormat.NONE)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        showErrorToast(e)
                        runOnUiThread {
                            callback?.invoke(false, Android30RenameFormat.NONE)
                        }
                    }
                }
            }
        } else if (needsStupidWritePermissions(newPath)) {
            handleSAFDialog(newPath) {
                if (!it) {
                    return@handleSAFDialog
                }

                val document = getSomeDocumentFile(oldPath)
                if (document == null || (File(oldPath).isDirectory != document.isDirectory)) {
                    runOnUiThread {
                        toast(R.string.unknown_error_occurred)
                        callback?.invoke(false, Android30RenameFormat.NONE)
                    }
                    return@handleSAFDialog
                }

                try {
                    ensureBackgroundThread {
                        try {
                            DocumentsContract.renameDocument(
                                applicationContext.contentResolver,
                                document.uri,
                                newPath.getFilenameFromPath()
                            )
                        } catch (ignored: FileNotFoundException) {
                            // FileNotFoundException is thrown in some weird cases, but renaming works just fine
                        } catch (e: Exception) {
                            showErrorToast(e)
                            callback?.invoke(false, Android30RenameFormat.NONE)
                            return@ensureBackgroundThread
                        }

                        updateInMediaStore(oldPath, newPath)
                        rescanPaths(arrayListOf(oldPath, newPath)) {
                            if (!baseConfig.keepLastModified) {
                                updateLastModified(newPath, System.currentTimeMillis())
                            }
                            deleteFromMediaStore(oldPath)
                            runOnUiThread {
                                callback?.invoke(true, Android30RenameFormat.NONE)
                            }
                        }
                    }
                } catch (e: Exception) {
                    showErrorToast(e)
                    runOnUiThread {
                        callback?.invoke(false, Android30RenameFormat.NONE)
                    }
                }
            }
        } else renameCasually(oldPath, newPath, isRenamingMultipleFiles, callback)
    }

    private fun renameCasually(
        oldPath: String,
        newPath: String,
        isRenamingMultipleFiles: Boolean,
        callback: ((success: Boolean, android30RenameFormat: Android30RenameFormat) -> Unit)?
    ) {
        val oldFile = File(oldPath)
        val newFile = File(newPath)
        val tempFile = try {
            createTempFile(oldFile) ?: return
        } catch (exception: Exception) {
            if (isRPlus() && exception is java.nio.file.FileSystemException) {
                // if we are renaming multiple files at once, we should give the Android 30+ permission dialog all uris together, not one by one
                if (isRenamingMultipleFiles) {
                    callback?.invoke(false, Android30RenameFormat.CONTENT_RESOLVER)
                } else {
                    val fileUris =
                        getFileUrisFromFileDirItems(arrayListOf(File(oldPath).toFileDirItem(this)))
                    updateSDK30Uris(fileUris) { success ->
                        if (success) {
                            val values = ContentValues().apply {
                                put(Images.Media.DISPLAY_NAME, newPath.getFilenameFromPath())
                            }

                            try {
                                contentResolver.update(fileUris.first(), values, null, null)
                                callback?.invoke(true, Android30RenameFormat.NONE)
                            } catch (e: Exception) {
                                showErrorToast(e)
                                callback?.invoke(false, Android30RenameFormat.NONE)
                            }
                        } else {
                            callback?.invoke(false, Android30RenameFormat.NONE)
                        }
                    }
                }
            } else {
                if (exception is IOException && File(oldPath).isDirectory && isRestrictedWithSAFSdk30(
                        oldPath
                    )
                ) {
                    toast(R.string.cannot_rename_folder)
                } else {
                    showErrorToast(exception)
                }
                callback?.invoke(false, Android30RenameFormat.NONE)
            }
            return
        }

        val oldToTempSucceeds = oldFile.renameTo(tempFile)
        val tempToNewSucceeds = tempFile.renameTo(newFile)
        if (oldToTempSucceeds && tempToNewSucceeds) {
            if (newFile.isDirectory) {
                updateInMediaStore(oldPath, newPath)
                rescanPath(newPath) {
                    runOnUiThread {
                        callback?.invoke(true, Android30RenameFormat.NONE)
                    }
                    if (!oldPath.equals(newPath, true)) {
                        deleteFromMediaStore(oldPath)
                    }
                    scanPathRecursively(newPath)
                }
            } else {
                if (!baseConfig.keepLastModified) {
                    newFile.setLastModified(System.currentTimeMillis())
                }
                updateInMediaStore(oldPath, newPath)
                scanPathsRecursively(arrayListOf(newPath)) {
                    if (!oldPath.equals(newPath, true)) {
                        deleteFromMediaStore(oldPath)
                    }
                    runOnUiThread {
                        callback?.invoke(true, Android30RenameFormat.NONE)
                    }
                }
            }
        } else {
            tempFile.delete()
            newFile.delete()
            if (isRPlus()) {
                // if we are renaming multiple files at once, we should give the Android 30+ permission dialog all uris together, not one by one
                if (isRenamingMultipleFiles) {
                    callback?.invoke(false, Android30RenameFormat.SAF)
                } else {
                    val fileUris =
                        getFileUrisFromFileDirItems(arrayListOf(File(oldPath).toFileDirItem(this)))
                    updateSDK30Uris(fileUris) { success ->
                        if (!success) {
                            return@updateSDK30Uris
                        }
                        try {
                            val sourceUri = fileUris.first()
                            val sourceFile = File(oldPath).toFileDirItem(this)

                            if (oldPath.equals(newPath, true)) {
                                val tempDestination = try {
                                    createTempFile(
                                        File(
                                            sourceFile.path
                                        )
                                    ) ?: return@updateSDK30Uris
                                } catch (exception: Exception) {
                                    showErrorToast(exception)
                                    callback?.invoke(false, Android30RenameFormat.NONE)
                                    return@updateSDK30Uris
                                }

                                val copyTempSuccess =
                                    copySingleFileSdk30(
                                        sourceFile,
                                        tempDestination.toFileDirItem(this)
                                    )
                                if (copyTempSuccess) {
                                    contentResolver.delete(sourceUri, null)
                                    tempDestination.renameTo(File(newPath))
                                    if (!baseConfig.keepLastModified) {
                                        newFile.setLastModified(System.currentTimeMillis())
                                    }
                                    updateInMediaStore(oldPath, newPath)
                                    scanPathsRecursively(arrayListOf(newPath)) {
                                        runOnUiThread {
                                            callback?.invoke(true, Android30RenameFormat.NONE)
                                        }
                                    }
                                } else {
                                    callback?.invoke(false, Android30RenameFormat.NONE)
                                }
                            } else {
                                val destinationFile = FileDirItem(
                                    newPath,
                                    newPath.getFilenameFromPath(),
                                    sourceFile.isDirectory,
                                    sourceFile.children,
                                    sourceFile.size,
                                    sourceFile.modified
                                )
                                val copySuccessful =
                                    copySingleFileSdk30(sourceFile, destinationFile)
                                if (copySuccessful) {
                                    if (!baseConfig.keepLastModified) {
                                        newFile.setLastModified(System.currentTimeMillis())
                                    }
                                    contentResolver.delete(sourceUri, null)
                                    updateInMediaStore(oldPath, newPath)
                                    scanPathsRecursively(arrayListOf(newPath)) {
                                        runOnUiThread {
                                            callback?.invoke(true, Android30RenameFormat.NONE)
                                        }
                                    }
                                } else {
                                    toast(R.string.unknown_error_occurred)
                                    callback?.invoke(false, Android30RenameFormat.NONE)
                                }
                            }

                        } catch (e: Exception) {
                            showErrorToast(e)
                            callback?.invoke(false, Android30RenameFormat.NONE)
                        }
                    }
                }
            } else {
                toast(R.string.unknown_error_occurred)
                callback?.invoke(false, Android30RenameFormat.NONE)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getFileOutputStream(
        fileDirItem: FileDirItem,
        allowCreatingNewFile: Boolean = false,
        callback: (outputStream: OutputStream?) -> Unit
    ) {
        val targetFile = File(fileDirItem.path)
        when {
            isRestrictedSAFOnlyRoot(fileDirItem.path) -> {
                handleAndroidSAFDialog(fileDirItem.path) {
                    if (!it) {
                        return@handleAndroidSAFDialog
                    }

                    val uri = getAndroidSAFUri(fileDirItem.path)
                    if (!getDoesFilePathExist(fileDirItem.path)) {
                        createAndroidSAFFile(fileDirItem.path)
                    }
                    callback.invoke(applicationContext.contentResolver.openOutputStream(uri, "wt"))
                }
            }

            needsStupidWritePermissions(fileDirItem.path) -> {
                handleSAFDialog(fileDirItem.path) {
                    if (!it) {
                        return@handleSAFDialog
                    }

                    var document = getDocumentFile(fileDirItem.path)
                    if (document == null && allowCreatingNewFile) {
                        document = getDocumentFile(fileDirItem.getParentPath())
                    }

                    if (document == null) {
                        showFileCreateError(fileDirItem.path)
                        callback(null)
                        return@handleSAFDialog
                    }

                    if (!getDoesFilePathExist(fileDirItem.path)) {
                        document = getDocumentFile(fileDirItem.path) ?: document.createFile(
                            "",
                            fileDirItem.name
                        )
                    }

                    if (document?.exists() == true) {
                        try {
                            callback(
                                applicationContext.contentResolver.openOutputStream(
                                    document.uri,
                                    "wt"
                                )
                            )
                        } catch (e: FileNotFoundException) {
                            showErrorToast(e)
                            callback(null)
                        }
                    } else {
                        showFileCreateError(fileDirItem.path)
                        callback(null)
                    }
                }
            }

            isAccessibleWithSAFSdk30(fileDirItem.path) -> {
                handleSAFDialogSdk30(fileDirItem.path) {
                    if (!it) {
                        return@handleSAFDialogSdk30
                    }

                    callback.invoke(
                        try {
                            val uri = createDocumentUriUsingFirstParentTreeUri(fileDirItem.path)
                            if (!getDoesFilePathExist(fileDirItem.path)) {
                                createSAFFileSdk30(fileDirItem.path)
                            }
                            applicationContext.contentResolver.openOutputStream(uri, "wt")
                        } catch (e: Exception) {
                            null
                        } ?: createCasualFileOutputStream(targetFile)
                    )
                }
            }

            isRestrictedWithSAFSdk30(fileDirItem.path) -> {
                callback.invoke(
                    try {
                        val fileUri = getFileUrisFromFileDirItems(arrayListOf(fileDirItem))
                        applicationContext.contentResolver.openOutputStream(fileUri.first(), "wt")
                    } catch (e: Exception) {
                        null
                    } ?: createCasualFileOutputStream(targetFile)
                )
            }

            else -> {
                callback.invoke(createCasualFileOutputStream(targetFile))
            }
        }
    }


     fun copySingleFileSdk30(source: FileDirItem, destination: FileDirItem): Boolean {
        val directory = destination.getParentPath()
        if (!createDirectorySync(directory)) {
            val error = String.format(getString(R.string.could_not_create_folder), directory)
            showErrorToast(error)
            return false
        }

        var inputStream: InputStream? = null
        var out: OutputStream? = null
        try {

            out = getFileOutputStreamSync(destination.path, source.path.getMimeType())
            inputStream = getFileInputStreamSync(source.path)!!

            var copiedSize = 0L
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var bytes = inputStream.read(buffer)
            while (bytes >= 0) {
                out!!.write(buffer, 0, bytes)
                copiedSize += bytes
                bytes = inputStream.read(buffer)
            }

            out?.flush()

            return if (source.size == copiedSize && getDoesFilePathExist(destination.path)) {
                if (baseConfig.keepLastModified) {
                    copyOldLastModified(source.path, destination.path)
                    val lastModified = File(source.path).lastModified()
                    if (lastModified != 0L) {
                        File(destination.path).setLastModified(lastModified)
                    }
                }
                true
            } else {
                false
            }
        } finally {
            inputStream?.close()
            out?.close()
        }
    }

    private fun copyOldLastModified(sourcePath: String, destinationPath: String) {
        val projection =
            arrayOf(Images.Media.DATE_TAKEN, Images.Media.DATE_MODIFIED)
        val uri = Files.getContentUri("external")
        val selection = "${MediaStore.MediaColumns.DATA} = ?"
        var selectionArgs = arrayOf(sourcePath)
        val cursor =
            applicationContext.contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                null
            )

        cursor?.use {
            if (cursor.moveToFirst()) {
                val dateTaken = cursor.getLongValue(Images.Media.DATE_TAKEN)
                val dateModified = cursor.getIntValue(Images.Media.DATE_MODIFIED)

                val values = ContentValues().apply {
                    put(Images.Media.DATE_TAKEN, dateTaken)
                    put(Images.Media.DATE_MODIFIED, dateModified)
                }

                selectionArgs = arrayOf(destinationPath)
                applicationContext.contentResolver.update(uri, values, selection, selectionArgs)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isShowingSAFDialog(path: String): Boolean {
        return if ((!isRPlus() && isPathOnSD(path) && !isSDCardSetAsDefaultStorage() && (baseConfig.sdTreeUri.isEmpty() || !hasProperStoredTreeUri(
                false
            )))
        ) {
            runOnUiThread {
                if (!isDestroyed && !isFinishing) {
                    WritePermissionDialog(this, WritePermissionDialog.Mode.SdCard) {
                        Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                            putExtra(EXTRA_SHOW_ADVANCED, true)
                            try {
                                startActivityForResult(this, OPEN_DOCUMENT_TREE_SD)
                                checkedDocumentPath = path
                                return@apply
                            } catch (e: Exception) {
                                type = "*/*"
                            }

                            try {
                                startActivityForResult(this, OPEN_DOCUMENT_TREE_SD)
                                checkedDocumentPath = path
                            } catch (e: ActivityNotFoundException) {
                                toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
                            } catch (e: Exception) {
                                toast(R.string.unknown_error_occurred)
                            }
                        }
                    }
                }
            }
            true
        } else {
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isShowingSAFDialogSdk30(path: String): Boolean {
        return if (isAccessibleWithSAFSdk30(path) && !hasProperStoredFirstParentUri(path)) {
            runOnUiThread {
                if (!isDestroyed && !isFinishing) {
                    val level = getFirstParentLevel(path)
                    WritePermissionDialog(
                        this,
                        WritePermissionDialog.Mode.OpenDocumentTreeSDK30(
                            path.getFirstParentPath(
                                this,
                                level
                            )
                        )
                    ) {
                        Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                            putExtra(EXTRA_SHOW_ADVANCED, true)
                            putExtra(
                                DocumentsContract.EXTRA_INITIAL_URI,
                                createFirstParentTreeUriUsingRootTree(path)
                            )
                            try {
                                startActivityForResult(this, OPEN_DOCUMENT_TREE_FOR_SDK_30)
                                checkedDocumentPath = path
                                return@apply
                            } catch (e: Exception) {
                                type = "*/*"
                            }

                            try {
                                startActivityForResult(this, OPEN_DOCUMENT_TREE_FOR_SDK_30)
                                checkedDocumentPath = path
                            } catch (e: ActivityNotFoundException) {
                                toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
                            } catch (e: Exception) {
                                toast(R.string.unknown_error_occurred)
                            }
                        }
                    }
                }
            }
            true
        } else {
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isShowingSAFCreateDocumentDialogSdk30(path: String): Boolean {
        return if (!hasProperStoredDocumentUriSdk30(path)) {
            runOnUiThread {
                if (!isDestroyed && !isFinishing) {
                    WritePermissionDialog(this, WritePermissionDialog.Mode.CreateDocumentSDK30) {
                        Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                            type = DocumentsContract.Document.MIME_TYPE_DIR
                            putExtra(EXTRA_SHOW_ADVANCED, true)
                            addCategory(Intent.CATEGORY_OPENABLE)
                            putExtra(
                                DocumentsContract.EXTRA_INITIAL_URI,
                                buildDocumentUriSdk30(path.getParentPath())
                            )
                            putExtra(Intent.EXTRA_TITLE, path.getFilenameFromPath())
                            try {
                                startActivityForResult(this, CREATE_DOCUMENT_SDK_30)
                                checkedDocumentPath = path
                                return@apply
                            } catch (e: Exception) {
                                type = "*/*"
                            }

                            try {
                                startActivityForResult(this, CREATE_DOCUMENT_SDK_30)
                                checkedDocumentPath = path
                            } catch (e: ActivityNotFoundException) {
                                toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
                            } catch (e: Exception) {
                                toast(R.string.unknown_error_occurred)
                            }
                        }
                    }
                }
            }
            true
        } else {
            false
        }
    }

    private fun isShowingAndroidSAFDialog(path: String): Boolean {
        return if (isRestrictedSAFOnlyRoot(path) && (getAndroidTreeUri(path).isEmpty() || !hasProperStoredAndroidTreeUri(
                path
            ))
        ) {
            runOnUiThread {
                if (!isDestroyed && !isFinishing) {
                    ConfirmationAdvancedDialog(
                        this,
                        "",
                        R.string.confirm_storage_access_android_text,
                        R.string.ok,
                        R.string.cancel
                    ) { success ->
                        if (success) {
                            Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                                putExtra(EXTRA_SHOW_ADVANCED, true)
                                putExtra(
                                    DocumentsContract.EXTRA_INITIAL_URI,
                                    createAndroidDataOrObbUri(path)
                                )
                                try {
                                    startActivityForResult(
                                        this,
                                        OPEN_DOCUMENT_TREE_FOR_ANDROID_DATA_OR_OBB
                                    )
                                    checkedDocumentPath = path
                                    return@apply
                                } catch (e: Exception) {
                                    type = "*/*"
                                }

                                try {
                                    startActivityForResult(
                                        this,
                                        OPEN_DOCUMENT_TREE_FOR_ANDROID_DATA_OR_OBB
                                    )
                                    checkedDocumentPath = path
                                } catch (e: ActivityNotFoundException) {
                                    toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
                                } catch (e: Exception) {
                                    toast(R.string.unknown_error_occurred)
                                }
                            }
                        }
                    }
                }
            }
            true
        } else {
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isShowingOTGDialog(path: String): Boolean {
        return if (!isRPlus() && isPathOnOTG(path) && (baseConfig.otgTreeUri.isEmpty() || !hasProperStoredTreeUri(
                true
            ))
        ) {
            showOTGPermissionDialog(path)
            true
        } else {
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showOTGPermissionDialog(path: String) {
        runOnUiThread {
            if (!isDestroyed && !isFinishing) {
                WritePermissionDialog(this, WritePermissionDialog.Mode.Otg) {
                    Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                        try {
                            startActivityForResult(this, OPEN_DOCUMENT_TREE_OTG)
                            checkedDocumentPath = path
                            return@apply
                        } catch (e: Exception) {
                            type = "*/*"
                        }

                        try {
                            startActivityForResult(this, OPEN_DOCUMENT_TREE_OTG)
                            checkedDocumentPath = path
                        } catch (e: ActivityNotFoundException) {
                            toast(R.string.system_service_disabled, Toast.LENGTH_LONG)
                        } catch (e: Exception) {
                            toast(R.string.unknown_error_occurred)
                        }
                    }
                }
            }
        }
    }

    private fun deleteFile(
        fileDirItem: FileDirItem,
        allowDeleteFolder: Boolean = false,
        isDeletingMultipleFiles: Boolean,
        callback: ((wasSuccess: Boolean) -> Unit)? = null
    ) {
        ensureBackgroundThread {
            deleteFileBg(fileDirItem, allowDeleteFolder, isDeletingMultipleFiles, callback)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun resizeImage(
        oldPath: String,
        newPath: String,
        size: Point,
        callback: (success: Boolean) -> Unit
    ) {
        var oldExif: ExifInterface? = null
        if (isNougatPlus()) {
            val inputStream = contentResolver.openInputStream(Uri.fromFile(File(oldPath)))
            oldExif = ExifInterface(inputStream!!)
        }

        val newBitmap =
            Glide.with(applicationContext).asBitmap().load(oldPath).submit(size.x, size.y).get()

        val newFile = File(newPath)
        val newFileDirItem = FileDirItem(newPath, newPath.getFilenameFromPath())
        getFileOutputStream(newFileDirItem, true) { out ->
            if (out != null) {
                out.use {
                    try {
                        newBitmap.compress(newFile.absolutePath.getCompressionFormat(), 90, out)

                        if (isNougatPlus()) {
                            val newExif = ExifInterface(newFile.absolutePath)
                            oldExif?.copyNonDimensionAttributesTo(newExif)
                        }
                    } catch (ignored: Exception) {
                    }

                    callback(true)
                }
            } else {
                callback(false)
            }
        }
    }

    fun rescanPathsAndUpdateLastModified(
        paths: ArrayList<String>,
        pathLastModifiedMap: Map<String, Long>,
        callback: () -> Unit
    ) {
        fixDateTaken(paths, false)
        for (path in paths) {
            val file = File(path)
            val lastModified = pathLastModifiedMap[path]
            if (config.keepLastModified && lastModified != null && lastModified != 0L) {
                File(file.absolutePath).setLastModified(lastModified)
                updateLastModified(file.absolutePath, lastModified)
            }
        }
        rescanPaths(paths, callback)
    }

    private fun createCasualFileOutputStream(targetFile: File): OutputStream? {

        if (targetFile.parentFile?.exists() == false) {
            targetFile.parentFile?.mkdirs()
        }

        return try {
            FileOutputStream(targetFile)
        } catch (e: Exception) {
            showErrorToast(e)
            null
        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun tryRotateByExif(
        path: String,
        degrees: Int,
        showToasts: Boolean,
        callback: () -> Unit
    ): Boolean {

        return try {
            val file = File(path)
            val oldLastModified = file.lastModified()
            if (saveImageRotation(path, degrees)) {
                fileRotatedSuccessfully(path, oldLastModified)
                callback.invoke()
                if (showToasts) {
                    toast(R.string.file_saved)
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            // lets not show IOExceptions, rotating is saved just fine even with them
            if (showToasts && e !is IOException) {
                showErrorToast(e)
            }
            false
        }

    }

    private fun createTempFile(file: File): File? {
        return if (file.isDirectory) {
            createTempDir("temp", "${System.currentTimeMillis()}", file.parentFile)
        } else {
            if (isRPlus()) {
                // this can throw FileSystemException, lets catch and handle it at the place calling this function
                kotlin.io.path.createTempFile(
                    file.parentFile!!.toPath(),
                    "temp",
                    "${System.currentTimeMillis()}"
                ).toFile()
            } else {
                createTempFile("temp", "${System.currentTimeMillis()}", file.parentFile)
            }
        }
    }

    private fun onApplyWindowInsets(callback: (WindowInsetsCompat) -> Unit) {
        window.decorView.setOnApplyWindowInsetsListener { view, insets ->
            callback(WindowInsetsCompat.toWindowInsetsCompat(insets))
            view.onApplyWindowInsets(insets)
            insets
        }
    }

    private fun fileRotatedSuccessfully(path: String, lastModified: Long) {

        if (config.keepLastModified && lastModified != 0L) {
            File(path).setLastModified(lastModified)
            updateLastModified(path, lastModified)
        }

        Picasso.get().invalidate(path.getFileKey(lastModified))
        // we cannot refresh a specific image in Glide Cache, so just clear it all
        val glide = Glide.get(applicationContext)
        glide.clearDiskCache()
        runOnUiThread {
            glide.clearMemory()
        }
    }

}