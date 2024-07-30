package com.simplemobiletools.gallery.pro.helpers

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Looper
import android.provider.ContactsContract
import android.util.Log
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.StringRes
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.extensions.normalizeString
import com.simplemobiletools.gallery.pro.extensions.times
import com.simplemobiletools.gallery.pro.models.contacts.LocalContact

const val HOUR_MINUTES = 60
const val MINUTE_SECONDS = 60
const val DAY_MINUTES = 24 * HOUR_MINUTES
const val WEEK_MINUTES = DAY_MINUTES * 7
const val MONTH_MINUTES = DAY_MINUTES * 30
const val YEAR_MINUTES = DAY_MINUTES * 365

const val HOUR_SECONDS = HOUR_MINUTES * 60
const val DAY_SECONDS = DAY_MINUTES * 60
const val MONTH_SECONDS = MONTH_MINUTES * 60

// shared preferences
const val DIRECTORY_SORT_ORDER = "directory_sort_order"
const val GROUP_FOLDER_PREFIX = "group_folder_"
const val VIEW_TYPE_PREFIX = "view_type_folder_"
const val SHOW_HIDDEN_MEDIA = "show_hidden_media"
const val TEMPORARILY_SHOW_HIDDEN = "temporarily_show_hidden"
const val TEMPORARILY_SHOW_EXCLUDED = "temporarily_show_excluded"
const val EXCLUDED_PASSWORD_PROTECTION = "excluded_password_protection"
const val EXCLUDED_PASSWORD_HASH = "excluded_password_hash"
const val EXCLUDED_PROTECTION_TYPE = "excluded_protection_type"
const val IS_THIRD_PARTY_INTENT = "is_third_party_intent"
const val AUTOPLAY_VIDEOS = "autoplay_videos"
const val REMEMBER_LAST_VIDEO_POSITION = "remember_last_video_position"
const val LOOP_VIDEOS = "loop_videos"
const val OPEN_VIDEOS_ON_SEPARATE_SCREEN = "open_videos_on_separate_screen"
const val ANIMATE_GIFS = "animate_gifs"
const val MAX_BRIGHTNESS = "max_brightness"
const val CROP_THUMBNAILS = "crop_thumbnails"
const val SHOW_THUMBNAIL_VIDEO_DURATION = "show_thumbnail_video_duration"
const val SCREEN_ROTATION = "screen_rotation"
const val DISPLAY_FILE_NAMES = "display_file_names"
const val BLACK_BACKGROUND = "dark_background"
const val PINNED_FOLDERS = "pinned_folders"
const val FILTER_MEDIA = "filter_media"
const val DEFAULT_FOLDER = "default_folder"
const val DIR_COLUMN_CNT = "dir_column_cnt"
const val DIR_LANDSCAPE_COLUMN_CNT = "dir_landscape_column_cnt"
const val DIR_HORIZONTAL_COLUMN_CNT = "dir_horizontal_column_cnt"
const val DIR_LANDSCAPE_HORIZONTAL_COLUMN_CNT = "dir_landscape_horizontal_column_cnt"
const val MEDIA_COLUMN_CNT = "media_column_cnt"
const val MEDIA_LANDSCAPE_COLUMN_CNT = "media_landscape_column_cnt"
const val MEDIA_HORIZONTAL_COLUMN_CNT = "media_horizontal_column_cnt"
const val MEDIA_LANDSCAPE_HORIZONTAL_COLUMN_CNT = "media_landscape_horizontal_column_cnt"
const val SHOW_ALL =
    "show_all"                           // display images and videos from all folders together
