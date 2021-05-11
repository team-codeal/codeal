package com.example.prototypefirebase

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.prototypefirebase.codeal.CodealEntity
import com.example.prototypefirebase.codeal.CodealTeam
import com.example.prototypefirebase.codeal.factories.CodealTeamFactory
import com.example.utils.recyclers.lists.ListAdapter

class BoardActivity : AppCompatActivity() {

    private lateinit var teamID: String

    private var listNames: MutableList<String> = mutableListOf()
    private var listNameToTasksList: MutableMap<String, MutableList<String>>
            = hashMapOf()

    private lateinit var tasksRecyclerView: RecyclerView
    private var listAdapter: ListAdapter
            = ListAdapter(listNames, listNameToTasksList, this)

    private lateinit var teamInfoListener: CodealEntity<CodealTeam>.CodealListener

    private lateinit var teamNameHolder: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board)
        supportActionBar?.hide();

        tasksRecyclerView = findViewById(R.id.item_list_list)
        PagerSnapHelper().attachToRecyclerView(tasksRecyclerView)
        tasksRecyclerView.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL, false)

        teamID = intent.getStringExtra("TeamID").toString()


        teamNameHolder = findViewById(com.example.prototypefirebase.R.id.textViewLabel)

        tasksRecyclerView.adapter = listAdapter
        tasksRecyclerView.adapter?.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

    }

    override fun onResume() {
        super.onResume()
        teamInfoListener = CodealTeamFactory.get(teamID).addListener { possiblyUpdatedTeam ->
            teamNameHolder.text = possiblyUpdatedTeam.name
            val possiblyUpdatedLists = possiblyUpdatedTeam.lists
            mergeListsWith(possiblyUpdatedLists)
        }
    }

    override fun onStop() {
        super.onStop()
        teamInfoListener.remove()
    }

    private fun mergeListsWith(newLists: MutableMap<String, List<String>>) {

        // to the current lists, add the new ones
        newLists.forEach { (listName, newTasks) ->
            if (!listNameToTasksList.containsKey(listName)) {
                listNameToTasksList[listName] = newTasks.toMutableList()
                listNames.add(listName)
                listAdapter.notifyItemInserted(listNames.size - 1)
            } else {

                val oldTasks = listNameToTasksList[listName]!!

                // delete deleted tasks
                oldTasks.forEachIndexed { index, task ->
                    if (!newTasks.contains(task)) {
                        oldTasks.removeAt(index)
                        listAdapter.notifyItemChanged(listNames.indexOf(listName),
                            ListAdapter.TaskChangedMessage(
                                ListAdapter.TaskChangingCommitment.TASK_DELETED,
                                index))
                    }
                }

                // add new tasks to the list
                newTasks.forEach { task ->
                    if (!oldTasks.contains(task)) {
                        oldTasks.add(task)
                        listAdapter.notifyItemChanged(listNames.indexOf(listName),
                            ListAdapter.TaskChangedMessage(
                                ListAdapter.TaskChangingCommitment.TASK_ADDED,
                                oldTasks.size - 1))
                    }
                }
            }
        }

        // from the current lists, delete the ones deleted
        listNameToTasksList.forEach { (listName, _) ->
            if (!newLists.containsKey(listName)) {
                listNameToTasksList.remove(listName)
                val indexToRemove = listNames.indexOf(listName)
                listNames.removeAt(indexToRemove)
                listAdapter.notifyItemRemoved(indexToRemove)
            }
        }
    }

    fun openAddTask(view: View) {
        val taskIntent = Intent(this, AddTaskActivity::class.java)
        taskIntent.putExtra("TeamID", teamID)
        startActivity(taskIntent)
    }

}