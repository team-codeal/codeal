package com.example.prototypefirebase.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prototypefirebase.R
import com.example.prototypefirebase.Task
import com.example.utils.recyclers.tasks.OnTaskClickListener
import com.google.firebase.firestore.FirebaseFirestore

class FeedFragment : Fragment(), OnTaskClickListener {

    private lateinit var dashboardViewModel: FeedViewModel
    private lateinit var feedRecyclerView : RecyclerView
    private var tasks = ArrayList<Task>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
            ViewModelProvider(this).get(FeedViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_feed, container, false)
        
        getTasks()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        feedRecyclerView = view.findViewById(R.id.recycler_view_feed)!!
    }

    private fun getTasks() {

        tasks.clear()

        val db = FirebaseFirestore.getInstance()
        db.collection("tasks1")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val task = Task(
                        document.data["FirebaseID"] as String,
                        document.data["Name"] as String,
                        document.data["Text"] as String
                    )
                    tasks.add(task)
                }

                val taskAdapter = FeedTasksAdapter(tasks, this)
                feedRecyclerView.adapter = taskAdapter
                feedRecyclerView.layoutManager = LinearLayoutManager(activity)
                taskAdapter.notifyDataSetChanged()

            }
            .addOnFailureListener {
                Toast.makeText(this@FeedFragment.context, "Failed to find!", Toast.LENGTH_SHORT).show()
            }
    }
}