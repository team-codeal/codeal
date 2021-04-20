package com.example.prototypefirebase

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.prototypefirebase.codeal.CodealTask
import com.google.firebase.firestore.FirebaseFirestore


class ViewTaskDetailActivity : AppCompatActivity() {
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

        val task = CodealTask(taskID){
            taskNameHolder.text = it.name
            taskTextHolder.text = it.content
            taskListHolder.text = it.listName
        }

        updateTaskButton.setOnClickListener {
            val taskName = taskNameHolder.text.toString()
            val taskText = taskTextHolder.text.toString()
            val taskListName = taskListHolder.text.toString()

            task.change(taskName, taskText, taskListName)
            Toast.makeText(this@ViewTaskDetailActivity, "Task updated successfully!", Toast.LENGTH_SHORT).show()
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