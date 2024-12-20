package ca.hojat.smart.gallery.shared.data.domain

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.bumptech.glide.signature.ObjectKey
import ca.hojat.smart.gallery.shared.extensions.formatDate
import ca.hojat.smart.gallery.shared.extensions.formatSize
import ca.hojat.smart.gallery.shared.helpers.FAVORITES
import ca.hojat.smart.gallery.shared.helpers.SORT_BY_DATE_MODIFIED
import ca.hojat.smart.gallery.shared.helpers.SORT_BY_NAME
import ca.hojat.smart.gallery.shared.helpers.SORT_BY_PATH
import ca.hojat.smart.gallery.shared.helpers.SORT_BY_RANDOM
import ca.hojat.smart.gallery.shared.helpers.SORT_BY_SIZE
import ca.hojat.smart.gallery.shared.helpers.RECYCLE_BIN

@Entity(tableName = "directories", indices = [Index(value = ["path"], unique = true)])
data class Directory(
    @PrimaryKey(autoGenerate = true) var id: Long?,
    @ColumnInfo(name = "path") var path: String,
    @ColumnInfo(name = "thumbnail") var tmb: String,
    @ColumnInfo(name = "filename") var name: String,
    @ColumnInfo(name = "media_count") var mediaCnt: Int,
    @ColumnInfo(name = "last_modified") var modified: Long,
    @ColumnInfo(name = "date_taken") var taken: Long,
    @ColumnInfo(name = "size") var size: Long,
    @ColumnInfo(name = "location") var location: Int,
    @ColumnInfo(name = "media_types") var types: Int,
    @ColumnInfo(name = "sort_value") var sortValue: String,

    // used with "Group direct subfolders" enabled
    @Ignore var subfoldersCount: Int = 0,
    @Ignore var subfoldersMediaCount: Int = 0,
    @Ignore var containsMediaFilesDirectly: Boolean = true
) {

    constructor() : this(null, "", "", "", 0, 0L, 0L, 0L, 0, 0, "", 0, 0)

    fun getBubbleText(
        sorting: Int,
        context: Context,
        dateFormat: String? = null,
        timeFormat: String? = null
    ) = when {
        sorting and SORT_BY_NAME != 0 -> name
        sorting and SORT_BY_PATH != 0 -> path
        sorting and SORT_BY_SIZE != 0 -> size.formatSize()
        sorting and SORT_BY_DATE_MODIFIED != 0 -> modified.formatDate(
            context,
            dateFormat,
            timeFormat
        )

        sorting and SORT_BY_RANDOM != 0 -> name
        else -> taken.formatDate(context)
    }

    fun areFavorites() = path == FAVORITES

    fun isRecycleBin() = path == RECYCLE_BIN

    fun getKey() = ObjectKey("$path-$modified")
}
