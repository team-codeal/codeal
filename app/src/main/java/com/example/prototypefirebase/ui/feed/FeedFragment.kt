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
import com.example.prototypefirebase.codeal.factories.CodealCommentFactory
import com.example.prototypefirebase.codeal.factories.CodealTaskFactory
import com.example.prototypefirebase.codeal.factories.CodealTeamFactory
import com.example.prototypefirebase.codeal.factories.CodealUserFactory
import com.example.utils.recyclers.FeedAdapter

class FeedFragment : Fragment() {

    private lateinit var dashboardViewModel: FeedViewModel
    private var feedContent: MutableList<Any> = mutableListOf()
    private lateinit var feedAdapter: FeedAdapter
    private lateinit var feedRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        feedAdapter = FeedAdapter(feedContent, requireContext()) {
            val intent = Intent(context, ViewTaskDetailActivity::class.java)
            intent.putExtra("TaskID", (feedContent[it] as String))
            startActivity(intent)
        }

        CodealUserFactory.get().addOnReady {
            for(teamID in it.teams){
                CodealTeamFactory.get(teamID).addOnReady { team ->
                    val teamTasks = team.tasks
                    feedContent.addAll(teamTasks)
                    feedAdapter.notifyItemRangeInserted(
                        feedContent.size - teamTasks.size,
                        teamTasks.size
                    )
                    teamTasks.forEach { taskID ->
                        CodealTaskFactory.get(taskID).addOnReady { task ->
                            task.commentsIDs.forEach { commentID ->
                                CodealCommentFactory.get(commentID).addOnReady { comment ->
                                    feedContent.add(comment)
                                    feedAdapter.notifyItemInserted(feedContent.size - 1)
                                }
                            }
                        }
                    }
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

        feedRecyclerView.adapter = feedAdapter
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