const val HIDE_FOLDER_TOOLTIP_SHOWN = "hide_folder_tooltip_shown"
const val EXCLUDED_FOLDERS = "excluded_folders"
const val INCLUDED_FOLDERS = "included_folders"
const val ALBUM_COVERS = "album_covers"
const val HIDE_SYSTEM_UI = "hide_system_ui"
const val DELETE_EMPTY_FOLDERS = "delete_empty_folders"
const val ALLOW_PHOTO_GESTURES = "allow_photo_gestures"
const val ALLOW_VIDEO_GESTURES = "allow_video_gestures"
const val TEMP_FOLDER_PATH = "temp_folder_path"
const val VIEW_TYPE_FOLDERS = "view_type_folders"
const val VIEW_TYPE_FILES = "view_type_files"
const val SHOW_EXTENDED_DETAILS = "show_extended_details"
const val EXTENDED_DETAILS = "extended_details"
const val HIDE_EXTENDED_DETAILS = "hide_extended_details"
const val ALLOW_INSTANT_CHANGE = "allow_instant_change"
const val LAST_FILEPICKER_PATH = "last_filepicker_path"
const val TEMP_SKIP_DELETE_CONFIRMATION = "temp_skip_delete_confirmation"
const val TEMP_SKIP_RECYCLE_BIN = "temp_skip_recycle_bin"
const val BOTTOM_ACTIONS = "bottom_actions"
const val LAST_VIDEO_POSITION_PREFIX = "last_video_position_"
const val VISIBLE_BOTTOM_ACTIONS = "visible_bottom_actions"
const val WERE_FAVORITES_PINNED = "were_favorites_pinned"
const val WAS_RECYCLE_BIN_PINNED = "was_recycle_bin_pinned"
const val USE_RECYCLE_BIN = "use_recycle_bin"
const val GROUP_BY = "group_by"
const val EVER_SHOWN_FOLDERS = "ever_shown_folders"
const val SHOW_RECYCLE_BIN_AT_FOLDERS = "show_recycle_bin_at_folders"
const val SHOW_RECYCLE_BIN_LAST = "show_recycle_bin_last"
const val ALLOW_ZOOMING_IMAGES = "allow_zooming_images"
const val WAS_SVG_SHOWING_HANDLED = "was_svg_showing_handled"
const val LAST_BIN_CHECK = "last_bin_check"
const val SHOW_HIGHEST_QUALITY = "show_highest_quality"
const val ALLOW_DOWN_GESTURE = "allow_down_gesture"
const val LAST_EDITOR_CROP_ASPECT_RATIO = "last_editor_crop_aspect_ratio"
const val LAST_EDITOR_CROP_OTHER_ASPECT_RATIO_X = "last_editor_crop_other_aspect_ratio_x_2"
const val LAST_EDITOR_CROP_OTHER_ASPECT_RATIO_Y = "last_editor_crop_other_aspect_ratio_y_2"
const val GROUP_DIRECT_SUBFOLDERS = "group_direct_subfolders"
const val SHOW_WIDGET_FOLDER_NAME = "show_widget_folder_name"
const val ALLOW_ONE_TO_ONE_ZOOM = "allow_one_to_one_zoom"
const val ALLOW_ROTATING_WITH_GESTURES = "allow_rotating_with_gestures"
const val LAST_EDITOR_DRAW_COLOR = "last_editor_draw_color"
const val LAST_EDITOR_BRUSH_SIZE = "last_editor_brush_size"
const val SHOW_NOTCH = "show_notch"
const val FILE_LOADING_PRIORITY = "file_loading_priority"
const val SPAM_FOLDERS_CHECKED = "spam_folders_checked"
const val SHOW_THUMBNAIL_FILE_TYPES = "show_thumbnail_file_types"
const val MARK_FAVORITE_ITEMS = "mark_favorite_items"
const val EDITOR_BRUSH_COLOR = "editor_brush_color"
const val EDITOR_BRUSH_HARDNESS = "editor_brush_hardness"
const val EDITOR_BRUSH_SIZE = "editor_brush_size"
const val WERE_FAVORITES_MIGRATED = "were_favorites_migrated"
const val FOLDER_THUMBNAIL_STYLE = "folder_thumbnail_style"
const val FOLDER_MEDIA_COUNT = "folder_media_count"
const val LIMIT_FOLDER_TITLE = "folder_limit_title"
const val THUMBNAIL_SPACING = "thumbnail_spacing"
const val FILE_ROUNDED_CORNERS = "file_rounded_corners"
const val CUSTOM_FOLDERS_ORDER = "custom_folders_order"
const val AVOID_SHOWING_ALL_FILES_PROMPT = "avoid_showing_all_files_prompt"
const val SEARCH_ALL_FILES_BY_DEFAULT = "search_all_files_by_default"
const val LAST_EXPORTED_FAVORITES_FOLDER = "last_exported_favorites_folder"

// slideshow
const val SLIDESHOW_INTERVAL = "slideshow_interval"
const val SLIDESHOW_INCLUDE_VIDEOS = "slideshow_include_videos"
const val SLIDESHOW_INCLUDE_GIFS = "slideshow_include_gifs"
const val SLIDESHOW_RANDOM_ORDER = "slideshow_random_order"
const val SLIDESHOW_MOVE_BACKWARDS = "slideshow_move_backwards"
const val SLIDESHOW_ANIMATION = "slideshow_animation"
const val SLIDESHOW_LOOP = "loop_slideshow"
const val SLIDESHOW_DEFAULT_INTERVAL = 5
const val SLIDESHOW_SLIDE_DURATION = 500L
const val SLIDESHOW_FADE_DURATION = 1500L
const val SLIDESHOW_START_ON_ENTER = "slideshow_start_on_enter"

// slideshow animations
const val SLIDESHOW_ANIMATION_NONE = 0
const val SLIDESHOW_ANIMATION_SLIDE = 1
const val SLIDESHOW_ANIMATION_FADE = 2

