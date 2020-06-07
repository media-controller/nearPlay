package media.controller.nearplay

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse.Type
import kotlinx.android.synthetic.main.activity_main.*
import media.controller.nearplay.Configuration.fineLocationPermissionRequestCode
import media.controller.nearplay.repository.spotify.AppRemote
import media.controller.nearplay.repository.spotify.Configuration.CLIENT_ID
import media.controller.nearplay.repository.spotify.Configuration.REDIRECT_URI
import media.controller.nearplay.repository.spotify.Configuration.REQUEST_CODE
import media.controller.nearplay.repository.spotify.Configuration.scopeUris
import media.controller.nearplay.viewModels.MainViewModel
import media.controller.nearplay.viewModels.SpotifyAppRemoteViewModel

class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private var currentNavController: LiveData<NavController>? = null
    private val viewModel: MainViewModel by viewModels()
    private val remote = AppRemote
    private val remoteVM: SpotifyAppRemoteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        } // Else, need to wait for onRestoreInstanceState

        remoteVM.playerState.observe(this, Observer {
            log_in_to_spotify_button.visibility = if (it == null) View.VISIBLE else View.GONE
        })
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Now that BottomNavigationBar has restored its instance state
        // and its selectedItemId, we can proceed with setting up the
        // BottomNavigationBar with Navigation
        setupBottomNavigationBar()
    }

    /**
     * Called on first creation and when restoring state.
     */
    private fun setupBottomNavigationBar() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)

        val navGraphIds = listOf(
            R.navigation.navigation_search,
            R.navigation.navigation_queue,
            R.navigation.navigation_party
        )

        // Setup the bottom navigation view with a list of navigation graphs
        val controller = bottomNavigationView.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.nav_host_container,
            intent = intent
        )

        // Whenever the selected controller changes, setup the action bar.
        controller.observe(this, Observer { navController ->
            //            setupActionBarWithNavController(navController)
        })
        currentNavController = controller

        setupGestureCompatibleUI()
    }


    override fun onSupportNavigateUp(): Boolean = currentNavController?.value?.navigateUp() ?: false

    private fun setupGestureCompatibleUI() {
        renderFullScreen()
        applyBottomNavInsetsListenerIfApplicable()
    }

    private fun renderFullScreen() {
        val rootView = window.decorView.rootView
        ViewCompat.requestApplyInsets(rootView)
        rootView.systemUiVisibility =
//            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
//            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }

    private fun applyBottomNavInsetsListenerIfApplicable() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        bottomNavigationView.setOnApplyWindowInsetsListener { view, insets ->
            view.updatePadding(bottom = insets.systemWindowInsetBottom)
            insets
        }
    }

    var connected = false

    fun onLogInToSpotifyButtonClicked(view: View) {
        //requestSpotifyAuthentication(TOKEN)

        if (!connected) {
            remote.connectToSpotifyAppRemote()
        } else {
            remote.disconnect()
        }
        connected = !connected
    }

    private fun requestSpotifyAuthentication(type: Type) {
        val request = AuthorizationRequest.Builder(CLIENT_ID, type, REDIRECT_URI).apply {
            setScopes(scopeUris.toTypedArray())
            setShowDialog(false)
            setCampaign("nearplay")
            setState("1234567890vSTATE") //TODO: Make secure?
            setCustomParam("vKey", "vValue")
        }.build()

        AuthorizationClient.openLoginActivity(
            this,
            REQUEST_CODE,
            request
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) { //Spotify Auth Activity
            //viewModel.setAuthentication(AuthorizationClient.getResponse(resultCode, intent))
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == fineLocationPermissionRequestCode) {
            // Request for camera permission.
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permission has been granted. Start camera preview Activity.
//                layout.showSnackbar(R.string.camera_permission_granted, Snackbar.LENGTH_SHORT)
//                startCamera()
            } else {
                // Permission request was denied.
//                layout.showSnackbar(R.string.camera_permission_denied, Snackbar.LENGTH_SHORT)
            }
        }
    }

}
