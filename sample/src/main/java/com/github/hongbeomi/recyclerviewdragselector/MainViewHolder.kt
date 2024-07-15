package com.github.hongbeomi.recyclerviewdragselector

import android.graphics.drawable.ColorDrawable
import androidx.recyclerview.widget.RecyclerView
import com.github.hongbeomi.recyclerviewdragselector.databinding.ItemMainBinding

class MainViewHolder(
    private val binding: ItemMainBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: MainItem) = with(binding.tvItem) {
        text = item.title
        setBackgroundColor(item.backgroundColor)

        foreground = if (item.isSelected) {
            ColorDrawable(context.getColor(R.color.selected_foreground_color))
        } else {
            null
        }

    }

}