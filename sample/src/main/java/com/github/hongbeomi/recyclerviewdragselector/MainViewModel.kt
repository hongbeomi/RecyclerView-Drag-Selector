package com.github.hongbeomi.recyclerviewdragselector

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.random.Random

class MainViewModel : ViewModel() {

    private val _itemList: MutableLiveData<List<MainItem>> = MutableLiveData(emptyList())
    val itemList: LiveData<List<MainItem>> = _itemList

    private val _isActive: MutableLiveData<Boolean> = MutableLiveData(true)
    val isActive: LiveData<Boolean> = _isActive

    init {
        _itemList.value = (0..50).map {
            MainItem(
                title = it.toString(),
                backgroundColor = randomColor()
            )
        }
    }

    fun toggleSelect(item: MainItem) {
        _itemList.value = _itemList.value?.map {
            if (it == item) {
                it.copy(isSelected = !item.isSelected)
            } else {
                it
            }
        }
    }

    fun toggleActive(value: Boolean) {
        _isActive.value = value
    }

    @ColorInt
    private fun randomColor(): Int = Color.argb(
        255,
        Random.nextInt(256),
        Random.nextInt(256),
        Random.nextInt(256)
    )

}