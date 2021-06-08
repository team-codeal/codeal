package com.example.prototypefirebase.ui.teams

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prototypefirebase.BoardActivity
import com.example.prototypefirebase.R
import com.example.prototypefirebase.codeal.factories.CodealTeamFactory
import com.example.prototypefirebase.codeal.factories.CodealUserFactory
import com.example.utils.recyclers.teams.OnTeamClickListener
import com.example.utils.recyclers.teams.TeamAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton


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

        val addTeamButton: FloatingActionButton = view.findViewById(R.id.add_team_button)
        addTeamButton.setOnClickListener{ openAddTeam() }

        teamsRecyclerView = view.findViewById(R.id.recycler_view_teams)!!
        teamsRecyclerView.adapter = teamAdapter
        teamsRecyclerView.layoutManager = LinearLayoutManager(activity)
    }

    override fun onStart() {
        super.onStart()
        loadTeams()
    }

    private fun loadTeams() {
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

    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    private fun openAddTeam(){

        val inflater : LayoutInflater =  requireActivity()
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_create_team, null)!!

        val ctx = requireContext()

        val createTeamButton: Button = popupView.findViewById(R.id.create_team_button)

        val teamNameHolder: EditText = popupView.findViewById(R.id.Enter_name)
        val teamDescriptionHolder: EditText = popupView.findViewById(R.id.Enter_desc)

        val focusable = true
        val width = (requireView().width / (1080.0 / 980.0)).toInt()
        val height = (requireView().height / (1946.0 / 1000.0)).toInt()
        val popupWindow = PopupWindow(popupView, width, height, focusable)

        createTeamButton.setOnClickListener {

            CodealUserFactory.get().addOnReady {
                val teamName = teamNameHolder.text.toString()
                val teamDesc = teamDescriptionHolder.text.toString()
                val teamMembers: List<String> = listOf(it.id)

                CodealTeamFactory.create(teamName, teamDesc, teamMembers).addOnReady { team ->
                    Toast.makeText(
                        ctx,
                        "Team created successfully!",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    val intent = Intent(ctx, BoardActivity::class.java)
                    intent.putExtra("TeamID", team.id)
                    startActivity(intent)
                }
            }
            popupWindow.dismiss()
        }

        popupWindow.setOnDismissListener{ loadTeams() }

        popupWindow.animationStyle = android.R.style.Animation_InputMethod

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        dimBehindPopup(popupWindow)
    }

    private fun dimBehindPopup(popupWindow: PopupWindow) {
        val container = popupWindow.contentView.rootView
        val context: Context = popupWindow.contentView.context
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val p = container.layoutParams as WindowManager.LayoutParams
        p.flags = p.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
        p.dimAmount = 0.6f
        wm.updateViewLayout(container, p)
    }

}
