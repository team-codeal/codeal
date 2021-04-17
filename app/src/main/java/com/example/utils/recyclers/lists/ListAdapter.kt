package com.example.utils.recyclers.lists

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prototypefirebase.R
import com.example.prototypefirebase.ViewTaskDetailActivity
import com.example.prototypefirebase.codeal.CodealTask
import com.example.utils.recyclers.tasks.OnTaskClickListener
import com.example.utils.recyclers.tasks.TaskAdapter

class ListAdapter(
    private val lists: Map<String, List<String>>,
    private val context: Context
) : RecyclerView.Adapter<ListViewHolder>() {

    private val listNames = lists.keys.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        return ListViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.list_list_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return lists.size
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {

        val list = listNames[position]

        val listNameHolder : TextView = holder.itemView.findViewById(R.id.list_name)
        listNameHolder.text = list

        val tasksRecyclerView: RecyclerView = holder.itemView.findViewById(R.id.item_item_list)
        tasksRecyclerView.layoutManager = LinearLayoutManager(context)
        tasksRecyclerView.adapter = TaskAdapter(lists[list]!!.map { x -> CodealTask(x) {
            tasksRecyclerView.adapter?.notifyDataSetChanged()
        } }) {
            val intent = Intent(context, ViewTaskDetailActivity::class.java)
            intent.putExtra("TaskID", lists[list]?.get(it))
            startActivity(context, intent, null)
        }

    }

}
