package com.example.prototypefirebase.codeal

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.collections.ArrayList

typealias CodealCommentCallback = ((CodealComment) -> Unit)

class CodealComment {

    var id: String = ""
        private set
    var ownerID: String = ""
        private set
    var content: String = ""
        private set
    var date: Date = Date()
    var parentTaskID: String = ""

    var ready: Boolean = false
        private set

    var updateCallback: CodealCommentCallback? = null
        set(value) {
            field = value
            if (ready) value?.invoke(this)
        }

    companion object {
        private const val COMMENTS_DB_COLLECTION_NAME: String = "comments"
        private const val COMMENTS_DB_CONTENT_FIELD_NAME: String = "content"
        private const val COMMENTS_DB_DATE_FIELD_NAME: String = "date"
        private const val COMMENTS_DB_OWNER_ID_FIELD_NAME: String = "owner_id"
        private const val COMMENTS_DB_PARENT_TASK_ID_FIELD_NAME: String = "parent_comment_id"
    }


    //constructor for an existing comment
    constructor(commentID: String, callback: CodealCommentCallback? = null) {
        this.id = commentID
        updateCallback = callback
        initTaskInfoByID()
    }

    //constructor for a new comment
    constructor(parentTaskID: String, content: String, ownerID: String, callback: CodealCommentCallback? = null) {
        this.parentTaskID = parentTaskID
        this.content = content
        this.ownerID = ownerID
        this.date = Date()
        updateCallback = callback
        uploadCommentInfoToDB()
    }

    private fun uploadCommentInfoToDB() {
        val commentsDB = FirebaseFirestore.getInstance().collection(COMMENTS_DB_COLLECTION_NAME)
        val commentInfo = mutableMapOf(
            COMMENTS_DB_CONTENT_FIELD_NAME to content,
            COMMENTS_DB_OWNER_ID_FIELD_NAME to ownerID,
            COMMENTS_DB_DATE_FIELD_NAME to date,
            COMMENTS_DB_PARENT_TASK_ID_FIELD_NAME to parentTaskID
        )
        commentsDB.add(commentInfo).addOnSuccessListener { commentDocument ->
            id = commentDocument.id
            CodealTask(parentTaskID) { task ->
                val oldCommentsIDs: List<String> = task.commentsIDs
                val newCommentsIDs = ArrayList(oldCommentsIDs).apply { add(id) }
                task.change(commentsIDs = newCommentsIDs)
            }
            ready = true
            updateCallback?.invoke(this)
        }
    }

    private fun initTaskInfoByID() {
        val commentsDB = FirebaseFirestore.getInstance().collection(COMMENTS_DB_COLLECTION_NAME)
        commentsDB.document(id).get()
            .addOnSuccessListener { commentDocument ->
                ownerID = commentDocument?.get(COMMENTS_DB_OWNER_ID_FIELD_NAME) as String? ?:
                        run {
                            // TODO What needs to be done here is unclear
                            val newOwnerID = ""
                            commentsDB.document(id).update(COMMENTS_DB_OWNER_ID_FIELD_NAME,
                                newOwnerID)
                            newOwnerID
                        }
                parentTaskID = commentDocument?.get(COMMENTS_DB_PARENT_TASK_ID_FIELD_NAME) as String? ?:
                        run {
                            // TODO such kind of database state is illegal. should delete document
                            //  then?
                            val newParentTaskID = ""
                            commentsDB.document(id).update(COMMENTS_DB_OWNER_ID_FIELD_NAME,
                                newParentTaskID)
                            newParentTaskID
                        }
                content = commentDocument?.get(COMMENTS_DB_CONTENT_FIELD_NAME) as String? ?:
                        run {
                            val newContent = ""
                            commentsDB.document(id).update(COMMENTS_DB_CONTENT_FIELD_NAME,
                                newContent)
                            newContent
                        }
                date = (commentDocument?.get(COMMENTS_DB_DATE_FIELD_NAME) as Timestamp?)?.toDate()
                    ?:
                        run {
                            val newDate = Date()
                            commentsDB.document(id).update(COMMENTS_DB_DATE_FIELD_NAME, newDate)
                            newDate
                        }
                ready = true
                updateCallback?.invoke(this)
            }
            .addOnFailureListener { exception ->
                throw exception
            }
    }

}
