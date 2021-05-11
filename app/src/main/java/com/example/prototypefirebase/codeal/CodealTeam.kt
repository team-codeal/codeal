package com.example.prototypefirebase.codeal

import com.example.prototypefirebase.codeal.CodealEntity.*
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import java.util.stream.Collectors

//todo check and refactor business methods manipulating team and users

class CodealTeam : CodealEntity<CodealTeam> {

    var name: String = ""
        private set
    var members: List<String> = emptyList()
        private set
    var ownerID: String = ""
        private set
    var description: String = ""
        private set
    var tasks: List<String> = emptyList()
        private set
        get() {
            return lists.values.stream().flatMap { x -> x.stream() }.collect(Collectors.toList())
        }
    var lists: MutableMap<String, List<String>> = mutableMapOf()
        private set

    companion object {
        private const val TEAMS_DB_COLLECTION_NAME: String = "teams"
        private const val USERS_DB_COLLECTION_NAME: String = "user_profiles"
        private const val TEAMS_DB_TEAM_NAME_FIELD_NAME: String = "Name"
        private const val TEAMS_DB_TEAM_DESCRIPTION_FIELD_NAME: String = "Desc"
        private const val TEAMS_DB_TEAM_MEMBERS_FIELD_NAME: String = "Members"
        private const val TEAMS_DB_TEAM_TASKS_FIELD_NAME: String = "Tasks"
        private const val TEAMS_DB_TEAM_OWNER_ID_FIELD_NAME: String = "Owner"
        private const val TEAMS_DB_TEAM_LISTS_FIELD_NAME: String = "Lists"
    }

    //constructor for an existing command
    constructor(id: String) {
        this.id = id
    }

    //constructor for a new command
    constructor(teamName: String, teamDesc: String, teamMembers: List<String>) {
        id = ""
        name = teamName
        description = teamDesc
        ownerID = ""
        members = teamMembers
        uploadTeamInfoToDB()
    }

    // TODO this is very bad, should implement a change() method instead
    internal fun addTask(taskID: String, listName: String) {
        val teamsDB = getDB()
        if (lists.containsKey(listName)) {
            lists[listName] = ArrayList<String>().apply {
                lists[listName]?.let { addAll(it) }
                add(taskID)
            }
        } else {
            lists[listName] = listOf(taskID)
        }
        teamsDB.document(id).update(mapOf(
            TEAMS_DB_TEAM_LISTS_FIELD_NAME to lists
        ))
    }

    internal fun deleteTask(taskID: String, listName: String){
        val teamsDB = FirebaseFirestore.getInstance().collection(TEAMS_DB_COLLECTION_NAME)
        lists[listName] = ArrayList<String>().apply {
            lists[listName]?.let{ addAll(it.stream().filter{x -> x != taskID}.collect(Collectors.toList()))}
        }
        teamsDB.document(id).update(mapOf(
            TEAMS_DB_TEAM_LISTS_FIELD_NAME to lists
        ))
    }

    // that's only for making a new team
    private fun uploadTeamInfoToDB() {

        val db = FirebaseFirestore.getInstance()

        val teamDB = db.collection(TEAMS_DB_COLLECTION_NAME)

        val teamInfo = mutableMapOf(
            TEAMS_DB_TEAM_NAME_FIELD_NAME to this.name,
            TEAMS_DB_TEAM_OWNER_ID_FIELD_NAME to this.ownerID,
            TEAMS_DB_TEAM_MEMBERS_FIELD_NAME to this.members,
            TEAMS_DB_TEAM_DESCRIPTION_FIELD_NAME to this.description
        )

        teamDB.add(teamInfo).addOnSuccessListener { teamDocument ->
            id = teamDocument.id
            if ((listeners.isNotEmpty() or oneTimeListeners.isNotEmpty())) {
                setFirebaseListener()
            }
        }

        // doesn't add duplicates, because of FieldValue.arrayUnion
        members.forEach(::addPersonToTeam)
    }

