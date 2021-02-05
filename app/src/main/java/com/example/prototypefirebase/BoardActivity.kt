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
    private var titles = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board)


        val taskAdapter = TaskAdapter(tasks,this)
        item_tasks_list.layoutManager = LinearLayoutManager(this)
        item_tasks_list.adapter = taskAdapter
        taskAdapter.notifyDataSetChanged()

        getTasks()

    }

    override fun onStart() {
        super.onStart()


    }

    override fun onStop() {
        super.onStop()

        //val taskAdapter = TaskAdapter(tasks,this)
        //item_tasks_list.layoutManager = LinearLayoutManager(this)
        //item_tasks_list.adapter = taskAdapter
        //taskAdapter.notifyDataSetChanged()
    }
    override fun onRestart() {
        super.onRestart()
        getUpdatedTasks()
    }
    
    fun openAddTask(view: View){
        val taskIntent = Intent(this, TaskActivity::class.java)
        startActivity(taskIntent)
    }

    private fun getTasks(){
        val db = FirebaseFirestore.getInstance()
        db.collection("tasks1")
            .get()
            .addOnSuccessListener { result ->
                var i = 0
                for (document in result){
                    val task = Task(document.data["FirebaseID"] as String,
                        document.data["Name"] as String, document.data["Text"] as String
                    )
                    i++
                    Toast.makeText(this@BoardActivity,"Task$i added successfully",Toast.LENGTH_SHORT).show()
                    tasks.add(task)
                }
                if(tasks.size == 0){
                    Toast.makeText(this@BoardActivity,"Tasks have no elements!",Toast.LENGTH_SHORT).show()

                }

                val taskAdapter = TaskAdapter(tasks,this)
                item_tasks_list.layoutManager = LinearLayoutManager(this)
                item_tasks_list.adapter = taskAdapter
                //taskAdapter.addNewItem(tasks)
                taskAdapter.notifyDataSetChanged()

            }
            .addOnFailureListener {
                Toast.makeText(this@BoardActivity,"Failed to find!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getUpdatedTasks(){

        val updatedTasks = ArrayList<Task>()

        val db = FirebaseFirestore.getInstance()
        db.collection("tasks1")
            .get()
            .addOnSuccessListener { result ->
                var i = 0
                for (document in result){
                    val task = Task(document.data["FirebaseID"] as String,
                        document.data["Name"] as String, document.data["Text"] as String
                    )
                    i++
                    Toast.makeText(this@BoardActivity,"Updates Task$i added successfully",Toast.LENGTH_SHORT).show()
                    updatedTasks.add(task)
                }
                if(updatedTasks.size == 0){
                    Toast.makeText(this@BoardActivity,"Tasks have no elements!",Toast.LENGTH_SHORT).show()

                }

                val taskAdapter = TaskAdapter(tasks,this)
                item_tasks_list.layoutManager = LinearLayoutManager(this)
                item_tasks_list.adapter = taskAdapter
                taskAdapter.addNewItem(updatedTasks)
                //taskAdapter.notifyDataSetChanged()

            }
            .addOnFailureListener {
                Toast.makeText(this@BoardActivity,"Failed to find!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createDummyList(){

        for (i in 1..10){
            titles.add("Task $i")
        }
    }

    override fun onTaskItemClicked(position: Int) {

        val intent = Intent(this,TaskDetail::class.java)
        intent.putExtra("TaskID",tasks[position].firebaseID)
        startActivity(intent)
    }
}