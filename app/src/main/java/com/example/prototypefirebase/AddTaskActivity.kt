package com.example.prototypefirebase

import android.os.Bundle
import android.view.View.GONE
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.prototypefirebase.codeal.factories.CodealTaskFactory


class AddTaskActivity : AppCompatActivity() {

    private lateinit var teamID: String
    private lateinit var taskListName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        teamID = intent.getStringExtra("TeamID")!!
        taskListName = intent.getStringExtra("TaskList")!!

        setContentView(R.layout.activity_task_detail)
        supportActionBar?.hide();

        val saveTaskButton: Button = findViewById(R.id.updateTask)
        val taskNameHolder: TextView = findViewById(R.id.Name_task)
        val taskTextHolder: TextView = findViewById(R.id.Text_task)

        val listNameHolder: TextView = findViewById(R.id.List_task)
        listNameHolder.text = taskListName

        val newCommentContainer: ConstraintLayout = findViewById(R.id.new_comment_container)
        newCommentContainer.visibility = GONE

        val deleteButton: Button = findViewById(R.id.deleteTask)
        deleteButton.visibility = GONE

        saveTaskButton.setOnClickListener {
            val taskName = taskNameHolder.text.toString()
            val taskText = taskTextHolder.text.toString()

            CodealTaskFactory.create(taskName, taskText, teamID, taskListName)

            Toast.makeText(this@AddTaskActivity,
                "Task added successfully!",
                Toast.LENGTH_SHORT)
                .show()

            finish()
        }
    }
}