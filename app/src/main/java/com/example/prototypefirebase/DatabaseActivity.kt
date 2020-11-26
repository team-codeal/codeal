package com.example.prototypefirebase

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_database.*

class DatabaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_database)

        saveButton.setOnClickListener {
            val firstName = inputFirstName.text.toString()
            val lastName = inputLastName.text.toString()
            saveFireStore(firstName, lastName)
        }

        updateButton.setOnClickListener {
            readFireStoreData()
        }

        readFireStoreData()
    }

    private fun saveFireStore(firstname: String, lastname: String) {
        val db = FirebaseFirestore.getInstance()
        val user: MutableMap<String, Any> = HashMap()
        user["firstName"] = firstname
        user["lastName"] = lastname

        db.collection("users2")
            .add(user)
            .addOnSuccessListener {
                Toast.makeText(this@DatabaseActivity, "record added successfully", Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(this@DatabaseActivity, "record failed to add", Toast.LENGTH_SHORT).show()
            }
        readFireStoreData()
    }

    private fun readFireStoreData() {
        val db = FirebaseFirestore.getInstance()
        db.collection("users2")
            .get()
            .addOnCompleteListener {

                val result: StringBuffer = StringBuffer()

                if (it.isSuccessful) {
                    for (document in it.result!!) {
                        result.append(document.data.getValue("firstName")).append(" ")
                            .append(document.data.getValue("lastName")).append("\n\n")
                    }
                    textViewResult.text = result
                }
            }
    }
}
