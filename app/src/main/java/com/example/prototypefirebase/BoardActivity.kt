package com.example.prototypefirebase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prototypefirebase.codeal.CodealTask
import androidx.recyclerview.widget.RecyclerView
import com.example.utils.recyclers.tasks.OnTaskClickListener
import com.example.utils.recyclers.tasks.TaskAdapter
import com.google.firebase.firestore.FirebaseFirestore
import java.util.ArrayList

class BoardActivity : AppCompatActivity(), OnTaskClickListener {

    private var tasks = ArrayList<CodealTask>()
    private lateinit var teamID: String

    private var taskAdapter: TaskAdapter = TaskAdapter(tasks, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board)
        supportActionBar?.hide();

        val tasksRecyclerView: RecyclerView = findViewById(R.id.item_tasks_list)
        tasksRecyclerView.layoutManager = LinearLayoutManager(this)
        tasksRecyclerView.adapter = taskAdapter

        taskAdapter.notifyDataSetChanged()

        teamID = intent.getStringExtra("TeamID").toString()
    }


    override fun onResume() {
        super.onResume()
        getTasks()
    }

    fun openAddTask(view: View) {
        val taskIntent = Intent(this, AddTaskActivity::class.java)
        taskIntent.putExtra("TeamID", teamID)
        startActivity(taskIntent)
    }

    private fun getTasks() {
        val db = FirebaseFirestore.getInstance()

        // todo clearing every time is pointless. could use live updating

        tasks.clear()
        db.collection("tasks1")
            .whereEqualTo("Team", teamID)
            .get()
            .addOnSuccessListener { result ->

                for (document in result) {
                    val task = CodealTask(document.id) {taskAdapter.notifyDataSetChanged()}
                    tasks.add(task)
                }

            }
            .addOnFailureListener {
                Toast.makeText(this@BoardActivity, "Failed to find!", Toast.LENGTH_SHORT).show()
            }
    }
    override fun onTaskItemClicked(position: Int) {

        val intent = Intent(this, ViewTaskDetailActivity::class.java)
        intent.putExtra("TaskID", tasks[position].id)
        startActivity(intent)
    }
}