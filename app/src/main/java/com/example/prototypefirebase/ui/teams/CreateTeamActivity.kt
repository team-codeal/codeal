package com.example.prototypefirebase.ui.teams

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.prototypefirebase.BoardActivity
import com.example.prototypefirebase.R
import com.example.prototypefirebase.codeal.factories.CodealTeamFactory
import com.example.prototypefirebase.codeal.factories.CodealUserFactory

class CreateTeamActivity : AppCompatActivity() {

    lateinit var teamNameHolder: EditText
    lateinit var teamDescriptionHolder: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_team)
        supportActionBar?.hide();

        val createTeamButton: Button = findViewById(R.id.create_team_button)

        teamNameHolder = findViewById(R.id.Enter_name)
        teamDescriptionHolder = findViewById(R.id.Enter_desc)

        createTeamButton.setOnClickListener {

            CodealUserFactory.get().addOnReady {
                val teamName = teamNameHolder.text.toString()
                val teamDesc = teamDescriptionHolder.text.toString()
                val teamMembers: List<String> = listOf(it.id)

                CodealTeamFactory.create(teamName, teamDesc, teamMembers).addOnReady { team ->
                    Toast.makeText(this@CreateTeamActivity,
                        "Team created successfully!",
                        Toast.LENGTH_SHORT)
                        .show()
                    val intent = Intent(this, BoardActivity::class.java)
                    intent.putExtra("TeamID", team.id)
                    startActivity(intent)
                }
            }
        }
    }
}
