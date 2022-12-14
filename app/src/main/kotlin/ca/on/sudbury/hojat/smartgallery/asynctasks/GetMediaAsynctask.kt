package ca.on.sudbury.hojat.smartgallery.asynctasks

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_BY_DATE_TAKEN
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_BY_DATE_MODIFIED
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_BY_SIZE
import ca.on.sudbury.hojat.smartgallery.helpers.FAVORITES
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.extensions.getFavoritePaths
import ca.on.sudbury.hojat.smartgallery.helpers.MediaFetcher
import ca.on.sudbury.hojat.smartgallery.helpers.SHOW_ALL
import ca.on.sudbury.hojat.smartgallery.helpers.GroupBy
import ca.on.sudbury.hojat.smartgallery.helpers.RECYCLE_BIN
import ca.on.sudbury.hojat.smartgallery.models.Medium
import ca.on.sudbury.hojat.smartgallery.models.ThumbnailItem

@SuppressLint("StaticFieldLeak")
class GetMediaAsynctask(
    val context: Context,
    private val mPath: String,
    val isPickImage: Boolean = false,
    val isPickVideo: Boolean = false,
    val showAll: Boolean, val callback: (media: ArrayList<ThumbnailItem>) -> Unit
) :
    AsyncTask<Void, Void, ArrayList<ThumbnailItem>>() {
    private val mediaFetcher = MediaFetcher(context)

    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg params: Void): ArrayList<ThumbnailItem> {
        val pathToUse = if (showAll) SHOW_ALL else mPath
        val folderGrouping = context.config.getFolderGrouping(pathToUse)
        val fileSorting = context.config.getFolderSorting(pathToUse)
        val getProperDateTaken = fileSorting and SORT_BY_DATE_TAKEN != 0 ||
                folderGrouping and GroupBy.DateTakenDaily.id != 0 ||
                folderGrouping and GroupBy.DateTakenMonthly.id != 0

        val getProperLastModified = fileSorting and SORT_BY_DATE_MODIFIED != 0 ||
                folderGrouping and GroupBy.LastModifiedDaily.id != 0 ||
                folderGrouping and GroupBy.LastModifiedMonthly.id != 0

        val getProperFileSize = fileSorting and SORT_BY_SIZE != 0
        val favoritePaths = context.getFavoritePaths()
        val getVideoDurations = context.config.showThumbnailVideoDuration
        val lastModifieds =
            if (getProperLastModified) mediaFetcher.getLastModifieds() else HashMap()
        val dateTakens = if (getProperDateTaken) mediaFetcher.getDateTakens() else HashMap()

        val media = if (showAll) {
            val foldersToScan = mediaFetcher.getFoldersToScan().filter {
                it != RECYCLE_BIN && it != FAVORITES && !context.config.isFolderProtected(it)
            }
            val media = ArrayList<Medium>()
            foldersToScan.forEach {
                val newMedia = mediaFetcher.getFilesFrom(
                    it,
                    isPickImage,
                    isPickVideo,
                    getProperDateTaken,
                    getProperLastModified,
                    getProperFileSize,
                    favoritePaths,
                    getVideoDurations,
                    lastModifieds,
                    dateTakens.clone() as HashMap<String, Long>,
                    null
                )
                media.addAll(newMedia)
            }

            mediaFetcher.sortMedia(media, context.config.getFolderSorting(SHOW_ALL))
            media
        } else {
            mediaFetcher.getFilesFrom(
                mPath,
                isPickImage,
                isPickVideo,
                getProperDateTaken,
                getProperLastModified,
                getProperFileSize,
                favoritePaths,
                getVideoDurations,
                lastModifieds,
                dateTakens,
                null
            )
        }

        return mediaFetcher.groupMedia(media, pathToUse)
    }

    @Deprecated("Deprecated in Java")
    override fun onPostExecute(media: ArrayList<ThumbnailItem>) {
        super.onPostExecute(media)
        callback(media)
    }

    fun stopFetching() {
        mediaFetcher.shouldStop = true
        cancel(true)
    }
}
