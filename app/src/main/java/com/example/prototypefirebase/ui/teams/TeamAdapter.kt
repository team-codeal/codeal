package com.example.prototypefirebase.ui.teams

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.prototypefirebase.R
import kotlinx.android.synthetic.main.team_row.view.*
import java.util.ArrayList

class TeamAdapter(
    private val teams: ArrayList<Model>,
    private val onTeamClickListener: OnTeamClickListener
): RecyclerView.Adapter<TeamViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder {
        return TeamViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.team_row, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return teams.size
    }

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        val team = teams[position]

        holder.itemView.team_name.text = team.name
        holder.itemView.team_short_desc.text = team.des
        holder.itemView.setOnClickListener {
            onTeamClickListener.onTeamItemClicked(position)
        }
    }

    fun addNewItem(itemsNew: ArrayList<Model>) {
        teams.clear()
        teams.addAll(itemsNew)
        notifyDataSetChanged()
    }

}