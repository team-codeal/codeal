package com.example.prototypefirebase.codeal

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class CodealTeam {

    var teamName: String = ""
        private set
    var members: List<String> = emptyList()
        private set
    var id: String
        private set

    private lateinit var user: CodealUser

    //constructor for an existing command
    constructor(id: String) {
        this.id = id;
        user = CodealUser()
    }

    //constructor for a new command
    constructor(tName: String, tMembers: List<String>) {
        teamName = tName
        members = tMembers
        id = ""
    }


    private fun addTeamToDatabase() {
        val db = FirebaseFirestore.getInstance()

        // add team to database
        db.collection("teams").document(this.id).set(this)

        // for each user, add this team to this user
        for (uid in this.members) {
            db.collection("users").document(uid).update("teams", FieldValue.arrayUnion(this.id))
        }
    }

    internal fun addPersonToTeam(uid: String) {
        this.members.toMutableList().add(uid)
        val db = FirebaseFirestore.getInstance()

        // add user to the team
        db.collection("teams").document(this.id).update("members", FieldValue.arrayUnion(uid))

        // add team to the user
        db.collection("users").document(uid).update("teams", FieldValue.arrayUnion(this.id))
    }

    internal fun deletePersonFromTeam(uid: String) {
        this.members.toMutableList().remove(uid)
        val db = FirebaseFirestore.getInstance()

        // delete user from team
        db.collection("teams").document(this.id).update("members", FieldValue.arrayRemove(uid))

        // delete team from the user
        db.collection("users").document(uid).update("teams", FieldValue.arrayRemove(this.id))
    }

    internal fun deleteTeam() {

        val db = FirebaseFirestore.getInstance()

        // for every user, delete team from their teams
        val teamRef = db.collection("teams").document(this.id)
        teamRef.get().addOnSuccessListener { documentSnapshot ->
            val team = documentSnapshot.toObject<CodealTeam>()
            if (team != null) {
                for (uid in team.members)
                    db.collection("users").document(uid)
                        .update("teams", FieldValue.arrayRemove(this.id))
            }
        }

        // delete team from database
        val updates = hashMapOf<String, Any>(
            "teamName" to FieldValue.delete(),
            "members" to FieldValue.delete(),
            "tid" to FieldValue.delete()
        )
        teamRef.update(updates)
    }

}