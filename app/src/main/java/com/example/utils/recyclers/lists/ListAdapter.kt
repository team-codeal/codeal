package com.example.utils.recyclers.lists

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.prototypefirebase.R
import com.example.prototypefirebase.ViewTaskDetailActivity
import com.example.prototypefirebase.codeal.CodealTask
import com.example.prototypefirebase.codeal.factories.CodealTaskFactory
import com.example.utils.recyclers.tasks.TaskAdapter

class ListAdapter(
    private val listNames: MutableList<String>,
    private val listNameToTasksList: MutableMap<String, MutableList<String>>,
    private val context: Context
) : RecyclerView.Adapter<ListViewHolder>() {

    private var taskAdapters: MutableMap<String, TaskAdapter> = HashMap()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        return ListViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_list_item, parent, false),
            context
        )
    }

    override fun getItemCount(): Int {
        return listNames.size
    }

    override fun onBindViewHolder(holder: ListViewHolder, listPosition: Int) {

        val listName = listNames[listPosition]
        val list = listNameToTasksList[listName]

        holder.label = listName

        val onTaskClickListener = { taskPosition: Int ->
            val intent = Intent(context, ViewTaskDetailActivity::class.java)
            intent.putExtra("TaskID", list?.get(taskPosition))
            startActivity(context, intent, null)
        }

        val onTaskSwiped = { taskPosition: Int, direction: TaskAdapter.SwipeDirection ->
            when(direction) {
                TaskAdapter.SwipeDirection.RIGHT -> {
                    if (listPosition != listNames.size - 1) {
                        val newListName = listNames[listPosition + 1]
                        val taskID = list?.get(taskPosition)
                        taskID?.let {
                            CodealTaskFactory.get(it).addOnReady { task ->
                                task.change(listName = newListName)
                            }
                        }
                    }
                }
                TaskAdapter.SwipeDirection.LEFT -> {
                    if (listPosition != 0) {
                        val newListName = listNames[listPosition - 1]
                        val taskID = list?.get(taskPosition)
                        taskID?.let {
                            CodealTaskFactory.get(it).addOnReady { task ->
                                task.change(listName = newListName)
                            }
                        }
                    }
                }
            }
        }

        holder.tasksRecyclerView.adapter = TaskAdapter(listNameToTasksList[listName]!!,
            onTaskClickListener, onTaskSwiped)

        taskAdapters[listName] = holder.tasksRecyclerView.adapter as TaskAdapter
    }

    enum class TaskChangingCommitment {
        TASK_ADDED,
        TASK_DELETED
    }

    data class TaskChangedMessage(val commitment: TaskChangingCommitment, val idx: Int)

    override fun onBindViewHolder(
        holder: ListViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {

        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        }

        for (payload in payloads) {
            if (payload is TaskChangedMessage) {
                val listName = listNames[position]
                val taskAdapter = taskAdapters[listName]
                val taskPosition = payload.idx
                when (payload.commitment) {
                    TaskChangingCommitment.TASK_ADDED ->
                        taskAdapter?.notifyItemInserted(taskPosition)
                    TaskChangingCommitment.TASK_DELETED ->
                        taskAdapter?.notifyItemRemoved(taskPosition)
                }
            }
        }
    }

}
