package com.example.utils.recyclers.tasks

import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.example.prototypefirebase.R

class TaskAdapter(
    private val taskIDs: MutableList<String>,
    private val onTaskClickedCallback: (Int) -> Unit,
    private val saveChangesCallback: (() -> Unit) = { },
    private val onTaskSwipedCallback: ((itemPosition: Int, direction: SwipeDirection) -> Unit)? =
        null,
    private val enabledSwipeDirections: Set<SwipeDirection> = emptySet()
) : RecyclerView.Adapter<TaskViewHolder>() {

    enum class SwipeDirection {
        RIGHT,
        LEFT
    }

    private lateinit var recyclerView: RecyclerView
    private val itemTouchHelper = ItemTouchHelper(TaskItemTouchHelperCallback())

    inner class TaskItemTouchHelperCallback: ItemTouchHelper.Callback() {

        private var haveToDeleteView = false

        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            val swipeToHelperSwipe: Map<SwipeDirection, Int> = mapOf(
                SwipeDirection.LEFT to LEFT,
                SwipeDirection.RIGHT to RIGHT
            )

            var dragFlags = UP or DOWN
            enabledSwipeDirections.map { x -> swipeToHelperSwipe[x]!! }.forEach { direction ->
                dragFlags = dragFlags or direction
            }

            return makeMovementFlags(dragFlags, 0)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            changeTaskIDsPositions(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
            return true
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            if (!isCurrentlyActive) {
                if (haveToDeleteView) {
                    viewHolder.itemView.visibility = INVISIBLE
                }
                return
            }
            val viewWidth = viewHolder.itemView.width
            val threshold = viewWidth / 2
            val pathLength = kotlin.math.abs(dX)
            if (pathLength > threshold) {
                val direction: SwipeDirection = if (dX > 0)
                    SwipeDirection.RIGHT else SwipeDirection.LEFT
                haveToDeleteView = true
                onTaskSwipedCallback?.invoke(viewHolder.bindingAdapterPosition, direction)
            }
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            if (haveToDeleteView) {
                haveToDeleteView = false
                viewHolder.itemView.visibility = GONE
            } else {
                saveChangesCallback.invoke()
            }
        }

        override fun isItemViewSwipeEnabled(): Boolean = false
        override fun isLongPressDragEnabled(): Boolean = true
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            throw UnsupportedOperationException("Tasks can't be swiped at the moment")
        }
    }

    private fun changeTaskIDsPositions(holderPosition: Int, targetPosition: Int) {
        if (holderPosition == NO_POSITION) return
        val taskID = taskIDs.removeAt(holderPosition)
        taskIDs.add(targetPosition, taskID)
        notifyItemMoved(holderPosition, targetPosition)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    fun attachItemTouchHelper() {
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    fun detachItemTouchHelper() {
        itemTouchHelper.attachToRecyclerView(null)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        return TaskViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_list_item,
                parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return taskIDs.size
    }

    override fun onViewDetachedFromWindow(holder: TaskViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.freeListenerIfExists()
        holder.itemView.setOnClickListener(null)
    }

    override fun onViewAttachedToWindow(holder: TaskViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.startListenerIfExists()
        holder.itemView.setOnClickListener {
            onTaskClickedCallback.invoke(holder.bindingAdapterPosition)
        }
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {

        val taskID = taskIDs[position]

        holder.taskID = taskID

    }
}