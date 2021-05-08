package com.example.utils.recyclers.lists

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prototypefirebase.R

class ListViewHolder(itemView: View, context: Context) : RecyclerView.ViewHolder(itemView) {

    private val listNameHolder : TextView = itemView.findViewById(R.id.list_name)
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
