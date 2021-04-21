package com.example.prototypefirebase

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prototypefirebase.codeal.CodealComment
import com.example.prototypefirebase.codeal.CodealTask
import com.example.prototypefirebase.codeal.CodealUser
import com.example.utils.recyclers.comments.CommentAdapter
import com.google.firebase.firestore.FirebaseFirestore


class ViewTaskDetailActivity : AppCompatActivity() {

    var comments: MutableList<CodealComment> = mutableListOf()

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
        val commentsRecyclerView: RecyclerView = findViewById(R.id.comments_recycler_view)

        commentsRecyclerView.layoutManager = LinearLayoutManager(this)

        CodealUser { user ->
            CodealTask(taskID){ task ->
                taskNameHolder.text = task.name
                taskTextHolder.text = task.content
                taskListHolder.text = task.listName

                commentsRecyclerView.adapter = CommentAdapter(comments)
                task.commentsIDs.forEach { commentID ->
                    CodealComment(commentID) { comment ->
                        comments.add(comment)
                        (commentsRecyclerView.adapter as CommentAdapter)
                            .notifyItemInserted(comments.size - 1)
                    }
                }

                uploadCommentButton.setOnClickListener {
                    val commentContent = newCommentHolder.text.toString()
                    CodealComment(task.id, commentContent, user.id) {
                        comments.add(it)
                        (commentsRecyclerView.adapter as CommentAdapter)
                            .notifyItemInserted(comments.size - 1)
                    }
                }
            }
        }

        updateTaskButton.setOnClickListener {
            val taskName = taskNameHolder.text.toString()
            val taskText = taskTextHolder.text.toString()
            val taskListName = taskListHolder.text.toString()

            CodealTask(taskID) {
                it.change(taskName, taskText, taskListName)
                Toast.makeText(this@ViewTaskDetailActivity, "Task updated successfully!", Toast.LENGTH_SHORT).show()
            }
        }

        deleteTaskButton.setOnClickListener {
            deleteTask(taskID)
            Toast.makeText(this@ViewTaskDetailActivity, "Task deleted successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteTask(id: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("tasks1").document(id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(
                    this@ViewTaskDetailActivity,
                    "Task successfully deleted!",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this@ViewTaskDetailActivity,
                    "Error with deleting task!",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
    }
}