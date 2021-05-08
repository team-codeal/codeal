package com.example.utils.recyclers.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.prototypefirebase.R

class TaskAdapter(
    private val taskIDs: MutableList<String>,
    private val onTaskClickListenerCallback: ((Int) -> Unit)
) : RecyclerView.Adapter<TaskViewHolder>() {
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