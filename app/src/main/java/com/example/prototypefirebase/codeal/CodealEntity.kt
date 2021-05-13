package com.example.prototypefirebase.codeal

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import java.util.concurrent.ConcurrentLinkedQueue

@Suppress("UNCHECKED_CAST")
abstract class CodealEntity<T : CodealEntity<T>> {

    inner class CodealListener(
        private val callback: (T) -> Unit
    ) {
        internal fun invoke() = callback.invoke(this@CodealEntity as T)
        fun remove() {
            this@CodealEntity.listeners.remove(this)
            this@CodealEntity.oneTimeListeners.remove(this)
            this@CodealEntity.listenersWaiting.remove(this)
            this@CodealEntity.oneTimeListenersWaiting.remove(this)
            if (listeners.isEmpty() and oneTimeListeners.isEmpty()) {
                firebaseSnapshotRegistration?.remove()
                firebaseSnapshotRegistration = null
            }
        }
    }

    var id = ""
        protected set

    protected var firebaseSnapshotRegistration: ListenerRegistration? = null
    protected val listeners: ConcurrentLinkedQueue<CodealListener> = ConcurrentLinkedQueue()
    protected val oneTimeListeners: ConcurrentLinkedQueue<CodealListener> = ConcurrentLinkedQueue()

    private val listenersWaiting: ConcurrentLinkedQueue<CodealListener> = ConcurrentLinkedQueue()
    private val oneTimeListenersWaiting: ConcurrentLinkedQueue<CodealListener>
            = ConcurrentLinkedQueue()

    /**
     * Shows (both to the client and self) whether the data was loaded at least once.
     * If ready == true, though, it doesn't mean that this object contains the *newest* data,
     * it only means it contains data that was loaded at some point in time before.
     */
    var ready: Boolean = false
        protected set

    /**
     * Shows (both to the client and self) whether this object is already present in the
     * database. This is needed to postpone things before this object is uploaded.
     * If created == true, the object should already be uploaded to the database, and if
     * created == false, it is still not uploaded.
     *
     * Default value is true, because usually we just read existing object from database (or cache),
     * so *it should be set to false in a creating constructor*.
     *
     * In implementations, doing "created = true" means firing all onCreated callbacks.
     */
    protected var created: Boolean = true
        set(value) {
            if (value) {
                field = true
                listenersWaiting.forEach { addListener(it) }
                listenersWaiting.clear()
                oneTimeListenersWaiting.forEach { addOnReady(it) }
                oneTimeListenersWaiting.clear()
            } else {
                field = false
            }
        }

    /**
     * Adds a listener to the Codeal object, meaning that whenever the object is updated
     * (when the database is updated), client's callback is invoked with the new Codeal object
     * information passed to it.
     *
     * The listener should be removed if not in use. Calling remove() 2 or more times doesn't
     * do anything.
     *
     * If the object already has some data from the database (could be old data), it will
     * invoke client's callback immediately, but when the new data is fetch, the callback will be
     * invoked again.
     *
     * @return a CodealListener object. Call its "remove()" method when you are done to save
     * bandwidth and so that your callback doesn't get called anymore.
     */
    fun addListener(callback: (T) -> Unit): CodealListener {
        val codealListener = CodealListener(callback)
        if (!created) {
            listenersWaiting.add(codealListener)
            return codealListener
        }
        listeners.add(codealListener)
        if (ready) codealListener.invoke()
        if (firebaseSnapshotRegistration == null) {
            setFirebaseListener()
        }
        return codealListener
    }

    /**
     * Adds a one-time listener to the Codeal object, meaning that your callback will be called
     * only ones when the object has the latest information from the database. Invocation can
     * happen immediately if the object already has the latest data.
     */
    fun addOnReady(callback: (T) -> Unit) {
        if (!created) {
            val codealListener = CodealListener(callback)
            oneTimeListenersWaiting.add(codealListener)
            return
        }
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

                oneTimeListeners.forEach {
                    it.invoke()
                    it.remove()
                }
            }
    }


    /**
     * Usually to be transported from waiting listeners. See doc on "created" property
     * It is assumed that when this method is called, created == true already.
     */
    private fun addListener(codealListener: CodealListener) {
        listeners.add(codealListener)
        if (ready) codealListener.invoke()
        if (firebaseSnapshotRegistration == null) {
            setFirebaseListener()
        }
    }

    /**
     * Usually to be transported from waiting onReady listeners. See doc on "created" property
     * It is assumed that when this method is called, created == true already.
     */
    private fun addOnReady(codealListener: CodealListener) {
        if (ready && firebaseSnapshotRegistration != null) {
            codealListener.invoke()
        } else {
            oneTimeListeners.add(codealListener)
            if (firebaseSnapshotRegistration == null) {
                setFirebaseListener()
            }
        }
    }

    /**
     * Should assign all inner data according to the snapshot
     */
    protected abstract fun getDataFromSnapshot(snapshot: DocumentSnapshot)

}
