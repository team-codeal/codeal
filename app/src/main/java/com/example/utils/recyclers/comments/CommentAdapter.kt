package com.example.utils.recyclers.comments

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.prototypefirebase.R
import com.example.prototypefirebase.codeal.CodealComment

class CommentAdapter(
    private val comments: MutableList<CodealComment>,
    private val context: Context
) : RecyclerView.Adapter<CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        return CommentViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.comment_item, parent, false))
    }

    override fun getItemCount(): Int {
        return comments.size
    }

    override fun onViewDetachedFromWindow(holder: CommentViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.freeListenerIfExists()
    }

    override fun onViewAttachedToWindow(holder: CommentViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.startListenerIfExists()
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.bindView(comment, context)
    }

}
