package com.example.prototypefirebase.codeal

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration

@Suppress("UNCHECKED_CAST")
abstract class CodealEntity<T : CodealEntity<T>> {

    inner class CodealListener(
        private val callback: (T) -> Unit
    ) {
        internal fun invoke() = callback.invoke(this@CodealEntity as T)
        fun remove() {
            this@CodealEntity.listeners.remove(this)
            this@CodealEntity.oneTimeListeners.remove(this)
            if (listeners.isEmpty() and oneTimeListeners.isEmpty()) {
                firebaseSnapshotRegistration?.remove()
                firebaseSnapshotRegistration = null
            }
        }
    }

    var id = ""
        protected set

    protected var firebaseSnapshotRegistration: ListenerRegistration? = null
    protected var listeners: MutableList<CodealListener> = mutableListOf()
    protected var oneTimeListeners: MutableList<CodealListener> = mutableListOf()

    var ready: Boolean = false
        protected set

    fun addListener(callback: (T) -> Unit): CodealListener {
        val codealListener = CodealListener(callback)
        listeners.add(codealListener)
        if (ready) codealListener.invoke()
        if (firebaseSnapshotRegistration == null) {
            setFirebaseListener()
        }
        return codealListener
    }

    fun addOnReady(callback: (T) -> Unit) {
        if (ready && firebaseSnapshotRegistration != null) {
            CodealListener(callback).invoke()
        } else {
            oneTimeListeners.add(CodealListener(callback))
            if (firebaseSnapshotRegistration == null) {
                setFirebaseListener()
            }
        }
    }

    internal abstract fun getDB(): CollectionReference

    protected fun setFirebaseListener() {
        firebaseSnapshotRegistration?.remove()
        firebaseSnapshotRegistration = null
        if (id == "") return
        firebaseSnapshotRegistration = getDB().document(id)
            .addSnapshotListener { docSnapshot, e ->
                if (docSnapshot == null || e != null) {
                    return@addSnapshotListener
                }
                getDataFromSnapshot(docSnapshot)
                ready = true

                listeners.forEach { it.invoke() }

                oneTimeListeners.forEach { it.invoke() }
                oneTimeListeners.forEach { it.remove() }
            }
    }

    /**
     * Should assign all inner data according to the snapshot
     */
    protected abstract fun getDataFromSnapshot(snapshot: DocumentSnapshot)

}
