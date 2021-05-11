package com.example.prototypefirebase.ui.feed

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prototypefirebase.R
import com.example.prototypefirebase.ViewTaskDetailActivity
import com.example.prototypefirebase.codeal.factories.CodealTeamFactory
import com.example.prototypefirebase.codeal.factories.CodealUserFactory
import com.example.utils.recyclers.tasks.TaskAdapter
import com.example.utils.recyclers.tasks.TaskViewHolder

class FeedFragment : Fragment() {

    private lateinit var dashboardViewModel: FeedViewModel
    private var userTasks: MutableList<String> = mutableListOf()
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var feedRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        taskAdapter = TaskAdapter(userTasks) {
            val intent = Intent(context, ViewTaskDetailActivity::class.java)
            intent.putExtra("TaskID", userTasks[it])
            startActivity(intent)
        }

        CodealUserFactory.get().addOnReady {
            for(teamID in it.teams){
                CodealTeamFactory.get(teamID).addOnReady { team ->
                    val teamTasks = team.tasks
                    userTasks.addAll(teamTasks)
                    taskAdapter.notifyItemRangeInserted(
                        userTasks.size - teamTasks.size,
                        teamTasks.size
                    )
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
            ViewModelProvider(this).get(FeedViewModel::class.java)

        return inflater.inflate(R.layout.fragment_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        feedRecyclerView = view.findViewById(R.id.recycler_view_feed)!!

        feedRecyclerView.adapter = taskAdapter
        feedRecyclerView.layoutManager = LinearLayoutManager(activity)

    }

    override fun onDestroyView() {
        feedRecyclerView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {}

            override fun onViewDetachedFromWindow(v: View) {
                feedRecyclerView.adapter = null
            }
        })
        super.onDestroyView()
    }
}
