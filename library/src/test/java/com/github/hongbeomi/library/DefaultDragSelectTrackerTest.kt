package com.github.hongbeomi.library

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.math.max
import kotlin.math.min


@ExtendWith(InstantTaskExecutorExtension::class)
internal class DefaultDragSelectTrackerTest {

    companion object {
        @JvmStatic
        fun provideOneWayDragIndexList() = listOf(
            listOf(0, 1, 2, 3, 4), // actual [0, 1, 2, 3, 4]
            listOf(4, 3, 2, 1, 0) // actual [4, 3, 2, 1, 0]
        )

        @JvmStatic
        fun provideBacktrackDragIndexList() = listOf(
            listOf(0, 1, 2, 3, 4, 3), // actual [0, 1, 2, 3]
            listOf(0, 1, 2, 3, 4, 3, 2, 1, 0), // actual [0]
            listOf(4, 3, 2, 1, 0, 1, 2), // actual [4, 3, 2]
            listOf(4, 3, 2, 1, 0, 1, 2, 3, 4) // actual [4]
        )

        @JvmStatic
        fun provideBacktrackOverDragIndexList() = listOf(
            listOf(3, 4, 5, 1), // actual [1, 2, 3]
            listOf(2, 1, 5) // actual [2, 3, 4, 5]
        )
    }

    private var selectionList = selectionListOf()

    private val receiver: DragSelectReceiver = object : DragSelectReceiver {
        override fun getItemCount(): Int = selectionList.size

        override fun setSelected(index: Int, selected: Boolean) {
            selectionList = selectionList.mapIndexed { position, item ->
                if (position == index) {
                    item.copy(isSelected = selected)
                } else {
                    item
                }
            }
        }

        override fun isSelected(index: Int): Boolean = selectionList[index].isSelected

        override fun isIndexSelectable(index: Int): Boolean = true
    }
    private val tracker: DragSelectTracker = DefaultDragSelectTracker.create(receiver)

    @AfterEach
    fun tearDown() {
        selectionList = selectionListOf()
    }

    @ParameterizedTest
    @MethodSource("provideOneWayDragIndexList")
    fun `선택 가능한 리스트가 주어지고_한 방향으로 드래그 할 때_올바르게 리스트가 선택되어 있는지 검증`(dragIndexList: List<Int>) {
        tracker.start(dragIndexList.first())

        dragIndexList.forEach {
            tracker.handleDrag(it)
        }

        assertSelectionList(dragIndexList)
    }

    @ParameterizedTest
    @MethodSource("provideBacktrackDragIndexList")
    fun `선택 가능한 리스트가 주어지고_한 방향으로 드래그 했다가 반대 방향으로 드래그할 때_올바르게 리스트가 선택되어 있는지 검증`(dragIndexList: List<Int>) {
        tracker.start(dragIndexList.first())

        dragIndexList.forEach {
            tracker.handleDrag(it)
        }

        val last = dragIndexList.last()
        val actualIndexList = dragIndexList.takeWhile { it != last } + last

        assertSelectionList(actualIndexList)
    }

    @ParameterizedTest
    @MethodSource("provideBacktrackOverDragIndexList")
    fun `선택 가능한 리스트가 주어지고_한 방향으로 드래그 했다가 처음 선택한 부분을 넘어선 곳을 드래그할 때_올바르게 리스트가 선택되어 있는지 검증`(dragIndexList: List<Int>) {
        val first = dragIndexList.first()
        tracker.start(first)

        dragIndexList.forEach {
            tracker.handleDrag(it)
        }

        val last = dragIndexList.last()
        val start = min(first, last)
        val end = max(first, last)
        val actualList = (start..end).toList()

        assertSelectionList(actualList)
    }

    private data class Selection(val isSelected: Boolean = false)

    private fun selectionListOf(): List<Selection> = (1..10).map { Selection() }

    private fun assertSelectionList(actualSelectIndexList: List<Int>) {
        selectionList.forEachIndexed { index, selection ->
            assertEquals(selection.isSelected, actualSelectIndexList.contains(index))
        }
    }

}