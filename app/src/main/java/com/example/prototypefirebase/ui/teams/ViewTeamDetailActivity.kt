package com.example.prototypefirebase.ui.teams

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.prototypefirebase.BoardActivity
import com.example.prototypefirebase.R
import com.google.firebase.firestore.FirebaseFirestore

class ViewTeamDetailActivity : AppCompatActivity() {


    private lateinit var tid: String
    private lateinit var editTeamName: EditText
    private lateinit var editTeamDesc: EditText
    private lateinit var editTeamMembers: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team_detail)
        supportActionBar?.hide();
        tid = intent.getStringExtra("TeamID").toString()
        getCurrTeam()

        editTeamName = findViewById(R.id.edit_team_name)
        editTeamDesc = findViewById(R.id.edit_team_desc)
        editTeamMembers = findViewById(R.id.edit_team_members)

        val editTeamUpdateButton: Button = findViewById(R.id.edit_team_update_button)
        editTeamUpdateButton.setOnClickListener {
            val newTaskName = editTeamName.text.toString()
            val newTaskDesc = editTeamDesc.text.toString()
            val newTeamMembers = editTeamMembers.text.toString()

            saveCurrTeam(newTaskName,newTaskDesc,newTeamMembers)
        }

        val editTeamToTaskButton: Button = findViewById(R.id.edit_team_to_tasks)
        editTeamToTaskButton.setOnClickListener {
            toBoard()

        }
    }

    private fun toBoard(){
        val intent = Intent(this,BoardActivity::class.java)
        intent.putExtra("TeamID",tid)
        startActivity(intent)
    }

    private fun getCurrTeam(){
        val docRef = FirebaseFirestore.getInstance()
            .collection("teams")
            .document(tid)

        docRef.get()
            .addOnSuccessListener { document ->
                if(document != null){
                    val data = document.data
                    val teamName = data?.get("Name")
                    val teamDesc = data?.get("Desc")
                    val teamMembers = data?.get("Members")

                    editTeamName.text = Editable.Factory.getInstance().newEditable(teamName.toString())
                    editTeamDesc.text = Editable.Factory.getInstance().newEditable(teamDesc.toString())
                    editTeamMembers.text = Editable.Factory.getInstance().newEditable(teamMembers.toString())
                }
            }
    }

    private fun saveCurrTeam(name: String, desc: String, members: String){
        val docRef = FirebaseFirestore.getInstance().collection("teams").document(tid)

        docRef.update("Name", name)
        docRef.update("Desc", desc)
        docRef.update("Members", members)
            .addOnSuccessListener {
                Toast.makeText(this@ViewTeamDetailActivity,"Team saved successfully!",Toast.LENGTH_SHORT).show()
            }
    }
}