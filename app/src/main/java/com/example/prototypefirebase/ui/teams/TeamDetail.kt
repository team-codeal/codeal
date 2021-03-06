package com.example.prototypefirebase.ui.teams

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.widget.Toast
import com.example.prototypefirebase.BoardActivity
import com.example.prototypefirebase.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_team_detail.*

class TeamDetail : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team_detail)

        val id = intent.getStringExtra("FirebaseID")
        getCurrTeam(id!!)

        edit_team_update_button.setOnClickListener {
            val newTaskName = edit_team_name.text.toString()
            val newTaskDesc = edit_team_desc.text.toString()
            val newTeamMembers = edit_team_members.text.toString()

            saveCurrTeam(id,newTaskName,newTaskDesc,newTeamMembers)
        }
        edit_team_to_tasks.setOnClickListener {
            toBoard(id)
        }
    }

    private fun toBoard(id: String){
        val intent = Intent(this,BoardActivity::class.java)
        intent.putExtra("FirebaseID",id)
        startActivity(intent)
    }

    private fun getCurrTeam(id: String){
        val docRef = FirebaseFirestore.getInstance()
            .collection("teams")
            .document(id)

        docRef.get()
            .addOnSuccessListener { document ->
                if(document != null){
                    val data = document.data
                    val teamName = data?.get("Name")
                    val teamDesc = data?.get("Desc")
                    val teamMembers = data?.get("Members")

                    edit_team_name.text = Editable.Factory.getInstance().newEditable(teamName.toString())
                    edit_team_desc.text = Editable.Factory.getInstance().newEditable(teamDesc.toString())
                    edit_team_members.text = Editable.Factory.getInstance().newEditable(teamMembers.toString())
                }
            }
    }

    private fun saveCurrTeam(id: String, name: String, desc: String, members: String){
        val docRef = FirebaseFirestore.getInstance().collection("teams").document(id)

        docRef.update("Name", name)
        docRef.update("Desc", desc)
        docRef.update("Members", members)
            .addOnSuccessListener {
                Toast.makeText(this@TeamDetail,"Team saved successfully!",Toast.LENGTH_SHORT).show()
            }
    }
}