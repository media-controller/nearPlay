package media.controller.nearplay

import android.content.Context

object Configuration {

    lateinit var context: Context

    fun injectContext(context: Context) {
        Configuration.context = context
    }

    const val fineLocationPermissionRequestCode = 8724
    const val fineLocationPermission = "android.permission.ACCESS_FINE_LOCATION"
    const val readExternalPermissionsRequestCode = 8724
    const val readExternalPermissions = "android.permission.READ_EXTERNAL_STORAGE"
}