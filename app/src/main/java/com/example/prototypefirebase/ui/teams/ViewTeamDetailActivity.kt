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

class ViewTeamDetailActivity : AppCompatActivity() {

    private lateinit var tid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team_detail)
        supportActionBar?.hide();
        tid = intent.getStringExtra("TeamID").toString()
        getCurrTeam()

        edit_team_update_button.setOnClickListener {
            val newTaskName = edit_team_name.text.toString()
            val newTaskDesc = edit_team_desc.text.toString()
            val newTeamMembers = edit_team_members.text.toString()

            saveCurrTeam(newTaskName,newTaskDesc,newTeamMembers)
        }
        edit_team_to_tasks.setOnClickListener {
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

                    edit_team_name.text = Editable.Factory.getInstance().newEditable(teamName.toString())
                    edit_team_desc.text = Editable.Factory.getInstance().newEditable(teamDesc.toString())
                    edit_team_members.text = Editable.Factory.getInstance().newEditable(teamMembers.toString())
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