package com.example.prototypefirebase

import android.content.Context
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
import com.example.prototypefirebase.codeal.CodealEntity
import com.example.prototypefirebase.codeal.CodealTask
import com.example.prototypefirebase.codeal.factories.CodealCommentFactory
import com.example.prototypefirebase.codeal.factories.CodealTaskFactory
import com.example.prototypefirebase.codeal.factories.CodealUserFactory
import com.example.utils.recyclers.comments.CommentAdapter
import java.util.*
import kotlin.Comparator


class ViewTaskDetailActivity : AppCompatActivity() {

    private var comments: MutableList<CodealComment> = mutableListOf()

    private lateinit var commentsRecyclerView: RecyclerView

    private var commentsListener: CodealEntity<CodealTask>.CodealListener? = null

    private lateinit var taskID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)
        supportActionBar?.hide();

        taskID = intent.getStringExtra("TaskID")!!

        val updateTaskButton: Button = findViewById(R.id.updateTask)
        val deleteTaskButton: Button = findViewById(R.id.deleteTask)
        val taskNameHolder: TextView = findViewById(R.id.Name_task)
        val taskTextHolder: TextView = findViewById(R.id.Text_task)
        val taskListHolder: TextView = findViewById(R.id.List_task)

        val newCommentHolder: EditText = findViewById(R.id.new_comment_plain_text)

        val uploadCommentButton: Button = findViewById(R.id.add_new_comment_button)

        commentsRecyclerView = findViewById(R.id.comments_recycler_view)

        commentsRecyclerView.layoutManager = LinearLayoutManager(this)
        commentsRecyclerView.adapter = CommentAdapter(comments, this)

        CodealUserFactory.get().addOnReady { user ->
            CodealTaskFactory.get(taskID).addOnReady { task ->
                taskNameHolder.text = task.name
                taskTextHolder.text = task.content
                taskListHolder.text = task.listName

                uploadCommentButton.setOnClickListener {
                    val commentContent = newCommentHolder.text.toString()
                    CodealCommentFactory.create(task.id, commentContent, user.id)
                        .addOnReady(::addComment)
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

            CodealTaskFactory.get(taskID).addOnReady {
                it.change(name = taskName, content = taskText)
                Toast.makeText(this@ViewTaskDetailActivity,
                    "Task updated successfully!",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        deleteTaskButton.setOnClickListener {
            CodealTaskFactory.get(taskID).addOnReady {
                it.delete()

                Toast.makeText(this@ViewTaskDetailActivity,
                    "Task deleted successfully!",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        commentsListener = CodealTaskFactory.get(taskID)
            .addListener { mergeCommentsWith(it.commentsIDs) }
    }

    override fun onStop() {
        super.onStop()
        commentsListener?.remove()
        commentsListener = null
    }

    private fun mergeCommentsWith(newCommentsIDs: List<String>) {
        val currentCommentsIDs = comments.map { it.id }
        newCommentsIDs.forEach { commentID ->
            if (!currentCommentsIDs.contains(commentID)) {
                CodealCommentFactory.get(commentID).addOnReady { addComment(it) }
            }
        }

        currentCommentsIDs.forEachIndexed { index, commentID ->
            if (!newCommentsIDs.contains(commentID)) {
                comments.removeAt(index)
                commentsRecyclerView.adapter?.notifyItemRemoved(index)
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

    override fun onDestroy() {
        super.onDestroy()
        commentsRecyclerView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {}

            override fun onViewDetachedFromWindow(v: View) {
                commentsRecyclerView.adapter = null
            }
        })
    }
}
