package com.example.prototypefirebase.ui.teams

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prototypefirebase.BoardActivity
import com.example.prototypefirebase.R
import com.example.prototypefirebase.codeal.CodealEntity
import com.example.prototypefirebase.codeal.CodealTeam
import com.example.prototypefirebase.codeal.factories.CodealTeamFactory
import com.example.prototypefirebase.codeal.factories.CodealUserFactory
import com.example.utils.recyclers.teammembers.TeamMemberAdapter
import com.google.firebase.firestore.FirebaseFirestore

class ViewTeamDetailActivity : AppCompatActivity() {

    private lateinit var editTeamName: EditText
    private lateinit var editTeamDesc: EditText
    private lateinit var editTeamMemberMail: EditText

    private lateinit var teamMembersRecycler: RecyclerView
    private lateinit var recyclerAdapter: TeamMemberAdapter

    private lateinit var teamID: String
    private val teamMembers: MutableList<String> = mutableListOf()
    private var teamMembersListener: CodealEntity<CodealTeam>.CodealListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team_detail)
        supportActionBar?.hide();
        teamID = intent.getStringExtra("TeamID").toString()

        editTeamName = findViewById(R.id.edit_team_name)
        editTeamDesc = findViewById(R.id.edit_team_desc)
        teamMembersRecycler = findViewById(R.id.team_members_recycler_view)
        editTeamMemberMail = findViewById(R.id.edit_team_member_mail)

        recyclerAdapter = TeamMemberAdapter(teamMembers, this)
        teamMembersRecycler.layoutManager = LinearLayoutManager(this)
        teamMembersRecycler.adapter = recyclerAdapter

        val addPersonToTeamButton: Button = findViewById(R.id.add_person_to_team_button)
        addPersonToTeamButton.setOnClickListener {
            val newMail = editTeamMemberMail.text.toString()
            editTeamMemberMail.setText("")

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

        teamMembersListener = CodealTeamFactory.get(teamID).addListener { team ->
            editTeamName.setText(team.name)
            editTeamDesc.setText(team.description)
            mergeMembersWith(team.members)
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

    override fun onDestroy() {
        teamMembersListener?.remove()
        teamMembersRecycler.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {}

            override fun onViewDetachedFromWindow(v: View) {
                teamMembersRecycler.adapter = null
            }
        })
        super.onDestroy()
    }

    private fun mergeMembersWith(newTeamMembers: List<String>) {
        newTeamMembers.forEach { teamMemberID ->
            if (teamMemberID !in teamMembers) {
                teamMembers.add(teamMemberID)
                recyclerAdapter.notifyItemInserted(teamMembers.size - 1)
            }
        }

        teamMembers.forEachIndexed { index, teamMemberID ->
            if (teamMemberID !in newTeamMembers) {
                teamMembers.removeAt(index)
                recyclerAdapter.notifyItemRemoved(index)
            }
        }
    }

    private fun toBoard() {
        val intent = Intent(this, BoardActivity::class.java)
        intent.putExtra("TeamID", teamID)
        startActivity(intent)
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
                    val team = CodealTeam(teamID)
                    team.addPersonToTeam(userID)
                    Toast.makeText(
                        this@ViewTeamDetailActivity,
                        "This person successfully added :)",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}
