package org.nihongo.mochi.domain.services

import okio.FileSystem
import okio.Path
import java.util.zip.ZipInputStream
import java.io.FileInputStream
import java.io.File

actual fun unzip(zipFilePath: Path, targetDir: Path, fileSystem: FileSystem) {
    val zipFile = File(zipFilePath.toString())
    val targetDirectory = File(targetDir.toString())
    
    if (!targetDirectory.exists()) {
        targetDirectory.mkdirs()
    }

    ZipInputStream(FileInputStream(zipFile)).use { zipInputStream ->
        var entry = zipInputStream.nextEntry
        while (entry != null) {
            val file = File(targetDirectory, entry.name)
            
            if (entry.isDirectory) {
                file.mkdirs()
            } else {
                // Ensure parent directory exists
                file.parentFile?.mkdirs()
                
                file.outputStream().use { outputStream ->
                    zipInputStream.copyTo(outputStream)
                }
            }
            zipInputStream.closeEntry()
            entry = zipInputStream.nextEntry
        }
    }
}
