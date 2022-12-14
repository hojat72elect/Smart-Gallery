package ca.on.sudbury.hojat.smartgallery.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.Menu
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.widget.PopupMenu
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.extensions.getProperTextColor
import ca.on.sudbury.hojat.smartgallery.interfaces.RefreshRecyclerViewListener
import ca.on.sudbury.hojat.smartgallery.views.MyRecyclerView
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.extensions.isWhiteTheme
import ca.on.sudbury.hojat.smartgallery.usecases.IsSPlusUseCase
import kotlinx.android.synthetic.main.item_manage_folder.view.*

class ManageFoldersAdapter(
    activity: BaseSimpleActivity,
    var folders: ArrayList<String>,
    private val isShowingExcludedFolders: Boolean,
    val listener: RefreshRecyclerViewListener?,
    recyclerView: MyRecyclerView, itemClick: (Any) -> Unit
) : MyRecyclerViewAdapter(activity, recyclerView, itemClick) {

    private val config = activity.config

    init {
        setupDragListener(true)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        createViewHolder(R.layout.item_manage_folder, parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val folder = folders[position]
        holder.bindView(folder, allowSingleClick = true, allowLongClick = true) { itemView, _ ->
            setupView(itemView, folder)
        }
        bindViewHolder(holder)
    }

    override fun getItemCount() = folders.size

    private fun getSelectedItems() =
        folders.filter { selectedKeys.contains(it.hashCode()) } as ArrayList<String>

    private fun setupView(view: View, folder: String) {
        view.apply {
            manage_folder_holder?.isSelected = selectedKeys.contains(folder.hashCode())
            manage_folder_title.apply {
                text = folder
                setTextColor(context.getProperTextColor())
            }

            overflow_menu_icon.drawable.apply {
                mutate()
                setTint(activity.getProperTextColor())
            }

            overflow_menu_icon.setOnClickListener {
                showPopupMenu(overflow_menu_anchor, folder)
            }
        }
    }

    private fun showPopupMenu(view: View, folder: String) {
        finishActMode()
        val theme = getPopupMenuTheme(activity)
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

    private fun getPopupMenuTheme(owner: Context): Int {
        return if (IsSPlusUseCase() && baseConfig.isUsingSystemTheme) {
            R.style.AppTheme_YouPopupMenuStyle
        } else if (owner.isWhiteTheme()) {
            R.style.AppTheme_PopupMenuLightStyle
        } else {
            R.style.AppTheme_PopupMenuDarkStyle
        }
    }
}
