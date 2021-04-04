package com.example.prototypefirebase.ui.feed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.prototypefirebase.R
import com.example.prototypefirebase.codeal.CodealTask
import com.example.utils.recyclers.tasks.OnTaskClickListener
import kotlinx.android.synthetic.main.layout_list_item.view.*
import java.util.ArrayList

class FeedTasksAdapter(
    private val tasks: ArrayList<CodealTask>,
    private val onTaskClickListener: OnTaskClickListener
) : RecyclerView.Adapter<FeedTasksViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedTasksViewHolder {
        return FeedTasksViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.layout_list_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    override fun onBindViewHolder(holder: FeedTasksViewHolder, position: Int) {
        val task = tasks[position]

        holder.itemView.task_name.text = task.name

        holder.itemView.setOnClickListener {
            onTaskClickListener.onTaskItemClicked(position)
        }
    }

}