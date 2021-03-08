package com.example.prototypefirebase

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_task_detail.*
import kotlinx.android.synthetic.main.layout_list_item.*

class TaskDetail : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)
        supportActionBar?.hide();
        val id = intent.getStringExtra("TaskID")
        getCurrTask(id!!)
        updateTask.setOnClickListener {
            val taskName = Name_task.text.toString()
            val taskText = Text_task.text.toString()

            updateTask(id, taskName, taskText)
        }

        deleteTask.setOnClickListener {
            deleteTask(id)
        }
    }

    private fun getCurrTask(id: String) {
        val docRef = FirebaseFirestore.getInstance()
            .collection("tasks1")
            .document(id)

        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val data = document.data
                    val name = data?.get("Name")
                    val content = data?.get("Text")

                    Name_task.text = Editable.Factory.getInstance().newEditable(name.toString())
                    Text_task.text = Editable.Factory.getInstance().newEditable(content.toString())
                }
            }
    }

    private fun updateTask(id: String, taskName: String, taskText: String) {
        val docRef = FirebaseFirestore.getInstance().collection("tasks1").document(id)

        docRef
            .update("Name", taskName)
            .addOnSuccessListener {
                Toast.makeText(this@TaskDetail, "Name updated successfully!", Toast.LENGTH_SHORT)
                    .show()

            }
            .addOnFailureListener {
                Toast.makeText(this@TaskDetail, "Error with updating name!", Toast.LENGTH_SHORT)
                    .show()
            }

        docRef
            .update("Text", taskText)
            .addOnSuccessListener {
                Toast.makeText(this@TaskDetail, "Content updated successfully!", Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(this@TaskDetail, "Error with updating content!", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun deleteTask(id: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("tasks1").document(id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this@TaskDetail, "Task successfully deleted!", Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(this@TaskDetail, "Error with deleting task!", Toast.LENGTH_SHORT)
                    .show()
            }
    }
}