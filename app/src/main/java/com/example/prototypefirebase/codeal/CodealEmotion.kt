package com.example.prototypefirebase.codeal

import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class CodealEmotion : CodealEntity<CodealEmotion> {

    var ownerID: String = ""
        private set
    var parentObjectID: String = ""
        private set
    var date: Date = Date()
        private set

    companion object {
        internal const val EMOTIONS_DB_COLLECTION_NAME: String = "emotions"
        internal const val EMOTIONS_DB_OWNER_ID_FIELD_NAME: String = "owner_id"
        internal const val EMOTIONS_DB_PARENT_OBJECT_ID_FIELD_NAME: String = "parent_object"
        internal const val EMOTIONS_DB_DATE_FIELD_NAME: String = "date"
    }

    // constructor for an existing emotion
    constructor(emotionID: String) {
        id = emotionID
    }

    // constructor for a new emotion
    internal constructor(ownerID: String, parentObjectID: String) {
        this.ownerID = ownerID
        this.parentObjectID = parentObjectID
        this.date = Date()
        uploadEmotionInfoToDB()
    }

    private fun uploadEmotionInfoToDB() {
        val emotionsDB = getDB()
        val emotionInfo = mutableMapOf<String, Any>(
            EMOTIONS_DB_OWNER_ID_FIELD_NAME to ownerID,
            EMOTIONS_DB_PARENT_OBJECT_ID_FIELD_NAME to parentObjectID
        )
        emotionsDB.add(emotionInfo).addOnSuccessListener { emotionDocument ->
            id = emotionDocument.id
            ready = true
            if ((listeners.isNotEmpty() or oneTimeListeners.isNotEmpty())) {
                setFirebaseListener()
            }
        }
    }

    internal fun delete() {
        val emotionsDB = getDB()
        emotionsDB.document(id).delete()
    }

    override fun getDB(): CollectionReference =
        FirebaseFirestore.getInstance().collection(EMOTIONS_DB_COLLECTION_NAME)

    override fun getDataFromSnapshot(snapshot: DocumentSnapshot) {
        val emotionsDB = getDB()
        ownerID = snapshot.get(EMOTIONS_DB_OWNER_ID_FIELD_NAME) as String? ?: run {
            val newOwnerID = ""
            emotionsDB.document(id).update(EMOTIONS_DB_OWNER_ID_FIELD_NAME, newOwnerID)
            newOwnerID
        }
        parentObjectID = snapshot
            .get(EMOTIONS_DB_PARENT_OBJECT_ID_FIELD_NAME) as String? ?: run {
            val newParentObjectID = ""
            emotionsDB.document(id).update(
                EMOTIONS_DB_PARENT_OBJECT_ID_FIELD_NAME,
                newParentObjectID
            )
            newParentObjectID
        }
        date = (snapshot.get(EMOTIONS_DB_DATE_FIELD_NAME) as Timestamp?)?.toDate()
            ?: run {
                val newDate = Date()
                emotionsDB.document(id).update(EMOTIONS_DB_DATE_FIELD_NAME, newDate)
                newDate
            }
    }

}
