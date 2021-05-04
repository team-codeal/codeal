package com.example.prototypefirebase.codeal

import com.google.firebase.firestore.FirebaseFirestore

typealias CodealEmotionCallback = ((CodealEmotion) -> Unit)

class CodealEmotion : CodealEntity {

    var id: String = ""
        private set

    var ownerID: String = ""
        private set

    var parentObjectID: String = ""
        private set

    var ready: Boolean = false
        private set

    var updateCallback: CodealEmotionCallback? = null

    companion object {
        internal const val EMOTIONS_DB_COLLECTION_NAME: String = "emotions"
        internal const val EMOTIONS_DB_OWNER_ID_FIELD_NAME: String = "owner_id"
        internal const val EMOTIONS_DB_PARENT_OBJECT_ID_FIELD_NAME: String = "parent_object"

        internal fun emotionsDB() = FirebaseFirestore.getInstance()
            .collection(EMOTIONS_DB_COLLECTION_NAME)
    }

    // constructor for an existing emotion
    constructor(emotionID: String, callback: CodealEmotionCallback? = null) {
        id = emotionID
        updateCallback = callback
        initEmotionInfoByID()
    }

    // constructor for a new emotion
    internal constructor(ownerID: String, parentObjectID: String,
                callback: CodealEmotionCallback? = null) {
        this.ownerID = ownerID
        this.parentObjectID = parentObjectID
        updateCallback = callback
        uploadEmotionInfoToDB()
    }

    private fun uploadEmotionInfoToDB() {
        val emotionsDB = emotionsDB()
        val emotionInfo = mutableMapOf<String, Any>(
            EMOTIONS_DB_OWNER_ID_FIELD_NAME to ownerID,
            EMOTIONS_DB_PARENT_OBJECT_ID_FIELD_NAME to parentObjectID
        )
        emotionsDB.add(emotionInfo).addOnSuccessListener { emotionDocument ->
            this.id = emotionDocument.id
            ready = true
            updateCallback?.invoke(this)
        }
    }

    private fun initEmotionInfoByID() {
        val emotionsDB = emotionsDB()
        emotionsDB.document(id).get()
            .addOnSuccessListener { tasksDocument ->
                ownerID = tasksDocument?.get(EMOTIONS_DB_OWNER_ID_FIELD_NAME) as String? ?:
                        run {
                            val newOwnerID = ""
                            emotionsDB.document(id).update(EMOTIONS_DB_OWNER_ID_FIELD_NAME, newOwnerID)
                            newOwnerID
                        }
                parentObjectID = tasksDocument
                    ?.get(EMOTIONS_DB_PARENT_OBJECT_ID_FIELD_NAME) as String? ?:
                        run {
                            val newParentObjectID = ""
                            emotionsDB.document(id).update(EMOTIONS_DB_PARENT_OBJECT_ID_FIELD_NAME,
                                newParentObjectID)
                            newParentObjectID
                        }
                ready = true
                updateCallback?.invoke(this)
            }
            .addOnFailureListener { exception ->
                throw exception
            }
    }

    internal fun delete() {
        val emotionsDB = emotionsDB()
        emotionsDB.document(id).delete()
    }

}