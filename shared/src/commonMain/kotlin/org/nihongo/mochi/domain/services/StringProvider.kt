package org.nihongo.mochi.domain.services

/**
 * Interface to provide localized strings without direct dependency on Compose Resources in ViewModels.
 */
interface StringProvider {
    /**
     * Returns a localized string for the given key.
     * If the key is not found, returns the key itself or a fallback.
     */
    fun getString(key: String): String
    
    /**
     * Returns a localized string for the given key, with format arguments.
     */
    fun getString(key: String, vararg args: Any): String
}
