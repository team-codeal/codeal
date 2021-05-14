package com.example.utils.recyclers.teams

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.prototypefirebase.R

class TeamViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    fun bindView(name: String, des: String) {
        teamNameHolder.text = name
        teamDescriptionHolder.text = des
    }

    val teamNameHolder: TextView = itemView.findViewById(R.id.team_name)
    val teamDescriptionHolder: TextView = itemView.findViewById(R.id.team_short_desc)

}