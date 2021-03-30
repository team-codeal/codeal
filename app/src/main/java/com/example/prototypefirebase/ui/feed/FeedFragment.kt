package com.example.prototypefirebase.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prototypefirebase.OnTaskClickListener
import com.example.prototypefirebase.R
import com.example.prototypefirebase.Task
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_feed.*

class FeedFragment : Fragment(), OnTaskClickListener {

    private lateinit var dashboardViewModel: FeedViewModel
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
        /*val textView: TextView = root.findViewById(R.id.text_feed)
        dashboardViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })*/
        return root
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
                recycler_view_feed.layoutManager = LinearLayoutManager(this.context)
                recycler_view_feed.adapter = taskAdapter
                taskAdapter.notifyDataSetChanged()

            }
            .addOnFailureListener {
                Toast.makeText(this@FeedFragment.context, "Failed to find!", Toast.LENGTH_SHORT).show()
            }
    }
}