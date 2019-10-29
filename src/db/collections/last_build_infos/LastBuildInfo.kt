package db.collections.last_build_infos

import io.jsondb.annotation.Document
import io.jsondb.annotation.Id

import java.io.Serializable

@Document(collection = "last_build_infos", schemaVersion = "1.0")
data class LastBuildInfo(
    @Id var appVersion: String? = null,
    var buildNumber: Int = 0
) : Serializable
