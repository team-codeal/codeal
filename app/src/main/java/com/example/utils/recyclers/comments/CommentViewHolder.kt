package com.example.utils.recyclers.comments

import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.example.prototypefirebase.R
import com.example.prototypefirebase.codeal.CodealComment
import com.example.prototypefirebase.codeal.CodealEntity
import com.example.prototypefirebase.codeal.CodealUser
import com.example.prototypefirebase.codeal.factories.CodealEmotionFactory
import com.example.prototypefirebase.codeal.factories.CodealUserFactory
import com.example.utils.recyclers.comments.CommentViewHolder.LikeState.*
import java.text.Format
import java.text.SimpleDateFormat

class CommentViewHolder(itemView: View)
    : RecyclerView.ViewHolder(itemView) {

    enum class LikeState {
        LIKED,
        NOT_LIKED
    }

    private var lastLikeState: LikeState = NOT_LIKED

    private val commentAuthorHolder: TextView = itemView.findViewById(R.id.comment_owner)
    private val commentDateHolder: TextView = itemView.findViewById(R.id.comment_date)
    private val commentContentHolder: TextView = itemView.findViewById(R.id.comment_content)
    private val commentLikeCountHolder: TextView = itemView.findViewById(R.id.comment_like_count)
    private val commentLikeButton: CheckBox = itemView.findViewById(R.id.comment_like_button)
    private val userAvatarHolder: ImageView = itemView.findViewById(R.id.comment_user_avatar)

    var listener: CodealEntity<CodealComment>.CodealListener? = null
    lateinit var context: Context

    var comment: CodealComment? = null

    fun freeListenerIfExists() {
        listener?.remove()
        listener = null
    }

    fun startListenerIfExists() {
        freeListenerIfExists()
        listener = comment?.addListener(::setView)
    }

    fun bindView(comment: CodealComment, context: Context) {
        this.comment = comment
        this.context = context
        setLikeButtonState(NOT_LIKED)
    }

    private fun like(comment: CodealComment, currentUser: CodealUser) {
        setLikeButtonState(LIKED)
        commentLikeCountHolder.text = (comment.emotions.size + 1).toString()
        commentLikeButton.setOnClickListener(null)
        comment.likeBy(currentUser.id)
        CodealUserFactory.get(comment.ownerID).sendReaction()
    }

    private fun unlike(comment: CodealComment, authorLike: CodealUser) {
        setLikeButtonState(NOT_LIKED)
        commentLikeCountHolder.text = (comment.emotions.size - 1).toString()
        commentLikeButton.setOnClickListener(null)
        comment.removeLikeBy(authorLike.id)
    }

    private fun setLikeButtonState(newLikeState: LikeState) {
        lastLikeState = newLikeState
        commentLikeButton.isChecked = newLikeState == LIKED
    }

    private fun setView(comment: CodealComment) {
        commentLikeButton.isChecked = lastLikeState == LIKED

        CodealUserFactory.get().addOnReady { currentUser ->
            commentLikeButton.setOnClickListener { like(comment, currentUser) }

            comment.emotions.forEach { emotionID ->
                CodealEmotionFactory.get(emotionID).addOnReady { emotion ->
                    if (emotion.ownerID == currentUser.id) {
                        setLikeButtonState(LIKED)
                        commentLikeButton.setOnClickListener { unlike(comment, currentUser) }
                    }
                }
            }
        }

        CodealUserFactory.get(comment.ownerID).addOnReady { user ->
            commentAuthorHolder.text = user.name
            if (user.photoURL == Uri.EMPTY) return@addOnReady
            Glide.with(context).load(user.photoURL)
                .apply(
                    RequestOptions()
                        .fitCenter()
                        .format(DecodeFormat.PREFER_ARGB_8888)
                        .override(Target.SIZE_ORIGINAL)
                )
                .into(userAvatarHolder)
        }

        val formatter: Format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        commentDateHolder.text = formatter.format(comment.date)
        commentContentHolder.text = comment.content
        commentLikeCountHolder.text = comment.emotions.size.toString()
    }

}
