package com.example.prototypefirebase.codeal

import com.example.prototypefirebase.codeal.factories.CodealEmotionFactory
import com.example.prototypefirebase.codeal.factories.CodealTeamFactory
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

//TODO This Task class is for the global team "ultimate team". change later, delete the todo

class CodealTask : CodealEntity<CodealTask>, Likeable<CodealTask> {

    var name: String = ""
        private set
    var content: String = ""
        private set
    var listName: String = ""
        private set
    var deadline: Date? = null
    var teamID: String = ""
        private set
    var ownerID: String = ""
        private set
    var commentsIDs: List<String> = emptyList()
        private set
    override var emotions: List<String> = emptyList()
        private set

    companion object {
        private const val TASKS_DB_COLLECTION_NAME: String = "tasks1"
        private const val TASKS_DB_TASK_NAME: String = "Name"
        private const val TASKS_DB_TASK_CONTENT: String = "Text"
        private const val TASKS_DB_TASK_LIST: String = "list"
        private const val TASKS_DB_TEAM_ID: String = "teamID"
        private const val TASKS_DB_COMMENTS_IDs: String = "comments_ids"
        private const val TASKS_DB_DEADLINE: String = "deadline"
        private const val TASKS_DB_EMOTIONS_IDS_FIELD_NAME: String = "emotions"
    }

    // constructor for an existing task
    constructor(taskID: String) {
        id = taskID
    }

    // constructor for a new
    constructor(
        taskName: String,
        taskContent: String,
        teamID: String,
        listName: String,
        ownerID: String,
        deadLine: Date? = null) {
        name = taskName
        content = taskContent
        this.teamID = teamID
        this.listName = listName
        this.deadline = deadLine
        this.ownerID = ownerID
        this.emotions = emptyList()
        uploadTaskInfoToDB()
    }

    fun change(
        name: String = this.name,
        content: String = this.content,
        listName: String = this.listName,
        commentsIDs: List<String> = this.commentsIDs,
        deadline: Date? = this.deadline
    ) {
        // TODO only update changed values. Use reflection?
        this.name = name
        this.content = content
        val prevListName = this.listName
        this.listName = listName
        this.commentsIDs = commentsIDs
        this.deadline = deadline

        if (listName != prevListName) {
            CodealTeamFactory.get(teamID).addOnReady {
                it.deleteTask(id, prevListName)
                it.addTask(id, listName)
            }
        }

        uploadTaskInfoToDB()
    }


    private fun uploadTaskInfoToDB() {
        val tasksDB = FirebaseFirestore.getInstance().collection(TASKS_DB_COLLECTION_NAME)
        val taskInfo = mutableMapOf(
            TASKS_DB_TASK_NAME to this.name,
            TASKS_DB_TASK_CONTENT to this.content,
            TASKS_DB_COMMENTS_IDs to commentsIDs,
            TASKS_DB_TASK_LIST to this.listName,
            TASKS_DB_TEAM_ID to this.teamID,
            TASKS_DB_DEADLINE to this.deadline
        )
        if (id != "") {
            // if the task already existed in the database
            tasksDB.document(id).update(taskInfo)
        } else {
            // if the task is new
            tasksDB.add(taskInfo).addOnSuccessListener { taskDocument ->
                id = taskDocument.id
                CodealTeamFactory.get(teamID).addOnReady { it.addTask(id, listName) }
                ready = true
                if ((listeners.isNotEmpty() or oneTimeListeners.isNotEmpty())) {
                    setFirebaseListener()
                }
            }
        }
    }

    fun delete() {
        val db = FirebaseFirestore.getInstance()

        CodealTeamFactory.get(teamID).addOnReady {
            it.deleteTask(id, listName)

            db.collection(TASKS_DB_COLLECTION_NAME).document(id).delete()
        }
    }

    override fun getDB(): CollectionReference
            = FirebaseFirestore.getInstance().collection(TASKS_DB_COLLECTION_NAME)

    override fun getDataFromSnapshot(snapshot: DocumentSnapshot) {
        val tasksDB = getDB()
        name = snapshot.get(TASKS_DB_TASK_NAME) as String? ?: run {
            val newName = "New Task"
            tasksDB.document(id).update(TASKS_DB_TASK_NAME, newName)
            newName
        }
        content = snapshot.get(TASKS_DB_TASK_CONTENT) as String? ?: run {
            val newContent = ""
            tasksDB.document(id).update(TASKS_DB_TASK_CONTENT, newContent)
            newContent
        }
        listName = snapshot.get(TASKS_DB_TASK_LIST) as String? ?: run {
            val newList = ""
            tasksDB.document(id).update(TASKS_DB_TASK_LIST, newList)
            newList
        }
        teamID = snapshot.get(TASKS_DB_TEAM_ID) as String? ?: run {
            val newTeamID = ""
            tasksDB.document(id).update(TASKS_DB_TEAM_ID, newTeamID)
            newTeamID
        }
        commentsIDs = (snapshot.get(TASKS_DB_COMMENTS_IDs)
                as? List<*>)?.filterIsInstance<String>() ?: run {
            val newComments = emptyList<String>()
            tasksDB.document(id).update(TASKS_DB_COMMENTS_IDs, newComments)
            newComments
        }
        deadline = (snapshot.get(TASKS_DB_DEADLINE) as Timestamp?)?.toDate()
        emotions = (snapshot.get(TASKS_DB_EMOTIONS_IDS_FIELD_NAME)
                as? List<*>)?.filterIsInstance<String>() ?:
                run {
                    val newEmotionsList = emptyList<String>()
                    tasksDB.document(id).update(TASKS_DB_EMOTIONS_IDS_FIELD_NAME,
                        newEmotionsList)
                    newEmotionsList
                }
    }

    override fun likeBy(userID: String) {
        CodealEmotionFactory.create(userID, id).addOnReady { emotion ->
            val tasksDB = getDB()
            tasksDB.document(id).update(TASKS_DB_EMOTIONS_IDS_FIELD_NAME,
                FieldValue.arrayUnion(emotion.id))
        }
    }

    override fun removeLikeBy(userID: String) {
        val emotionsDB = FirebaseFirestore.getInstance()
            .collection(CodealEmotion.EMOTIONS_DB_COLLECTION_NAME)

        emotionsDB
            .whereEqualTo(CodealEmotion.EMOTIONS_DB_PARENT_OBJECT_ID_FIELD_NAME, id)
            .whereEqualTo(CodealEmotion.EMOTIONS_DB_OWNER_ID_FIELD_NAME, userID)
            .get().addOnSuccessListener { queryResult ->

                if (queryResult.isEmpty) return@addOnSuccessListener

                for (emotionDocument in queryResult.documents) {
                    val emotionID = emotionDocument.id
                    CodealEmotionFactory.get(emotionID).addOnReady { emotion ->
                        val commentsDB = getDB()
                        commentsDB.document(id).update(TASKS_DB_EMOTIONS_IDS_FIELD_NAME,
                            FieldValue.arrayRemove(emotion.id))
                        emotion.delete()
                    }
                }
            }
    }

}
