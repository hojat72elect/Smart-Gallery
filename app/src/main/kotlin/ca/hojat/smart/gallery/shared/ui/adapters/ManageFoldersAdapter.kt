package ca.hojat.smart.gallery.shared.ui.adapters

import android.os.Build
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.databinding.ItemManageFolderBinding
import ca.hojat.smart.gallery.shared.extensions.config
import ca.hojat.smart.gallery.shared.extensions.getPopupMenuTheme
import ca.hojat.smart.gallery.shared.extensions.getProperTextColor
import ca.hojat.smart.gallery.shared.extensions.setupViewBackground
import ca.hojat.smart.gallery.shared.activities.BaseActivity
import ca.hojat.smart.gallery.shared.ui.views.MyRecyclerView

@RequiresApi(Build.VERSION_CODES.O)
class ManageFoldersAdapter(
    activity: BaseActivity,
    var folders: ArrayList<String>,
    private val isShowingExcludedFolders: Boolean,
    val listener: RefreshRecyclerViewListener?,
    recyclerView: MyRecyclerView,
    itemClick: (Any) -> Unit
) : MyRecyclerViewAdapter(activity, recyclerView, itemClick) {

    private val config = activity.config

    init {
        setupDragListener()
    }

    override fun getActionMenuId() = R.menu.cab_remove_only

    override fun prepareActionMode(menu: Menu) {}

    override fun actionItemPressed(id: Int) {
        when (id) {
            R.id.cab_remove -> removeSelection()
        }
    }

    override fun getSelectableItemCount() = folders.size

    override fun getIsItemSelectable(position: Int) = true

    override fun getItemSelectionKey(position: Int) = folders.getOrNull(position)?.hashCode()

    override fun getItemKeyPosition(key: Int) = folders.indexOfFirst { it.hashCode() == key }

    override fun onActionModeCreated() {}

    override fun onActionModeDestroyed() {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return createViewHolder(ItemManageFolderBinding.inflate(layoutInflater, parent, false).root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val folder = folders[position]
        holder.bindView(
            any = folder,
            allowSingleClick = true,
            allowLongClick = true
        ) { itemView, _ ->
            setupView(itemView, folder)
        }
        bindViewHolder(holder)
    }

    override fun getItemCount() = folders.size

    private fun getSelectedItems() =
        folders.filter { selectedKeys.contains(it.hashCode()) } as ArrayList<String>

    private fun setupView(view: View, folder: String) {
        ItemManageFolderBinding.bind(view).apply {
            root.setupViewBackground(activity)
            manageFolderHolder.isSelected = selectedKeys.contains(folder.hashCode())
            manageFolderTitle.apply {
                text = folder
                setTextColor(context.getProperTextColor())
            }

            overflowMenuIcon.drawable.apply {
                mutate()
                setTint(activity.getProperTextColor())
            }

            overflowMenuIcon.setOnClickListener {
                showPopupMenu(overflowMenuAnchor, folder)
            }
        }
    }

    private fun showPopupMenu(view: View, folder: String) {
        finishActMode()
        val theme = activity.getPopupMenuTheme()
        val contextTheme = ContextThemeWrapper(activity, theme)

        PopupMenu(contextTheme, view, Gravity.END).apply {
            inflate(getActionMenuId())
            setOnMenuItemClickListener { item ->
                val eventTypeId = folder.hashCode()
                when (item.itemId) {
                    R.id.cab_remove -> {
                        executeItemMenuOperation(eventTypeId) {
                            removeSelection()
                        }
                    }
                }
                true
            }
            show()
        }
    }

    private fun executeItemMenuOperation(eventTypeId: Int, callback: () -> Unit) {
        selectedKeys.clear()
        selectedKeys.add(eventTypeId)
        callback()
    }

    private fun removeSelection() {
        val removeFolders = ArrayList<String>(selectedKeys.size)
        val positions = getSelectedItemPositions()

        getSelectedItems().forEach {
            removeFolders.add(it)
            if (isShowingExcludedFolders) {
                config.removeExcludedFolder(it)
            } else {
                config.removeIncludedFolder(it)
            }
        }

        folders.removeAll(removeFolders.toSet())
        removeSelectedItems(positions)
        if (folders.isEmpty()) {
            listener?.refreshItems()
        }
    }
}
