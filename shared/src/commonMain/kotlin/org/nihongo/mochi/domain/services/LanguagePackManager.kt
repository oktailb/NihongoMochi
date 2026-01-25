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
    // Base URL for language packs and common data
    private val baseUrl = "https://raw.githubusercontent.com/oktailb/NihongoMochi-Data/main"
    
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

    private fun getCommonDir(): Path {
        return appStorageDir.resolve("common")
    }

    suspend fun downloadPack(locale: String): Boolean {
        if (_status.value[locale] == DownloadStatus.DOWNLOADING) return false
        
        _status.update { it + (locale to DownloadStatus.DOWNLOADING) }
        
        return try {
            // Mapping ZIP files to their MD5 files
            // lessons.zip contains the grammar lessons for a specific language
            val filesToVerify = listOf(
                "data.zip" to "data.md5",
                "lessons.zip" to "lessons.md5"
            )
            
            for ((zipName, md5Name) in filesToVerify) {
                val zipUrl = "$baseUrl/langs/$locale/$zipName"
                val md5Url = "$baseUrl/langs/$locale/$md5Name"
                
                println("Attempting to download MD5 from: $md5Url")
                // 1. Download MD5 first
                val expectedMd5 = downloadText(md5Url) ?: throw Exception("Failed to download MD5 for $zipName")
                
                // 2. Download ZIP
                val targetPath = getLocaleDir(locale).resolve(zipName)
                println("Attempting to download ZIP from: $zipUrl")
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

    /**
     * Synchronizes a common resource ZIP (like grammar.zip or exercices.zip) using MD5 check.
     */
    suspend fun syncCommonZip(resourceName: String): Boolean {
        val commonDir = getCommonDir()
        val zipName = "$resourceName.zip"
        val md5Name = "$resourceName.md5"
        val localZipPath = commonDir.resolve(zipName)
        
        val remoteMd5Url = "$baseUrl/$md5Name"
        val remoteZipUrl = "$baseUrl/$zipName"

        return try {
            println("Syncing zip: $zipName")
            
            // 1. Get remote MD5
            val remoteMd5 = downloadText(remoteMd5Url)
            if (remoteMd5 == null) {
                println("Could not reach MD5 for $zipName, skipping sync.")
                return false
            }

            // 2. Download and verify new zip
            println("Downloading and verifying: $remoteZipUrl")
            val success = downloadFile(remoteZipUrl, localZipPath)
            if (success && verifyMd5(localZipPath, remoteMd5)) {
                unzip(localZipPath, commonDir, fileSystem)
                fileSystem.delete(localZipPath)
                println("Successfully synced $zipName")
                true
            } else {
                println("Failed to sync or verify $zipName")
                if (fileSystem.exists(localZipPath)) fileSystem.delete(localZipPath)
                false
            }
        } catch (e: Exception) {
            println("Exception during sync of $zipName: ${e.message}")
            false
        }
    }

    private suspend fun downloadText(url: String): String? {
        return try {
            val response = httpClient.get(url)
            if (response.status == HttpStatusCode.OK) {
                response.bodyAsText().trim()
            } else {
                println("Download text failed (Status: ${response.status}) for URL: $url")
                null
            }
        } catch (e: Exception) {
            println("Download text error for URL $url: ${e.message}")
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
            val match = hash.equals(expectedMd5, ignoreCase = true)
            if (!match) println("MD5 mismatch for $targetPath. Expected: $expectedMd5, Actual: $hash")
            match
        } catch (e: Exception) {
            println("MD5 verification error for $targetPath: ${e.message}")
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
                println("Download file failed (Status: ${response.status}) for URL: $url")
                false
            }
        } catch (e: Exception) {
            println("Download file error for URL $url: ${e.message}")
            false
        }
    }

    /**
     * Attempts to load a resource file from local storage.
     * Looks first in locale-specific dir, then in common dir.
     */
    fun loadLocalResource(fileName: String, locale: String? = null): String? {
        val pathsToTry = mutableListOf<Path>()
        if (locale != null) {
            pathsToTry.add(getLocaleDir(locale).resolve(fileName))
        }
        pathsToTry.add(getCommonDir().resolve(fileName))

        for (path in pathsToTry) {
            try {
                if (fileSystem.exists(path)) {
                    return fileSystem.read(path) {
                        readUtf8()
                    }
                }
            } catch (e: Exception) {}
        }
        return null
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
