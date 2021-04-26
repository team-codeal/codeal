package com.example.prototypefirebase.codeal

interface Likeable {

    val emotions: List<String>

    fun likeBy(userID: String)

    fun removeLikeBy(userID: String)

}