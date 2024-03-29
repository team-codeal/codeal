package com.example.utils.recyclers.lists

import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.example.prototypefirebase.R
import com.example.prototypefirebase.ViewTaskDetailActivity
import com.example.prototypefirebase.codeal.suppliers.CodealTaskSupplier
import com.example.utils.recyclers.tasks.TaskAdapter


class ListAdapter(
    private val listNames: MutableList<String>,
    private val listNameToTasksList: MutableMap<String, MutableList<String>>,
    private val addTaskCallback: (view: View, taskList: String) -> Unit,
    private val saveChangesCallback: () -> Unit,
    private val context: Context
) : RecyclerView.Adapter<ListViewHolder>() {

    private var taskAdapters: MutableMap<String, TaskAdapter> = HashMap()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val listViewHolder = ListViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_list_item, parent, false),
            context
        )
        val itemView = listViewHolder.itemView

        itemView.layoutParams.height = (getScreenWidth() * 1.8).toInt()
        return listViewHolder
    }

    private fun getScreenWidth(): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size.x
    }

    override fun getItemCount(): Int {
        return listNames.size
    }

    override fun onViewAttachedToWindow(holder: ListViewHolder) {
        super.onViewAttachedToWindow(holder)
        val adapter: TaskAdapter? = holder.tasksRecyclerView.adapter as? TaskAdapter
        adapter?.attachItemTouchHelper()
    }

    override fun onViewDetachedFromWindow(holder: ListViewHolder) {
        super.onViewDetachedFromWindow(holder)
        val adapter: TaskAdapter? = holder.tasksRecyclerView.adapter as? TaskAdapter
        adapter?.detachItemTouchHelper()
    }

    override fun onBindViewHolder(holder: ListViewHolder, listPosition: Int) {

        val listName = listNames[listPosition]
        val list = listNameToTasksList[listName]

        holder.label = listName
        holder.addTaskListener = { view -> addTaskCallback.invoke(view, listName) }

        val onTaskClickListener = { taskPosition: Int ->
            val intent = Intent(context, ViewTaskDetailActivity::class.java)
            intent.putExtra("TaskID", list?.get(taskPosition))
            startActivity(context, intent, null)
        }

        val onTaskSwiped = { taskPosition: Int, direction: TaskAdapter.SwipeDirection ->
            if (taskPosition != NO_POSITION) {
                when(direction) {
                    TaskAdapter.SwipeDirection.RIGHT -> {
                        if (listPosition != listNames.size - 1) {
                            val newListName = listNames[listPosition + 1]
                            val taskID = list?.get(taskPosition)
                            taskID?.let {
                                CodealTaskSupplier.get(it).addOnReady { task ->
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
                                CodealTaskSupplier.get(it).addOnReady { task ->
                                    task.change(listName = newListName)
                                }
                            }
                        }
                    }
                }
            }
        }

        val swipeDirections: MutableSet<TaskAdapter.SwipeDirection> = mutableSetOf()
        swipeDirections.apply {
            if (listPosition != 0) add(TaskAdapter.SwipeDirection.LEFT)
            if (listPosition != listNames.size - 1) add(TaskAdapter.SwipeDirection.RIGHT)
        }

        holder.tasksRecyclerView.adapter = TaskAdapter(listNameToTasksList[listName]!!,
            onTaskClickListener, saveChangesCallback, onTaskSwiped, swipeDirections)

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
