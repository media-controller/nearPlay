package media.controller.nearplay.repository.spotify

import com.adamratzman.spotify.SpotifyClientApi
import com.adamratzman.spotify.models.Token
import com.adamratzman.spotify.spotifyImplicitGrantApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebClient @Inject constructor(
    auth: Auth,
    private val config: Config
) {

    private val spotifyClientApiMutableStateFlow = MutableStateFlow<SpotifyClientApi?>(null)
    val spotifyClientApiStateFlow: StateFlow<SpotifyClientApi?> get() = spotifyClientApiMutableStateFlow

    val api: SpotifyClientApi get() = spotifyClientApiStateFlow.value!!

    init {
        auth.authStateFlow.map {
            when (it) {
                is Auth.State.Code  -> TODO()
                is Auth.State.Token -> {
                    spotifyImplicitGrantApi(
                        config.clientID,
                        config.redirectURI,
                        Token(
                            accessToken = it.authToken,
                            tokenType = "Bearer",
                            expiresIn = it.expiresIn
                        )
                    )
                }
                null                -> null
            }
        }.onEach { spotifyClientApiMutableStateFlow.value = it }
            .launchIn(GlobalScope)
    }

    fun getSavedTracksFlow() = api.library.getSavedTracks().flow()

}