const val RECYCLE_BIN = "recycle_bin"
const val SHOW_FAVORITES = "show_favorites"
const val SHOW_RECYCLE_BIN = "show_recycle_bin"
const val IS_IN_RECYCLE_BIN = "is_in_recycle_bin"
const val SHOW_NEXT_ITEM = "show_next_item"
const val SHOW_PREV_ITEM = "show_prev_item"
const val GO_TO_NEXT_ITEM = "go_to_next_item"
const val GO_TO_PREV_ITEM = "go_to_prev_item"
const val MAX_COLUMN_COUNT = 20
const val SHOW_TEMP_HIDDEN_DURATION = 300000L
const val CLICK_MAX_DURATION = 150
const val CLICK_MAX_DISTANCE = 100
const val MAX_CLOSE_DOWN_GESTURE_DURATION = 300
const val DRAG_THRESHOLD = 8
const val MONTH_MILLISECONDS = MONTH_SECONDS * 1000L
const val MIN_SKIP_LENGTH = 2000
const val HIDE_SYSTEM_UI_DELAY = 500L
const val MAX_PRINT_SIDE_SIZE = 4096
const val FAST_FORWARD_VIDEO_MS = 10000

const val DIRECTORY = "directory"
const val MEDIUM = "medium"
const val PATH = "path"
const val GET_IMAGE_INTENT = "get_image_intent"
const val GET_VIDEO_INTENT = "get_video_intent"
const val GET_ANY_INTENT = "get_any_intent"
const val SET_WALLPAPER_INTENT = "set_wallpaper_intent"
const val IS_VIEW_INTENT = "is_view_intent"
const val PICKED_PATHS = "picked_paths"
const val SHOULD_INIT_FRAGMENT = "should_init_fragment"
const val PORTRAIT_PATH = "portrait_path"
const val SKIP_AUTHENTICATION = "skip_authentication"

// rotations
const val ROTATE_BY_SYSTEM_SETTING = 0
const val ROTATE_BY_DEVICE_ROTATION = 1
const val ROTATE_BY_ASPECT_RATIO = 2

// file loading priority
const val PRIORITY_SPEED = 0
const val PRIORITY_COMPROMISE = 1
const val PRIORITY_VALIDITY = 2

// extended details values
const val EXT_NAME = 1
const val EXT_PATH = 2
const val EXT_SIZE = 4
const val EXT_RESOLUTION = 8
const val EXT_LAST_MODIFIED = 16
const val EXT_DATE_TAKEN = 32
const val EXT_CAMERA_MODEL = 64
const val EXT_EXIF_PROPERTIES = 128
const val EXT_GPS = 2048

// media types
const val TYPE_IMAGES = 1
const val TYPE_VIDEOS = 2
const val TYPE_GIFS = 4
const val TYPE_RAWS = 8
const val TYPE_SVGS = 16
const val TYPE_PORTRAITS = 32

fun getDefaultFileFilter() = TYPE_IMAGES or TYPE_VIDEOS or TYPE_GIFS or TYPE_RAWS or TYPE_SVGS

const val LOCATION_INTERNAL = 1
const val LOCATION_SD = 2
const val LOCATION_OTG = 3

const val GROUP_BY_NONE = 1
const val GROUP_BY_LAST_MODIFIED_DAILY = 2
const val GROUP_BY_DATE_TAKEN_DAILY = 4
const val GROUP_BY_FILE_TYPE = 8
const val GROUP_BY_EXTENSION = 16
const val GROUP_BY_FOLDER = 32
const val GROUP_BY_LAST_MODIFIED_MONTHLY = 64
const val GROUP_BY_DATE_TAKEN_MONTHLY = 128
const val GROUP_DESCENDING = 1024
const val GROUP_SHOW_FILE_COUNT = 2048

// bottom actions
const val BOTTOM_ACTION_TOGGLE_FAVORITE = 1
const val BOTTOM_ACTION_EDIT = 2
const val BOTTOM_ACTION_SHARE = 4
const val BOTTOM_ACTION_DELETE = 8
const val BOTTOM_ACTION_ROTATE = 16
const val BOTTOM_ACTION_PROPERTIES = 32
const val BOTTOM_ACTION_CHANGE_ORIENTATION = 64
const val BOTTOM_ACTION_SLIDESHOW = 128
const val BOTTOM_ACTION_SHOW_ON_MAP = 256
const val BOTTOM_ACTION_TOGGLE_VISIBILITY = 512
const val BOTTOM_ACTION_RENAME = 1024
const val BOTTOM_ACTION_SET_AS = 2048
const val BOTTOM_ACTION_COPY = 4096
const val BOTTOM_ACTION_MOVE = 8192
const val BOTTOM_ACTION_RESIZE = 16384

const val DEFAULT_BOTTOM_ACTIONS =
    BOTTOM_ACTION_TOGGLE_FAVORITE or BOTTOM_ACTION_EDIT or BOTTOM_ACTION_SHARE or BOTTOM_ACTION_DELETE

