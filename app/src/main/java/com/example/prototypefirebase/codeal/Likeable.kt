package com.example.prototypefirebase.codeal

interface Likeable<T : CodealEntity<T>> {

    val emotions: List<String>

    fun likeBy(userID: String)

    fun removeLikeBy(userID: String)

}