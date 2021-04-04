package com.example.prototypefirebase.ui.teams

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.prototypefirebase.R
import com.google.firebase.firestore.FirebaseFirestore

class CreateTeamActivity : AppCompatActivity() {

    lateinit var teamNameHolder: EditText
    lateinit var teamDescriptionHolder: EditText
    lateinit var teamMembersHolder: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_team)
        supportActionBar?.hide();

        val createTeamButton: Button = findViewById(R.id.create_team_button)

        teamNameHolder = findViewById(R.id.Enter_name)
        teamDescriptionHolder = findViewById(R.id.Enter_desc)
        teamMembersHolder = findViewById(R.id.members_add)

        createTeamButton.setOnClickListener {
            //Toast.makeText(this@CreateTeam,"SUCCESS!", Toast.LENGTH_SHORT).show()
            val teamName = teamNameHolder.text.toString()
            val teamDesc = teamDescriptionHolder.text.toString()
            val teamMembers = teamMembersHolder.text.toString()
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
                Toast.makeText(this@CreateTeamActivity, "Team created successfully!", Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(this@CreateTeamActivity, "Failed to create!", Toast.LENGTH_SHORT)
                    .show()
            }
    }
}