// aspect ratios used at the editor for cropping
const val ASPECT_RATIO_FREE = 0
const val ASPECT_RATIO_ONE_ONE = 1
const val ASPECT_RATIO_FOUR_THREE = 2
const val ASPECT_RATIO_SIXTEEN_NINE = 3
const val ASPECT_RATIO_OTHER = 4

// constants related to image quality
const val LOW_TILE_DPI = 160
const val NORMAL_TILE_DPI = 220
const val WEIRD_TILE_DPI = 240
const val HIGH_TILE_DPI = 280

const val ROUNDED_CORNERS_NONE = 1
const val ROUNDED_CORNERS_SMALL = 2
const val ROUNDED_CORNERS_BIG = 3

const val FOLDER_MEDIA_CNT_LINE = 1
const val FOLDER_MEDIA_CNT_BRACKETS = 2
const val FOLDER_MEDIA_CNT_NONE = 3

const val FOLDER_STYLE_SQUARE = 1
const val FOLDER_STYLE_ROUNDED_CORNERS = 2

fun getPermissionToRequest() =
    if (isTiramisuPlus()) PERMISSION_READ_MEDIA_IMAGES else PERMISSION_WRITE_STORAGE

// possible icons at the top left corner
enum class NavigationIcon(@StringRes val accessibilityResId: Int) {
    Cross(R.string.close),
    Arrow(R.string.back),
    None(0)
}

const val EXTERNAL_STORAGE_PROVIDER_AUTHORITY = "com.android.externalstorage.documents"
const val EXTRA_SHOW_ADVANCED = "android.content.extra.SHOW_ADVANCED"

const val APP_NAME = "app_name"
const val APP_LICENSES = "app_licenses"
const val APP_FAQ = "app_faq"
const val APP_VERSION_NAME = "app_version_name"
const val APP_ICON_IDS = "app_icon_ids"
const val APP_ID = "app_id"
const val APP_LAUNCHER_NAME = "app_launcher_name"
const val REAL_FILE_PATH = "real_file_path_2"
const val IS_FROM_GALLERY = "is_from_gallery"

const val REFRESH_PATH = "refresh_path"

const val BLOCKED_NUMBERS_EXPORT_DELIMITER = ","
const val BLOCKED_NUMBERS_EXPORT_EXTENSION = ".txt"
const val NOMEDIA = ".nomedia"

const val SHOW_FAQ_BEFORE_MAIL = "show_faq_before_mail"

const val SAVE_DISCARD_PROMPT_INTERVAL = 1000L
const val SD_OTG_PATTERN = "^/storage/[A-Za-z0-9]{4}-[A-Za-z0-9]{4}$"
const val SD_OTG_SHORT = "^[A-Za-z0-9]{4}-[A-Za-z0-9]{4}$"

const val SMT_PRIVATE =
    "smt_private"   // used at the contact source of local contacts hidden from other apps
const val FIRST_GROUP_ID = 10000L
const val MD5 = "MD5"

const val DARK_GREY = 0xFF333333.toInt()

const val LOWER_ALPHA = 0.25f
const val MEDIUM_ALPHA = 0.5f
const val HIGHER_ALPHA = 0.75f


// shared preferences
const val PREFS_KEY = "Prefs"
const val APP_RUN_COUNT = "app_run_count"
const val LAST_VERSION = "last_version"
const val SD_TREE_URI = "tree_uri_2"
const val PRIMARY_ANDROID_DATA_TREE_URI = "primary_android_data_tree_uri_2"
const val OTG_ANDROID_DATA_TREE_URI = "otg_android_data_tree__uri_2"
const val SD_ANDROID_DATA_TREE_URI = "sd_android_data_tree_uri_2"
const val PRIMARY_ANDROID_OBB_TREE_URI = "primary_android_obb_tree_uri_2"
const val OTG_ANDROID_OBB_TREE_URI = "otg_android_obb_tree_uri_2"
const val SD_ANDROID_OBB_TREE_URI = "sd_android_obb_tree_uri_2"
const val OTG_TREE_URI = "otg_tree_uri_2"
const val SD_CARD_PATH = "sd_card_path_2"
const val OTG_REAL_PATH = "otg_real_path_2"
const val INTERNAL_STORAGE_PATH = "internal_storage_path"
const val TEXT_COLOR = "text_color"
const val BACKGROUND_COLOR = "background_color"
const val PRIMARY_COLOR = "primary_color_2"
const val ACCENT_COLOR = "accent_color"
const val APP_ICON_COLOR = "app_icon_color"

