package com.example.prototypefirebase.codeal

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

//TODO This Task class is for the global team "ultimate team". change later, delete the todo

typealias CodealTaskCallback = ((CodealTask) -> Unit)

class CodealTask : CodealEntity {

    var id: String = ""
        private set
    var name: String = ""
        private set
    var content: String = ""
        private set
    var listName: String = ""
        private set
    var teamID: String = ""
    var commentsIDs: List<String> = emptyList()
        private set

    private var ready: Boolean = false

    var updateCallback: CodealTaskCallback? = null

    companion object {
        private const val TASKS_DB_COLLECTION_NAME: String = "tasks1"
        private const val TASKS_DB_TASK_NAME: String = "Name"
        private const val TASKS_DB_TASK_CONTENT: String = "Text"
        private const val TASKS_DB_TASK_LIST: String = "list"
        private const val TASKS_DB_TEAM_ID: String = "teamID"
        private const val TASKS_DB_COMMENTS_IDs: String = "comments_ids"
    }

    // constructor for an existing task
    constructor(taskID: String, callback: CodealTaskCallback? = null) {
        id = taskID
        updateCallback = callback
        initTaskInfoByID()
    }

    // constructor for a new
    constructor(
        taskName: String, taskContent: String, teamID: String, listName: String,
        callback: CodealTaskCallback? = null
    ) {
        name = taskName
        content = taskContent
        this.teamID = teamID
        this.listName = listName
        updateCallback = callback
        uploadTaskInfoToDB()
    }

    fun change(
        name: String = this.name,
        content: String = this.content,
        listName: String = this.listName,
        commentsIDs: List<String> = this.commentsIDs
    ) {
        // TODO only update changed values. Use reflection?
        this.name = name
        this.content = content
        val prevListName = this.listName
        this.listName = listName
        this.commentsIDs = commentsIDs

        if (listName != prevListName) {
            CodealTeam(teamID) {
                it.deleteTask(id, prevListName)
                it.addTask(id, listName)
            }
        }

        uploadTaskInfoToDB()
    }


    private fun uploadTaskInfoToDB() {
        val tasksDB = FirebaseFirestore.getInstance().collection(TASKS_DB_COLLECTION_NAME)
        val taskInfo = mutableMapOf<String, Any>(
            TASKS_DB_TASK_NAME to this.name,
            TASKS_DB_TASK_CONTENT to this.content,
            TASKS_DB_COMMENTS_IDs to commentsIDs,
            TASKS_DB_TASK_LIST to this.listName,
            TASKS_DB_TEAM_ID to this.teamID
        )
        if (id != "") {
            // if the task already existed in the database
            tasksDB.document(id).update(taskInfo)
        } else {
            // if the task is new
            tasksDB.add(taskInfo).addOnSuccessListener { taskDocument ->
                this.id = taskDocument.id
                CodealTeam(teamID) { it.addTask(id, listName) }
                ready = true
                updateCallback?.invoke(this)
            }
        }
    }

    private fun initTaskInfoByID() {
        val tasksDB = FirebaseFirestore.getInstance().collection(TASKS_DB_COLLECTION_NAME)
        tasksDB.document(id).get()
            .addOnSuccessListener { tasksDocument ->
                name = tasksDocument?.get(TASKS_DB_TASK_NAME) as String? ?: run {
                    val newName = "New Task"
                    tasksDB.document(id).update(TASKS_DB_TASK_NAME, newName)
                    newName
                }
                content = tasksDocument?.get(TASKS_DB_TASK_CONTENT) as String? ?: run {
                    val newContent = ""
                    tasksDB.document(id).update(TASKS_DB_TASK_CONTENT, newContent)
                    newContent
                }
                listName = tasksDocument?.get(TASKS_DB_TASK_LIST) as String? ?: run {
                    val newList = ""
                    tasksDB.document(id).update(TASKS_DB_TASK_LIST, newList)
                    newList
                }
                teamID = tasksDocument?.get(TASKS_DB_TEAM_ID) as String? ?: run {
                    val newTeamID = ""
                    tasksDB.document(id).update(TASKS_DB_TEAM_ID, newTeamID)
                    newTeamID
                }
                commentsIDs = (tasksDocument?.get(TASKS_DB_COMMENTS_IDs)
                        as? List<*>)?.filterIsInstance<String>() ?: run {
                    val newComments = emptyList<String>()
                    tasksDB.document(id).update(TASKS_DB_COMMENTS_IDs, newComments)
                    newComments
                }
                ready = true
                updateCallback?.invoke(this)
            }
            .addOnFailureListener { exception ->
                throw exception
            }
    }

    fun delete() {
        val db = FirebaseFirestore.getInstance()
        db.collection(TASKS_DB_COLLECTION_NAME).document(id)
            .delete()
        // TODO get constant from CodealTeam which describes the name of teams collection
        CodealTeam(teamID) {
            it.deleteTask(id, listName)
        }
    }

    private fun getUserFromFirebase(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }

}
