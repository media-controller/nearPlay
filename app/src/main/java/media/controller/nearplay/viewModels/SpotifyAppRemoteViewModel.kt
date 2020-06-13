package media.controller.nearplay.viewModels

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import androidx.palette.graphics.Palette
import com.spotify.protocol.types.Image
import com.spotify.protocol.types.LibraryState
import com.spotify.protocol.types.PlayerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import media.controller.nearplay.repository.spotify.AppRemote
import media.controller.nearplay.repository.spotify.Auth

class SpotifyAppRemoteViewModel @ViewModelInject constructor(
    val auth: Auth,
    val remote: AppRemote,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val connector = auth.authStateFlow.map {
        when (it) {
            is Auth.State.Code  -> TODO()
            is Auth.State.Token -> remote.connectToSpotifyAppRemote()
            null                -> null
        }
    }.launchIn(viewModelScope)

    val playerState = remote.playerState.asLiveData()

    val interpolatedPlaybackPosition = playerState.switchMap { interpolatePlaybackPosition(it) }
    private fun interpolatePlaybackPosition(playerState: PlayerState?, updateInterval: Long = 100L) = liveData {
        emit(playerState?.playbackPosition)
        playerState?.run {
            if (!isPaused) {
                val timeOfLastPlaybackPositionUpdate = System.currentTimeMillis()
                do {
                    delay(updateInterval)
                    val elapsed = System.currentTimeMillis() - timeOfLastPlaybackPositionUpdate
                    val interpolatedPlaybackPosition = playbackPosition + elapsed
                    emit(interpolatedPlaybackPosition)
                } while (true)
            }
        }
    }

    val nowPlayingLibraryState = playerState.switchMap {
        liveData {
            val libraryState: LibraryState? = it?.track?.uri?.let {
                remote.getLibraryState(it)
            }
            emit(libraryState)
        }
    }

    fun toggleTrackFavorite() = viewModelScope.launch {
        nowPlayingLibraryState.value?.let {
            if (it.canAdd) {
                if (it.isAdded) {
                    remote.removeFromLibrary(it.uri)
                } else {
                    remote.addToLibrary(it.uri)
                }
            }
        }
    }

    private val trackImageUri = playerState
        .map { it?.track?.imageUri }
        .distinctUntilChanged()

    val trackArt = trackImageUri.switchMap { imageUri ->
        liveData {
            emit(imageUri?.let { remote.getImage(it, Image.Dimension.LARGE) })
        }
    }

    val trackPalette = trackArt.switchMap { bitmap ->
        liveData(Dispatchers.Default) {
            emit(bitmap?.let { Palette.from(it).generate() })
        }
    }

    val userCanPlayOnDemand = remote.capabilities
        .map { it?.canPlayOnDemand }.asLiveData()

    val userStatus = remote.userStatus.asLiveData()
    val playerContext = remote.playerContext.asLiveData()
    val recommendedFlow = remote.recommendedFlow().asLiveData()
}