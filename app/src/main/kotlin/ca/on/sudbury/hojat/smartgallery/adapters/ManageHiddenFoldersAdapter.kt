package ca.on.sudbury.hojat.smartgallery.adapters

import android.view.Menu
import android.view.View
import android.view.ViewGroup
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.extensions.getProperTextColor
import ca.on.sudbury.hojat.smartgallery.interfaces.RefreshRecyclerViewListener
import ca.on.sudbury.hojat.smartgallery.views.MyRecyclerView
import ca.on.sudbury.hojat.smartgallery.extensions.removeNoMedia
import ca.on.sudbury.hojat.smartgallery.usecases.IsPathOnSdUseCase
import kotlinx.android.synthetic.main.item_manage_folder.view.*

class ManageHiddenFoldersAdapter(
    activity: BaseSimpleActivity,
    var folders: ArrayList<String>,
    val listener: RefreshRecyclerViewListener?,
    recyclerView: MyRecyclerView, itemClick: (Any) -> Unit
) : MyRecyclerViewAdapter(activity, recyclerView, itemClick) {

    init {
        setupDragListener(true)
    }

    override fun getActionMenuId() = R.menu.cab_hidden_folders

    override fun prepareActionMode(menu: Menu) {}

    override fun actionItemPressed(id: Int) {
        when (id) {
            R.id.cab_unhide -> tryUnhideFolders()
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
        }
    }

    private fun tryUnhideFolders() {
        val removeFolders = ArrayList<String>(selectedKeys.size)

        val sdCardPaths = ArrayList<String>()
        getSelectedItems().forEach {
            if (IsPathOnSdUseCase(activity, it)) {
                sdCardPaths.add(it)
            }
        }

        if (sdCardPaths.isNotEmpty()) {
            activity.handleSAFDialog(sdCardPaths.first()) {
                if (it) {
                    unhideFolders(removeFolders)
                }
            }
        } else {
            unhideFolders(removeFolders)
        }
    }

    private fun unhideFolders(removeFolders: ArrayList<String>) {
        val position = getSelectedItemPositions()
        getSelectedItems().forEach {
            removeFolders.add(it)
            activity.removeNoMedia(it)
        }

        folders.removeAll(removeFolders.toSet())
        removeSelectedItems(position)
        if (folders.isEmpty()) {
            listener?.refreshItems()
        }
    }
}
