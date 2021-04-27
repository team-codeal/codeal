package com.example.prototypefirebase

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prototypefirebase.codeal.CodealComment
import com.example.prototypefirebase.codeal.CodealTask
import com.example.prototypefirebase.codeal.CodealUser
import com.example.utils.recyclers.comments.CommentAdapter
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.Comparator


class ViewTaskDetailActivity : AppCompatActivity() {

    private var comments: MutableList<CodealComment> = mutableListOf()

    private lateinit var commentsRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)
        supportActionBar?.hide();

        val taskID = intent.getStringExtra("TaskID")!!

        val updateTaskButton: Button = findViewById(R.id.updateTask)
        val deleteTaskButton: Button = findViewById(R.id.deleteTask)
        val taskNameHolder: TextView = findViewById(R.id.Name_task)
        val taskTextHolder: TextView = findViewById(R.id.Text_task)
        val taskListHolder: TextView = findViewById(R.id.List_task)

        val newCommentHolder: EditText = findViewById(R.id.new_comment_plain_text)

        val uploadCommentButton: Button = findViewById(R.id.add_new_comment_button)

        newCommentHolder.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                newCommentHolder.animate()
                    .translationY(-60f)
                    .duration = 500
                uploadCommentButton.animate()
                    .translationY(-60f)
                    .duration = 500
            } else {
                newCommentHolder.animate()
                    .translationY(60f)
                    .duration = 500
                uploadCommentButton.animate()
                    .translationY(60f)
                    .duration = 500
            }
        }

        commentsRecyclerView = findViewById(R.id.comments_recycler_view)

        commentsRecyclerView.layoutManager = LinearLayoutManager(this)

        CodealUser { user ->
            CodealTask(taskID){ task ->
                taskNameHolder.text = task.name
                taskTextHolder.text = task.content
                taskListHolder.text = task.listName

                commentsRecyclerView.adapter = CommentAdapter(comments, this)
                task.commentsIDs.forEach { commentID ->
                    CodealComment(commentID, ::addComment)
                }

                uploadCommentButton.setOnClickListener {
                    val commentContent = newCommentHolder.text.toString()
                    CodealComment(task.id, commentContent, user.id, ::addComment)
                    // https://stackoverflow.com/questions/1109022/how-do-you-close-hide-the-android-soft-keyboard-programmatically
                    // Only runs if there is a view that is currently focused
                    this@ViewTaskDetailActivity.currentFocus?.let { view ->
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE)
                                as? InputMethodManager
                        imm?.hideSoftInputFromWindow(view.windowToken, 0)
                    }
                    newCommentHolder.clearFocus()
                    newCommentHolder.setText("")
                }
            }
        }

        updateTaskButton.setOnClickListener {
            val taskName = taskNameHolder.text.toString()
            val taskText = taskTextHolder.text.toString()
            val taskListName = taskListHolder.text.toString()

            CodealTask(taskID) {
                it.change(taskName, taskText, taskListName)
                Toast.makeText(this@ViewTaskDetailActivity,
                    "Task updated successfully!",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        deleteTaskButton.setOnClickListener {
            CodealTask(taskID){
                it.delete()

                Toast.makeText(this@ViewTaskDetailActivity,
                    "Task deleted successfully!",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun addComment(comment: CodealComment) {
        if (!comment.ready) throw Exception("comment wasn't ready")

        // that comparator sorts so that 0th is the newest
        val index = comments.binarySearch(comment,
            Comparator { x, y -> -1 * x.date.compareTo(y.date) })

        val insertionIndex = if (index >= 0) index else (-index - 1)

        comments.add(insertionIndex, comment)
        (commentsRecyclerView.adapter as CommentAdapter)
            .notifyItemInserted(insertionIndex)
        commentsRecyclerView.smoothScrollToPosition(0)
    }


}