package media.controller.nearplay.repository.spotify

import com.tfcporciuncula.flow.FlowSharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Auth @Inject constructor(
    private val prefs: FlowSharedPreferences
) {

    val authStateFlow: StateFlow<State?> get() = _auth
    private val _auth = MutableStateFlow<State?>(null)

    fun setAuthState(state: State){
        hasSpotifyAssociation.set(value = true)
        _auth.value = state
    }

    private val hasSpotifyAssociation = prefs.getBoolean("hasSpotifyAssociation")
    val spotifyAssociated = hasSpotifyAssociation.asFlow()

    sealed class State {
        data class Code(
            val authCode: String,
            val state: String
        ) : State()

        data class Token(
            val authToken: String,
            val expiresIn: Int,
            val state: String
        ) : State()
    }
}