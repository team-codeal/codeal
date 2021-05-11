package com.example.prototypefirebase.codeal

import android.net.Uri
import com.example.prototypefirebase.codeal.CodealEntity.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import java.lang.IllegalStateException

// TODO this class really is begging for a real-time listener which would invoke the callback

class CodealUser : CodealEntity<CodealUser> {

    private var fbUser: FirebaseUser =
        getUserFromFirebase() ?: throw IllegalStateException("The user is not logged in")

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

    var isSelf: Boolean = false
        private set
        get() = fbUser.uid == id

    private var reactionListener : ListenerRegistration? = null

    companion object {
        const val USER_DB_COLLECTION_NAME: String = "user_profiles"
        const val USER_DB_USER_NAME_FIELD_NAME: String = "name"
        const val USER_DB_USER_BIO_FIELD_NAME: String = "bio"
        const val USER_DB_USER_STATUS_FIELD_NAME: String = "status"
        const val USER_DB_USER_MAIL_FIELD_NAME: String = "mail"
        const val USER_DB_USER_TEAMS_FIELD_NAME: String = "teams"
        const val USER_DB_USER_PHOTO_URL_FIELD_NAME: String = "photo_url"
        const val USER_DB_USER_INCOMING_REACTIONS_FIELD_NAME: String = "incoming_reactions"
    }

    internal constructor() {
        // it is asserted that the user is logged in
        id = fbUser.uid
        isSelf = true
    }

    internal constructor(userID: String) {
        id = userID
    }

    fun change(name: String = this.name, bio: String = this.bio, status: String = this.status) {
        //TODO only update changed values. Use reflection?
        this.name = name
        this.bio = bio
        this.status = status
        uploadUserInfoToDB()
    }

    // TODO strange architecture
    internal fun sendReaction() {
        getDB().document(id).update(USER_DB_USER_INCOMING_REACTIONS_FIELD_NAME,
            FieldValue.arrayUnion(""))
    }

    // TODO refactor for adding a new listener instead of deleting older ones
    var incomingReactionCallback: (() -> Unit)? = null
        set(value) {
            reactionListener?.remove()
            field = value
            reactionListener = getDB().document(id)
                .addSnapshotListener { userSnapshot, e ->
                    if (e != null) {
                        return@addSnapshotListener
                    }

                    val reactions = (userSnapshot?.get(USER_DB_USER_INCOMING_REACTIONS_FIELD_NAME)
                            as? List<*>)?.filterIsInstance<String>()

                    if (reactions?.isNotEmpty() == true) {
                        getDB().document(id).update(USER_DB_USER_INCOMING_REACTIONS_FIELD_NAME,
                            FieldValue.arrayRemove(""))
                        value?.invoke()
                    }
                }
        }

    private fun getFirebaseUserObject(): FirebaseUser {
        return if (isSelf) fbUser else throw IllegalStateException("This user is not the current " +
                "app user")
    }

    private fun uploadUserInfoToDB() {
        val userDB = getDB()
        val userInfo = mutableMapOf(
            USER_DB_USER_NAME_FIELD_NAME to this.name,
            USER_DB_USER_BIO_FIELD_NAME to this.bio,
            USER_DB_USER_STATUS_FIELD_NAME to this.status,
            USER_DB_USER_MAIL_FIELD_NAME to this.mail,
            USER_DB_USER_TEAMS_FIELD_NAME to this.teams,
            USER_DB_USER_PHOTO_URL_FIELD_NAME to this.photoURL.toString()
        )
        userDB.document(id).update(userInfo)
    }

    private fun getUserFromFirebase(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }

    override fun getDB(): CollectionReference
            = FirebaseFirestore.getInstance().collection(USER_DB_COLLECTION_NAME)

    override fun getDataFromSnapshot(snapshot: DocumentSnapshot) {
        val userDB = getDB()
        if (!snapshot.exists()) {
            // user doesn't have a profile yet
            userDB.document(id).set(emptyMap<String, Any>())
            return
        }
        name = snapshot.get(USER_DB_USER_NAME_FIELD_NAME) as String? ?: run {
            val newName = fbUser.displayName ?: ""
            userDB.document(id).update(USER_DB_USER_NAME_FIELD_NAME, newName)
            newName
        }
        bio = snapshot.get(USER_DB_USER_BIO_FIELD_NAME) as String? ?: run {
            val newBio = ""
            userDB.document(id).update(USER_DB_USER_BIO_FIELD_NAME, newBio)
            newBio
        }
        status = snapshot.get(USER_DB_USER_STATUS_FIELD_NAME) as String? ?: run {
            val newStatus = ""
            userDB.document(id).update(USER_DB_USER_STATUS_FIELD_NAME, newStatus)
            newStatus
        }
        mail = snapshot.get(USER_DB_USER_MAIL_FIELD_NAME) as String? ?: run {
            val newMail = if (isSelf) fbUser.email.toString() else null
            userDB.document(id).update(USER_DB_USER_MAIL_FIELD_NAME, newMail)
            newMail ?: ""
        }
        teams = (snapshot.get(USER_DB_USER_TEAMS_FIELD_NAME)
                as? List<*>)?.filterIsInstance<String>() ?: run {
            val newTeams = emptyList<String>()
            userDB.document(id).update(USER_DB_USER_TEAMS_FIELD_NAME, newTeams)
            newTeams
        }
        val photoURLString =
            snapshot.get(USER_DB_USER_PHOTO_URL_FIELD_NAME) as String? ?: run {
                if (!isSelf) return@run ""
                val newPhotoURL = getFirebaseUserObject().photoUrl?.toString()
                userDB.document(id).update(USER_DB_USER_PHOTO_URL_FIELD_NAME, newPhotoURL)
                newPhotoURL
            }
        val reactions = (snapshot.get(USER_DB_USER_TEAMS_FIELD_NAME)
                as? List<*>)?.filterIsInstance<String>() ?: run {
            val newIncomingReactions = emptyList<String>()
            userDB.document(id).update(
                USER_DB_USER_INCOMING_REACTIONS_FIELD_NAME,
                newIncomingReactions
            )
            newIncomingReactions
        }
        if (reactions.isNotEmpty()) userDB.document(id)
            .update(USER_DB_USER_INCOMING_REACTIONS_FIELD_NAME, emptyList<String>())
        photoURL = Uri.parse(photoURLString)
    }

}
