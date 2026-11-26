package com.explorer.makanza.services

import java.io.File
import java.util.UUID

interface StorageService {
    suspend fun saveImage(bytes: ByteArray, extension: String): String
    suspend fun deleteImage(imageUrl: String): Boolean
}

class LocalStorageService(
    private val uploadDir: String = "./uploads",
    private val baseUrl: String = "/uploads"
) : StorageService {

    init {
        File(uploadDir).mkdirs()
    }

    override suspend fun saveImage(bytes: ByteArray, extension: String): String {
        val ext = if (extension.startsWith(".")) extension else ".$extension"
        val fileName = "${UUID.randomUUID()}$ext"
        val file = File(uploadDir, fileName)
        file.writeBytes(bytes)
        return "$baseUrl/$fileName"
    }

    override suspend fun deleteImage(imageUrl: String): Boolean {
        val fileName = imageUrl.substringAfterLast("/")
        val file = File(uploadDir, fileName)
        return if (file.exists()) file.delete() else false
    }
}
