package com.github.hongbeomi.recyclerviewdragselector

import android.content.Context
import android.util.AttributeSet


class RectangleTextView(
    context: Context,
    attrs: AttributeSet?
) : androidx.appcompat.widget.AppCompatTextView(context, attrs) {

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measuredWidth, (measuredWidth))
    }
}