const val LAST_ICON_COLOR = "last_icon_color"
const val CUSTOM_TEXT_COLOR = "custom_text_color"
const val CUSTOM_BACKGROUND_COLOR = "custom_background_color"
const val CUSTOM_PRIMARY_COLOR = "custom_primary_color"
const val CUSTOM_ACCENT_COLOR = "custom_accent_color"
const val CUSTOM_APP_ICON_COLOR = "custom_app_icon_color"
const val WIDGET_BG_COLOR = "widget_bg_color"
const val WIDGET_TEXT_COLOR = "widget_text_color"
const val PASSWORD_PROTECTION = "password_protection"
const val PASSWORD_HASH = "password_hash"
const val PROTECTION_TYPE = "protection_type"
const val APP_PASSWORD_PROTECTION = "app_password_protection"
const val APP_PASSWORD_HASH = "app_password_hash"
const val APP_PROTECTION_TYPE = "app_protection_type"
const val DELETE_PASSWORD_PROTECTION = "delete_password_protection"
const val DELETE_PASSWORD_HASH = "delete_password_hash"
const val DELETE_PROTECTION_TYPE = "delete_protection_type"

const val PROTECTED_FOLDER_HASH = "protected_folder_hash_"
const val PROTECTED_FOLDER_TYPE = "protected_folder_type_"
const val KEEP_LAST_MODIFIED = "keep_last_modified"
const val USE_ENGLISH = "use_english"
const val WAS_USE_ENGLISH_TOGGLED = "was_use_english_toggled"
const val WAS_SHARED_THEME_EVER_ACTIVATED = "was_shared_theme_ever_activated"
const val IS_USING_SHARED_THEME = "is_using_shared_theme"
const val IS_USING_AUTO_THEME = "is_using_auto_theme"
const val IS_USING_SYSTEM_THEME = "is_using_system_theme"
const val SHOULD_USE_SHARED_THEME = "should_use_shared_theme"
const val WAS_SHARED_THEME_FORCED = "was_shared_theme_forced"
const val WAS_CUSTOM_THEME_SWITCH_DESCRIPTION_SHOWN = "was_custom_theme_switch_description_shown"

const val LAST_CONFLICT_RESOLUTION = "last_conflict_resolution"
const val LAST_CONFLICT_APPLY_TO_ALL = "last_conflict_apply_to_all"
const val LAST_COPY_PATH = "last_copy_path"
const val HAD_THANK_YOU_INSTALLED = "had_thank_you_installed"
const val SKIP_DELETE_CONFIRMATION = "skip_delete_confirmation"
const val ENABLE_PULL_TO_REFRESH = "enable_pull_to_refresh"
const val SCROLL_HORIZONTALLY = "scroll_horizontally"

const val USE_24_HOUR_FORMAT = "use_24_hour_format"


const val OTG_PARTITION = "otg_partition_2"
const val IS_USING_MODIFIED_APP_ICON = "is_using_modified_app_icon"

const val WAS_ORANGE_ICON_CHECKED = "was_orange_icon_checked"
const val WAS_APP_ON_SD_SHOWN = "was_app_on_sd_shown"
const val WAS_BEFORE_ASKING_SHOWN = "was_before_asking_shown"
const val WAS_BEFORE_RATE_SHOWN = "was_before_rate_shown"

const val WAS_APP_ICON_CUSTOMIZATION_WARNING_SHOWN = "was_app_icon_customization_warning_shown"
const val APP_SIDELOADING_STATUS = "app_sideloading_status"
const val DATE_FORMAT = "date_format"
const val WAS_OTG_HANDLED = "was_otg_handled_2"

const val WAS_APP_RATED = "was_app_rated"
const val WAS_SORTING_BY_NUMERIC_VALUE_ADDED = "was_sorting_by_numeric_value_added"
const val WAS_FOLDER_LOCKING_NOTICE_SHOWN = "was_folder_locking_notice_shown"
const val LAST_RENAME_USED = "last_rename_used"
const val LAST_RENAME_PATTERN_USED = "last_rename_pattern_used"
const val LAST_EXPORTED_SETTINGS_FOLDER = "last_exported_settings_folder"

const val LAST_BLOCKED_NUMBERS_EXPORT_PATH = "last_blocked_numbers_export_path"
const val BLOCK_UNKNOWN_NUMBERS = "block_unknown_numbers"
const val BLOCK_HIDDEN_NUMBERS = "block_hidden_numbers"
const val FONT_SIZE = "font_size"

const val START_NAME_WITH_SURNAME = "start_name_with_surname"
const val FAVORITES = "favorites"

const val COLOR_PICKER_RECENT_COLORS = "color_picker_recent_colors"

const val SHOW_ONLY_CONTACTS_WITH_NUMBERS = "show_only_contacts_with_numbers"
const val IGNORED_CONTACT_SOURCES = "ignored_contact_sources_2"
const val LAST_USED_CONTACT_SOURCE = "last_used_contact_source"


const val WAS_LOCAL_ACCOUNT_INITIALIZED = "was_local_account_initialized"


const val MERGE_DUPLICATE_CONTACTS = "merge_duplicate_contacts"


const val VIEW_TYPE = "view_type"


const val PASSWORD_RETRY_COUNT = "password_retry_count"
const val PASSWORD_COUNTDOWN_START_MS = "password_count_down_start_ms"

