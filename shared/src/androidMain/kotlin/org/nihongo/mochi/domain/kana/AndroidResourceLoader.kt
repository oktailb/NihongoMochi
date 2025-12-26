package org.nihongo.mochi.domain.kana

import android.content.Context
import android.util.Log
import java.io.InputStreamReader

class AndroidResourceLoader(private val context: Context) : ResourceLoader {
    override fun loadJson(fileName: String): String {
        // fileName is expected to be a relative path from resources root, e.g. "kana/hiragana.json"
        
        // 1. Try Assets
        try {
            Log.d("Mochi", "Attempting to load from assets: $fileName")
            return context.assets.open(fileName).use { stream ->
                InputStreamReader(stream).use { reader ->
                    reader.readText()
                }
            }
        } catch (e: Exception) {
            Log.w("Mochi", "Failed to load from assets: ${e.message}")
            // Debug: List assets to see what's there
            try {
                val path = if (fileName.contains("/")) fileName.substringBeforeLast("/") else ""
                val files = context.assets.list(path)
                Log.d("Mochi", "Assets in '$path': ${files?.joinToString()}")
            } catch (ignore: Exception) {}
        }

        // 2. Try Java Resources (ClassLoader)
        try {
            // Ensure leading slash for getResourceAsStream if needed, or relative for classLoader
            val resourcePath = if (fileName.startsWith("/")) fileName else "/$fileName"
            val relativePath = if (fileName.startsWith("/")) fileName.substring(1) else fileName
            
            Log.d("Mochi", "Attempting to load from resources: $resourcePath")
            val stream = this::class.java.getResourceAsStream(resourcePath) 
                ?: this::class.java.classLoader?.getResourceAsStream(relativePath)
            
            if (stream != null) {
                return stream.use { s ->
                    InputStreamReader(s).use { reader ->
                        reader.readText()
                    }
                }
            } else {
                Log.e("Mochi", "Resource stream is null for $resourcePath")
            }
        } catch (e: Exception) {
            Log.e("Mochi", "Failed to load from resources: ${e.message}")
        }

        return "{}"
    }
}
