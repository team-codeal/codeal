package com.example.utils.recyclers.tasks

import android.content.Context
import android.view.View
import android.widget.CheckBox
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.prototypefirebase.R
import com.example.prototypefirebase.codeal.CodealEntity
import com.example.prototypefirebase.codeal.CodealTask
import com.example.prototypefirebase.codeal.CodealUser
import com.example.prototypefirebase.codeal.factories.CodealEmotionFactory
import com.example.prototypefirebase.codeal.factories.CodealTaskFactory
import com.example.prototypefirebase.codeal.factories.CodealUserFactory
import java.util.*

class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    enum class LikeState {
        LIKED,
        NOT_LIKED
    }
    private var lastLikeState: LikeState = LikeState.NOT_LIKED

    private val taskNameHolder: TextView = itemView.findViewById(R.id.task_name)
    private val taskContentHolder: TextView = itemView.findViewById(R.id.task_content)
    private val taskCommentCountHolder: TextView = itemView.findViewById(R.id.comment_count)
    private val taskHolder: RelativeLayout = itemView.findViewById(R.id.task_holder)
    private val taskLikeCountHolder: TextView = itemView.findViewById(R.id.task_like_count)
    private val taskLikeButton: CheckBox = itemView.findViewById(R.id.task_like_button)

    var taskID: String? = null

    private var listenerRegistration: CodealEntity<CodealTask>.CodealListener? = null
    lateinit var context: Context
    var task: CodealTask? = null

    fun startListenerIfExists() {
        listenerRegistration = taskID?.let {
            CodealTaskFactory.get(it).addListener { task ->
                taskCommentCountHolder.text = task.commentsIDs.size.toString()
                taskContentHolder.text = task.content
                taskNameHolder.text = task.name

                setView(task)

                val deadline = task.deadline
                if (deadline != null) {
                    val maxDaysTillDeadline = 2
                    val now = Calendar.getInstance().apply {
                        add(Calendar.DATE, maxDaysTillDeadline)
                    }
                    val deadlineCalendar = Calendar.getInstance().apply { time = deadline }
                    val deadLineIsTodayOrBefore =
                        now.get(Calendar.DAY_OF_YEAR) >= deadlineCalendar.get(Calendar.DAY_OF_YEAR)
                                && now.get(Calendar.YEAR) >= deadlineCalendar.get(Calendar.YEAR)
                    if (deadLineIsTodayOrBefore) {
                        taskHolder.setBackgroundResource(R.drawable.deadline_task_item)
                    } else {
                        taskHolder.setBackgroundResource(0)
                    }
                } else {
                    taskHolder.setBackgroundResource(0)
                }
            }
        }
    }

    fun freeListenerIfExists() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    private fun setLikeButtonState(newLikeState: LikeState) {
        lastLikeState = newLikeState
        taskLikeButton.isChecked = newLikeState == LikeState.LIKED
    }

    private fun like(task: CodealTask, currentUser: CodealUser) {
        setLikeButtonState(LikeState.LIKED)
        taskLikeCountHolder.text = (task.emotions.size + 1).toString()
        taskLikeButton.setOnClickListener(null)
        task.likeBy(currentUser.id)
        CodealUserFactory.get(task.ownerID).sendReaction()
    }

    private fun unlike(task: CodealTask, authorLike: CodealUser) {
        setLikeButtonState(LikeState.NOT_LIKED)
        taskLikeCountHolder.text = (task.emotions.size - 1).toString()
        taskLikeButton.setOnClickListener(null)
        task.removeLikeBy(authorLike.id)
    }

    private fun setView(task: CodealTask) {
        taskLikeButton.isChecked = lastLikeState == LikeState.LIKED

        CodealUserFactory.get().addOnReady { currentUser ->
            taskLikeButton.setOnClickListener { like(task, currentUser) }

            task.emotions.forEach { emotionID ->
                CodealEmotionFactory.get(emotionID).addOnReady { emotion ->
                    if (emotion.ownerID == currentUser.id) {
                        setLikeButtonState(LikeState.LIKED)
                        taskLikeButton.setOnClickListener { unlike(task, currentUser) }
                    }
                }
            }
        }
        taskLikeCountHolder.text = task.emotions.size.toString()
    }
}