const val MAX_PASSWORD_RETRY_COUNT = 3
const val DEFAULT_PASSWORD_COUNTDOWN = 5
const val MINIMUM_PIN_LENGTH = 4

// licenses
internal const val LICENSE_KOTLIN = 1L
const val LICENSE_SUBSAMPLING = 2L
const val LICENSE_GLIDE = 4L
const val LICENSE_CROPPER = 8L
const val LICENSE_FILTERS = 16L
const val LICENSE_RTL = 32L
const val LICENSE_JODA = 64L
const val LICENSE_STETHO = 128L
const val LICENSE_OTTO = 256L
const val LICENSE_PHOTOVIEW = 512L
const val LICENSE_PICASSO = 1024L
const val LICENSE_PATTERN = 2048L
const val LICENSE_REPRINT = 4096L
const val LICENSE_GIF_DRAWABLE = 8192L
const val LICENSE_AUTOFITTEXTVIEW = 16384L
const val LICENSE_ROBOLECTRIC = 32768L
const val LICENSE_ESPRESSO = 65536L
const val LICENSE_GSON = 131072L
const val LICENSE_LEAK_CANARY = 262144L
const val LICENSE_NUMBER_PICKER = 524288L
const val LICENSE_EXOPLAYER = 1048576L
const val LICENSE_PANORAMA_VIEW = 2097152L
const val LICENSE_SANSELAN = 4194304L
const val LICENSE_GESTURE_VIEWS = 8388608L
const val LICENSE_INDICATOR_FAST_SCROLL = 16777216L
const val LICENSE_EVENT_BUS = 33554432L
const val LICENSE_AUDIO_RECORD_VIEW = 67108864L
const val LICENSE_SMS_MMS = 134217728L
const val LICENSE_APNG = 268435456L
const val LICENSE_PDF_VIEW_PAGER = 536870912L
const val LICENSE_M3U_PARSER = 1073741824L
const val LICENSE_ANDROID_LAME = 2147483648L
const val LICENSE_PDF_VIEWER = 4294967296L
const val LICENSE_ZIP4J = 8589934592L

// global intents
const val OPEN_DOCUMENT_TREE_FOR_ANDROID_DATA_OR_OBB = 1000
const val OPEN_DOCUMENT_TREE_OTG = 1001
const val OPEN_DOCUMENT_TREE_SD = 1002
const val OPEN_DOCUMENT_TREE_FOR_SDK_30 = 1003
const val REQUEST_SET_AS = 1004
const val REQUEST_EDIT_IMAGE = 1005
const val SELECT_EXPORT_SETTINGS_FILE_INTENT = 1006
const val REQUEST_CODE_SET_DEFAULT_DIALER = 1007
const val CREATE_DOCUMENT_SDK_30 = 1008
const val REQUEST_CODE_SET_DEFAULT_CALLER_ID = 1010

// sorting
const val SORT_ORDER = "sort_order"
const val SORT_FOLDER_PREFIX =
    "sort_folder_"       // storing folder specific values at using "Use for this folder only"
const val SORT_BY_NAME = 1
const val SORT_BY_DATE_MODIFIED = 2
const val SORT_BY_SIZE = 4
const val SORT_BY_DATE_TAKEN = 8
const val SORT_BY_EXTENSION = 16
const val SORT_BY_PATH = 32

const val SORT_BY_FIRST_NAME = 128
const val SORT_BY_MIDDLE_NAME = 256
const val SORT_BY_SURNAME = 512
const val SORT_DESCENDING = 1024

const val SORT_BY_RANDOM = 16384
const val SORT_USE_NUMERIC_VALUE = 32768
const val SORT_BY_FULL_NAME = 65536
const val SORT_BY_CUSTOM = 131072


// security
const val WAS_PROTECTION_HANDLED = "was_protection_handled"
const val PROTECTION_NONE = -1
const val PROTECTION_PATTERN = 0
const val PROTECTION_PIN = 1
const val PROTECTION_FINGERPRINT = 2

// renaming
const val RENAME_SIMPLE = 0
const val RENAME_PATTERN = 1

const val SHOW_ALL_TABS = -1


// permissions
const val PERMISSION_READ_STORAGE = 1
const val PERMISSION_WRITE_STORAGE = 2
const val PERMISSION_CAMERA = 3
const val PERMISSION_RECORD_AUDIO = 4
const val PERMISSION_READ_CONTACTS = 5
const val PERMISSION_WRITE_CONTACTS = 6
const val PERMISSION_READ_CALENDAR = 7
const val PERMISSION_WRITE_CALENDAR = 8
const val PERMISSION_CALL_PHONE = 9
const val PERMISSION_READ_CALL_LOG = 10
const val PERMISSION_WRITE_CALL_LOG = 11
const val PERMISSION_GET_ACCOUNTS = 12
const val PERMISSION_READ_SMS = 13
const val PERMISSION_SEND_SMS = 14
const val PERMISSION_READ_PHONE_STATE = 15
const val PERMISSION_MEDIA_LOCATION = 16
const val PERMISSION_POST_NOTIFICATIONS = 17
const val PERMISSION_READ_MEDIA_IMAGES = 18
const val PERMISSION_READ_MEDIA_VIDEO = 19
const val PERMISSION_READ_MEDIA_AUDIO = 20
const val PERMISSION_ACCESS_COARSE_LOCATION = 21
const val PERMISSION_ACCESS_FINE_LOCATION = 22
const val PERMISSION_READ_MEDIA_VISUAL_USER_SELECTED = 23
const val PERMISSION_READ_SYNC_SETTINGS = 24

