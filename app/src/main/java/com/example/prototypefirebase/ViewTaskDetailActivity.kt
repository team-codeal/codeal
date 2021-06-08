package com.example.prototypefirebase

import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prototypefirebase.codeal.CodealComment
import com.example.prototypefirebase.codeal.CodealEntity
import com.example.prototypefirebase.codeal.CodealTask
import com.example.prototypefirebase.codeal.suppliers.CodealCommentSupplier
import com.example.prototypefirebase.codeal.suppliers.CodealTaskSupplier
import com.example.prototypefirebase.codeal.suppliers.CodealUserSupplier
import com.example.utils.recyclers.comments.CommentAdapter
import kotlinx.android.synthetic.main.activity_task_detail.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Comparator


class ViewTaskDetailActivity : AppCompatActivity() {

    private var comments: MutableList<CodealComment> = mutableListOf()

    private lateinit var commentsRecyclerView: RecyclerView

    private var taskDeadline: Date? = null

    private var commentsListener: CodealEntity<CodealTask>.CodealListener? = null

    private lateinit var taskID: String

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)
        supportActionBar?.hide();

        taskID = intent.getStringExtra("TaskID")!!

        val updateTaskButton: Button = findViewById(R.id.updateTask)
        val deleteTaskButton: Button = findViewById(R.id.deleteTask)
        val taskNameHolder: TextView = findViewById(R.id.Name_task)
        val taskTextHolder: TextView = findViewById(R.id.Text_task)
        val taskListHolder: TextView = findViewById(R.id.task_list)

        val newCommentHolder: EditText = findViewById(R.id.new_comment_plain_text)

        val deadlineHolder: TextView = findViewById(R.id.deadline_line)
        deadlineHolder.setOnClickListener {
            val datePickerDialog = DatePickerDialog(this)
            datePickerDialog.setOnDateSetListener { _, year, month, dayOfMonth ->
                val taskDeadline = Calendar.getInstance().also { it.set(year, month, dayOfMonth) }
                refreshDate(taskDeadline.time, deadlineHolder)
            }
            datePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL") { _, which ->
                if (which == DialogInterface.BUTTON_NEGATIVE) {
                    refreshDate(null, deadlineHolder)
                }
            }
            datePickerDialog.show()
        }

        val uploadCommentButton: Button = findViewById(R.id.add_new_comment_button)

        commentsRecyclerView = findViewById(R.id.comments_recycler_view)

        commentsRecyclerView.layoutManager = LinearLayoutManager(this)
        commentsRecyclerView.adapter = CommentAdapter(comments, this)

        CodealUserSupplier.get().addOnReady { user ->
            CodealTaskSupplier.get(taskID).addOnReady { task ->
                taskNameHolder.text = task.name
                taskTextHolder.text = task.content
                taskListHolder.text = task.listName
                refreshDate(task.deadline, deadlineHolder)

                uploadCommentButton.setOnClickListener {
                    val commentContent = newCommentHolder.text.toString()
                    CodealCommentSupplier.create(task.id, commentContent, user.id)
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

            CodealTaskSupplier.get(taskID).addOnReady {
                it.change(name = taskName, content = taskText, deadline = taskDeadline)
                Toast.makeText(this@ViewTaskDetailActivity,
                    "Task updated successfully!",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        deleteTaskButton.setOnClickListener {
            CodealTaskSupplier.get(taskID).addOnReady {
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
        commentsListener = CodealTaskSupplier.get(taskID)
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
                CodealCommentSupplier.get(commentID).addOnReady { addComment(it) }
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

    private fun refreshDate(deadline: Date?, deadlineHolder: TextView) {
        taskDeadline = deadline
        if (deadline == null) {
            deadlineHolder.text = ""
            return
        }
        val format1 = SimpleDateFormat("yyyy-MM-dd")
        val formatted: String = format1.format(deadline)
        deadlineHolder.text = formatted
    }
}
