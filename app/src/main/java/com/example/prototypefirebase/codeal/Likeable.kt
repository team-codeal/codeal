package com.example.prototypefirebase.codeal

interface Likeable {

    val emotions: List<String>

    fun likeBy(userID: String, callback: Any? = null)

    fun removeLikeBy(userID: String, callback: Any? = null)

}