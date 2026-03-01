package com.example.bfuhelper.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bfuhelper.databinding.ListItemDayBinding
import com.example.bfuhelper.model.sport.SportItem

class SportItemAdapter : ListAdapter<SportItem, SportItemAdapter.SportItemViewHolder>(SportItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SportItemViewHolder {
        val binding = ListItemDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SportItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SportItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class SportItemViewHolder(private val binding: ListItemDayBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(sportItem: SportItem) {
            binding.sportItem = sportItem
            binding.executePendingBindings() // Важно для немедленного обновления привязки данных
        }
    }

    class SportItemDiffCallback : DiffUtil.ItemCallback<SportItem>() {
        override fun areItemsTheSame(oldItem: SportItem, newItem: SportItem): Boolean {
            return oldItem.day == newItem.day && oldItem.month == newItem.month
        }

        override fun areContentsTheSame(oldItem: SportItem, newItem: SportItem): Boolean {
            return oldItem == newItem
        }
    }
}