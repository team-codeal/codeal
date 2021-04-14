package com.example.prototypefirebase.codeal

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import java.util.stream.Collectors

//todo check and refactor business methods manipulating team and users

typealias CodealTeamCallback = ((CodealTeam) -> Unit)

class CodealTeam {

    var name: String = ""
        private set

    // list of ids
    var members: List<String> = emptyList()
        private set

    var id: String
        private set

    lateinit var ownerID: String
        private set

    var description: String = ""
        private set

    var tasks: List<String> = emptyList()
        private set
        get() {
            return lists.values.stream().flatMap { x -> x.stream() }.collect(Collectors.toList())
        }

    // a list of "List" names, e.g. "To Do", "Doing", "Done" etc
    var lists: MutableMap<String, List<String>> = mutableMapOf()
        private set

    var ready: Boolean = false
        private set

    var updateCallback: CodealTeamCallback? = null
        set(value) {
            field = value
            if (ready) value?.invoke(this)
        }

    companion object {
        private const val TEAMS_DB_COLLECTION_NAME: String = "teams"
        private const val TEAMS_DB_TEAM_NAME_FIELD_NAME: String = "Name"
        private const val TEAMS_DB_TEAM_DESCRIPTION_FIELD_NAME: String = "Desc"
        private const val TEAMS_DB_TEAM_MEMBERS_FIELD_NAME: String = "Members"
        private const val TEAMS_DB_TEAM_TASKS_FIELD_NAME: String = "Tasks"
        private const val TEAMS_DB_TEAM_OWNER_ID_FIELD_NAME: String = "Owner"
        private const val TEAMS_DB_TEAM_LISTS_FIELD_NAME: String = "Lists"
    }

    //constructor for an existing command
    constructor(id: String, callback: CodealTeamCallback? = null) {
        this.id = id
        updateCallback = callback
        initTeamInfoById()
    }

    //constructor for a new command
    constructor(teamName: String, teamMembers: List<String>, callback: CodealTeamCallback? = null) {
        id = ""
        name = teamName
        members = teamMembers
        updateCallback = callback
        uploadTeamInfoToDB()
    }

    private fun initTeamInfoById() {
        val teamsDB = FirebaseFirestore.getInstance().collection(TEAMS_DB_COLLECTION_NAME)
        teamsDB.document(id).get()
            .addOnSuccessListener { teamDocument ->
                name = teamDocument?.get(TEAMS_DB_TEAM_NAME_FIELD_NAME) as? String ?:
                        run {
                            val newName = "New command"
                            teamsDB.document(id).update(TEAMS_DB_TEAM_NAME_FIELD_NAME, newName)
                            newName
                        }
                members = (teamDocument?.get(TEAMS_DB_TEAM_MEMBERS_FIELD_NAME)
                        as? List<*>)?.filterIsInstance<String>() ?:
                        run {
                            val newMembers = emptyList<String>()
                            teamsDB.document(id).update(TEAMS_DB_TEAM_MEMBERS_FIELD_NAME, newMembers)
                            newMembers
                        }
                ownerID = teamDocument?.get(TEAMS_DB_TEAM_OWNER_ID_FIELD_NAME) as? String ?:
                        run {
                            val newOwnerID = ""
                            teamsDB.document(id)
                                .update(TEAMS_DB_TEAM_OWNER_ID_FIELD_NAME, newOwnerID)
                            newOwnerID
                        }
                description = teamDocument?.get(TEAMS_DB_TEAM_DESCRIPTION_FIELD_NAME) as? String ?:
                        run {
                            val newOwnerID = ""
                            teamsDB.document(id)
                                .update(TEAMS_DB_TEAM_DESCRIPTION_FIELD_NAME, newOwnerID)
                            newOwnerID
                        }
//                tasks = (teamDocument?.get(TEAMS_DB_TEAM_TASKS_FIELD_NAME) as? List<*>?)
//                    ?.filterIsInstance<String>() ?:
//                        run {
//                            val newOwnerID = emptyList<String>()
//                            teamsDB.document(id)
//                                .update(TEAMS_DB_TEAM_DESCRIPTION_FIELD_NAME, newOwnerID)
//                            newOwnerID
//                        }
                val listsRaw = (teamDocument?.get(TEAMS_DB_TEAM_LISTS_FIELD_NAME)
                        as Map<*, *>?)
                        ?:
                        run {
                            val newLists = HashMap<String, List<String>>().apply {
                                put("Todo", listOf("Hello! Here is your new task"))
                                put("Doing", emptyList())
                                put("Well Done", emptyList())
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
                ready = true
                updateCallback?.invoke(this)
            }
            .addOnFailureListener { exception ->
                throw exception
            }
    }

    // that's only for making a new team
    private fun uploadTeamInfoToDB() {

        val db = FirebaseFirestore.getInstance()

        val teamDB = db.collection(TEAMS_DB_COLLECTION_NAME)
        // val userDB = db.collection(CodealUser.USER_DB_COLLECTION_NAME)

        val teamInfo = mutableMapOf(
            TEAMS_DB_TEAM_NAME_FIELD_NAME to this.name,
            TEAMS_DB_TEAM_OWNER_ID_FIELD_NAME to this.ownerID,
            TEAMS_DB_TEAM_MEMBERS_FIELD_NAME to this.members,
            TEAMS_DB_TEAM_DESCRIPTION_FIELD_NAME to this.description
        )
        teamDB.add(teamInfo).addOnSuccessListener { teamDocument ->
            id = teamDocument.id
            ready = true
            updateCallback?.invoke(this)
        }

        //todo for each user, add this team to this user
    }

    private fun addPersonToTeam(uid: String) {
        this.members.toMutableList().add(uid)
        val db = FirebaseFirestore.getInstance()

        // add user to the team
        db.collection(TEAMS_DB_COLLECTION_NAME).document(this.id).update("members", FieldValue.arrayUnion(uid))

        // add team to the user
        db.collection("users").document(uid).update(TEAMS_DB_COLLECTION_NAME, FieldValue.arrayUnion(this.id))
    }

    private fun deletePersonFromTeam(uid: String) {
        this.members.toMutableList().remove(uid)
        val db = FirebaseFirestore.getInstance()

        // delete user from team
        db.collection(TEAMS_DB_COLLECTION_NAME).document(this.id).update("members", FieldValue.arrayRemove(uid))

        // delete team from the user
        db.collection("users").document(uid).update(TEAMS_DB_COLLECTION_NAME, FieldValue.arrayRemove(this.id))
    }

    private fun deleteTeam() {

        val db = FirebaseFirestore.getInstance()

        // for every user, delete team from their teams
        val teamRef = db.collection(TEAMS_DB_COLLECTION_NAME).document(this.id)
        teamRef.get().addOnSuccessListener { documentSnapshot ->
            val team = documentSnapshot.toObject<CodealTeam>()
            if (team != null) {
                for (uid in team.members)
                    db.collection("users").document(uid)
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

}