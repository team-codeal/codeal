package com.example.prototypefirebase

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

class TaskActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_task)
        supportActionBar?.hide();

        val saveTaskButton: Button = findViewById(R.id.saveTask)
        val taskNameHolder: TextView = findViewById(R.id.Name_task)
        val taskTextHolder: TextView = findViewById(R.id.Text_task)

        saveTaskButton.setOnClickListener {
            val taskName = taskNameHolder.text.toString()
            val taskText = taskTextHolder.text.toString()
            saveTask(taskName, taskText)
        }
    }

    private fun saveTask(name: String, text: String) {
        val db = FirebaseFirestore.getInstance()
        val id = db.collection("tasks").document().id
        val task = hashMapOf<String, String>(
            "FirebaseID" to id,
            "Name" to name,
            "Text" to text
        )


        db.collection("tasks1").document(id)
            .set(task)
            .addOnSuccessListener {
                Toast.makeText(this@TaskActivity, "Task saved successfully!", Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(this@TaskActivity, "Failed to save!", Toast.LENGTH_SHORT).show()
            }

    }


}