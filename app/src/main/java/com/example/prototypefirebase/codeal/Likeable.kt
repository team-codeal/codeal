package com.example.prototypefirebase.codeal

interface Likeable<T : CodealEntity> {

    val emotions: List<String>

    fun likeBy(userID: String, callback: ((T) -> Unit)? = null)

    fun removeLikeBy(userID: String, callback: ((T) -> Unit)? = null)

}