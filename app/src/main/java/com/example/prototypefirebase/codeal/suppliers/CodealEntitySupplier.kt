package com.example.prototypefirebase.codeal.suppliers

import com.example.prototypefirebase.codeal.CodealEntity

interface CodealEntitySupplier<T: CodealEntity<T>> {

    /**
     * Get a CodealEntity object by it's id (or some identification).
     * @param id id or url or generally some identification for a CodealEntity object from Firestore
     * @return CodealEntity object that of that id.
     */
    fun get(id: String): T

}
