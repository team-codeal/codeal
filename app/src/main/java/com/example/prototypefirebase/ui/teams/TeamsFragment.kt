package com.example.prototypefirebase.ui.teams

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prototypefirebase.R
import com.example.prototypefirebase.codeal.CodealTeam
import com.example.prototypefirebase.codeal.CodealUser
import com.example.prototypefirebase.codeal.factories.CodealTeamFactory
import com.example.prototypefirebase.codeal.factories.CodealUserFactory
import com.example.utils.recyclers.teams.OnTeamClickListener
import com.example.utils.recyclers.teams.TeamAdapter
import com.google.firebase.firestore.FirebaseFirestore

class TeamsFragment : Fragment(), OnTeamClickListener {

    private var teams = ArrayList<Model>()
    private var teamAdapter: TeamAdapter = TeamAdapter(teams, this)
    private lateinit var teamsRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_teams, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        teamsRecyclerView = view.findViewById(R.id.recycler_view_teams)!!
        teamsRecyclerView.adapter = teamAdapter
        teamsRecyclerView.layoutManager = LinearLayoutManager(activity)
    }

    override fun onStart() {
        super.onStart()
        CodealUserFactory.get().addOnReady {
            val userTeams = it.teams
            getTeams(userTeams)
        }

    }

    override fun onTeamItemClicked(position: Int) {

        val intent = Intent(this.context, ViewTeamDetailActivity::class.java)
        intent.putExtra("TeamID", teams[position].firebaseID)
        startActivity(intent)
    }

    private fun getTeams(userTeams: List<String>) {
        // TODO clearing teams every time could be inefficient. Consider using
        //  callbacks/updaters etc
        teams.clear()

        for (team in userTeams) {
            CodealTeamFactory.get(team).addOnReady {
                teams.add(
                    Model(
                        it.name,
                        it.description,
                        it.id
                    )
                )
                teamAdapter.notifyDataSetChanged()
            }
        }
    }

}