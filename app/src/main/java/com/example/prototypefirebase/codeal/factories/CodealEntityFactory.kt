package com.example.prototypefirebase.codeal.factories

import com.example.prototypefirebase.codeal.CodealEntity

interface CodealEntityFactory<T: CodealEntity<T>> {

    fun get(id: String): T

}
