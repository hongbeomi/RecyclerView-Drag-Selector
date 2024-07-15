package com.github.hongbeomi.library

import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

internal fun RecyclerView.getItemPosition(e: MotionEvent): Int {
    val v = findChildViewUnder(e.x, e.y) ?: return RecyclerView.NO_POSITION
    return getChildAdapterPosition(v)
}