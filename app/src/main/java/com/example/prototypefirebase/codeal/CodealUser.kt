package com.example.prototypefirebase.codeal

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.IllegalStateException
import java.net.URI
import java.net.URL

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
    var mail: String = ""
        private set
    var teams: List<String> = emptyList()
        private set
    var photoURL: Uri = Uri.EMPTY
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
        const val USER_DB_USER_MAIL_FIELD_NAME: String = "mail"
        const val USER_DB_USER_TEAMS_FIELD_NAME: String = "teams"
        const val USER_DB_USER_PHOTO_URL_FIELD_NAME: String = "photo_url"
    }

    constructor(callback: CodealUserCallback? = null) {
        // it is asserted that the user is logged in
        updateCallback = callback
        id = fbUser.uid
        mail = fbUser.email.toString()
        isSelf = true
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
            USER_DB_USER_STATUS_FIELD_NAME to this.status,
            USER_DB_USER_MAIL_FIELD_NAME to this.mail,
            USER_DB_USER_TEAMS_FIELD_NAME to this.teams,
            USER_DB_USER_PHOTO_URL_FIELD_NAME to this.photoURL
        )
        userDB.document(id).update(userInfo)
    }

    private fun userDB() = FirebaseFirestore.getInstance().collection(USER_DB_COLLECTION_NAME)

    private fun initUserInfoById() {
        val userDB = userDB()
        userDB.document(id).get()
            .addOnSuccessListener { profile ->
                if (!profile.exists()) {
                    // user doesn't have a profile yet
                    userDB.document(id).set(emptyMap<String, Any>())
                        .addOnSuccessListener{ _ -> initUserInfoById() }
                    return@addOnSuccessListener
                }
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
                mail = profile?.get(USER_DB_USER_MAIL_FIELD_NAME) as String? ?:
                        run {
                            val newMail = ""
                            userDB.document(id).update(USER_DB_USER_MAIL_FIELD_NAME, newMail)
                            newMail
                        }
                teams = (profile?.get(USER_DB_USER_TEAMS_FIELD_NAME)
                        as? List<*>)?.filterIsInstance<String>() ?:
                        run {
                            val newTeams = emptyList<String>()
                            userDB.document(id).update(USER_DB_USER_TEAMS_FIELD_NAME, newTeams)
                            newTeams
                        }
                val photoURLString = profile?.get(USER_DB_USER_PHOTO_URL_FIELD_NAME) as String? ?:
                        run {
                            if (!isSelf) return@run ""
                            val newPhotoURL = getFirebaseUserObject().photoUrl?.toString()
                            userDB.document(id).update(USER_DB_USER_PHOTO_URL_FIELD_NAME, newPhotoURL)
                            newPhotoURL
                        }
                photoURL = Uri.parse(photoURLString)
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