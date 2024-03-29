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
import com.example.prototypefirebase.codeal.suppliers.CodealTeamSupplier
import com.example.utils.recyclers.lists.ListAdapter
import java.lang.IllegalStateException

class BoardActivity : AppCompatActivity() {

    private lateinit var teamID: String

    private var currentTeam: CodealTeam? = null

    private val listCanonicalNames = listOf("Todo", "Doing", "Done")
    private var listNames: MutableList<String> = mutableListOf()
    private var listNameToTasksList: MutableMap<String, MutableList<String>>
            = hashMapOf()

    private lateinit var tasksRecyclerView: RecyclerView
    private var listAdapter: ListAdapter =
        ListAdapter(listNames,
            listNameToTasksList,
            ::openAddTaskActivity,
            { currentTeam?.lists = toCodealTeamMap(listNameToTasksList) },
            this)

    private fun toCodealTeamMap(boardMap: MutableMap<String, MutableList<String>>):
            MutableMap<String, List<String>> {
        val codealTeamMap = mutableMapOf<String, List<String>>()
        boardMap.forEach { (listName, list) ->
            codealTeamMap[listName] = list
        }
        return codealTeamMap
    }

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

        teamID = intent.getStringExtra("TeamID")!!

        CodealTeamSupplier.get(teamID).addOnReady { currentTeam = it }

        teamNameHolder = findViewById(com.example.prototypefirebase.R.id.textViewLabel)

        tasksRecyclerView.adapter = listAdapter
        tasksRecyclerView.adapter?.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

    }

    override fun onResume() {
        super.onResume()
        teamInfoListener = CodealTeamSupplier.get(teamID).addListener { possiblyUpdatedTeam ->
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

        fun listNameToCanonicalIndex(newListName: String): Int {
            val canonicalIndex = listCanonicalNames.indexOf(newListName)
            if (canonicalIndex == -1) {
                throw IllegalStateException("There is a custom list in this board. Not allowed")
            }
            return if (canonicalIndex > listNames.size) listNames.size else canonicalIndex
        }

        // to the current lists, add the new ones
        for ((listName, newTasks) in newLists) {
            if (!listNameToTasksList.containsKey(listName)) {
                listNameToTasksList[listName] = newTasks.toMutableList()
                val indexToInsert = listNameToCanonicalIndex(listName)
                listNames.add(indexToInsert, listName)
                listAdapter.notifyItemInserted(indexToInsert)
            } else {

                val oldTasks = listNameToTasksList[listName]!!

                // delete deleted tasks
                val tasksToDelete: MutableList<String> = mutableListOf()
                oldTasks.forEachIndexed { index, task ->
                    if (!newTasks.contains(task)) {
                        tasksToDelete.add(task)
                        listAdapter.notifyItemChanged(listNames.indexOf(listName),
                            ListAdapter.TaskChangedMessage(
                                ListAdapter.TaskChangingCommitment.TASK_DELETED,
                                index))
                    }
                }
                oldTasks.removeAll(tasksToDelete)

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

    private fun openAddTaskActivity(view: View, taskList: String) {
        view.isClickable = false
        val taskIntent = Intent(this, AddTaskActivity::class.java)
        taskIntent.putExtra("TeamID", teamID)
        taskIntent.putExtra("TaskList", taskList)
        startActivity(taskIntent)
        view.isClickable = true
    }

}
