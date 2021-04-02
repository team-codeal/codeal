package com.example.prototypefirebase

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class AddTaskActivity : AppCompatActivity() {

    private lateinit var tid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tid = intent.getStringExtra("TeamID").toString()
        
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
            "Team" to tid,
            "Text" to text
        )

        db.collection("tasks1").document(id)
            .set(task)
            .addOnSuccessListener {
                Toast.makeText(this@AddTaskActivity, "Task saved successfully!", Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(this@AddTaskActivity, "Failed to save!", Toast.LENGTH_SHORT).show()
            }

        // add task to team
        db.collection("teams").document(tid)
            .update("Tasks", FieldValue.arrayUnion(id))
    }


}