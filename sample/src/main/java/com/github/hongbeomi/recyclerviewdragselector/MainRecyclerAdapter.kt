package com.github.hongbeomi.recyclerviewdragselector

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.github.hongbeomi.recyclerviewdragselector.databinding.ItemMainBinding

class MainRecyclerAdapter : ListAdapter<MainItem, MainViewHolder>(MainItem.diffUtil) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder =
        MainViewHolder(
            ItemMainBinding.inflate(LayoutInflater.from(parent.context))
        )

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) =
        holder.bind(getItem(position))
}