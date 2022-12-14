package ca.on.sudbury.hojat.smartgallery.helpers

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.BaseColumns
import android.provider.MediaStore
import android.provider.MediaStore.Files
import android.provider.MediaStore.Images
import android.text.format.DateFormat
import androidx.annotation.RequiresApi
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databases.GalleryDatabase
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.extensions.getUpdatedDeletedMedia
import ca.on.sudbury.hojat.smartgallery.extensions.getNoMediaFoldersSync
import ca.on.sudbury.hojat.smartgallery.extensions.shouldFolderBeVisible
import ca.on.sudbury.hojat.smartgallery.extensions.isVideoFast
import ca.on.sudbury.hojat.smartgallery.extensions.getDistinctPath
import ca.on.sudbury.hojat.smartgallery.extensions.humanizePath
import ca.on.sudbury.hojat.smartgallery.extensions.getParentPath
import ca.on.sudbury.hojat.smartgallery.extensions.isExternalStorageManager
import ca.on.sudbury.hojat.smartgallery.extensions.isImageFast
import ca.on.sudbury.hojat.smartgallery.extensions.isRawFast
import ca.on.sudbury.hojat.smartgallery.extensions.getDuration
import ca.on.sudbury.hojat.smartgallery.extensions.getDocumentFile
import ca.on.sudbury.hojat.smartgallery.extensions.getStringValue
import ca.on.sudbury.hojat.smartgallery.extensions.getDoesFilePathExist
import ca.on.sudbury.hojat.smartgallery.extensions.queryCursor
import ca.on.sudbury.hojat.smartgallery.extensions.normalizeString
import ca.on.sudbury.hojat.smartgallery.extensions.getLongValue
import ca.on.sudbury.hojat.smartgallery.extensions.getIntValue
import ca.on.sudbury.hojat.smartgallery.extensions.areDigitsOnly
import ca.on.sudbury.hojat.smartgallery.extensions.hasOTGConnected
import ca.on.sudbury.hojat.smartgallery.models.Medium
import ca.on.sudbury.hojat.smartgallery.models.ThumbnailItem
import ca.on.sudbury.hojat.smartgallery.models.ThumbnailSection
import ca.on.sudbury.hojat.smartgallery.usecases.IsRPlusUseCase
import ca.on.sudbury.hojat.smartgallery.repositories.SupportedExtensionsRepository
import ca.on.sudbury.hojat.smartgallery.usecases.IsGifUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsPathOnOtgUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsSvgUseCase
import timber.log.Timber
import java.io.File
import java.util.Locale
import java.util.Calendar
import kotlin.math.roundToInt

class MediaFetcher(val context: Context) {
    var shouldStop = false

    // on Android 11 we fetch all files at once from MediaStore and have it split by folder, use it if available
    @RequiresApi(Build.VERSION_CODES.Q)
    fun getFilesFrom(
        curPath: String,
        isPickImage: Boolean,
        isPickVideo: Boolean,
        getProperDateTaken: Boolean,
        getProperLastModified: Boolean,
        getProperFileSize: Boolean,
        favoritePaths: ArrayList<String>,
        getVideoDurations: Boolean,
        lastModifieds: HashMap<String, Long>,
        dateTakens: HashMap<String, Long>,
        android11Files: HashMap<String, ArrayList<Medium>>?
    ): ArrayList<Medium> {
        val filterMedia = context.config.filterMedia
        if (filterMedia == 0) {
            return ArrayList()
        }

        val curMedia = ArrayList<Medium>()
        if (
            IsPathOnOtgUseCase(context, curPath)) {
            if (context.hasOTGConnected()) {
                val newMedia = getMediaOnOTG(
                    curPath,
                    isPickImage,
                    isPickVideo,
                    filterMedia,
                    favoritePaths,
                    getVideoDurations
                )
                curMedia.addAll(newMedia)
            }
        } else {
            if (curPath != FAVORITES && curPath != RECYCLE_BIN && IsRPlusUseCase() && !isExternalStorageManager()) {
                if (android11Files?.containsKey(curPath.lowercase(Locale.ROOT)) == true) {
                    curMedia.addAll(android11Files[curPath.lowercase(Locale.ROOT)]!!)
                } else if (android11Files == null) {
                    val files = getAndroid11FolderMedia(
                        isPickImage,
                        isPickVideo,
                        favoritePaths,
                        false,
                        getProperDateTaken,
                        dateTakens
                    )
                    if (files.containsKey(curPath.lowercase(Locale.ROOT))) {
                        curMedia.addAll(files[curPath.lowercase(Locale.ROOT)]!!)
                    }
                }
            }

            if (curMedia.isEmpty()) {
                val newMedia = getMediaInFolder(
                    curPath,
                    isPickImage,
                    isPickVideo,
                    filterMedia,
                    getProperDateTaken,
                    getProperLastModified,
                    getProperFileSize,
                    favoritePaths,
                    getVideoDurations,
                    lastModifieds.clone() as HashMap<String, Long>,
                    dateTakens.clone() as HashMap<String, Long>
                )

                if (curPath == FAVORITES && IsRPlusUseCase() && !isExternalStorageManager()) {
                    val files =
                        getAndroid11FolderMedia(
                            isPickImage,
                            isPickVideo,
                            favoritePaths,
                            true,
                            getProperDateTaken,
                            dateTakens.clone() as HashMap<String, Long>
                        )
                    newMedia.forEach { newMedium ->
                        for ((_, media) in files) {
                            media.forEach { medium ->
                                if (medium.path == newMedium.path) {
                                    newMedium.size = medium.size
                                }
                            }
                        }
                    }
                }
                curMedia.addAll(newMedia)
            }
        }

        sortMedia(curMedia, context.config.getFolderSorting(curPath))

        return curMedia
    }

