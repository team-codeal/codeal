package com.example.prototypefirebase

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prototypefirebase.codeal.CodealTask
import com.example.prototypefirebase.codeal.CodealTeam
import com.example.utils.recyclers.tasks.OnTaskClickListener
import com.example.utils.recyclers.tasks.TaskAdapter
import java.util.*
import java.util.stream.Collectors

class BoardActivity : AppCompatActivity(), OnTaskClickListener {

    private var tasks: MutableList<CodealTask> = mutableListOf()
    private lateinit var teamID: String

    private lateinit var team: CodealTeam

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

        team = CodealTeam(teamID, ::getTasks)
    }


    override fun onResume() {
        super.onResume()
        if (team.ready) getTasks(team)
    }

    fun openAddTask(view: View) {
        val taskIntent = Intent(this, AddTaskActivity::class.java)
        taskIntent.putExtra("TeamID", teamID)
        startActivity(taskIntent)
    }

    private fun getTasks(team: CodealTeam) {
        // todo clearing every time is pointless. could use live updating

        tasks.clear()
        tasks.addAll(team.tasks.stream()
            .map { x -> CodealTask(x) { taskAdapter.notifyDataSetChanged() } }
            .collect(Collectors.toList()))

    }

    override fun onTaskItemClicked(position: Int) {

        val intent = Intent(this, ViewTaskDetailActivity::class.java)
        intent.putExtra("TaskID", tasks[position].id)
        startActivity(intent)
    }
}