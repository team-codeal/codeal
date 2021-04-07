package com.example.prototypefirebase.codeal

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.IllegalStateException

//TODO This Task class is for the global team "ultimate team". change later, delete the todo

typealias CodealTaskCallback = ((CodealTask) -> Unit)

class CodealTask(taskID: String, callback: CodealTaskCallback? = null) {

    private var fbUser: FirebaseUser

    var id: String = taskID
        private set
    var name: String = ""
        private set
    var content: String = ""
        private set
    var listName: String = ""
        private set

    private var ready: Boolean = false

    var updateCallback: CodealTaskCallback? = callback

    companion object {
        private const val TASKS_DB_COLLECTION_NAME: String = "tasks1"
        private const val TASKS_DB_TASK_NAME: String = "Name"
        private const val TASKS_DB_TASK_CONTENT: String = "Text"
        private const val TASK_DB_TASK_LIST: String = "list"
    }

    init {
        fbUser = getUserFromFirebase() ?: throw IllegalStateException("The user is not logged in")
        initTaskInfoByID()
    }

    fun change(name: String = this.name,
               content: String = this.content,
               listName: String = this.listName) {
        // TODO only update changed values. Use reflection?
        this.name = name
        this.content = content
        this.listName = listName
        uploadTaskInfoToDB()
    }

    private fun uploadTaskInfoToDB() {
        val tasksDB = FirebaseFirestore.getInstance().collection(TASKS_DB_COLLECTION_NAME)
        val taskInfo = mutableMapOf<String, Any>(
            TASKS_DB_TASK_NAME to this.name,
            TASKS_DB_TASK_CONTENT to this.content,
            TASK_DB_TASK_LIST to this.listName
        )
        tasksDB.document(id).update(taskInfo)
    }

    private fun initTaskInfoByID() {
        val tasksDB = FirebaseFirestore.getInstance().collection(TASKS_DB_COLLECTION_NAME)
        tasksDB.document(id).get()
            .addOnSuccessListener { tasksDocument ->
                name = tasksDocument?.get(TASKS_DB_TASK_NAME) as String? ?:
                        run {
                            val newName = "New Task"
                            tasksDB.document(id).update(TASKS_DB_TASK_NAME, newName)
                            newName
                        }
                content = tasksDocument?.get(TASKS_DB_TASK_CONTENT) as String? ?:
                        run {
                            val newContent = ""
                            tasksDB.document(id).update(TASKS_DB_TASK_CONTENT, newContent)
                            newContent
                        }
                listName = tasksDocument?.get(TASK_DB_TASK_LIST) as String? ?:
                        run {
                            val newList = ""
                            tasksDB.document(id).update(TASK_DB_TASK_LIST, newList)
                            newList
                        }
                ready = true
                updateCallback?.invoke(this)
            }
            .addOnFailureListener { exception ->
                throw exception
            }
    }

    private fun getUserFromFirebase(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }

}
