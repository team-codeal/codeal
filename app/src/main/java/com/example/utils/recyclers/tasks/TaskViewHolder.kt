package com.example.utils.recyclers.tasks

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.prototypefirebase.R
import com.example.prototypefirebase.codeal.CodealEntity
import com.example.prototypefirebase.codeal.CodealTask
import com.example.prototypefirebase.codeal.factories.CodealTaskFactory
import java.util.concurrent.ConcurrentLinkedQueue

class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val taskNameHolder: TextView = itemView.findViewById(R.id.task_name)
    private val taskContentHolder: TextView = itemView.findViewById(R.id.task_content)
    private val taskCommentCountHolder: TextView = itemView.findViewById(R.id.comment_count)

    var taskID: String? = null

    private var listenerRegistration: CodealEntity<CodealTask>.CodealListener? = null

    fun startListenerIfExists() {
        listenerRegistration = taskID?.let {
            CodealTaskFactory.get(it).addListener { task ->
                taskCommentCountHolder.text = task.commentsIDs.size.toString()
                taskContentHolder.text = task.content
                taskNameHolder.text = task.name
            }
        }
    }

    fun freeListenerIfExists() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }
}