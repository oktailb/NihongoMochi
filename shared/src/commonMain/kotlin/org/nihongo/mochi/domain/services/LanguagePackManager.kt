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
            val filesToVerify = listOf(
                "data.zip" to "data.md5",
                "lessons.zip" to "lessons.md5"
            )
            
            for ((zipName, md5Name) in filesToVerify) {
                val zipUrl = "$baseUrl/langs/$locale/$zipName"
                val md5Url = "$baseUrl/langs/$locale/$md5Name"
                val localMd5Path = getLocaleDir(locale).resolve(md5Name)
                
                println("Checking MD5 from: $md5Url")
                val expectedMd5 = downloadText(md5Url) ?: throw Exception("Failed to download MD5 for $zipName")
                
                // Check if already up to date
                val localMd5 = if (fileSystem.exists(localMd5Path)) {
                    fileSystem.read(localMd5Path) { readUtf8().trim() }
                } else null
                
                if (localMd5 == expectedMd5) {
                    println("$zipName for $locale is already up to date.")
                    continue
                }
                
                // Download ZIP
                val targetPath = getLocaleDir(locale).resolve(zipName)
                println("Downloading ZIP: $zipUrl")
                val success = downloadFile(zipUrl, targetPath)
                if (!success) {
                    throw Exception("Failed to download $zipName")
                }
                
                // Verify Integrity
                if (!verifyMd5(targetPath, expectedMd5)) {
                    fileSystem.delete(targetPath)
                    throw Exception("MD5 Verification failed for $zipName")
                }
                
                // Extract and cleanup
                unzip(targetPath, getLocaleDir(locale), fileSystem)
                fileSystem.delete(targetPath)
                
                // Save MD5 locally to avoid re-downloading next time
                fileSystem.write(localMd5Path) { writeUtf8(expectedMd5) }
            }
            
            _status.update { it + (locale to DownloadStatus.SUCCESS) }
            true
        } catch (e: Exception) {
            println("Error downloading pack for $locale: ${e.message}" )
            _status.update { it + (locale to DownloadStatus.ERROR) }
            false
        }
    }

    suspend fun syncCommonZip(resourceName: String): Boolean {
        val commonDir = getCommonDir()
        val zipName = "$resourceName.zip"
        val md5Name = "$resourceName.md5"
        val localZipPath = commonDir.resolve(zipName)
        val localMd5Path = commonDir.resolve(md5Name)
        
        val remoteMd5Url = "$baseUrl/$md5Name"
        val remoteZipUrl = "$baseUrl/$zipName"

        return try {
            println("Syncing zip: $zipName")
            
            val remoteMd5 = downloadText(remoteMd5Url)
            if (remoteMd5 == null) {
                println("Could not reach MD5 for $zipName, skipping sync.")
                return false
            }

            // Check if already up to date
            val localMd5 = if (fileSystem.exists(localMd5Path)) {
                fileSystem.read(localMd5Path) { readUtf8().trim() }
            } else null
            
            if (localMd5 == remoteMd5) {
                println("$zipName is already up to date.")
                return true
            }

            println("Downloading and verifying: $remoteZipUrl")
            val success = downloadFile(remoteZipUrl, localZipPath)
            if (success && verifyMd5(localZipPath, remoteMd5)) {
                unzip(localZipPath, commonDir, fileSystem)
                fileSystem.delete(localZipPath)
                
                // Save MD5 locally
                fileSystem.write(localMd5Path) { writeUtf8(remoteMd5) }
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
