package com.example.prototypefirebase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.util.ArrayList

class BoardActivity : AppCompatActivity(), OnTaskClickListener {

    private var tasks = ArrayList<Task>()

    private var taskAdapter: TaskAdapter = TaskAdapter(tasks, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board)
        supportActionBar?.hide();

        val tasksRecyclerView: RecyclerView = findViewById(R.id.item_tasks_list)
        tasksRecyclerView.layoutManager = LinearLayoutManager(this)
        tasksRecyclerView.adapter = taskAdapter

        taskAdapter.notifyDataSetChanged()

        getTasks()
    }

    fun openAddTask(view: View) {
        val taskIntent = Intent(this, AddTaskActivity::class.java)
        startActivity(taskIntent)
    }

    private fun getTasks() {
        val db = FirebaseFirestore.getInstance()
        // todo clearing every time is pointless. could use live updating
        tasks.clear()
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

                taskAdapter.notifyDataSetChanged()

            }
            .addOnFailureListener {
                Toast.makeText(this@BoardActivity, "Failed to find!", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onTaskItemClicked(position: Int) {

        val intent = Intent(this, ViewTaskDetailActivity::class.java)
        intent.putExtra("TaskID", tasks[position].firebaseID)
        startActivity(intent)
    }
}