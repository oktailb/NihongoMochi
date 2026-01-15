package org.nihongo.mochi.ui

import org.jetbrains.compose.resources.getString
import org.nihongo.mochi.domain.services.StringProvider
import kotlinx.coroutines.runBlocking

/**
 * Implementation of [StringProvider] using Compose Resources.
 * Note: [getString] is a suspend function in Compose Resources.
 * In a ViewModel, we might need to handle this via a scope or runBlocking 
 * (though runBlocking is generally discouraged on UI thread, it might be 
 * necessary for simple lookups if not using Flow).
 */
class ComposeStringProvider : StringProvider {
    
    override fun getString(key: String): String {
        val resource = ResourceUtils.resolveStringResource(key) ?: return key
        // This is a trade-off. In a real KMP app, we'd prefer an async API.
        // But for many ViewModels, a synchronous lookup is expected.
        return runBlocking { getString(resource) }
    }

    override fun getString(key: String, vararg args: Any): String {
        val resource = ResourceUtils.resolveStringResource(key) ?: return key
        return runBlocking { getString(resource, *args) }
    }
}
