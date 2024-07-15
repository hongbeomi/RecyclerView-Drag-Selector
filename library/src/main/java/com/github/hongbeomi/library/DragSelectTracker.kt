package com.github.hongbeomi.library

import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

interface DragSelectTracker {

    var hotspotOffsetTop: Int
    var hotspotOffsetBottom: Int

    fun start(selectPosition: Int): Boolean

    fun isActive(): Boolean

    fun onDrag(view: RecyclerView, event: MotionEvent)

    fun onDragStop()

    fun handleDrag(itemPosition: Int)

    fun reset()

}