package com.example.prototypefirebase.codeal

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class CodealTeam {

    var teamName: String = "Default team"
        set(value) {
            if (value != "Default team") field = value
        }
    var members: List<String> = emptyList()
        private set
    var tid: String = FirebaseFirestore.getInstance().collection("teams").document().id

    init {
        addTeamToDatabase()
    }


    constructor(id: String) {
        tid = id;
    }

    constructor(tName: String, tMembers: List<String>) {
        teamName = tName
        members = tMembers
    }


    private fun addTeamToDatabase() {
        val db = FirebaseFirestore.getInstance()

        // добавляем команду в базу данных
        db.collection("teams").document(this.tid).set(this)

        // добавляем команду к списку команд каждого участника
        for (uid in this.members) {
            db.collection("users").document(uid).update("teams", FieldValue.arrayUnion(this.tid))
        }
    }

    internal fun addPersonToTeam(uid: String) {
        this.members.toMutableList().add(uid)
        val db = FirebaseFirestore.getInstance()

        // Добавляем разработчика к списку участников команды
        db.collection("teams").document(this.tid).update("members", FieldValue.arrayUnion(uid))

        // Добавляем команду к списку команд разработчика (уточнить по поводу названия полей)
        db.collection("users").document(uid).update("teams", FieldValue.arrayUnion(this.tid))
    }

    internal fun deletePersonFromTeam(uid: String) {
        this.members.toMutableList().remove(uid)
        val db = FirebaseFirestore.getInstance()

        // Удаляем разработчика из команды
        db.collection("teams").document(this.tid).update("members", FieldValue.arrayRemove(uid))

        // Удаляем команду у разработчика
        db.collection("users").document(uid).update("teams", FieldValue.arrayRemove(this.tid))
    }

    internal fun deleteTeam() {

        val db = FirebaseFirestore.getInstance()

        // удаляем команду из списка команд всех её участников
        val teamRef = db.collection("teams").document(this.tid)
        teamRef.get().addOnSuccessListener { documentSnapshot ->
            val team = documentSnapshot.toObject<CodealTeam>()
            if (team != null) {
                for (uid in team.members)
                    db.collection("users").document(uid)
                        .update("teams", FieldValue.arrayRemove(this.tid))
            }
        }

        // удаляем команду из базы данных
        val updates = hashMapOf<String, Any>(
            "teamName" to FieldValue.delete(),
            "members" to FieldValue.delete(),
            "tid" to FieldValue.delete()
        )
        teamRef.update(updates)
    }

}