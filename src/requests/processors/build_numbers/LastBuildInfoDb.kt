package requests.processors.build_numbers

import db.collections.last_build_infos.LastBuildInfo
import utils.JsonDbCreator

class LastBuildInfoDb(private val appVersion: String) {

    companion object {
        private const val DB_FILES_LOCATION = "./db"
        private const val BASE_SCAN_PACKAGE = "db.collections.last_build_infos"
    }

    private val jsonDb = JsonDbCreator.create<LastBuildInfo>(DB_FILES_LOCATION, BASE_SCAN_PACKAGE)

    fun getNewBuildNumber(): Int {
        return jsonDb.findById<LastBuildInfo>(appVersion, LastBuildInfo::class.java)?.buildNumber?.let { it + 1 } ?: 1
    }

    fun saveBuildNumber(buildNumber: Int) {
        jsonDb.upsert<LastBuildInfo>(LastBuildInfo(appVersion, buildNumber))
    }
}