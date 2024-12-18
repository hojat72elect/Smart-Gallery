package ca.hojat.smart.gallery.shared.data.domain

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import ca.hojat.smart.gallery.shared.extensions.formatDate
import ca.hojat.smart.gallery.shared.extensions.formatSize
import ca.hojat.smart.gallery.shared.extensions.getAlbum
import ca.hojat.smart.gallery.shared.extensions.getAndroidSAFDirectChildrenCount
import ca.hojat.smart.gallery.shared.extensions.getAndroidSAFFileCount
import ca.hojat.smart.gallery.shared.extensions.getAndroidSAFFileSize
import ca.hojat.smart.gallery.shared.extensions.getAndroidSAFLastModified
import ca.hojat.smart.gallery.shared.extensions.getArtist
import ca.hojat.smart.gallery.shared.extensions.getDirectChildrenCount
import ca.hojat.smart.gallery.shared.extensions.getDocumentFile
import ca.hojat.smart.gallery.shared.extensions.getDuration
import ca.hojat.smart.gallery.shared.extensions.getFastDocumentFile
import ca.hojat.smart.gallery.shared.extensions.getFileCount
import ca.hojat.smart.gallery.shared.extensions.getFormattedDuration
import ca.hojat.smart.gallery.shared.extensions.getItemSize
import ca.hojat.smart.gallery.shared.extensions.getMediaStoreLastModified
import ca.hojat.smart.gallery.shared.extensions.getParentPath
import ca.hojat.smart.gallery.shared.extensions.getProperSize
import ca.hojat.smart.gallery.shared.extensions.getResolution
import ca.hojat.smart.gallery.shared.extensions.getSizeFromContentUri
import ca.hojat.smart.gallery.shared.extensions.getTitle
import ca.hojat.smart.gallery.shared.extensions.isImageFast
import ca.hojat.smart.gallery.shared.extensions.isPathOnOTG
import ca.hojat.smart.gallery.shared.extensions.isRestrictedSAFOnlyRoot
import ca.hojat.smart.gallery.shared.extensions.isVideoFast
import ca.hojat.smart.gallery.shared.extensions.normalizeString
import ca.hojat.smart.gallery.shared.helpers.AlphanumericComparator
import ca.hojat.smart.gallery.shared.helpers.SORT_BY_DATE_MODIFIED
import ca.hojat.smart.gallery.shared.helpers.SORT_BY_EXTENSION
import ca.hojat.smart.gallery.shared.helpers.SORT_BY_NAME
import ca.hojat.smart.gallery.shared.helpers.SORT_BY_SIZE
import ca.hojat.smart.gallery.shared.helpers.SORT_DESCENDING
import ca.hojat.smart.gallery.shared.helpers.SORT_USE_NUMERIC_VALUE
import com.bumptech.glide.signature.ObjectKey
import java.io.File

