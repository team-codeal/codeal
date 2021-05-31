package com.example.utils.recyclers.tasks

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.View.MeasureSpec.AT_MOST
import android.view.WindowManager
import androidx.recyclerview.widget.RecyclerView

class TasksRecyclerView : RecyclerView {

    constructor(context: Context) :
            super(context)


    constructor(context: Context, attrs: AttributeSet?) :
            super(context, attrs)


    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val newHeightSpec = MeasureSpec.makeMeasureSpec((getScreenWidth() * 1.5).toInt(), AT_MOST)
        super.onMeasure(widthSpec, newHeightSpec)
    }

    private fun getScreenWidth(): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size.x
    }

}