package com.example.prototypefirebase.codeal.factories

import com.example.prototypefirebase.codeal.CodealUser
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import java.util.*
import java.util.concurrent.TimeUnit

object CodealUserFactory : CodealEntityFactory<CodealUser> {

    private val cachedUsers: Cache<String, CodealUser> =
        CacheBuilder.newBuilder()
            .expireAfterAccess(7, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build()

    private const val currentUserSpecialID = ""

    override fun get(id: String) : CodealUser {
        return cachedUsers.get(id) { CodealUser(id) }
    }

    fun get() : CodealUser {
        return cachedUsers.get(currentUserSpecialID) { CodealUser() }
    }

}
