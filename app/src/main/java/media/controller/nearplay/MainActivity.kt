package media.controller.nearplay

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.navigation.NavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse.Type
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import media.controller.nearplay.repository.spotify.Auth
import media.controller.nearplay.repository.spotify.Config
import media.controller.nearplay.viewModels.MainViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    @Inject lateinit var auth: Auth
    @Inject lateinit var config: Config
    private val viewModel: MainViewModel by viewModels()
    private var currentNavController: LiveData<NavController>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        } // Else, need to wait for onRestoreInstanceState

        auth.authStateFlow.asLiveData().observe(this, Observer {
            log_in_to_spotify_button.visibility = when (it) {
                null                -> View.VISIBLE
                is Auth.State.Code,
                is Auth.State.Token -> View.GONE
            }
        })

        auth.spotifyAssociated.asLiveData().observe(this, Observer { spotifyAssociated ->
            if (spotifyAssociated && auth.authStateFlow.value == null)
                requestSpotifyAuthentication(Type.TOKEN)
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

    fun onLogInToSpotifyButtonClicked(view: View) {
        requestSpotifyAuthentication(Type.TOKEN)
    }



    private fun requestSpotifyAuthentication(type: Type) {
        val request = AuthorizationRequest
            .Builder(config.clientID, type, config.redirectURI)
            .setState("123456")
            .setShowDialog(false)
            .setScopes(config.scopeUris.toTypedArray())
            .setCampaign(SpotifyCampaign)
            .build()

        val intent = AuthorizationClient.createLoginActivityIntent(this, request)

        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val response = AuthorizationClient.getResponse(it.resultCode, it.data)
            when (response.type) {
                Type.CODE  -> auth.setAuthState(Auth.State.Code(response.code, response.state))
                Type.TOKEN -> auth.setAuthState(Auth.State.Token(response.accessToken, response.expiresIn, response.state))
                Type.EMPTY,
                Type.UNKNOWN,
                Type.ERROR,
                null       -> response //TODO()
            }
        }.launch(intent)
    }
}

const val SpotifyCampaign = "NearPlayCampaign"