package com.example.utils.recyclers.lists

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.prototypefirebase.R
import com.example.prototypefirebase.ViewTaskDetailActivity
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

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {

        val list = listNames[position]

        holder.label = list

        holder.tasksRecyclerView.adapter = TaskAdapter(listNameToTasksList[list]!!) {
            val intent = Intent(context, ViewTaskDetailActivity::class.java)
            intent.putExtra("TaskID", listNameToTasksList[list]
                ?.get(it))
            startActivity(context, intent, null)
        }
        taskAdapters[list] = holder.tasksRecyclerView.adapter as TaskAdapter

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
