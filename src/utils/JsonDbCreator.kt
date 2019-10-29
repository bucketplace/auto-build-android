package utils

import io.jsondb.JsonDBTemplate

object JsonDbCreator {

    inline fun <reified T> create(fileLocation: String, baseScanPackage: String): JsonDBTemplate {
        return JsonDBTemplate(fileLocation, baseScanPackage).apply {
            if (!collectionExists(T::class.java)) {
                createCollection(T::class.java)
            }
        }
    }
}