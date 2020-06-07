package media.controller.nearplay

import androidx.multidex.MultiDexApplication
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.spotify.android.appremote.api.SpotifyAppRemote
import media.controller.nearplay.repository.spotify.Configuration
import timber.log.Timber

class NearPlayApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            SpotifyAppRemote.setDebugMode(true)
            onCreateDebug()
        }

        SpotifyBroadcastReceiver().register(applicationContext)
        Configuration.injectContext(applicationContext)
        media.controller.nearplay.Configuration.injectContext(applicationContext)

        setupAppUpdater()
//        SpotifyRepository.connectToSpotifyAppRemote(applicationContext)
//        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
    }

    private fun setupAppUpdater() {
        // Creates instance of the manager.
        val appUpdateManager = AppUpdateManagerFactory.create(applicationContext)

        // Returns an intent object that you use to check for an update.
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                // For a flexible update, use AppUpdateType.FLEXIBLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                Timber.d("IMMEDIATE Update available.")
                // TODO: Request the update.
            }
        }
    }

    private fun onCreateDebug() {
        System.setProperty("kotlinx.coroutines.debug", "on")
        setupTimber()
    }

    private fun setupTimber() {
        val tree = LineNumberDebugTree()
        Timber.plant(tree)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Timber.d("Low Memory")
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Timber.d("(level = %s)", level)
    }

    override fun onTerminate() {
        super.onTerminate()
        Timber.d("App Terminating")
    }
}

class LineNumberDebugTree : Timber.DebugTree() {
    override fun createStackElementTag(element: StackTraceElement): String {
        return "[(${element.fileName}:${element.lineNumber})\$${element.methodName}]"
    }
}

