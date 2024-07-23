package com.github.hongbeomi.library

import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.MotionEvent.ACTION_UP
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import kotlin.math.abs
import kotlin.math.roundToInt

class OnItemDragSelectTouchListener(
    private val tracker: DragSelectTracker,
    private val isActiveEditMode: () -> Boolean,
) : RecyclerView.SimpleOnItemTouchListener() {

    companion object {
        private const val DISTANCE_THRESHOLD = 10
        private const val DEFAULT_START_X = 0f
        private const val DEFAULT_START_Y = 0f
        private const val CLICK_EVENT_THRESHOLD = 5
    }

    private var startX = DEFAULT_START_X
    private var startY = DEFAULT_START_Y

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        if (!isActiveEditMode()) {
            return super.onInterceptTouchEvent(rv, e)
        }
        when (e.action) {
            ACTION_DOWN -> {
                if (isActiveEditMode()) {
                    rv.parent.requestDisallowInterceptTouchEvent(true)
                }
                startX = e.x
                startY = e.y
            }

            ACTION_MOVE -> {
                val dx = abs(e.x - startX)
                val dy = abs(e.y - startY)

                if (dx > dy && dx > DISTANCE_THRESHOLD) {
                    val adapterIsEmpty = rv.adapter?.isEmpty() ?: true
                    val itemPosition = rv.getItemPosition(e)
                    tracker.start(itemPosition)

                    val result = tracker.isActive() && !adapterIsEmpty

                    if (result) {
                        (tracker as? DefaultDragSelectTracker)?.configBound(rv)
                    }
                    return true
                }
            }

            ACTION_UP -> {
                // click event handling
                if (
                    isOnClickEvent(
                        originalX = startX,
                        originalY = startY,
                        lastX = e.x,
                        lastY = e.y
                    )
                ) {
                    val itemPosition = rv.getItemPosition(e)
                    tracker.start(itemPosition)
                }

                tracker.onDragStop()
                startX = DEFAULT_START_X
                startY = DEFAULT_START_Y
            }
        }
        return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
        if (!isActiveEditMode()) {
            return
        }
        when (e.action) {
            ACTION_UP -> tracker.onDragStop()
            ACTION_MOVE -> tracker.onDrag(rv, e)
        }
    }

    private fun isOnClickEvent(
        originalX: Float,
        originalY: Float,
        lastX: Float,
        lastY: Float
    ): Boolean {
        val isBoundXRange =
            lastX.roundToInt() in originalX.roundToInt() - CLICK_EVENT_THRESHOLD..originalX.roundToInt() + CLICK_EVENT_THRESHOLD
        val isBoundYRange =
            lastY.roundToInt() in originalY.roundToInt() - CLICK_EVENT_THRESHOLD..originalY.roundToInt() + CLICK_EVENT_THRESHOLD
        return isBoundXRange && isBoundYRange
    }

    private fun Adapter<*>.isEmpty(): Boolean = itemCount == 0

}