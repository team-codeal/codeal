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
import com.google.firebase.firestore.FirebaseFirestore

class TeamsFragment : Fragment(), OnTeamClickListener {

    private var teams = ArrayList<Model>()

    private lateinit var teamsRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_teams, container, false)

        getTeams()

        //val textView: TextView = root.findViewById(R.id.TEAMS)
        //notificationsViewModel.text.observe(viewLifecycleOwner, Observer {
        //    textView.text = it
        //})

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        teamsRecyclerView = view.findViewById(R.id.recycler_view_teams)!!
    }

    override fun onStart() {
        super.onStart()
        getUpdateTeams()
    }

    override fun onTeamItemClicked(position: Int) {

        val intent = Intent(this.context, ViewTeamDetailActivity::class.java)
        intent.putExtra("FirebaseID", teams[position].firebaseID)
        startActivity(intent)
    }

    private fun getTeams() {

        val db = FirebaseFirestore.getInstance()
        db.collection("teams")
            .get()
            .addOnSuccessListener { result ->
                var i = 0
                for (document in result) {
                    val team = Model(
                        document.data["Name"] as String,
                        document.data["Desc"] as String,
                        document.data["FirebaseID"] as String,
                        document.data["Members"] as String
                    )
                    i++
                    teams.add(team)
                }
                val teamAdapter = TeamAdapter(teams, this)
                teamsRecyclerView.adapter = teamAdapter
                teamsRecyclerView.layoutManager = LinearLayoutManager(activity)
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

    private fun getUpdateTeams() {

        val newTeams = ArrayList<Model>()

        val db = FirebaseFirestore.getInstance()
        db.collection("teams")
            .get()
            .addOnSuccessListener { result ->
                var i = 0
                for (document in result) {
                    val team = Model(
                        document.data["Name"] as String,
                        document.data["Desc"] as String,
                        document.data["FirebaseID"] as String,
                        document.data["Members"] as String
                    )
                    i++
                    newTeams.add(team)
                }
                val teamAdapter = TeamAdapter(teams, this)
                teamsRecyclerView.adapter = teamAdapter
                teamsRecyclerView.layoutManager = LinearLayoutManager(activity)
                teamAdapter.addNewItem(newTeams)

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