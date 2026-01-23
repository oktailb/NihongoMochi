package org.nihongo.mochi.domain.services

import okio.FileSystem
import okio.Path

actual fun unzip(zipFilePath: Path, targetDir: Path, fileSystem: FileSystem) {
    // Stub for iOS - to be implemented with SSZipArchive or similar if needed later
    println("Unzip not yet implemented on iOS")
}
