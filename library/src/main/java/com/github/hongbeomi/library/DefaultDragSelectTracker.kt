package com.github.hongbeomi.library

import android.util.Log
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DefaultDragSelectTracker private constructor(
    private val receiver: DragSelectReceiver,
) : DragSelectTracker {

    companion object {
        fun create(
            receiver: DragSelectReceiver,
            config: (DefaultDragSelectTracker.() -> Unit)? = null,
        ): DragSelectTracker = DefaultDragSelectTracker(receiver).apply {
            config?.invoke(this)
        }

    }

    private val autoScroller = AutoScroller()

    var isDebug: Boolean = false

    override var hotspotOffsetTop: Int = 0
    override var hotspotOffsetBottom: Int = 0

    private var isDragSelectActive: Boolean = false
    private var lastDraggedIndex = -1
    private var initialSelection = Selection()
    private var minReached: Int = 0
    private var maxReached: Int = 0
    private var isInTopHotspot: Boolean = false
    private var isInBottomHotspot: Boolean = false

    override fun start(selectPosition: Int): Boolean {
        if (isDragSelectActive) {
            log("Drag select tracker is already active.")
            return false
        }
        reset()

        if (!receiver.isIndexSelectable(selectPosition)) {
            isDragSelectActive = false
            initialSelection = Selection(isSelected = false, index = -1)
            log("Index $selectPosition is not selectable.")
            return false
        }

        val isInitiallySelected = receiver.isSelected(selectPosition)
        receiver.setSelected(
            index = selectPosition,
            selected = !isInitiallySelected
        )
        isDragSelectActive = true
        initialSelection = Selection(
            isSelected = isInitiallySelected,
            index = selectPosition
        )
        lastDraggedIndex = selectPosition

        log("Drag selection initialized, starting at index $selectPosition.")
        return true
    }

    override fun isActive(): Boolean = isDragSelectActive

    fun configBound(view: RecyclerView) {
        autoScroller.setRecyclerView(view)
        log("RecyclerView height = ${view.measuredHeight}")
        log("Hotspot top bound = 0 to $hotspotOffsetTop")
        log("Hotspot bottom bound = ${view.measuredHeight - hotspotOffsetBottom} to ${view.measuredHeight}")
    }

    override fun onDrag(view: RecyclerView, event: MotionEvent) {
        val itemPosition = view.getItemPosition(event)
        val y = event.y

        when {
            y < hotspotOffsetTop -> {
                isInBottomHotspot = false
                if (!isInTopHotspot) {
                    isInTopHotspot = true
                    log("Now in TOP hotspot")
                    autoScroller.startAutoScroll()
                }
                val velocity = (hotspotOffsetTop.toFloat() - y).toInt() / 2
                autoScroller.setVelocity(velocity)
            }

            y >= (view.measuredHeight - hotspotOffsetBottom) -> {
                isInTopHotspot = false
                if (!isInBottomHotspot) {
                    isInBottomHotspot = true
                    log("Now in Bottom hotspot")
                    autoScroller.startAutoScroll()
                }
                val velocity = (y - (view.measuredHeight - hotspotOffsetBottom)).toInt() / 2
                autoScroller.setVelocity(velocity)
            }

            isInTopHotspot || isInBottomHotspot -> {
                log("Left the hotspot")
                autoScroller.stopAutoScroll()
                isInTopHotspot = false
                isInBottomHotspot = false
            }
        }

        // Drag selection logic
        handleDrag(itemPosition)
    }

    override fun handleDrag(itemPosition: Int) {
        if (itemPosition != RecyclerView.NO_POSITION && lastDraggedIndex != itemPosition) {
            lastDraggedIndex = itemPosition

            if (minReached == -1 || lastDraggedIndex < minReached) {
                minReached = lastDraggedIndex
            }

            if (maxReached == -1 || lastDraggedIndex > maxReached) {
                maxReached = lastDraggedIndex
            }

            selectRange(
                isInitiallySelected = initialSelection.isSelected,
                from = initialSelection.index,
                to = lastDraggedIndex,
                min = minReached,
                max = maxReached
            )
            if (initialSelection.index == lastDraggedIndex) {
                minReached = lastDraggedIndex
                maxReached = lastDraggedIndex
            }
        }
    }

    override fun onDragStop() {
        isDragSelectActive = false
        isInTopHotspot = false
        isInBottomHotspot = false
        autoScroller.stopAutoScroll()
    }

    private fun selectRange(
        isInitiallySelected: Boolean,
        from: Int,
        to: Int,
        min: Int,
        max: Int
    ) = with(receiver) {
        if (from == to) {
            // 손가락이 처음 아이템으로 돌아올 경우 그 밖의 모든 것을 선택 해제
            (min..max)
                .filter { it != from }
                .forEach { setSelected(it, isInitiallySelected) }
            return
        }

        // Left to Right / Right to Left 드래그
        val range = if (to < from) {
            to..from
        } else {
            from..to
        }
        // 첫 아이템 부터 다음 아이템 선택
        range.forEach { setSelected(it, isInitiallySelected.not()) }

        if (to < from) {
            if (min > -1 && min < to) {
                // 다시 Right to Left 드래그 시 처리
                (min until to).forEach {
                    setSelected(it, isInitiallySelected)
                }
            }
            if (max > -1) {
                (from + 1..max).forEach {
                    setSelected(it, isInitiallySelected)
                }
            }
        } else {
            if (max > -1 && max > to) {
                // 다시 Left to Right 드래그 시 처리
                (to + 1..max).forEach {
                    setSelected(it, isInitiallySelected)
                }
            }
            if (min > -1) {
                (min until from).forEach {
                    setSelected(it, isInitiallySelected)
                }
            }
        }
    }

    override fun reset() {
        isDragSelectActive = false
        lastDraggedIndex = -1
        minReached = -1
        maxReached = -1
        isInTopHotspot = false
        isInBottomHotspot = false
        autoScroller.stopAutoScroll()
        initialSelection = Selection(isSelected = false, index = -1)
    }

    private fun log(msg: String) {
        if (isDebug) {
            Log.d("DefaultDragSelectTracker", msg)
        }
    }

    private inner class AutoScroller {

        private val delay = 25L

        private var recyclerView: RecyclerView? = null
        private var velocity: Int = 0
        private var autoScrollJob: Job? = null

        fun startAutoScroll() {
            stopAutoScroll()
            autoScrollJob = CoroutineScope(Dispatchers.Main).launch {
                while (isActive) {
                    if (isInTopHotspot) {
                        recyclerView?.scrollBy(0, -velocity)
                    } else if (isInBottomHotspot) {
                        recyclerView?.scrollBy(0, velocity)
                    }
                    delay(delay)
                }
            }
        }

        fun stopAutoScroll() {
            autoScrollJob?.cancel()
            autoScrollJob = null
        }

        fun setRecyclerView(view: RecyclerView) {
            recyclerView = view
        }

        fun setVelocity(value: Int) {
            log("Auto scroll velocity = $value")
            velocity = value
        }

    }

    private data class Selection(
        val isSelected: Boolean = false,
        val index: Int = 0,
    )

}