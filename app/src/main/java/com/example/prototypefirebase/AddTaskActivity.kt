package com.example.prototypefirebase

import android.app.DatePickerDialog
import android.content.DialogInterface.BUTTON_NEGATIVE
import android.os.Build
import android.os.Bundle
import android.view.View.GONE
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.prototypefirebase.codeal.CodealUser
import com.example.prototypefirebase.codeal.factories.CodealTaskFactory
import kotlinx.android.synthetic.main.activity_task_detail.*
import java.text.SimpleDateFormat
import java.util.*
import com.example.prototypefirebase.codeal.factories.CodealUserFactory


class AddTaskActivity : AppCompatActivity() {

    private lateinit var teamID: String
    private lateinit var taskListName: String
    private var taskDate: Calendar? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        teamID = intent.getStringExtra("TeamID")!!
        taskListName = intent.getStringExtra("TaskList")!!

        setContentView(R.layout.activity_task_detail)
        supportActionBar?.hide();

        val saveTaskButton: Button = findViewById(R.id.updateTask)
        val taskNameHolder: TextView = findViewById(R.id.Name_task)
        val taskTextHolder: TextView = findViewById(R.id.Text_task)

        val listNameHolder: TextView = findViewById(R.id.task_list)
        listNameHolder.text = taskListName

        val deadLineHolder: TextView = findViewById(R.id.deadline_line)
        deadLineHolder.setOnClickListener {
            val datePickerDialog = DatePickerDialog(this)
            datePickerDialog.setOnDateSetListener { _, year, month, dayOfMonth ->
                taskDate = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
                refreshDate(deadLineHolder)
            }
            datePickerDialog.setButton(BUTTON_NEGATIVE, "CANCEL") { _, which ->
                if (which == BUTTON_NEGATIVE) {
                    taskDate = null
                    refreshDate(deadLineHolder)
                }
            }
            datePickerDialog.show()
        }

        val newCommentContainer: ConstraintLayout = findViewById(R.id.new_comment_container)
        newCommentContainer.visibility = GONE

        val deleteButton: Button = findViewById(R.id.deleteTask)
        deleteButton.visibility = GONE

        saveTaskButton.setOnClickListener {
            val taskName = taskNameHolder.text.toString()
            val taskText = taskTextHolder.text.toString()
            CodealUserFactory.get().addOnReady { currentUser ->
                CodealTaskFactory.create(taskName,
                    taskText,
                    teamID,
                    taskListName,
                    currentUser.id,
                    taskDate?.time)
            }

            Toast.makeText(
                this@AddTaskActivity,
                "Task added successfully!",
                Toast.LENGTH_SHORT
            )
                .show()

            finish()
        }
    }

    private fun refreshDate(deadLineHolder: TextView) {
        val taskDateSnapshot = taskDate
        if (taskDateSnapshot == null) {
            deadLineHolder.text = ""
            return
        }
        val format1 = SimpleDateFormat("yyyy-MM-dd")
        val formatted: String = format1.format(taskDateSnapshot.time)
        deadLineHolder.text = formatted
    }
}
