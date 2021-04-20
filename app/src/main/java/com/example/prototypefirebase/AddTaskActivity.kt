package com.example.prototypefirebase

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.prototypefirebase.codeal.CodealTask


class AddTaskActivity : AppCompatActivity() {

    private lateinit var teamID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        teamID = intent.getStringExtra("TeamID").toString()

        setContentView(R.layout.activity_create_task)
        supportActionBar?.hide();

        val saveTaskButton: Button = findViewById(R.id.saveTask)
        val taskNameHolder: TextView = findViewById(R.id.Name_task)
        val taskTextHolder: TextView = findViewById(R.id.Text_task)
        val taskListHolder: TextView = findViewById(R.id.List_task)

        saveTaskButton.setOnClickListener {
            val taskName = taskNameHolder.text.toString()
            val taskText = taskTextHolder.text.toString()
            val taskListName = taskListHolder.text.toString()

            CodealTask(taskName, taskText, teamID, taskListName)

            Toast.makeText(this@AddTaskActivity, "Task added successfully!", Toast.LENGTH_SHORT)
                .show()

            finish()
        }
    }
}