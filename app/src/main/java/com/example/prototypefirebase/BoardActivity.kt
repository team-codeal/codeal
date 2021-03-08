package com.example.prototypefirebase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.list_item_view.*
import java.util.ArrayList

class BoardActivity : AppCompatActivity(), OnTaskClickListener {

    private var tasks = ArrayList<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board)
        supportActionBar?.hide();

        val taskAdapter = TaskAdapter(tasks, this)
        item_tasks_list.layoutManager = LinearLayoutManager(this)
        item_tasks_list.adapter = taskAdapter
        taskAdapter.notifyDataSetChanged()

        getTasks()

    }


    override fun onRestart() {
        super.onRestart()
        getUpdatedTasks()
    }

    fun openAddTask(view: View) {
        val taskIntent = Intent(this, TaskActivity::class.java)
        startActivity(taskIntent)
    }

    private fun getTasks() {
        val db = FirebaseFirestore.getInstance()
        db.collection("tasks1")
            .get()
            .addOnSuccessListener { result ->
                var i = 0
                for (document in result) {
                    val task = Task(
                        document.data["FirebaseID"] as String,
                        document.data["Name"] as String,
                        document.data["Text"] as String
                    )
                    i++
                    tasks.add(task)
                }

                val taskAdapter = TaskAdapter(tasks, this)
                item_tasks_list.layoutManager = LinearLayoutManager(this)
                item_tasks_list.adapter = taskAdapter
                taskAdapter.notifyDataSetChanged()

            }
            .addOnFailureListener {
                Toast.makeText(this@BoardActivity, "Failed to find!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getUpdatedTasks() {

        val updatedTasks = ArrayList<Task>()

        val db = FirebaseFirestore.getInstance()
        db.collection("tasks1")
            .get()
            .addOnSuccessListener { result ->
                var i = 0
                for (document in result) {
                    val task = Task(
                        document.data["FirebaseID"] as String,
                        document.data["Name"] as String, document.data["Text"] as String
                    )
                    i++

                    updatedTasks.add(task)
                }

                val taskAdapter = TaskAdapter(tasks, this)
                item_tasks_list.layoutManager = LinearLayoutManager(this)
                item_tasks_list.adapter = taskAdapter
                taskAdapter.addNewItem(updatedTasks)

            }
            .addOnFailureListener {
                Toast.makeText(this@BoardActivity, "Failed to find!", Toast.LENGTH_SHORT).show()
            }
    }


    override fun onTaskItemClicked(position: Int) {

        val intent = Intent(this, TaskDetail::class.java)
        intent.putExtra("TaskID", tasks[position].firebaseID)
        startActivity(intent)
    }
}