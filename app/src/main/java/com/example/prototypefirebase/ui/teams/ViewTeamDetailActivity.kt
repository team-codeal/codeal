package com.example.prototypefirebase.ui.teams

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.prototypefirebase.BoardActivity
import com.example.prototypefirebase.R
import com.example.prototypefirebase.codeal.CodealTeam
import com.example.prototypefirebase.codeal.factories.CodealTeamFactory
import com.example.prototypefirebase.codeal.factories.CodealUserFactory
import com.google.firebase.firestore.FirebaseFirestore

class ViewTeamDetailActivity : AppCompatActivity() {


    private lateinit var teamID: String
    private var tMembers: StringBuffer = StringBuffer()
    private lateinit var editTeamName: EditText
    private lateinit var editTeamDesc: EditText
    private lateinit var editTeamMembers: EditText
    private lateinit var editTeamMemberMail: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team_detail)
        supportActionBar?.hide();
        teamID = intent.getStringExtra("TeamID").toString()
        getCurrTeam()

        editTeamName = findViewById(R.id.edit_team_name)
        editTeamDesc = findViewById(R.id.edit_team_desc)
        editTeamMembers = findViewById(R.id.edit_team_members)
        editTeamMemberMail = findViewById(R.id.edit_team_member_mail)

        val addPersonToTeamButton: Button = findViewById(R.id.add_person_to_team_button)
        addPersonToTeamButton.setOnClickListener {
            val newMail = editTeamMemberMail.text.toString()

            findPersonInDatabase(newMail)
        }

        val editTeamUpdateButton: Button = findViewById(R.id.edit_team_update_button)
        editTeamUpdateButton.setOnClickListener {
            val newTaskName = editTeamName.text.toString()
            val newTaskDesc = editTeamDesc.text.toString()

            saveCurrTeam(newTaskName, newTaskDesc)
            finish()
        }

        val editTeamToTaskButton: Button = findViewById(R.id.edit_team_to_tasks)
        editTeamToTaskButton.setOnClickListener {
            toBoard()

        }

        val leaveTeamButton: Button = findViewById(R.id.leave_team_button)
        leaveTeamButton.setOnClickListener {
            CodealTeamFactory.get(teamID).addOnReady {
                CodealUserFactory.get().addOnReady { user ->

                    it.deletePersonFromTeam(user.id)
                    Toast.makeText(
                        this@ViewTeamDetailActivity,
                        "You left team successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }

        }
    }

    private fun toBoard() {
        val intent = Intent(this, BoardActivity::class.java)
        intent.putExtra("TeamID", teamID)
        startActivity(intent)
    }

    private fun getCurrTeam() {
        CodealTeamFactory.get(teamID).addOnReady {
            editTeamName.setText(it.name)
            editTeamDesc.setText(it.description)
            val members = it.members
            for (member in members) {
                CodealUserFactory.get(member).addOnReady { user ->
                    tMembers.append(user.name).append("\n")
                    editTeamMembers.setText(tMembers)
                }
            }
        }
    }

    private fun saveCurrTeam(name: String, desc: String) {
        val docRef = FirebaseFirestore.getInstance().collection("teams").document(teamID)

        docRef.update("Name", name)
        docRef.update("Desc", desc)
            .addOnSuccessListener {
                Toast.makeText(
                    this@ViewTeamDetailActivity,
                    "Team saved successfully!",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun findPersonInDatabase(mail: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("user_profiles")
            .whereEqualTo("mail", mail)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(
                        this@ViewTeamDetailActivity,
                        "This person didn't register in the app :(",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val userID = documents.documents[0].id
                    val userName = documents.documents[0].data?.get("name")
                    val team = CodealTeam(teamID)
                    team.addPersonToTeam(userID)
                    if (!tMembers.contains(userName.toString())) {
                        tMembers.append(userName).append("\n")
                        editTeamMembers.setText(tMembers)
                    }
                    Toast.makeText(
                        this@ViewTeamDetailActivity,
                        "This person successfully added :)",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}
