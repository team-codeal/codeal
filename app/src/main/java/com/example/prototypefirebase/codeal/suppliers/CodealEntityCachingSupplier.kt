package com.example.prototypefirebase.codeal.suppliers

import com.example.prototypefirebase.codeal.CodealEntity
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import java.util.concurrent.TimeUnit

abstract class CodealEntityCachingSupplier<T : CodealEntity<T>> : CodealEntitySupplier<T> {

    protected val cachedEntities: LoadingCache<String, T> =
        CacheBuilder.newBuilder()
            .expireAfterAccess(7, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build(CacheLoader.from(::constructByIDRaw))

    override fun get(id: String) : T {
        return cachedEntities.get(id)
    }

    /**
     * Should construct a new Codeal object that already exists in the database
     */
    protected abstract fun constructByIDRaw(id: String?) : T

}
