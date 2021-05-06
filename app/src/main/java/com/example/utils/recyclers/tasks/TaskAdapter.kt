package com.example.utils.recyclers.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.prototypefirebase.codeal.CodealTask
import com.example.prototypefirebase.R
import com.example.prototypefirebase.codeal.factories.CodealTaskFactory

class TaskAdapter(
    private val taskIDs: MutableList<String>,
    private val onTaskClickListenerCallback: ((Int) -> Unit)
) : RecyclerView.Adapter<TaskViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        return TaskViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_list_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return taskIDs.size
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {

        val taskID = taskIDs[position]

        val taskNameHolder: TextView = holder.itemView.findViewById(R.id.task_name)
        val taskContentHolder: TextView = holder.itemView.findViewById(R.id.task_content)
        val taskCommentCountHolder: TextView = holder.itemView.findViewById(R.id.comment_count)

        CodealTaskFactory.get(taskID).addOnReady { task ->
            taskCommentCountHolder.text = task.commentsIDs.size.toString()
            taskContentHolder.text = task.content
            taskNameHolder.text = task.name
        }

        holder.itemView.setOnClickListener {
            onTaskClickListenerCallback.invoke(position)
        }
    }
}