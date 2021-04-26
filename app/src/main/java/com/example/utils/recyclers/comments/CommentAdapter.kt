package com.example.utils.recyclers.comments

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.prototypefirebase.R
import com.example.prototypefirebase.codeal.CodealComment
import com.example.prototypefirebase.codeal.CodealUser
import java.text.DateFormat.getDateInstance
import java.text.Format
import java.text.SimpleDateFormat

class CommentAdapter(
    private val comments: MutableList<CodealComment>
) : RecyclerView.Adapter<CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        return CommentViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.comment_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return comments.size
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {

        val comment = comments[position]

        val commentAuthorHolder: TextView = holder.itemView.findViewById(R.id.comment_owner)
        val commentDateHolder: TextView = holder.itemView.findViewById(R.id.comment_date)
        val commentContentHolder: TextView = holder.itemView.findViewById(R.id.comment_content)

        val formatter: Format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        if (comment.ownerID != "") {
            CodealUser(comment.ownerID) {
                commentAuthorHolder.text = it.name
            }
        }
        commentDateHolder.text = formatter.format(comment.date)
        commentContentHolder.text = comment.content
    }

}
