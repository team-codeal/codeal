package com.example.prototypefirebase.codeal

import com.example.prototypefirebase.codeal.factories.CodealEmotionFactory
import com.example.prototypefirebase.codeal.factories.CodealTaskFactory
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.collections.ArrayList

class CodealComment : CodealEntity<CodealComment>, Likeable<CodealComment> {

    var ownerID: String = ""
        private set
    var content: String = ""
        private set
    var date: Date = Date()
        private set
    var parentTaskID: String = ""
        private set

    override var emotions: List<String> = emptyList()
        private set

    companion object {
        private const val COMMENTS_DB_COLLECTION_NAME: String = "comments"
        private const val COMMENTS_DB_CONTENT_FIELD_NAME: String = "content"
        private const val COMMENTS_DB_DATE_FIELD_NAME: String = "date"
        private const val COMMENTS_DB_OWNER_ID_FIELD_NAME: String = "owner_id"
        private const val COMMENTS_DB_PARENT_TASK_ID_FIELD_NAME: String = "parent_object_id"
        private const val COMMENTS_DB_EMOTIONS_IDS_FIELD_NAME: String = "emotions"
    }

    //constructor for an existing comment
    constructor(commentID: String) {
        this.id = commentID
    }

    //constructor for a new comment
    constructor(parentTaskID: String, content: String, ownerID: String) {
        this.parentTaskID = parentTaskID
        this.content = content
        this.ownerID = ownerID
        this.date = Date()
        this.emotions = emptyList()
        uploadCommentInfoToDB()
    }

    override fun likeBy(userID: String) {
        CodealEmotionFactory.create(userID, id).addOnReady { emotion ->
            val commentsDB = getDB()
            commentsDB.document(id).update(COMMENTS_DB_EMOTIONS_IDS_FIELD_NAME,
                FieldValue.arrayUnion(emotion.id))
        }
    }

    override fun removeLikeBy(userID: String) {

        // find an emotion which was posted by user under userID
        // and delete it then

        // TODO this is sorta bad, cause cannot call getDB of CodealEmotion
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
                        commentsDB.document(id).update(COMMENTS_DB_EMOTIONS_IDS_FIELD_NAME,
                            FieldValue.arrayRemove(emotion.id))
                        emotion.delete()
                    }
                }
            }

    }

    private fun uploadCommentInfoToDB() {
        val commentsDB = getDB()
        val commentInfo = mutableMapOf(
            COMMENTS_DB_CONTENT_FIELD_NAME to content,
            COMMENTS_DB_OWNER_ID_FIELD_NAME to ownerID,
            COMMENTS_DB_DATE_FIELD_NAME to date,
            COMMENTS_DB_PARENT_TASK_ID_FIELD_NAME to parentTaskID,
            COMMENTS_DB_EMOTIONS_IDS_FIELD_NAME to emotions
        )
        commentsDB.add(commentInfo).addOnSuccessListener { commentDocument ->
            id = commentDocument.id
            CodealTaskFactory.get(parentTaskID).addOnReady { task ->
                val oldCommentsIDs: List<String> = task.commentsIDs
                val newCommentsIDs = ArrayList(oldCommentsIDs).apply { add(id) }
                task.change(commentsIDs = newCommentsIDs)
                created = true
            }
        }
        ready = true
    }

    override fun getDB(): CollectionReference
            = FirebaseFirestore.getInstance().collection(COMMENTS_DB_COLLECTION_NAME)

    override fun getDataFromSnapshot(snapshot: DocumentSnapshot) {
        val commentsDB = getDB()
        ownerID = snapshot.get(COMMENTS_DB_OWNER_ID_FIELD_NAME) as String? ?:
                run {
                    // TODO What needs to be done here is unclear
                    val newOwnerID = ""
                    commentsDB.document(id).update(COMMENTS_DB_OWNER_ID_FIELD_NAME,
                        newOwnerID)
                    newOwnerID
                }
        parentTaskID = snapshot.get(COMMENTS_DB_PARENT_TASK_ID_FIELD_NAME) as String? ?:
                run {
                    // TODO such kind of database state is illegal. should delete document
                    //  then?
                    val newParentTaskID = ""
                    commentsDB.document(id).update(COMMENTS_DB_OWNER_ID_FIELD_NAME,
                        newParentTaskID)
                    newParentTaskID
                }
        content = snapshot.get(COMMENTS_DB_CONTENT_FIELD_NAME) as String? ?:
                run {
                    val newContent = ""
                    commentsDB.document(id).update(COMMENTS_DB_CONTENT_FIELD_NAME,
                        newContent)
                    newContent
                }
        date = (snapshot.get(COMMENTS_DB_DATE_FIELD_NAME) as Timestamp?)?.toDate()
            ?:
                    run {
                        val newDate = Date()
                        commentsDB.document(id).update(COMMENTS_DB_DATE_FIELD_NAME, newDate)
                        newDate
                    }
        emotions = (snapshot.get(COMMENTS_DB_EMOTIONS_IDS_FIELD_NAME)
                as? List<*>)?.filterIsInstance<String>() ?:
                run {
                    val newEmotionsList = emptyList<String>()
                    commentsDB.document(id).update(COMMENTS_DB_EMOTIONS_IDS_FIELD_NAME,
                        newEmotionsList)
                    newEmotionsList
                }
    }
}
