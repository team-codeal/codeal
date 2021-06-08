package com.example.prototypefirebase.codeal.suppliers

import com.example.prototypefirebase.codeal.*
import java.util.*

object CodealUserSupplier : CodealEntityCachingSupplier<CodealUser>() {

    private const val CURRENT_USER_SPECIAL_ID = ""

    override fun constructByIDRaw(id: String?): CodealUser {
        if (id == null || id == CURRENT_USER_SPECIAL_ID) {
            return CodealUser()
        }
        return CodealUser(id)
    }

    /**
     * Special method to retrieve the current user
     */
    fun get() = get(CURRENT_USER_SPECIAL_ID)
}

object CodealTeamSupplier: CodealEntityCachingSupplier<CodealTeam>() {
    override fun constructByIDRaw(id: String?): CodealTeam
            = CodealTeam(id!!)

    fun create(teamName: String, teamDesc: String, teamMembers: List<String>): CodealTeam {
        return CodealTeam(teamName, teamDesc, teamMembers).apply {
            addOnReady { cachedEntities.put(it.id, it) }
        }
    }
}

object CodealTaskSupplier: CodealEntityCachingSupplier<CodealTask>() {
    override fun constructByIDRaw(id: String?): CodealTask
            = CodealTask(id!!)

    fun create(taskName: String = "",
               taskContent: String = "",
               teamID: String,
               listName: String,
               ownerID: String,
               deadline: Date? = null): CodealTask {
        return CodealTask(taskName, taskContent, teamID, listName, ownerID, deadline).apply {
            addOnReady { cachedEntities.put(it.id, it) }
        }
    }
}

object CodealEmotionSupplier: CodealEntityCachingSupplier<CodealEmotion>() {
    override fun constructByIDRaw(id: String?): CodealEmotion
            = CodealEmotion(id!!)

    fun create(ownerID: String, parentObjectID: String): CodealEmotion {
        return CodealEmotion(ownerID, parentObjectID).apply {
            addOnReady { cachedEntities.put(it.id, it) }
        }
    }
}

object CodealCommentSupplier: CodealEntityCachingSupplier<CodealComment>() {
    override fun constructByIDRaw(id: String?): CodealComment
            = CodealComment(id!!)

    fun create(parentTaskID: String, content: String, ownerID: String): CodealComment {
        return CodealComment(parentTaskID, content, ownerID).apply {
            addOnReady { cachedEntities.put(it.id, it) }
        }
    }
}
