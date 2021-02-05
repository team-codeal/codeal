package com.example.prototypefirebase

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_board.*
import kotlinx.android.synthetic.main.activity_task.*

class TaskActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        saveTask.setOnClickListener{
            val taskName = Name_task.text.toString()
            val taskText = Text_task.text.toString()
            saveTask(taskName,taskText)
        }
    }

    private fun saveTask(name: String, text: String){
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
                Toast.makeText(this@TaskActivity,"Task saved successfully!",Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this@TaskActivity,"Failed to save!",Toast.LENGTH_SHORT).show()
            }

    }

   /** private fun getTasks(){
        val db = FirebaseFirestore.getInstance()
        db.collection("tasks")
            .get()
            .addOnCompleteListener {
                val result: StringBuffer = StringBuffer()
                if(it.isSuccessful){
                    for(document in it.result!!){
                        result.append(document.data.getValue("Name")).append(" ")
                            .append(document.data.getValue("Text")).append("\n\n")
                    }
                    tasksText.text = result
                }
            }
    }**/
}