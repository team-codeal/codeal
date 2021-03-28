package com.example.utils.recyclers.teams

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.prototypefirebase.R
import com.example.prototypefirebase.ui.teams.Model
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

        val teamNameHolder: TextView = holder.itemView.findViewById(R.id.team_name)
        val teamDescriptionHolder: TextView = holder.itemView.findViewById(R.id.team_short_desc)

        teamNameHolder.text = team.name
        teamDescriptionHolder.text = team.des
        holder.itemView.setOnClickListener {
            onTeamClickListener.onTeamItemClicked(position)
        }
    }

}