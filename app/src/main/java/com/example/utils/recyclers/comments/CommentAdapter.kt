package com.example.utils.recyclers.comments

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.prototypefirebase.R
import com.example.prototypefirebase.codeal.CodealComment
import com.example.prototypefirebase.codeal.CodealEmotion
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

        val commentLikedInfoHolder: TextView = holder.itemView.findViewById(R.id.comment_liked_info)
        val commentLikeCountHolder: TextView = holder.itemView.findViewById(R.id.comment_like_count)
        val commentLikeButton: TextView = holder.itemView.findViewById(R.id.comment_like_button)

        val formatter: Format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        commentLikedInfoHolder.text = "No"

        CodealUser { currentUser ->
            commentLikeButton.setOnClickListener { _ ->
                // TODO explicitly typing the _ argument is disgusting
                //  this looks like an architecture issue
                comment.likeBy(currentUser.id) { _: CodealComment ->
                    notifyItemChanged(position)
                }
            }

            comment.emotions.forEach { emotionID ->
                CodealEmotion(emotionID) { emotion ->
                    if (emotion.ownerID == currentUser.id) {
                        commentLikedInfoHolder.text = "Yes"
                        commentLikeButton.setOnClickListener { _ ->
                            comment.removeLikeBy(currentUser.id) { _: CodealComment ->
                                notifyItemChanged(position)
                            }
                        }
                    }
                }
            }
        }

        if (comment.ownerID != "") {
            CodealUser(comment.ownerID) { user ->
                commentAuthorHolder.text = user.name
            }
        }
        commentDateHolder.text = formatter.format(comment.date)
        commentContentHolder.text = comment.content

        commentLikeCountHolder.text = comment.emotions.size.toString()

    }

}
