package com.example.prototypefirebase.ui.teams

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.prototypefirebase.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_create_team.*

class CreateTeam : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_team)

        create_team_button.setOnClickListener {
            //Toast.makeText(this@CreateTeam,"SUCCESS!", Toast.LENGTH_SHORT).show()
            val teamName = Enter_name.text.toString()
            val teamDesc = Enter_desc.text.toString()
            val teamMembers = members_add.text.toString()
            //Toast.makeText(this@CreateTeam,"Save team func!", Toast.LENGTH_SHORT).show()
            saveTeam(teamName, teamDesc, teamMembers)
        }

    }


    private fun saveTeam(name: String, desc: String, members: String){
        val db = FirebaseFirestore.getInstance()
        val id = db.collection("teams").document().id
        val team = hashMapOf<String,Any>(
            "FirebaseID" to id,
            "Name" to name,
            "Desc" to desc,
            "Members" to members
        )
        db.collection("teams").document(id)
            .set(team)
            .addOnSuccessListener {
                Toast.makeText(this@CreateTeam, "Team created successfully!", Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(this@CreateTeam, "Failed to create!", Toast.LENGTH_SHORT)
                    .show()
            }
    }
}