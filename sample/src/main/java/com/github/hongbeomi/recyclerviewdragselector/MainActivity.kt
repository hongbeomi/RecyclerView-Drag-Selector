package com.github.hongbeomi.recyclerviewdragselector

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.github.hongbeomi.library.DefaultDragSelectTracker
import com.github.hongbeomi.library.DragSelectReceiver
import com.github.hongbeomi.library.OnItemDragSelectTouchListener
import com.github.hongbeomi.recyclerviewdragselector.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<MainViewModel>()
    private val listAdapter by lazy { MainRecyclerAdapter() }
    private val dragSelectReceiver by lazy {
        object : DragSelectReceiver {
            override fun getItemCount(): Int = listAdapter.itemCount

            override fun setSelected(index: Int, selected: Boolean) {
                val item = listAdapter.currentList.getOrNull(index) ?: return
                if (item.isSelected != selected) {
                    Toast.makeText(
                        this@MainActivity,
                        "Selected letters: ${item.title}",
                        Toast.LENGTH_SHORT
                    ).show()
                    viewModel.toggleSelect(item)
                }
            }

            override fun isSelected(index: Int): Boolean =
                listAdapter.currentList.getOrNull(index)?.isSelected ?: false

            override fun isIndexSelectable(index: Int): Boolean =
                viewModel.isActive.value == true && listAdapter.currentList.getOrNull(index) is MainItem
        }
    }

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        initObservers()
    }

    private fun initViews() = with(binding) {
        switchIsActive.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleActive(isChecked)
        }

        rvList.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 3)
            adapter = listAdapter
            itemAnimator = null
            setHasFixedSize(true)
            val touchListener = OnItemDragSelectTouchListener(
                tracker = DefaultDragSelectTracker.create(dragSelectReceiver),
                isActiveEditMode = { viewModel.isActive.value == true }
            )
            addOnItemTouchListener(touchListener)
        }
    }

    private fun initObservers() = with(viewModel.itemList) {
        observe(this@MainActivity) {
            listAdapter.submitList(it)
        }
    }


}