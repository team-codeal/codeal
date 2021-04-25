package com.example.prototypefirebase.codeal

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.IllegalStateException

// TODO this class really is begging for a real-time listener which would invoke the callback

typealias CodealUserCallback = ((CodealUser) -> Unit)

class CodealUser {


    private var fbUser: FirebaseUser =
        getUserFromFirebase() ?: throw IllegalStateException("The user is not logged in")

    var id: String
        private set
    var name: String = ""
        private set
    var status: String = ""
        private set
    var bio: String = ""
        private set

    var ready: Boolean = false
        private set

    var isSelf: Boolean = false
        private set
        get() = fbUser.uid == id

    var updateCallback: CodealUserCallback? = null

    companion object {
        const val USER_DB_COLLECTION_NAME: String = "user_profiles"
        const val USER_DB_USER_NAME_FIELD_NAME: String = "name"
        const val USER_DB_USER_BIO_FIELD_NAME: String = "bio"
        const val USER_DB_USER_STATUS_FIELD_NAME: String = "status"
    }

    constructor(callback: CodealUserCallback? = null) {
        // it is asserted that the user is logged in
        updateCallback = callback
        id = fbUser.uid
        initUserInfoById()
    }

    constructor(userID: String, callback: CodealUserCallback? = null) {
        updateCallback = callback
        id = userID
        initUserInfoById()
    }

    fun getFirebaseUserObject(): FirebaseUser {
        return if (isSelf) fbUser else throw IllegalStateException("This user is not the current " +
                "app user")
    }

    fun change(name: String = this.name, bio: String = this.bio, status: String = this.status) {
        //TODO only update changed values. Use reflection?
        this.name = name
        this.bio = bio
        this.status = status
        uploadUserInfoToDB()
    }

    private fun uploadUserInfoToDB() {
        val userDB = userDB()
        val userInfo = mutableMapOf<String, Any>(
            USER_DB_USER_NAME_FIELD_NAME to this.name,
            USER_DB_USER_BIO_FIELD_NAME to this.bio,
            USER_DB_USER_STATUS_FIELD_NAME to this.status
        )
        userDB.document(id).update(userInfo)
    }

    private fun userDB() = FirebaseFirestore.getInstance().collection(USER_DB_COLLECTION_NAME)

    private fun initUserInfoById() {
        val userDB = userDB()
        userDB.document(id).get()
            .addOnSuccessListener { profile ->
                name = profile?.get(USER_DB_USER_NAME_FIELD_NAME) as String? ?:
                        run {
                            val newName = fbUser.displayName ?: ""
                            userDB.document(id).update(USER_DB_USER_NAME_FIELD_NAME, newName)
                            newName
                        }
                bio = profile?.get(USER_DB_USER_BIO_FIELD_NAME) as String? ?:
                        run {
                            val newBio = ""
                            userDB.document(id).update(USER_DB_USER_BIO_FIELD_NAME, newBio)
                            newBio
                        }
                status = profile?.get(USER_DB_USER_STATUS_FIELD_NAME) as String? ?:
                    run {
                        val newStatus = ""
                        userDB.document(id).update(USER_DB_USER_STATUS_FIELD_NAME, newStatus)
                        newStatus
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