    @SuppressLint("Recycle")
    fun getFoldersToScan(): ArrayList<String> {
        return try {
            val OTGPath = context.config.otgPath
            val folders = getLatestFileFolders()
            folders.addAll(arrayListOf(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                    .toString(),
                "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)}/Camera",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString()
            ).filter { context.getDoesFilePathExist(it, OTGPath) })

            val filterMedia = context.config.filterMedia
            val uri = Files.getContentUri("external")
            val projection = arrayOf(Images.Media.DATA)
            val selection = getSelectionQuery(filterMedia)
            val selectionArgs = getSelectionArgsQuery(filterMedia).toTypedArray()
            val cursor =
                context.contentResolver.query(uri, projection, selection, selectionArgs, null)
            folders.addAll(parseCursor(cursor!!))

            val config = context.config
            val shouldShowHidden = config.shouldShowHidden
            val excludedPaths = if (config.temporarilyShowExcluded) {
                HashSet()
            } else {
                config.excludedFolders
            }

            val includedPaths = config.includedFolders

            val folderNoMediaStatuses = HashMap<String, Boolean>()
            val distinctPathsMap = HashMap<String, String>()
            val distinctPaths = folders.distinctBy {
                when {
                    distinctPathsMap.containsKey(it) -> distinctPathsMap[it]
                    else -> {
                        val distinct = it.getDistinctPath()
                        distinctPathsMap[it.getParentPath()] = distinct.getParentPath()
                        distinct
                    }
                }
            }

            val noMediaFolders = context.getNoMediaFoldersSync()
            noMediaFolders.forEach { folder ->
                folderNoMediaStatuses["$folder/$NOMEDIA"] = true
            }

            distinctPaths.filter {
                it.shouldFolderBeVisible(
                    excludedPaths,
                    includedPaths,
                    shouldShowHidden,
                    folderNoMediaStatuses
                ) { path, hasNoMedia ->
                    folderNoMediaStatuses[path] = hasNoMedia
                }
            }.toMutableList() as ArrayList<String>
        } catch (e: Exception) {
            ArrayList()
        }
    }

    private fun getLatestFileFolders(): LinkedHashSet<String> {
        val uri = Files.getContentUri("external")
        val projection = arrayOf(Images.ImageColumns.DATA)
        val parents = LinkedHashSet<String>()
        var cursor: Cursor? = null
        try {
            if (IsRPlusUseCase()) {
                val bundle = Bundle().apply {
                    putInt(ContentResolver.QUERY_ARG_LIMIT, 10)
                    putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, arrayOf(BaseColumns._ID))
                    putInt(
                        ContentResolver.QUERY_ARG_SORT_DIRECTION,
                        ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
                    )
                }

                cursor = context.contentResolver.query(uri, projection, bundle, null)
                if (cursor?.moveToFirst() == true) {
                    do {
                        val path = cursor.getStringValue(Images.ImageColumns.DATA) ?: continue
                        parents.add(path.getParentPath())
                    } while (cursor.moveToNext())
                }
            } else {
                val sorting = "${BaseColumns._ID} DESC LIMIT 10"
                cursor = context.contentResolver.query(uri, projection, null, null, sorting)
                if (cursor?.moveToFirst() == true) {
                    do {
                        val path = cursor.getStringValue(Images.ImageColumns.DATA) ?: continue
                        parents.add(path.getParentPath())
                    } while (cursor.moveToNext())
                }
            }
        } catch (e: Exception) {
            Timber.e("MediaFetcher : ${e.message}")
        } finally {
            cursor?.close()
        }

