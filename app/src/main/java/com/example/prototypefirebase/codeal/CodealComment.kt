package com.example.prototypefirebase.codeal

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.collections.ArrayList

typealias CodealCommentCallback = ((CodealComment) -> Unit)

@Suppress("UNCHECKED_CAST")
class CodealComment : CodealEntity, Likeable<CodealComment> {

    var id: String = ""
        private set
    var ownerID: String = ""
        private set
    var content: String = ""
        private set
    var date: Date = Date()
    var parentTaskID: String = ""

    override var emotions: List<String> = emptyList()
        private set

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
        private const val COMMENTS_DB_EMOTIONS_IDS_FIELD_NAME: String = "emotions"
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
        this.emotions = emptyList()
        updateCallback = callback
        uploadCommentInfoToDB()
    }

    override fun likeBy(userID: String, callback: CodealCommentCallback?) {

        CodealEmotion(userID, id) { emotion ->
            emotions = emotions.toMutableList().also { it.add(emotion.id) }
            val commentsDB = commentsDB()
            commentsDB.document(id).update(COMMENTS_DB_EMOTIONS_IDS_FIELD_NAME,
                FieldValue.arrayUnion(emotion.id))
            callback?.invoke(this)
        }
    }

    override fun removeLikeBy(userID: String, callback: CodealCommentCallback?) {

        // find an emotion which was posted by user under userID
        // and delete it then

        val emotionsDB = CodealEmotion.emotionsDB()

        emotionsDB
            .whereEqualTo(CodealEmotion.EMOTIONS_DB_PARENT_OBJECT_ID_FIELD_NAME, id)
            .whereEqualTo(CodealEmotion.EMOTIONS_DB_OWNER_ID_FIELD_NAME, userID)
            .get().addOnSuccessListener { queryResult ->

                if (queryResult.isEmpty) return@addOnSuccessListener

                for (emotionDocument in queryResult.documents) {
                    val emotionID = emotionDocument.id
                    CodealEmotion(emotionID) { emotion ->
                        emotions = emotions.toMutableList().also { it.remove(emotion.id) }
                        val commentsDB = commentsDB()
                        commentsDB.document(id).update(COMMENTS_DB_EMOTIONS_IDS_FIELD_NAME,
                            FieldValue.arrayRemove(emotion.id))
                        emotion.delete()
                        callback?.invoke(this)
                    }
                }
            }

    }

    private fun commentsDB() =
        FirebaseFirestore.getInstance().collection(COMMENTS_DB_COLLECTION_NAME)

    private fun uploadCommentInfoToDB() {
        val commentsDB = commentsDB()
        val commentInfo = mutableMapOf(
            COMMENTS_DB_CONTENT_FIELD_NAME to content,
            COMMENTS_DB_OWNER_ID_FIELD_NAME to ownerID,
            COMMENTS_DB_DATE_FIELD_NAME to date,
            COMMENTS_DB_PARENT_TASK_ID_FIELD_NAME to parentTaskID,
            COMMENTS_DB_EMOTIONS_IDS_FIELD_NAME to emotions
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
                emotions = (commentDocument?.get(COMMENTS_DB_EMOTIONS_IDS_FIELD_NAME)
                        as? List<*>)?.filterIsInstance<String>() ?:
                        run {
                            val newEmotionsList = emptyList<String>()
                            commentsDB.document(id).update(COMMENTS_DB_EMOTIONS_IDS_FIELD_NAME,
                                newEmotionsList)
                            newEmotionsList
                        }
                ready = true
                updateCallback?.invoke(this)
            }
            .addOnFailureListener { exception ->
                throw exception
            }
    }

}
