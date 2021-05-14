package com.example.utils.recyclers.teammembers

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.example.prototypefirebase.R
import com.example.prototypefirebase.codeal.CodealEntity
import com.example.prototypefirebase.codeal.CodealUser
import com.example.prototypefirebase.codeal.factories.CodealUserFactory

class TeamMemberViewHolder(itemView: View)
    : RecyclerView.ViewHolder(itemView) {

    private val teamMemberNameHolder: TextView = itemView.findViewById(R.id.team_member_name)
    private val teamMemberStatusHolder: TextView = itemView.findViewById(R.id.team_member_status)
    private val teamMemberAvatar: ImageView = itemView.findViewById(R.id.team_member_avatar)

    var listener: CodealEntity<CodealUser>.CodealListener? = null

    var teamMemberID: String? = null
    var context: Context? = null

    fun freeListenerIfExists() {
        listener?.remove()
        listener = null
    }

    fun startListenerIfExists() {
        freeListenerIfExists()
        listener = teamMemberID?.let { CodealUserFactory.get(it).addListener(::setView)}
    }

    fun bindView(teamMemberID: String, context: Context) {
        this.teamMemberID = teamMemberID
        this.context = context
    }

    private fun setView(teamMember: CodealUser) {
        teamMemberNameHolder.text = teamMember.name
        teamMemberStatusHolder.text = teamMember.status
        context?.let {
            Glide.with(it).load(teamMember.photoURL)
                .apply(
                    RequestOptions()
                        .fitCenter()
                        .format(DecodeFormat.PREFER_ARGB_8888)
                        .override(Target.SIZE_ORIGINAL)
                )
                .into(teamMemberAvatar)
        }
    }

}
