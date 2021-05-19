package com.example.utils.recyclers.lists

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prototypefirebase.R

class ListViewHolder(itemView: View, context: Context) : RecyclerView.ViewHolder(itemView) {

    var addTaskListener: (View) -> Unit = { }
        set(value) {
            field = value
            addTaskButton.setOnClickListener { view -> field(view) }
        }
    
    private val listNameHolder : TextView = itemView.findViewById(R.id.list_name)
    private val addTaskButton: Button = itemView.findViewById(R.id.addTaskButton)
    val tasksRecyclerView: RecyclerView = itemView.findViewById(R.id.item_item_list)

    var label = ""
        set(value) {
            field = value
            listNameHolder.text = value
        }

    init {
        tasksRecyclerView.layoutManager = LinearLayoutManager(context)
    }

}
