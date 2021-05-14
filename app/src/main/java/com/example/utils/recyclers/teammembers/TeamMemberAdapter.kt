package com.example.utils.recyclers.teammembers

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.prototypefirebase.R

class TeamMemberAdapter(
    private val teamMemberIDS: MutableList<String>,
    private val context: Context
) : RecyclerView.Adapter<TeamMemberViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamMemberViewHolder {
        return TeamMemberViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.team_users_item, parent, false))
    }

    override fun getItemCount(): Int {
        return teamMemberIDS.size
    }

    override fun onViewDetachedFromWindow(holder: TeamMemberViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.freeListenerIfExists()
    }

    override fun onViewAttachedToWindow(holder: TeamMemberViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.startListenerIfExists()
    }

    override fun onBindViewHolder(holder: TeamMemberViewHolder, position: Int) {
        val teamMemberID = teamMemberIDS[position]
        holder.bindView(teamMemberID, context)
    }

}
