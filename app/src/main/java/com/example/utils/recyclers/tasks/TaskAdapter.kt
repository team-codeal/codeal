package com.example.utils.recyclers.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.prototypefirebase.codeal.CodealTask
import com.example.prototypefirebase.R
import java.util.ArrayList

class TaskAdapter(
    private val tasks: ArrayList<CodealTask>,
    private val onTaskClickListener: OnTaskClickListener
) : RecyclerView.Adapter<TaskViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        return TaskViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_list_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {

        val task = tasks[position]

        val taskNameHolder: TextView = holder.itemView.findViewById(R.id.task_name)
        taskNameHolder.text = task.name

        holder.itemView.setOnClickListener {
            onTaskClickListener.onTaskItemClicked(position)
        }
    }
}