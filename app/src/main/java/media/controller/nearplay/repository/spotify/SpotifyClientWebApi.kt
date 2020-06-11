package media.controller.nearplay.repository.spotify

import com.adamratzman.spotify.SpotifyClientApi
import com.adamratzman.spotify.models.Token
import com.adamratzman.spotify.spotifyImplicitGrantApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpotifyClientWebApi @Inject constructor(
    private val auth: Auth,
    private val spotifySpotifyConfig: SpotifyConfig
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
                        spotifySpotifyConfig.CLIENT_ID,
                        spotifySpotifyConfig.REDIRECT_URI,
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