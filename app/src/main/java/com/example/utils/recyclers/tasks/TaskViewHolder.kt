package com.example.utils.recyclers.tasks

import android.icu.text.DateFormat.DAY
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.prototypefirebase.R
import com.example.prototypefirebase.codeal.CodealEntity
import com.example.prototypefirebase.codeal.CodealTask
import com.example.prototypefirebase.codeal.factories.CodealTaskFactory
import java.util.*

class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val taskNameHolder: TextView = itemView.findViewById(R.id.task_name)
    private val taskContentHolder: TextView = itemView.findViewById(R.id.task_content)
    private val taskCommentCountHolder: TextView = itemView.findViewById(R.id.comment_count)
    private val taskHolder: RelativeLayout = itemView.findViewById(R.id.task_holder)

    var taskID: String? = null

    private var listenerRegistration: CodealEntity<CodealTask>.CodealListener? = null

    fun startListenerIfExists() {
        listenerRegistration = taskID?.let {
            CodealTaskFactory.get(it).addListener { task ->
                taskCommentCountHolder.text = task.commentsIDs.size.toString()
                taskContentHolder.text = task.content
                taskNameHolder.text = task.name

                val deadline = task.deadline
                if (deadline != null) {
                    val maxDaysTillDeadline = 2
                    val now = Calendar.getInstance().apply {
                        add(Calendar.DATE, maxDaysTillDeadline)
                    }
                    val deadlineCalendar = Calendar.getInstance().apply { time = deadline }
                    val deadLineIsTodayOrBefore =
                        now.get(Calendar.DAY_OF_YEAR) >= deadlineCalendar.get(Calendar.DAY_OF_YEAR)
                                && now.get(Calendar.YEAR) >= deadlineCalendar.get(Calendar.YEAR)
                    if (deadLineIsTodayOrBefore) {
                        taskHolder.setBackgroundResource(R.drawable.deadline_task_item)
                    } else {
                        taskHolder.setBackgroundResource(0)
                    }
                } else {
                    taskHolder.setBackgroundResource(0)
                }
            }
        }
    }

    fun freeListenerIfExists() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }
}