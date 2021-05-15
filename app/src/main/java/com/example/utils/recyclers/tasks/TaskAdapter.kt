package com.example.utils.recyclers.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import com.example.prototypefirebase.R

class TaskAdapter(
    private val taskIDs: MutableList<String>,
    private val onTaskClickListenerCallback: ((Int) -> Unit)
) : RecyclerView.Adapter<TaskViewHolder>() {

    inner class TaskItemTouchHelperCallback: ItemTouchHelper.Callback() {
        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            return makeMovementFlags(UP or DOWN, LEFT or RIGHT)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            changeTaskIDsPositions(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            TODO("Implement moving tasks between lists")
        }
    }

    private fun changeTaskIDsPositions(holderPosition: Int, targetPosition: Int) {
        val taskID = taskIDs.removeAt(holderPosition)
        taskIDs.add(targetPosition, taskID)
        notifyItemMoved(holderPosition, targetPosition)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        (ItemTouchHelper(TaskItemTouchHelperCallback())).attachToRecyclerView(recyclerView)
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
            onTaskClickListenerCallback.invoke(holder.bindingAdapterPosition)
        }
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {

        val taskID = taskIDs[position]

        holder.taskID = taskID

    }
}