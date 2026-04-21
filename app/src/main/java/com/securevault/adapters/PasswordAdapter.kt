package com.securevault.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.securevault.databinding.ItemPasswordBinding
import com.securevault.models.CATEGORY_ICONS
import com.securevault.models.PasswordEntry

class PasswordAdapter(
    private val onItemClick: (PasswordEntry) -> Unit,
    private val onFavoriteClick: (PasswordEntry) -> Unit,
    private val onDeleteClick: (PasswordEntry) -> Unit
) : ListAdapter<PasswordEntry, PasswordAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemPasswordBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemPasswordBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val entry = getItem(position)
        with(holder.binding) {
            tvIcon.text = CATEGORY_ICONS[entry.category] ?: "🔑"
            tvTitle.text = entry.title
            tvUsername.text = entry.username
            tvWebsite.text = if (entry.website.isNotEmpty()) entry.website else ""
            tvFavorite.text = if (entry.isFavorite) "⭐" else "☆"
            root.setOnClickListener { onItemClick(entry) }
            tvFavorite.setOnClickListener { onFavoriteClick(entry) }
            btnDelete.setOnClickListener { onDeleteClick(entry) }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<PasswordEntry>() {
            override fun areItemsTheSame(a: PasswordEntry, b: PasswordEntry) = a.id == b.id
            override fun areContentsTheSame(a: PasswordEntry, b: PasswordEntry) = a == b
        }
    }
}
