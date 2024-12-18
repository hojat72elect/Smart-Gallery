package ca.hojat.smart.gallery.feature_settings

import android.os.Bundle
import ca.hojat.smart.gallery.R
import ca.hojat.smart.gallery.databinding.ActivityManageFoldersBinding
import ca.hojat.smart.gallery.shared.activities.BaseActivity
import ca.hojat.smart.gallery.shared.extensions.beVisibleIf
import ca.hojat.smart.gallery.shared.extensions.config
import ca.hojat.smart.gallery.shared.extensions.getProperTextColor
import ca.hojat.smart.gallery.shared.extensions.internalStoragePath
import ca.hojat.smart.gallery.shared.extensions.isExternalStorageManager
import ca.hojat.smart.gallery.shared.extensions.viewBinding
import ca.hojat.smart.gallery.shared.helpers.NavigationIcon
import ca.hojat.smart.gallery.shared.ui.adapters.ManageFoldersAdapter
import ca.hojat.smart.gallery.shared.ui.adapters.RefreshRecyclerViewListener
import ca.hojat.smart.gallery.shared.ui.dialogs.FilePickerDialog

class ExcludedFoldersActivity : BaseActivity(), RefreshRecyclerViewListener {

    private val binding by viewBinding(ActivityManageFoldersBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        updateFolders()
        setupOptionsMenu()
        binding.manageFoldersToolbar.title =
            getString(R.string.excluded_folders)

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
        val folders = ArrayList<String>()
        config.excludedFolders.mapTo(folders) { it }
        var placeholderText = getString(R.string.excluded_activity_placeholder)
        binding.manageFoldersPlaceholder.apply {
            beVisibleIf(folders.isEmpty())
            setTextColor(getProperTextColor())

            if (!isExternalStorageManager()) {
                placeholderText = placeholderText.substringBefore("\n")
            }

            text = placeholderText
        }

        val adapter = ManageFoldersAdapter(this, folders, true, this, binding.manageFoldersList) {}
        binding.manageFoldersList.adapter = adapter
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
            internalStoragePath,
            pickFile = false,
            config.shouldShowHidden,
            showFAB = false,
            canAddShowHiddenButton = true,
            enforceStorageRestrictions = false,
        ) {
            config.lastFilePickerPath = it
            config.addExcludedFolder(it)
            updateFolders()
        }
    }
}