// conflict resolving
const val CONFLICT_SKIP = 1
const val CONFLICT_OVERWRITE = 2
const val CONFLICT_MERGE = 3
const val CONFLICT_KEEP_BOTH = 4

// font sizes
const val FONT_SIZE_SMALL = 0
const val FONT_SIZE_MEDIUM = 1
const val FONT_SIZE_LARGE = 2

const val MONDAY_BIT = 1
const val TUESDAY_BIT = 2
const val WEDNESDAY_BIT = 4
const val THURSDAY_BIT = 8
const val FRIDAY_BIT = 16
const val SATURDAY_BIT = 32
const val SUNDAY_BIT = 64


const val SIDELOADING_UNCHECKED = 0
const val SIDELOADING_TRUE = 1
const val SIDELOADING_FALSE = 2


val photoExtensions: Array<String>
    get() = arrayOf(
        ".jpg",
        ".png",
        ".jpeg",
        ".bmp",
        ".webp",
        ".heic",
        ".heif",
        ".apng",
        ".avif"
    )
val videoExtensions: Array<String>
    get() = arrayOf(
        ".mp4",
        ".mkv",
        ".webm",
        ".avi",
        ".3gp",
        ".mov",
        ".m4v",
        ".3gpp"
    )
val audioExtensions: Array<String>
    get() = arrayOf(
        ".mp3",
        ".wav",
        ".wma",
        ".ogg",
        ".m4a",
        ".opus",
        ".flac",
        ".aac",
        ".m4b"
    )
val rawExtensions: Array<String>
    get() = arrayOf(
        ".dng",
        ".orf",
        ".nef",
        ".arw",
        ".rw2",
        ".cr2",
        ".cr3"
    )

val extensionsSupportingEXIF: Array<String>
    get() = arrayOf(
        ".jpg",
        ".jpeg",
        ".png",
        ".webp",
        ".dng"
    )

const val DATE_FORMAT_ONE = "dd.MM.yyyy"
const val DATE_FORMAT_TWO = "dd/MM/yyyy"
const val DATE_FORMAT_THREE = "MM/dd/yyyy"
const val DATE_FORMAT_FOUR = "yyyy-MM-dd"
const val DATE_FORMAT_FIVE = "d MMMM yyyy"
const val DATE_FORMAT_SIX = "MMMM d yyyy"
const val DATE_FORMAT_SEVEN = "MM-dd-yyyy"
const val DATE_FORMAT_EIGHT = "dd-MM-yyyy"
const val DATE_FORMAT_NINE = "yyyyMMdd"
const val DATE_FORMAT_TEN = "yyyy.MM.dd"
const val DATE_FORMAT_ELEVEN = "yy-MM-dd"
const val DATE_FORMAT_TWELVE = "yyMMdd"
const val DATE_FORMAT_THIRTEEN = "yy.MM.dd"
const val DATE_FORMAT_FOURTEEN = "yy/MM/dd"

const val TIME_FORMAT_12 = "hh:mm a"
const val TIME_FORMAT_24 = "HH:mm"

val appIconColorStrings = arrayListOf(
    ".Red",
    ".Pink",
    ".Purple",
    ".Deep_purple",
    ".Indigo",
    ".Blue",
    ".Light_blue",
    ".Cyan",
    ".Teal",
    ".Green",
    ".Light_green",
    ".Lime",
    ".Yellow",
    ".Amber",
    ".Orange",
    ".Deep_orange",
    ".Brown",
    ".Blue_grey",
    ".Grey_black"
)

// most app icon colors from md_app_icon_colors with reduced alpha
// used at showing contact placeholders without image
val letterBackgroundColors = arrayListOf(
    0xCCD32F2F,
    0xCCC2185B,
    0xCC1976D2,
    0xCC0288D1,
    0xCC0097A7,
    0xCC00796B,
    0xCC388E3C,
    0xCC689F38,
    0xCCF57C00,
    0xCCE64A19
)

// view types
const val VIEW_TYPE_GRID = 1
const val VIEW_TYPE_LIST = 2

fun isOnMainThread() = Looper.myLooper() == Looper.getMainLooper()

