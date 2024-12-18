package ca.hojat.smart.gallery.shared.ui.adapters

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.TypedValue
import android.view.Menu
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestOptions
import com.qtalk.recyclerviewfastscroller.RecyclerViewFastScroller
import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.databinding.ItemFilepickerListBinding
import ca.hojat.smart.gallery.shared.extensions.baseConfig
import ca.hojat.smart.gallery.shared.extensions.formatSize
import ca.hojat.smart.gallery.shared.extensions.getAndroidSAFUri
import ca.hojat.smart.gallery.shared.extensions.getColoredDrawableWithColor
import ca.hojat.smart.gallery.shared.extensions.getOTGPublicPath
import ca.hojat.smart.gallery.shared.extensions.getTextSize
import ca.hojat.smart.gallery.shared.extensions.getTimeFormat
import ca.hojat.smart.gallery.shared.extensions.hasOTGConnected
import ca.hojat.smart.gallery.shared.extensions.isGif
import ca.hojat.smart.gallery.shared.extensions.isPathOnOTG
import ca.hojat.smart.gallery.shared.extensions.isRestrictedSAFOnlyRoot
import ca.hojat.smart.gallery.shared.helpers.getFilePlaceholderDrawables
import ca.hojat.smart.gallery.shared.data.domain.FileDirItem
import ca.hojat.smart.gallery.shared.activities.BaseActivity
import ca.hojat.smart.gallery.shared.ui.views.MyRecyclerView
import java.util.Locale

class FilepickerItemsAdapter(
    activity: BaseActivity, val fileDirItems: List<FileDirItem>, recyclerView: MyRecyclerView,
    itemClick: (Any) -> Unit
) : MyRecyclerViewAdapter(activity, recyclerView, itemClick),
    RecyclerViewFastScroller.OnPopupTextUpdate {

    private lateinit var fileDrawable: Drawable
    private lateinit var folderDrawable: Drawable
    private var fileDrawables = HashMap<String, Drawable>()
    private val hasOTGConnected = activity.hasOTGConnected()
    private var fontSize = 0f
    private val cornerRadius = resources.getDimension(R.dimen.rounded_corner_radius_small).toInt()
    private val dateFormat = activity.baseConfig.dateFormat
    private val timeFormat = activity.getTimeFormat()

    init {
        initDrawables()
        fontSize = activity.getTextSize()
    }

    override fun getActionMenuId() = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        createViewHolder(R.layout.item_filepicker_list, parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fileDirItem = fileDirItems[position]
        holder.bindView(
            fileDirItem,
            allowSingleClick = true,
            allowLongClick = false
        ) { itemView, _ ->
            setupView(ItemFilepickerListBinding.bind(itemView), fileDirItem)
        }
        bindViewHolder(holder)
    }

    override fun getItemCount() = fileDirItems.size

    override fun prepareActionMode(menu: Menu) {}

    override fun actionItemPressed(id: Int) {}

    override fun getSelectableItemCount() = fileDirItems.size

    override fun getIsItemSelectable(position: Int) = false

    override fun getItemKeyPosition(key: Int) =
        fileDirItems.indexOfFirst { it.path.hashCode() == key }

    override fun getItemSelectionKey(position: Int) = fileDirItems[position].path.hashCode()

    override fun onActionModeCreated() {}

    override fun onActionModeDestroyed() {}

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        if (!activity.isDestroyed && !activity.isFinishing) {
            Glide.with(activity).clear(ItemFilepickerListBinding.bind(holder.itemView).listItemIcon)
        }
    }

    private fun setupView(view: ItemFilepickerListBinding, fileDirItem: FileDirItem) {
        view.apply {
            listItemName.text = fileDirItem.name
            listItemName.setTextColor(textColor)
            listItemName.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)

            listItemDetails.setTextColor(textColor)
            listItemDetails.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)

            if (fileDirItem.isDirectory) {
                listItemIcon.setImageDrawable(folderDrawable)
                listItemDetails.text = getChildrenCnt(fileDirItem)
            } else {
                listItemDetails.text = fileDirItem.size.formatSize()
                val path = fileDirItem.path
                val placeholder = fileDrawables.getOrElse(
                    fileDirItem.name.substringAfterLast(".").lowercase(Locale.getDefault())
                ) { fileDrawable }
                val options = RequestOptions()
                    .signature(fileDirItem.getKey())
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .centerCrop()
                    .error(placeholder)

                var itemToLoad = if (fileDirItem.name.endsWith(".apk", true)) {
                    val packageInfo = root.context.packageManager.getPackageArchiveInfo(
                        path,
                        PackageManager.GET_ACTIVITIES
                    )
                    if (packageInfo != null) {
                        val appInfo = packageInfo.applicationInfo
                        appInfo.sourceDir = path
                        appInfo.publicSourceDir = path
                        appInfo.loadIcon(root.context.packageManager)
                    } else {
                        path
                    }
                } else {
                    path
                }

                if (!activity.isDestroyed && !activity.isFinishing) {
                    if (activity.isRestrictedSAFOnlyRoot(path)) {
                        itemToLoad = activity.getAndroidSAFUri(path)
                    } else if (hasOTGConnected && itemToLoad is String && activity.isPathOnOTG(
                            itemToLoad
                        )
                    ) {
                        itemToLoad = itemToLoad.getOTGPublicPath(activity)
                    }

                    if (itemToLoad.toString().isGif()) {
                        Glide.with(activity).asBitmap().load(itemToLoad).apply(options)
                            .into(listItemIcon)
                    } else {
                        Glide.with(activity)
                            .load(itemToLoad)
                            .transition(withCrossFade())
                            .apply(options)
                            .transform(CenterCrop(), RoundedCorners(cornerRadius))
                            .into(listItemIcon)
                    }
                }
            }
        }
    }

    private fun getChildrenCnt(item: FileDirItem): String {
        val children = item.children
        return activity.resources.getQuantityString(R.plurals.items, children, children)
    }

    private fun initDrawables() {
        folderDrawable =
            resources.getColoredDrawableWithColor(R.drawable.ic_folder_vector, textColor)
        folderDrawable.alpha = 180
        fileDrawable = resources.getDrawable(R.drawable.ic_file_generic)
        fileDrawables = getFilePlaceholderDrawables(activity)
    }

    override fun onChange(position: Int) =
        fileDirItems.getOrNull(position)?.getBubbleText(activity, dateFormat, timeFormat) ?: ""
}
