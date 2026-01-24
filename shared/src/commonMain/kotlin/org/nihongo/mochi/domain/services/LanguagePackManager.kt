package org.nihongo.mochi.domain.services

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.Buffer
import okio.HashingSource
import okio.use
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class DownloadStatus {
    IDLE, DOWNLOADING, SUCCESS, ERROR
}

// Platform-specific unzip function
expect fun unzip(zipFilePath: Path, targetDir: Path, fileSystem: FileSystem)

class LanguagePackManager(
    private val httpClient: HttpClient,
    private val fileSystem: FileSystem,
    private val appStorageDir: Path
) {
    // Base URL for language packs
    private val baseUrl = "https://raw.githubusercontent.com/oktailb/NihongoMochi-Data/main/langs"
    
    private val _status = MutableStateFlow<Map<String, DownloadStatus>>(emptyMap())
    val status: StateFlow<Map<String, DownloadStatus>> = _status.asStateFlow()

    fun getPackStatus(locale: String): DownloadStatus {
        return _status.value[locale] ?: if (isPackDownloaded(locale)) DownloadStatus.SUCCESS else DownloadStatus.IDLE
    }

    fun isPackDownloaded(locale: String): Boolean {
        // We consider it downloaded if at least meanings.json is present locally
        val meaningsPath = getLocaleDir(locale).resolve("meanings.json")
        return fileSystem.exists(meaningsPath)
    }

    private fun getLocaleDir(locale: String): Path {
        return appStorageDir.resolve("langs/$locale")
    }

    suspend fun downloadPack(locale: String): Boolean {
        if (_status.value[locale] == DownloadStatus.DOWNLOADING) return false
        
        _status.update { it + (locale to DownloadStatus.DOWNLOADING) }
        
        return try {
            // Mapping ZIP files to their MD5 files
            val filesToVerify = listOf(
                "data.zip" to "data.md5",
                "grammar.zip" to "grammar.md5"
            )
            
            for ((zipName, md5Name) in filesToVerify) {
                val zipUrl = "$baseUrl/$locale/$zipName"
                val md5Url = "$baseUrl/$locale/$md5Name"
                
                // 1. Download MD5 first
                val expectedMd5 = downloadText(md5Url) ?: throw Exception("Failed to download MD5 for $zipName")
                
                // 2. Download ZIP
                val targetPath = getLocaleDir(locale).resolve(zipName)
                val success = downloadFile(zipUrl, targetPath)
                if (!success) {
                    throw Exception("Failed to download $zipName")
                }
                
                // 3. Verify Integrity
                if (!verifyMd5(targetPath, expectedMd5)) {
                    fileSystem.delete(targetPath)
                    throw Exception("MD5 Verification failed for $zipName")
                }
                
                // 4. Extract and cleanup
                unzip(targetPath, getLocaleDir(locale), fileSystem)
                fileSystem.delete(targetPath)
            }
            
            _status.update { it + (locale to DownloadStatus.SUCCESS) }
            true
        } catch (e: Exception) {
            println("Error downloading pack for $locale: ${e.message}" )
            _status.update { it + (locale to DownloadStatus.ERROR) }
            false
        }
    }

    private suspend fun downloadText(url: String): String? {
        return try {
            val response = httpClient.get(url)
            if (response.status == HttpStatusCode.OK) {
                response.bodyAsText().trim()
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun verifyMd5(targetPath: Path, expectedMd5: String): Boolean {
        return try {
            val hash = fileSystem.read(targetPath) {
                HashingSource.md5(this).use { hashingSource ->
                    val buffer = Buffer()
                    while (hashingSource.read(buffer, 8192) != -1L) {
                        buffer.clear()
                    }
                    hashingSource.hash.hex()
                }
            }
            // Comparison is case-insensitive as hex can be upper or lower
            hash.equals(expectedMd5, ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun downloadFile(url: String, targetPath: Path): Boolean {
        return try {
            val response = httpClient.get(url)
            if (response.status == HttpStatusCode.OK) {
                val bytes = response.readBytes()
                fileSystem.createDirectories(targetPath.parent!!)
                fileSystem.write(targetPath) {
                    write(bytes)
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Attempts to load a resource file from the local storage for a specific locale.
     */
    fun loadLocalResource(fileName: String, locale: String): String? {
        val path = getLocaleDir(locale).resolve(fileName)
        return try {
            if (fileSystem.exists(path)) {
                fileSystem.read(path) {
                    readUtf8()
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    fun deletePack(locale: String) {
        val dir = getLocaleDir(locale)
        try {
            if (fileSystem.exists(dir)) {
                fileSystem.deleteRecursively(dir)
                _status.update { it + (locale to DownloadStatus.IDLE) }
            }
        } catch (e: Exception) {}
    }
}
