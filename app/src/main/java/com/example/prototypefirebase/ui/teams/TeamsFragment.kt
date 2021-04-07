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
        getTeams()
    }

    override fun onTeamItemClicked(position: Int) {

        val intent = Intent(this.context, ViewTeamDetailActivity::class.java)
        intent.putExtra("TeamID", teams[position].firebaseID)
        startActivity(intent)
    }

    private fun getTeams() {
        // TODO clearing teams every time could be inefficient. Consider using
        //  callbacks/updaters etc
        teams.clear()
        val db = FirebaseFirestore.getInstance()
        db.collection("teams")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val team = Model(
                        document.data["Name"] as String,
                        document.data["Desc"] as String,
                        document.data["FirebaseID"] as String,
                        document.data["Members"] as String
                    )
                    teams.add(team)
                }
                teamAdapter.notifyDataSetChanged()

            }
            .addOnFailureListener {
                Toast.makeText(
                    this@TeamsFragment.context,
                    "Failed to find!",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

}