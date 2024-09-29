package com.simplemobiletools.gallery.pro.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simplemobiletools.gallery.pro.databinding.EditorFilterItemBinding

class FiltersAdapter(
    val context: Context
) :
    RecyclerView.Adapter<FiltersAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            EditorFilterItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount()=0


    inner class ViewHolder(binding: EditorFilterItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }
}
