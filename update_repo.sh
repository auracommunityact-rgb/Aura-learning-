#!/bin/bash
cat << 'INNER' >> app/src/main/java/com/example/data/repository/AuraRepository.kt

    suspend fun uploadResultImage(imageBytes: ByteArray, fileName: String): String {
        return try {
            val bucket = client.storage["results"]
            bucket.upload(fileName, imageBytes, upsert = true)
            bucket.publicUrl(fileName)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
INNER
