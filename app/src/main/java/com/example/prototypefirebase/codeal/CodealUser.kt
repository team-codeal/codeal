package com.example.prototypefirebase.codeal

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.IllegalStateException

class CodealUser {

    private lateinit var fbUser: FirebaseUser

    var id: String
        private set
    lateinit var name: String
        private set
    lateinit var bio: String
        private set

    var isSelf: Boolean = false
        private set

    companion object {
        private const val USER_DB_COLLECTION_NAME: String = "user_profiles"
        private const val USER_DB_USER_NAME_FIELD_NAME: String = "name"
        private const val USER_DB_USER_BIO_FIELD_NAME: String = "bio"
    }

    constructor() {
        // it is asserted that the user is logged in
        fbUser = getUserFromFirebase() ?: throw IllegalStateException("The user is not logged in")
        id = fbUser.uid
        isSelf = true
        initUserInfoById()
    }

    constructor(userID: String) {
        id = userID
        isSelf = false
        initUserInfoById()
    }

    fun getFirebaseUserObject(): FirebaseUser {
        return if (isSelf) fbUser else throw IllegalStateException("This user is not the current " +
                "app user")
    }

    fun change(name: String = this.name, bio: String = this.bio) {
        //TODO only update changed values. Use reflection?
        this.name = name
        this.bio = bio
        uploadUserInfoToDB()
    }

    private fun uploadUserInfoToDB() {
        val userDB = FirebaseFirestore.getInstance().collection(USER_DB_COLLECTION_NAME)
        val userInfo = mutableMapOf<String, Any>(
            USER_DB_USER_NAME_FIELD_NAME to this.name,
            USER_DB_USER_BIO_FIELD_NAME to this.bio
        )
        userDB.document(id).update(userInfo)
    }

    private fun initUserInfoById() {
        val userDB = FirebaseFirestore.getInstance().collection(USER_DB_COLLECTION_NAME)
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
            }
            .addOnFailureListener { exception ->
                throw exception
            }
    }

    private fun getUserFromFirebase(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }


}