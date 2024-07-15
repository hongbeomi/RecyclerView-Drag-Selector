package com.github.hongbeomi.recyclerviewdragselector

import androidx.annotation.ColorInt
import androidx.recyclerview.widget.DiffUtil

data class MainItem(
    val title: String,
    val isSelected: Boolean = false,
    @ColorInt
    val backgroundColor: Int,
) {
    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<MainItem>() {
            override fun areItemsTheSame(oldItem: MainItem, newItem: MainItem): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: MainItem, newItem: MainItem): Boolean =
                oldItem == newItem
        }
    }

}