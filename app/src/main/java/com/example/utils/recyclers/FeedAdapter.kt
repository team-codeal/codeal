package com.example.utils.recyclers

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.prototypefirebase.R
import com.example.prototypefirebase.codeal.CodealComment
import com.example.utils.recyclers.FeedAdapter.FeedContentType.*
import com.example.utils.recyclers.comments.CommentViewHolder
import com.example.utils.recyclers.tasks.TaskViewHolder
import com.example.utils.recyclers.FeedAdapter.FeedContentType.Companion as FeedContentType1

class FeedAdapter(
    private val data: MutableList<Any>,
    private val context: Context,
    private val onTaskClickListenerCallback: ((Int) -> Unit)
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class FeedContentType(val value: Int) {
        COMMENT(0),
        TASK(1);

        companion object {
            fun fromInt(value: Int) = values().first { it.value == value }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (FeedContentType1.fromInt(viewType)) {
            COMMENT -> CommentViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.comment_item, parent, false))
            TASK -> TaskViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.layout_list_item,
                    parent, false
                )
            )
        }

    }

    override fun getItemViewType(position: Int): Int {
        val item = data[position]

        if (item is String) {
            return TASK.value
        }

        if (item is CodealComment) {
            return COMMENT.value
        }

        throw IllegalArgumentException("Unknown content type")
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        when (FeedContentType.fromInt(holder.itemViewType)) {
            TASK -> {
                val taskViewHolder = holder as TaskViewHolder
                taskViewHolder.freeListenerIfExists()
                taskViewHolder.itemView.setOnClickListener(null)
            }
            COMMENT -> (holder as CommentViewHolder).freeListenerIfExists()
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        when (FeedContentType.fromInt(holder.itemViewType)) {
            TASK -> {
                val taskViewHolder = holder as TaskViewHolder
                taskViewHolder.startListenerIfExists()
                taskViewHolder.itemView.setOnClickListener {
                    onTaskClickListenerCallback.invoke(taskViewHolder.bindingAdapterPosition)
                }
            }
            COMMENT -> (holder as CommentViewHolder).startListenerIfExists()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (FeedContentType.fromInt(holder.itemViewType)) {
            TASK -> {
                val taskViewHolder = holder as TaskViewHolder
                val taskID = data[position] as String
                taskViewHolder.taskID = taskID
            }
            COMMENT -> {
                val commentViewHolder = holder as CommentViewHolder
                val comment = data[position] as CodealComment
                commentViewHolder.comment = comment
                commentViewHolder.context = context
            }
        }
    }
}