open class FileDirItem(
    val path: String,
    val name: String = "",
    var isDirectory: Boolean = false,
    var children: Int = 0,
    var size: Long = 0L,
    var modified: Long = 0L,
    var mediaStoreId: Long = 0L
) :
    Comparable<FileDirItem> {
    companion object {
        var sorting = 0
    }

    override fun toString() =
        "FileDirItem(path=$path, name=$name, isDirectory=$isDirectory, children=$children, size=$size, modified=$modified, mediaStoreId=$mediaStoreId)"

    override fun compareTo(other: FileDirItem): Int {
        return if (isDirectory && !other.isDirectory) {
            -1
        } else if (!isDirectory && other.isDirectory) {
            1
        } else {
            var result: Int
            when {
                sorting and SORT_BY_NAME != 0 -> {
                    result = if (sorting and SORT_USE_NUMERIC_VALUE != 0) {
                        AlphanumericComparator().compare(
                            name.normalizeString().lowercase(),
                            other.name.normalizeString().lowercase()
                        )
                    } else {
                        name.normalizeString().lowercase()
                            .compareTo(other.name.normalizeString().lowercase())
                    }
                }

                sorting and SORT_BY_SIZE != 0 -> result = when {
                    size == other.size -> 0
                    size > other.size -> 1
                    else -> -1
                }

                sorting and SORT_BY_DATE_MODIFIED != 0 -> {
                    result = when {
                        modified == other.modified -> 0
                        modified > other.modified -> 1
                        else -> -1
                    }
                }

                else -> {
                    result = getExtension().lowercase().compareTo(other.getExtension().lowercase())
                }
            }

            if (sorting and SORT_DESCENDING != 0) {
                result *= -1
            }
            result
        }
    }

    private fun getExtension() = if (isDirectory) name else path.substringAfterLast('.', "")

    fun getBubbleText(context: Context, dateFormat: String? = null, timeFormat: String? = null) =
        when {
            sorting and SORT_BY_SIZE != 0 -> size.formatSize()
            sorting and SORT_BY_DATE_MODIFIED != 0 -> modified.formatDate(
                context,
                dateFormat,
                timeFormat
            )

            sorting and SORT_BY_EXTENSION != 0 -> getExtension().lowercase()
            else -> name
        }

    @SuppressLint("Recycle")
    fun getProperSize(context: Context, countHidden: Boolean): Long {
        return when {
            context.isRestrictedSAFOnlyRoot(path) -> context.getAndroidSAFFileSize(path)
            context.isPathOnOTG(path) -> context.getDocumentFile(path)?.getItemSize(countHidden)
                ?: 0

            path.startsWith("content://") -> {
                try {
                    context.contentResolver.openInputStream(Uri.parse(path))?.available()?.toLong()
                        ?: 0L
                } catch (e: Exception) {
                    context.getSizeFromContentUri(Uri.parse(path))
                }
            }

            else -> File(path).getProperSize(countHidden)
        }
    }

    fun getProperFileCount(context: Context, countHidden: Boolean): Int {
        return when {
            context.isRestrictedSAFOnlyRoot(path) -> context.getAndroidSAFFileCount(
                path,
                countHidden
            )

            context.isPathOnOTG(path) -> context.getDocumentFile(path)?.getFileCount(countHidden)
                ?: 0

            else -> File(path).getFileCount(countHidden)
        }
    }

    fun getDirectChildrenCount(context: Context, countHiddenItems: Boolean): Int {
        return when {
            context.isRestrictedSAFOnlyRoot(path) -> context.getAndroidSAFDirectChildrenCount(
                path,
                countHiddenItems
            )

            context.isPathOnOTG(path) -> context.getDocumentFile(path)?.listFiles()
                ?.filter { if (countHiddenItems) true else !it.name!!.startsWith(".") }?.size
                ?: 0

            else -> File(path).getDirectChildrenCount(context, countHiddenItems)
        }
    }

    fun getLastModified(context: Context): Long {
        return when {
            context.isRestrictedSAFOnlyRoot(path) -> context.getAndroidSAFLastModified(path)
            context.isPathOnOTG(path) -> context.getFastDocumentFile(path)?.lastModified() ?: 0L
            path.startsWith("content://") -> context.getMediaStoreLastModified(path)

            else -> File(path).lastModified()
        }
    }

    fun getParentPath() = path.getParentPath()

    fun getDuration(context: Context) = context.getDuration(path)?.getFormattedDuration()

    fun getArtist(context: Context) = context.getArtist(path)

    fun getAlbum(context: Context) = context.getAlbum(path)

    fun getTitle(context: Context) = context.getTitle(path)

    fun getResolution(context: Context) = context.getResolution(path)

    private fun getSignature(): String {
        val lastModified = if (modified > 1) {
            modified
        } else {
            File(path).lastModified()
        }

        return "$path-$lastModified-$size"
    }

    fun getKey() = ObjectKey(getSignature())

    fun assembleContentUri(): Uri {
        val uri = when {
            path.isImageFast() -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            path.isVideoFast() -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            else -> MediaStore.Files.getContentUri("external")
        }

        return Uri.withAppendedPath(uri, mediaStoreId.toString())
    }
}