fun ensureBackgroundThread(callback: () -> Unit) {
    if (isOnMainThread()) {
        Thread {
            callback()
        }.start()
    } else {
        callback()
    }
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.N)
fun isNougatPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
fun isOreoPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O



@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.P)
fun isPiePlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
fun isQPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.R)
fun isRPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
fun isSPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
fun isTiramisuPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
fun isUpsideDownCakePlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE





val normalizeRegex = "\\p{InCombiningDiacriticalMarks}+".toRegex()

fun getConflictResolution(resolutions: LinkedHashMap<String, Int>, path: String): Int {
    return if (resolutions.size == 1 && resolutions.containsKey("")) {
        resolutions[""]!!
    } else if (resolutions.containsKey(path)) {
        resolutions[path]!!
    } else {
        CONFLICT_SKIP
    }
}

val proPackages = arrayListOf("draw", "gallery", "filemanager", "contacts", "notes", "calendar")


fun getQuestionMarks(size: Int) = ("?," * size).trimEnd(',')

const val FIRST_CONTACT_ID = 1000000


const val DEFAULT_ORGANIZATION_TYPE = ContactsContract.CommonDataKinds.Organization.TYPE_WORK
const val DEFAULT_WEBSITE_TYPE = ContactsContract.CommonDataKinds.Website.TYPE_HOMEPAGE
const val DEFAULT_MIMETYPE = ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE

// contact photo changes
const val PHOTO_ADDED = 1
const val PHOTO_REMOVED = 2
const val PHOTO_CHANGED = 3


// apps with special handling
const val TELEGRAM_PACKAGE = "org.telegram.messenger"
const val SIGNAL_PACKAGE = "org.thoughtcrime.securesms"
const val WHATSAPP_PACKAGE = "com.whatsapp"
const val VIBER_PACKAGE = "com.viber.voip"
const val THREEMA_PACKAGE = "ch.threema.app"


fun getEmptyLocalContact() = LocalContact(
    0,
    "",
    "",
    "",
    "",
    "",
    "",
    null,
    "",
    ArrayList(),
    ArrayList(),
    ArrayList(),
    0,
    ArrayList(),
    "",
    ArrayList(),
    "",
    "",
    ArrayList(),
    ArrayList(),
    null
)



fun getFilePlaceholderDrawables(context: Context): HashMap<String, Drawable> {
    val fileDrawables = HashMap<String, Drawable>()
    hashMapOf<String, Int>().apply {
        put("aep", R.drawable.ic_file_aep)
        put("ai", R.drawable.ic_file_ai)
        put("avi", R.drawable.ic_file_avi)
        put("css", R.drawable.ic_file_css)
        put("csv", R.drawable.ic_file_csv)
        put("dbf", R.drawable.ic_file_dbf)
        put("doc", R.drawable.ic_file_doc)
        put("docx", R.drawable.ic_file_doc)
        put("dwg", R.drawable.ic_file_dwg)
        put("exe", R.drawable.ic_file_exe)
        put("fla", R.drawable.ic_file_fla)
        put("flv", R.drawable.ic_file_flv)
        put("htm", R.drawable.ic_file_html)
        put("html", R.drawable.ic_file_html)
        put("ics", R.drawable.ic_file_ics)
        put("indd", R.drawable.ic_file_indd)
        put("iso", R.drawable.ic_file_iso)
        put("jpg", R.drawable.ic_file_jpg)
        put("jpeg", R.drawable.ic_file_jpg)
        put("js", R.drawable.ic_file_js)
        put("json", R.drawable.ic_file_json)
        put("m4a", R.drawable.ic_file_m4a)
        put("mp3", R.drawable.ic_file_mp3)
        put("mp4", R.drawable.ic_file_mp4)
        put("ogg", R.drawable.ic_file_ogg)
        put("pdf", R.drawable.ic_file_pdf)
        put("plproj", R.drawable.ic_file_plproj)
        put("ppt", R.drawable.ic_file_ppt)
        put("pptx", R.drawable.ic_file_ppt)
        put("prproj", R.drawable.ic_file_prproj)
        put("psd", R.drawable.ic_file_psd)
        put("rtf", R.drawable.ic_file_rtf)
        put("sesx", R.drawable.ic_file_sesx)
        put("sql", R.drawable.ic_file_sql)
        put("svg", R.drawable.ic_file_svg)
        put("txt", R.drawable.ic_file_txt)
        put("vcf", R.drawable.ic_file_vcf)
        put("wav", R.drawable.ic_file_wav)
        put("wmv", R.drawable.ic_file_wmv)
        put("xls", R.drawable.ic_file_xls)
        put("xlsx", R.drawable.ic_file_xls)
        put("xml", R.drawable.ic_file_xml)
        put("zip", R.drawable.ic_file_zip)
    }.forEach { (key, value) ->
        fileDrawables[key] = context.resources.getDrawable(value)
    }
    return fileDrawables
}