        return parents
    }

    private fun getSelectionQuery(filterMedia: Int): String {
        val query = StringBuilder()
        if (filterMedia and MediaType.Image.id != 0) {
            SupportedExtensionsRepository.photoExtensions.forEach {
                query.append("${Images.Media.DATA} LIKE ? OR ")
            }
        }

        if (filterMedia and MediaType.Portrait.id != 0) {
            query.append("${Images.Media.DATA} LIKE ? OR ")
            query.append("${Images.Media.DATA} LIKE ? OR ")
        }

        if (filterMedia and MediaType.Video.id != 0) {
            SupportedExtensionsRepository.videoExtensions.forEach { _ ->
                query.append("${Images.Media.DATA} LIKE ? OR ")
            }
        }

        if (filterMedia and MediaType.Gif.id != 0) {
            query.append("${Images.Media.DATA} LIKE ? OR ")
        }

        if (filterMedia and MediaType.Raw.id != 0) {
            SupportedExtensionsRepository.rawExtensions.forEach {
                query.append("${Images.Media.DATA} LIKE ? OR ")
            }
        }

        if (filterMedia and MediaType.Svg.id != 0) {
            query.append("${Images.Media.DATA} LIKE ? OR ")
        }

        return query.toString().trim().removeSuffix("OR")
    }

    private fun getSelectionArgsQuery(filterMedia: Int): ArrayList<String> {
        val args = ArrayList<String>()
        if (filterMedia and MediaType.Image.id != 0) {
            SupportedExtensionsRepository.photoExtensions.forEach {
                args.add("%$it")
            }
        }

        if (filterMedia and MediaType.Portrait.id != 0) {
            args.add("%.jpg")
            args.add("%.jpeg")
        }

        if (filterMedia and MediaType.Video.id != 0) {
            SupportedExtensionsRepository.videoExtensions.forEach {
                args.add("%$it")
            }
        }

        if (filterMedia and MediaType.Gif.id != 0) {
            args.add("%.gif")
        }

        if (filterMedia and MediaType.Raw.id != 0) {
            SupportedExtensionsRepository.rawExtensions.forEach {
                args.add("%$it")
            }
        }

        if (filterMedia and MediaType.Svg.id != 0) {
            args.add("%.svg")
        }

        return args
    }

    private fun parseCursor(cursor: Cursor): LinkedHashSet<String> {
        val foldersToIgnore = arrayListOf("/storage/emulated/legacy")
        val config = context.config
        val includedFolders = config.includedFolders
        val OTGPath = config.otgPath
        val foldersToScan = config.everShownFolders.filter {
            it == FAVORITES || it == RECYCLE_BIN || context.getDoesFilePathExist(
                it,
                OTGPath
            )
        }.toHashSet()

        cursor.use {
            if (cursor.moveToFirst()) {
                do {
                    val path = cursor.getStringValue(Images.Media.DATA)
                    val parentPath = File(path).parent ?: continue
                    if (!includedFolders.contains(parentPath) && !foldersToIgnore.contains(
                            parentPath
                        )
                    ) {
                        foldersToScan.add(parentPath)
                    }
                } while (cursor.moveToNext())
            }
        }

        includedFolders.forEach {
            addFolder(foldersToScan, it)
        }

        return foldersToScan.toMutableSet() as LinkedHashSet<String>
    }

    private fun addFolder(curFolders: HashSet<String>, folder: String) {
        curFolders.add(folder)
        val files = File(folder).listFiles() ?: return
        for (file in files) {
            if (file.isDirectory) {
                addFolder(curFolders, file.absolutePath)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getMediaInFolder(
        folder: String,
        isPickImage: Boolean,
        isPickVideo: Boolean,
        filterMedia: Int,
        getProperDateTaken: Boolean,
        getProperLastModified: Boolean,
        getProperFileSize: Boolean,
        favoritePaths: ArrayList<String>,
        getVideoDurations: Boolean,
        lastModifieds: HashMap<String, Long>,
        dateTakens: HashMap<String, Long>
    ): ArrayList<Medium> {
        val media = ArrayList<Medium>()
        val isRecycleBin = folder == RECYCLE_BIN
        val deletedMedia = if (isRecycleBin) {
            context.getUpdatedDeletedMedia()
        } else {
            ArrayList()
        }

        val config = context.config
        val checkProperFileSize =
            getProperFileSize || config.fileLoadingPriority == FileLoadingPriority.Compromise.id
        val checkFileExistence = config.fileLoadingPriority == FileLoadingPriority.Validity.id
        val showHidden = config.shouldShowHidden
        val showPortraits = filterMedia and MediaType.Portrait.id != 0
        val fileSizes =
            if (checkProperFileSize || checkFileExistence) getFolderSizes(folder) else HashMap()

        val files = when (folder) {
            FAVORITES -> favoritePaths.filter { showHidden || !it.contains("/.") }.map { File(it) }
                .toMutableList() as ArrayList<File>
            RECYCLE_BIN -> deletedMedia.map { File(it.path) }.toMutableList() as ArrayList<File>
            else -> File(folder).listFiles()?.toMutableList() ?: return media
        }

        for (curFile in files) {
            var file = curFile
            if (shouldStop) {
                break
            }

            var path = file.absolutePath
            var isPortrait = false
            val isImage = path.isImageFast()
            val isVideo = if (isImage) false else path.isVideoFast()
            val isGif = if (isImage || isVideo) false else IsGifUseCase(path)
            val isRaw = if (isImage || isVideo || isGif) false else path.isRawFast()
            val isSvg = if (isImage || isVideo || isGif || isRaw) false else IsSvgUseCase(path)

            if (!isImage && !isVideo && !isGif && !isRaw && !isSvg) {
                if (showPortraits && file.name.startsWith("img_", true) && file.isDirectory) {
                    val portraitFiles = file.listFiles() ?: continue
                    val cover = portraitFiles.firstOrNull { it.name.contains("cover", true) }
                        ?: portraitFiles.firstOrNull()
                    if (cover != null && !files.contains(cover)) {
                        file = cover
                        path = cover.absolutePath
                        isPortrait = true
                    } else {
                        continue
                    }
                } else {
                    continue
                }
            }

            if (isVideo && (isPickImage || filterMedia and MediaType.Video.id == 0))
                continue

            if (isImage && (isPickVideo || filterMedia and MediaType.Image.id == 0))
                continue

            if (isGif && filterMedia and MediaType.Gif.id == 0)
                continue

            if (isRaw && filterMedia and MediaType.Raw.id == 0)
                continue

            if (isSvg && filterMedia and MediaType.Svg.id == 0)
                continue

            val filename = file.name
            if (!showHidden && filename.startsWith('.'))
                continue

            var size = 0L
            if (checkProperFileSize || checkFileExistence) {
                var newSize = fileSizes.remove(path)
                if (newSize == null) {
                    newSize = file.length()
                }
                size = newSize
            }

            if ((checkProperFileSize || checkFileExistence) && size <= 0L) {
                continue
            }

            if (checkFileExistence && (!file.exists() || !file.isFile)) {
                continue
            }

            if (isRecycleBin) {
                deletedMedia.firstOrNull { it.path == path }?.apply {
                    media.add(this)
                }
            } else {
                var lastModified: Long
                var newLastModified = lastModifieds.remove(path)
                if (newLastModified == null) {
                    newLastModified = if (getProperLastModified) {
                        file.lastModified()
                    } else {
                        0L
                    }
                }
                lastModified = newLastModified

                var dateTaken = lastModified
                val videoDuration =
                    if (getVideoDurations && isVideo) context.getDuration(path) ?: 0 else 0

                if (getProperDateTaken) {
                    var newDateTaken = dateTakens.remove(path)
                    if (newDateTaken == null) {
                        newDateTaken = if (getProperLastModified) {
                            lastModified
                        } else {
                            file.lastModified()
                        }
                    }
                    dateTaken = newDateTaken
                }

                val type = when {
                    isVideo -> MediaType.Video.id
                    isGif -> MediaType.Gif.id
                    isRaw -> MediaType.Raw.id
                    isSvg -> MediaType.Svg.id
                    isPortrait -> MediaType.Portrait.id
                    else -> MediaType.Image.id
                }

                val isFavorite = favoritePaths.contains(path)
                val medium = file.parent?.let {
                    Medium(
                        null,
                        filename,
                        path,
                        it,
                        lastModified,
                        dateTaken,
                        size,
                        type,
                        videoDuration,
                        isFavorite,
                        0L,
                        0L
                    )
                }
                if (medium != null) {
                    media.add(medium)
                }
            }
        }

        return media
    }

    fun getAndroid11FolderMedia(
        isPickImage: Boolean,
        isPickVideo: Boolean,
        favoritePaths: ArrayList<String>,
        getFavoritePathsOnly: Boolean,
        getProperDateTaken: Boolean,
        dateTakens: HashMap<String, Long>
    ): HashMap<String, ArrayList<Medium>> {
        val media = HashMap<String, ArrayList<Medium>>()
        if (!IsRPlusUseCase() || Environment.isExternalStorageManager()) {
            return media
        }

        val filterMedia = context.config.filterMedia
        val showHidden = context.config.shouldShowHidden

        val projection = arrayOf(
            Images.Media._ID,
            Images.Media.DISPLAY_NAME,
            Images.Media.DATA,
            Images.Media.DATE_MODIFIED,
            Images.Media.DATE_TAKEN,
            Images.Media.SIZE,
            MediaStore.MediaColumns.DURATION
        )

        val uri = Files.getContentUri("external")

        context.queryCursor(uri, projection) { cursor ->
            if (shouldStop) {
                return@queryCursor
            }

            try {
                val mediaStoreId = cursor.getLongValue(Images.Media._ID)
                val filename = cursor.getStringValue(Images.Media.DISPLAY_NAME)
                val path = cursor.getStringValue(Images.Media.DATA)
                if (getFavoritePathsOnly && !favoritePaths.contains(path)) {
                    return@queryCursor
                }

                val isPortrait = false
                val isImage = path.isImageFast()
                val isVideo = if (isImage) false else path.isVideoFast()
                val isGif = if (isImage || isVideo) false else IsGifUseCase(path)
                val isRaw = if (isImage || isVideo || isGif) false else path.isRawFast()
                val isSvg = if (isImage || isVideo || isGif || isRaw) false else IsSvgUseCase(path)

                if (!isImage && !isVideo && !isGif && !isRaw && !isSvg) {
                    return@queryCursor
                }

                if (isVideo && (isPickImage || filterMedia and MediaType.Video.id == 0))
                    return@queryCursor

                if (isImage && (isPickVideo || filterMedia and MediaType.Image.id == 0))
                    return@queryCursor

                if (isGif && filterMedia and MediaType.Gif.id == 0)
                    return@queryCursor

                if (isRaw && filterMedia and MediaType.Raw.id == 0)
                    return@queryCursor

                if (isSvg && filterMedia and MediaType.Svg.id == 0)
                    return@queryCursor

                if (!showHidden && filename.startsWith('.'))
                    return@queryCursor

                val size = cursor.getLongValue(Images.Media.SIZE)
                if (size <= 0L) {
                    return@queryCursor
                }

                val type = when {
                    isVideo -> MediaType.Video.id
                    isGif -> MediaType.Gif.id
                    isRaw -> MediaType.Raw.id
                    isSvg -> MediaType.Svg.id
                    isPortrait -> MediaType.Portrait.id
                    else -> MediaType.Image.id
                }

                val lastModified = cursor.getLongValue(Images.Media.DATE_MODIFIED) * 1000
                var dateTaken = cursor.getLongValue(Images.Media.DATE_TAKEN)

                if (getProperDateTaken) {
                    dateTaken = dateTakens.remove(path) ?: lastModified
                }

                if (dateTaken == 0L) {
                    dateTaken = lastModified
                }

                val videoDuration =
                    (cursor.getIntValue(MediaStore.MediaColumns.DURATION) / 1000.toDouble()).roundToInt()
                val isFavorite = favoritePaths.contains(path)
                val medium =
                    Medium(
                        null,
                        filename,
                        path,
                        path.getParentPath(),
                        lastModified,
                        dateTaken,
                        size,
                        type,
                        videoDuration,
                        isFavorite,
                        0L,
                        mediaStoreId
                    )
                val parent = medium.parentPath.lowercase(Locale.getDefault())
                val currentFolderMedia = media[parent]
                if (currentFolderMedia == null) {
                    media[parent] = ArrayList()
                }

                media[parent]?.add(medium)
            } catch (_: Exception) {
            }
        }

        return media
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getMediaOnOTG(
        folder: String,
        isPickImage: Boolean,
        isPickVideo: Boolean,
        filterMedia: Int,
        favoritePaths: ArrayList<String>,
        getVideoDurations: Boolean
    ): ArrayList<Medium> {
        val media = ArrayList<Medium>()
        val files = context.getDocumentFile(folder)?.listFiles() ?: return media
        val checkFileExistence =
            context.config.fileLoadingPriority == FileLoadingPriority.Validity.id
        val showHidden = context.config.shouldShowHidden
        val oTGPath = context.config.otgPath

        for (file in files) {
            if (shouldStop) {
                break
            }

            val filename = file.name ?: continue
            val isImage = filename.isImageFast()
            val isVideo = if (isImage) false else filename.isVideoFast()
            val isGif = if (isImage || isVideo) false else IsGifUseCase(filename)
            val isRaw = if (isImage || isVideo || isGif) false else filename.isRawFast()
            val isSvg = if (isImage || isVideo || isGif || isRaw) false else IsSvgUseCase(filename)

            if (!isImage && !isVideo && !isGif && !isRaw && !isSvg)
                continue

            if (isVideo && (isPickImage || filterMedia and MediaType.Video.id == 0))
                continue

            if (isImage && (isPickVideo || filterMedia and MediaType.Image.id == 0))
                continue

            if (isGif && filterMedia and MediaType.Gif.id == 0)
                continue

            if (isRaw && filterMedia and MediaType.Raw.id == 0)
                continue

            if (isSvg && filterMedia and MediaType.Svg.id == 0)
                continue

            if (!showHidden && filename.startsWith('.'))
                continue

            val size = file.length()
            if (size <= 0L || (checkFileExistence && !context.getDoesFilePathExist(
                    file.uri.toString(),
                    oTGPath
                ))
            )
                continue

            val dateTaken = file.lastModified()
            val dateModified = file.lastModified()

            val type = when {
                isVideo -> MediaType.Video.id
                isGif -> MediaType.Gif.id
                isRaw -> MediaType.Raw.id
                isSvg -> MediaType.Svg.id
                else -> MediaType.Image.id
            }

            val path = Uri.decode(
                file.uri.toString().replaceFirst(
                    "${context.config.otgTreeUri}/document/${context.config.otgPartition}%3A",
                    "${context.config.otgPath}/"
                )
            )
            val videoDuration = if (getVideoDurations) context.getDuration(path) ?: 0 else 0
            val isFavorite = favoritePaths.contains(path)
            val medium = Medium(
                null,
                filename,
                path,
                folder,
                dateModified,
                dateTaken,
                size,
                type,
                videoDuration,
                isFavorite,
                0L,
                0L
            )
            media.add(medium)
        }

        return media
    }

    fun getFolderDateTakens(folder: String): HashMap<String, Long> {
        val dateTakens = HashMap<String, Long>()
        if (folder != FAVORITES) {
            val projection = arrayOf(
                Images.Media.DISPLAY_NAME,
                Images.Media.DATE_TAKEN
            )

            val uri = Files.getContentUri("external")
            val selection = "${Images.Media.DATA} LIKE ? AND ${Images.Media.DATA} NOT LIKE ?"
            val selectionArgs = arrayOf("$folder/%", "$folder/%/%")

            context.queryCursor(uri, projection, selection, selectionArgs) { cursor ->
                try {
                    val dateTaken = cursor.getLongValue(Images.Media.DATE_TAKEN)
                    if (dateTaken != 0L) {
                        val name = cursor.getStringValue(Images.Media.DISPLAY_NAME)
                        dateTakens["$folder/$name"] = dateTaken
                    }
                } catch (_: Exception) {
                }
            }
        }

        val dateTakenValues = try {
            if (folder == FAVORITES) {
                GalleryDatabase.getInstance(context.applicationContext).DateTakensDao()
                    .getAllDateTakens()
            } else {
                GalleryDatabase.getInstance(context.applicationContext).DateTakensDao()
                    .getDateTakensFromPath(folder)
            }
        } catch (e: Exception) {
            return dateTakens
        }

        dateTakenValues.forEach {
            dateTakens[it.fullPath] = it.taken
        }

        return dateTakens
    }

    fun getDateTakens(): HashMap<String, Long> {
        val dateTakens = HashMap<String, Long>()
        val projection = arrayOf(
            Images.Media.DATA,
            Images.Media.DATE_TAKEN
        )

        val uri = Files.getContentUri("external")

        try {
            context.queryCursor(uri, projection) { cursor ->
                try {
                    val dateTaken = cursor.getLongValue(Images.Media.DATE_TAKEN)
                    if (dateTaken != 0L) {
                        val path = cursor.getStringValue(Images.Media.DATA)
                        dateTakens[path] = dateTaken
                    }
                } catch (_: Exception) {
                }
            }

            val dateTakenValues =
                GalleryDatabase.getInstance(context.applicationContext).DateTakensDao()
                    .getAllDateTakens()

            dateTakenValues.forEach {
                dateTakens[it.fullPath] = it.taken
            }
        } catch (_: Exception) {
        }

        return dateTakens
    }

    fun getFolderLastModifieds(folder: String): HashMap<String, Long> {
        val lastModifieds = HashMap<String, Long>()
        if (folder != FAVORITES) {
            val projection = arrayOf(
                Images.Media.DISPLAY_NAME,
                Images.Media.DATE_MODIFIED
            )

            val uri = Files.getContentUri("external")
            val selection = "${Images.Media.DATA} LIKE ? AND ${Images.Media.DATA} NOT LIKE ?"
            val selectionArgs = arrayOf("$folder/%", "$folder/%/%")

            context.queryCursor(uri, projection, selection, selectionArgs) { cursor ->
                try {
                    val lastModified = cursor.getLongValue(Images.Media.DATE_MODIFIED) * 1000
                    if (lastModified != 0L) {
                        val name = cursor.getStringValue(Images.Media.DISPLAY_NAME)
                        lastModifieds["$folder/$name"] = lastModified
                    }
                } catch (_: Exception) {
                }
            }
        }

        return lastModifieds
    }

    fun getLastModifieds(): HashMap<String, Long> {
        val lastModifieds = HashMap<String, Long>()
        val projection = arrayOf(
            Images.Media.DATA,
            Images.Media.DATE_MODIFIED
        )

        val uri = Files.getContentUri("external")

        try {
            context.queryCursor(uri, projection) { cursor ->
                try {
                    val lastModified = cursor.getLongValue(Images.Media.DATE_MODIFIED) * 1000
                    if (lastModified != 0L) {
                        val path = cursor.getStringValue(Images.Media.DATA)
                        lastModifieds[path] = lastModified
                    }
                } catch (_: Exception) {
                }
            }
        } catch (_: Exception) {
        }

        return lastModifieds
    }

    private fun getFolderSizes(folder: String): HashMap<String, Long> {
        val sizes = HashMap<String, Long>()
        if (folder != FAVORITES) {
            val projection = arrayOf(
                Images.Media.DISPLAY_NAME,
                Images.Media.SIZE
            )

            val uri = Files.getContentUri("external")
            val selection = "${Images.Media.DATA} LIKE ? AND ${Images.Media.DATA} NOT LIKE ?"
            val selectionArgs = arrayOf("$folder/%", "$folder/%/%")

            context.queryCursor(uri, projection, selection, selectionArgs) { cursor ->
                try {
                    val size = cursor.getLongValue(Images.Media.SIZE)
                    if (size != 0L) {
                        val name = cursor.getStringValue(Images.Media.DISPLAY_NAME)
                        sizes["$folder/$name"] = size
                    }
                } catch (_: Exception) {
                }
            }
        }

        return sizes
    }

    fun sortMedia(media: ArrayList<Medium>, sorting: Int) {
        if (sorting and SORT_BY_RANDOM != 0) {
            media.shuffle()
            return
        }

        media.sortWith { o1, o2 ->
            o1 as Medium
            o2 as Medium
            var result = when {
                sorting and SORT_BY_NAME != 0 -> {
                    if (sorting and SORT_USE_NUMERIC_VALUE != 0) {
                        AlphanumericComparator().compare(
                            o1.name.normalizeString().lowercase(Locale.ROOT),
                            o2.name.normalizeString().lowercase(Locale.ROOT)
                        )
                    } else {
                        o1.name.normalizeString().lowercase(Locale.ROOT)
                            .compareTo(o2.name.normalizeString().lowercase(Locale.ROOT))
                    }
                }
                sorting and SORT_BY_PATH != 0 -> {
                    if (sorting and SORT_USE_NUMERIC_VALUE != 0) {
                        AlphanumericComparator().compare(
                            o1.path.lowercase(Locale.ROOT),
                            o2.path.lowercase(Locale.ROOT)
                        )
                    } else {
                        o1.path.lowercase(Locale.ROOT).compareTo(o2.path.lowercase(Locale.ROOT))
                    }
                }
                sorting and SORT_BY_SIZE != 0 -> o1.size.compareTo(o2.size)
                sorting and SORT_BY_DATE_MODIFIED != 0 -> o1.modified.compareTo(o2.modified)
                else -> o1.taken.compareTo(o2.taken)
            }

            if (sorting and SORT_DESCENDING != 0) {
                result *= -1
            }
            result
        }
    }

    fun groupMedia(media: ArrayList<Medium>, path: String): ArrayList<ThumbnailItem> {
        val pathToCheck = path.ifEmpty { SHOW_ALL }
        val currentGrouping = context.config.getFolderGrouping(pathToCheck)
        if (currentGrouping and GroupBy.None.id != 0) {
            return media as ArrayList<ThumbnailItem>
        }

        val thumbnailItems = ArrayList<ThumbnailItem>()
        if (context.config.scrollHorizontally) {
            media.mapTo(thumbnailItems) { it }
            return thumbnailItems
        }

        val mediumGroups = LinkedHashMap<String, ArrayList<Medium>>()
        media.forEach {
            val key = it.getGroupingKey(currentGrouping)
            if (!mediumGroups.containsKey(key)) {
                mediumGroups[key] = ArrayList()
            }
            mediumGroups[key]!!.add(it)
        }

        val sortDescending = currentGrouping and GroupBy.Descending.id != 0
        val sorted =
            if (currentGrouping and GroupBy.LastModifiedDaily.id != 0 || currentGrouping and GroupBy.LastModifiedMonthly.id != 0 ||
                currentGrouping and GroupBy.DateTakenDaily.id != 0 || currentGrouping and GroupBy.DateTakenMonthly.id != 0
            ) {
                mediumGroups.toSortedMap(if (sortDescending) compareByDescending {
                    it.toLongOrNull() ?: 0L
                } else {
                    compareBy { it.toLongOrNull() ?: 0L }
                })
            } else {
                mediumGroups.toSortedMap(if (sortDescending) compareByDescending { it } else compareBy { it })
            }

        mediumGroups.clear()
        for ((key, value) in sorted) {
            mediumGroups[key] = value
        }

        val today = formatDate(System.currentTimeMillis().toString(), true)
        val yesterday =
            formatDate((System.currentTimeMillis() - DAY_SECONDS * 1000).toString(), true)
        for ((key, value) in mediumGroups) {
            var currentGridPosition = 0
            val sectionKey = getFormattedKey(key, currentGrouping, today, yesterday, value.size)
            thumbnailItems.add(ThumbnailSection(sectionKey))

            value.forEach {
                it.gridPosition = currentGridPosition++
            }

            thumbnailItems.addAll(value)
        }

        return thumbnailItems
    }

    private fun getFormattedKey(
        key: String,
        grouping: Int,
        today: String,
        yesterday: String,
        count: Int
    ): String {
        var result = when {
            grouping and GroupBy.LastModifiedDaily.id != 0 || grouping and GroupBy.DateTakenDaily.id != 0 -> getFinalDate(
                formatDate(key, true),
                today,
                yesterday
            )
            grouping and GroupBy.LastModifiedMonthly.id != 0 || grouping and GroupBy.DateTakenMonthly.id != 0 -> formatDate(
                key,
                false
            )
            grouping and GroupBy.FileType.id != 0 -> getFileTypeString(key)
            grouping and GroupBy.Extension.id != 0 -> key.uppercase(Locale.ROOT)
            grouping and GroupBy.Folder.id != 0 -> context.humanizePath(key)
            else -> key
        }

        if (result.isEmpty()) {
            result = context.getString(R.string.unknown)
        }

        return if (grouping and GroupBy.ShowFileCount.id != 0) {
            "$result ($count)"
        } else {
            result
        }
    }

    private fun getFinalDate(date: String, today: String, yesterday: String): String {
        return when (date) {
            today -> context.getString(R.string.today)
            yesterday -> context.getString(R.string.yesterday)
            else -> date
        }
    }

    private fun formatDate(timestamp: String, showDay: Boolean): String {
        return if (timestamp.areDigitsOnly()) {
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp.toLong()
            val format = if (showDay) context.config.dateFormat else "MMMM yyyy"
            DateFormat.format(format, cal).toString()
        } else {
            ""
        }
    }

    private fun getFileTypeString(key: String): String {
        val stringId = when (key.toInt()) {
            MediaType.Image.id -> R.string.images
            MediaType.Video.id -> R.string.videos
            MediaType.Gif.id -> R.string.gifs
            MediaType.Raw.id -> R.string.raw_images
            MediaType.Svg.id -> R.string.svgs
            else -> R.string.portraits
        }
        return context.getString(stringId)
    }
}