    fun addPersonToTeam(uid: String) {

        // add user to the team
        getDB().document(id)
            .update(TEAMS_DB_TEAM_MEMBERS_FIELD_NAME, FieldValue.arrayUnion(uid))

        val db = FirebaseFirestore.getInstance()
        // add team to the user
        db.collection(CodealUser.USER_DB_COLLECTION_NAME).document(uid)
            .update(CodealUser.USER_DB_USER_TEAMS_FIELD_NAME, FieldValue.arrayUnion(id))
    }

    fun deletePersonFromTeam(uid: String) {
        this.members.toMutableList().remove(uid)
        val db = FirebaseFirestore.getInstance()

        // delete user from team
        db.collection(TEAMS_DB_COLLECTION_NAME).document(this.id).update(TEAMS_DB_TEAM_MEMBERS_FIELD_NAME, FieldValue.arrayRemove(uid))

        // delete team from the user
        db.collection(USERS_DB_COLLECTION_NAME).document(uid).update(TEAMS_DB_COLLECTION_NAME, FieldValue.arrayRemove(this.id))
    }

    private fun deleteTeam() {

        val db = FirebaseFirestore.getInstance()

        // for every user, delete team from their teams
        val teamRef = db.collection(TEAMS_DB_COLLECTION_NAME).document(this.id)
        teamRef.get().addOnSuccessListener { documentSnapshot ->
            val team = documentSnapshot.toObject<CodealTeam>()
            if (team != null) {
                for (uid in team.members)
                    db.collection("user_profiles").document(uid)
                        .update(TEAMS_DB_COLLECTION_NAME, FieldValue.arrayRemove(this.id))
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

    override fun getDB(): CollectionReference
            = FirebaseFirestore.getInstance().collection(TEAMS_DB_COLLECTION_NAME)

    override fun getDataFromSnapshot(snapshot: DocumentSnapshot) {
        val teamsDB = getDB()
        name = snapshot.get(TEAMS_DB_TEAM_NAME_FIELD_NAME) as? String ?:
                run {
                    val newName = "New command"
                    teamsDB.document(id).update(TEAMS_DB_TEAM_NAME_FIELD_NAME, newName)
                    newName
                }
        members = (snapshot.get(TEAMS_DB_TEAM_MEMBERS_FIELD_NAME)
                as? List<*>)?.filterIsInstance<String>() ?:
                run {
                    val newMembers = emptyList<String>()
                    teamsDB.document(id).update(TEAMS_DB_TEAM_MEMBERS_FIELD_NAME, newMembers)
                    newMembers
                }
        ownerID = snapshot.get(TEAMS_DB_TEAM_OWNER_ID_FIELD_NAME) as? String ?:
                run {
                    val newOwnerID = ""
                    teamsDB.document(id)
                        .update(TEAMS_DB_TEAM_OWNER_ID_FIELD_NAME, newOwnerID)
                    newOwnerID
                }
        description = snapshot.get(TEAMS_DB_TEAM_DESCRIPTION_FIELD_NAME) as? String ?:
                run {
                    val newOwnerID = ""
                    teamsDB.document(id)
                        .update(TEAMS_DB_TEAM_DESCRIPTION_FIELD_NAME, newOwnerID)
                    newOwnerID
                }
        val listsRaw = (snapshot.get(TEAMS_DB_TEAM_LISTS_FIELD_NAME)
                as Map<*, *>?)
            ?:
            run {
                val newLists = HashMap<String, List<String>>().apply {
                    put("Todo", emptyList())
                    put("Doing", emptyList())
                    put("Done", emptyList())
                }
                teamsDB.document(id)
                    .update(TEAMS_DB_TEAM_LISTS_FIELD_NAME, newLists)
                newLists
            }
        listsRaw.forEach { (key, value) ->
            if (key !is String || value !is List<*>) return@forEach
            val listOfTaskIDs : List<String> = value.filterIsInstance<String>()
            lists[key] = listOfTaskIDs
        }
    }

}