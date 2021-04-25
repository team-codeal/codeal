package com.example.prototypefirebase

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.prototypefirebase.codeal.CodealTeam
import com.example.utils.recyclers.lists.ListAdapter

class BoardActivity : AppCompatActivity() {

    private lateinit var teamID: String

    private lateinit var listNames: MutableList<String>
    private lateinit var listNameToTasksList: MutableMap<String, MutableList<String>>

    private lateinit var tasksRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board)
        supportActionBar?.hide();

        tasksRecyclerView = findViewById(R.id.item_list_list)
        PagerSnapHelper().attachToRecyclerView(tasksRecyclerView)
        tasksRecyclerView.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL, false)

        teamID = intent.getStringExtra("TeamID").toString()

        CodealTeam(teamID, ::getTasks)
    }

    private fun getTasks(team: CodealTeam) {
        listNames = ArrayList(team.lists.keys)
        listNameToTasksList = hashMapOf()
        team.lists.forEach { (listName, taskList) ->
            listNameToTasksList[listName] = taskList.toMutableList()
        }
        tasksRecyclerView.adapter = ListAdapter(listNames, listNameToTasksList, this)
    }

    fun openAddTask(view: View) {
        val taskIntent = Intent(this, AddTaskActivity::class.java)
        taskIntent.putExtra("TeamID", teamID)
        startActivity(taskIntent)
    }

}