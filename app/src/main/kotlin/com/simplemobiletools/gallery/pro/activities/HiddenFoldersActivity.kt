package com.simplemobiletools.gallery.pro.activities

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.simplemobiletools.gallery.pro.dialogs.FilePickerDialog
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.beVisibleIf
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getProperTextColor
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.viewBinding
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.NavigationIcon
import com.simplemobiletools.gallery.pro.new_architecture.shared.helpers.ensureBackgroundThread
import com.simplemobiletools.gallery.pro.interfaces.RefreshRecyclerViewListener
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.adapters.ManageHiddenFoldersAdapter
import com.simplemobiletools.gallery.pro.databinding.ActivityManageFoldersBinding
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.config
import com.simplemobiletools.gallery.pro.new_architecture.shared.extensions.getNoMediaFolders
import com.simplemobiletools.gallery.pro.new_architecture.shared.activities.BaseActivity

@RequiresApi(Build.VERSION_CODES.O)
class HiddenFoldersActivity : BaseActivity(), RefreshRecyclerViewListener {

    private val binding by viewBinding(ActivityManageFoldersBinding::inflate)
    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        updateFolders()
        setupOptionsMenu()
        binding.manageFoldersToolbar.title = getString(R.string.hidden_folders)

        updateMaterialActivityViews(
            binding.manageFoldersCoordinator,
            binding.manageFoldersList,
            useTransparentNavigation = true,
            useTopSearchMenu = false
        )
        setupMaterialScrollListener(binding.manageFoldersList, binding.manageFoldersToolbar)
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(binding.manageFoldersToolbar, NavigationIcon.Arrow)
    }

    private fun updateFolders() {
        getNoMediaFolders {
            runOnUiThread {
                binding.manageFoldersPlaceholder.apply {
                    text = getString(R.string.hidden_folders_placeholder)
                    beVisibleIf(it.isEmpty())
                    setTextColor(getProperTextColor())
                }

                val adapter =
                    ManageHiddenFoldersAdapter(this, it, this, binding.manageFoldersList) {}
                binding.manageFoldersList.adapter = adapter
            }
        }
    }

    private fun setupOptionsMenu() {
        binding.manageFoldersToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.add_folder -> addFolder()
                else -> return@setOnMenuItemClickListener false
            }
            return@setOnMenuItemClickListener true
        }
    }

    override fun refreshItems() {
        updateFolders()
    }

    private fun addFolder() {
        FilePickerDialog(
            activity = this,
            currPath = config.lastFilePickerPath,
            pickFile = false,
            showHidden = config.shouldShowHidden,
            showFAB = false,
            canAddShowHiddenButton = true
        ) {
            config.lastFilePickerPath = it
            ensureBackgroundThread {
                addNoMedia(it) {
                    updateFolders()
                }
            }
        }
    }
}
