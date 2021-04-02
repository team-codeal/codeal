package com.example.prototypefirebase

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_board.*
import kotlinx.android.synthetic.main.activity_task.*

class TaskActivity : AppCompatActivity() {

    private lateinit var tid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)
        supportActionBar?.hide()
        tid = intent.getStringExtra("TeamID").toString()
        saveTask.setOnClickListener {
            val taskName = Name_task.text.toString()
            val taskText = Text_task.text.toString()
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
                Toast.makeText(this@TaskActivity, "Task saved successfully!", Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(this@TaskActivity, "Failed to save!", Toast.LENGTH_SHORT).show()
            }

        // add task to team
        db.collection("teams").document(tid)
            .update("Tasks", FieldValue.arrayUnion(id))
    }


}