package media.controller.nearplay.repository.spotify

import android.content.Context
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.ConnectionParams.AuthMethod
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.PlayerApi
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.*
import com.spotify.protocol.client.error.RemoteClientException
import com.spotify.protocol.error.SpotifyAppRemoteException
import com.spotify.protocol.types.*
import com.spotify.protocol.types.PlaybackSpeed.PodcastPlaybackSpeed
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import media.controller.nearplay.util.asFlow
import media.controller.nearplay.util.awaitCompletion
import media.controller.nearplay.util.awaitResult
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext


@Singleton
@ExperimentalCoroutinesApi
class AppRemote @Inject constructor(
    @ApplicationContext private val context: Context,
    private val config: Config
) : CoroutineScope {

    //<editor-fold desc="Connection Logic">

    private val _errors = MutableStateFlow<SpotifyAppRemoteException?>(null)
    val errors: StateFlow<SpotifyAppRemoteException?> get() = _errors

    val thing = errors.map { exception: SpotifyAppRemoteException? ->
        when (exception) {
            is AuthenticationFailedException        -> Unit
            is CouldNotFindSpotifyApp               -> Unit
            is LoggedOutException                   -> Unit
            is NotLoggedInException                 -> Unit
            is OfflineModeException                 -> Unit
            is RemoteClientException                -> Unit
            is SpotifyConnectionTerminatedException -> Unit
            is SpotifyDisconnectedException         -> Unit
            is SpotifyRemoteServiceException        -> Unit
            is UnsupportedFeatureVersionException   -> Unit
            is UserNotAuthorizedException           -> Unit
        }
    }

    lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    private val connector = object : Connector.ConnectionListener {
        override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
            job = SupervisorJob()
            connectionMutableStateFlow.value = spotifyAppRemote
        }

        override fun onFailure(throwable: Throwable) {
            if (this@AppRemote::job.isInitialized) {
                job.cancel(CancellationException("Spotify Connection Listener Emitted onFailure", throwable))
            }
            connectionMutableStateFlow.value = null
            _errors.value = throwable as SpotifyAppRemoteException
            if (throwable is UserNotAuthorizedException) {
            }
        }
    }

    fun connectToSpotifyAppRemote(connectionParameters: ConnectionParams = defaultConnectionParameters()) {
        SpotifyAppRemote.connect(context, connectionParameters, connector)
    }

    fun disconnect() {
        SpotifyAppRemote.disconnect(connection.value)
    }

    private fun defaultConnectionParameters(showAuthView: Boolean = false) = ConnectionParams
        .Builder(config.clientID)
        .setRedirectUri(config.redirectURI)
        .setAuthMethod(AuthMethod.APP_ID)
        .showAuthView(showAuthView)
        //.setRequiredFeatures() TODO: Broken, un-needed?
        //.setJsonMapper() TODO
        .build()

    //</editor-fold>

    private val connectionMutableStateFlow = MutableStateFlow<SpotifyAppRemote?>(null)
    val connection: StateFlow<SpotifyAppRemote?> get() = connectionMutableStateFlow

    //<editor-fold desc="Images API">
    private val images get() = connection.value?.imagesApi
    suspend fun getImage(imageUri: ImageUri, imageDimension: Image.Dimension = Image.Dimension.LARGE) =
        images?.getImage(imageUri, imageDimension)?.awaitResult(this.coroutineContext)
    //</editor-fold>

    //<editor-fold desc="User API">
    private val user get() = connection.value?.userApi

    val capabilities = connection.flatMapLatest { subscribeToCapabilities() ?: flowOf(null) }
    private fun subscribeToCapabilities() = user?.subscribeToCapabilities()?.asFlow()

    val userStatus = connection.flatMapLatest { subscribeToUserStatus() ?: flowOf(null) }
    private fun subscribeToUserStatus() = user?.subscribeToUserStatus()?.asFlow()

    suspend fun addToLibrary(uri: String) = user?.addToLibrary(uri)?.awaitCompletion(this.coroutineContext)
    suspend fun removeFromLibrary(uri: String) = user?.removeFromLibrary(uri)?.awaitCompletion(this.coroutineContext)
    suspend fun getLibraryState(uri: String) = user?.getLibraryState(uri)?.awaitResult(this.coroutineContext)
    //</editor-fold>

    //<editor-fold desc="Player API">
    private val player get() = connection.value?.playerApi

    val playerState: Flow<PlayerState?> = connection.flatMapLatest { subscribeToPlayerState() ?: flowOf(null) }
    private fun subscribeToPlayerState() = player?.subscribeToPlayerState()?.asFlow()

    val playerContext: Flow<PlayerContext?> = connection.flatMapLatest { subscribeToPlayerContext() ?: flowOf(null) }
    private fun subscribeToPlayerContext() = player?.subscribeToPlayerContext()?.asFlow()

    suspend fun play(uri: String) = player?.play(uri)?.awaitCompletion(this.coroutineContext)
    suspend fun play(uri: String, streamType: PlayerApi.StreamType) = player?.play(uri, streamType)?.awaitCompletion(this.coroutineContext)
    suspend fun queue(uri: String) = player?.queue(uri)?.awaitCompletion(this.coroutineContext)
    suspend fun resume() = player?.resume()?.awaitCompletion(this.coroutineContext)
    suspend fun pause() = player?.pause()?.awaitCompletion(this.coroutineContext)
    suspend fun setPodcastPlaybackSpeed(speed: PodcastPlaybackSpeed?) =
        player?.setPodcastPlaybackSpeed(speed)?.awaitCompletion(this.coroutineContext)

    suspend fun skipNext() = player?.skipNext()?.awaitCompletion(this.coroutineContext)
    suspend fun skipPrevious() = player?.skipPrevious()?.awaitCompletion(this.coroutineContext)
    suspend fun skipToIndex(uri: String, index: Int) = player?.skipToIndex(uri, index)?.awaitCompletion(this.coroutineContext)
    suspend fun setShuffle(enabled: Boolean) = player?.setShuffle(enabled)?.awaitCompletion(this.coroutineContext)
    suspend fun toggleShuffle() = player?.toggleShuffle()?.awaitCompletion(this.coroutineContext)
    enum class RepeatMode(val int: Int) { OFF(0), ONE(1), ALL(2) }

    suspend fun setRepeat(repeatMode: RepeatMode) = player?.setRepeat(repeatMode.int)?.awaitCompletion(this.coroutineContext)
    suspend fun toggleRepeat() = player?.toggleRepeat()?.awaitCompletion(this.coroutineContext)
    suspend fun seekTo(positionMs: Long) = player?.seekTo(positionMs)?.awaitCompletion(this.coroutineContext)
    suspend fun seekToRelativePosition(milliseconds: Long) =
        player?.seekToRelativePosition(milliseconds)?.awaitCompletion(this.coroutineContext)

    suspend fun getCrossfadeState(): CrossfadeState? = player?.crossfadeState?.awaitResult(this.coroutineContext)
    //</editor-fold>

    //<editor-fold desc="Content API">
    private val content get() = connection.value?.contentApi

    enum class ContentType(val contentType: String) {
        AUTOMOTIVE("automotive"),
        DEFAULT("default"),
        NAVIGATION("navigation"),
        FITNESS("fitness"),
        WAKE("wake"),
        SLEEP("sleep")
    }

    fun recommendedFlow() = connection.filterNotNull().flatMapLatest {
        val items = getRecommendedContentItems()?.items ?: emptyArray()
        items.map { it.flowAll(50) }.asFlow().flatMapMerge { it }
    }

    suspend fun getRecommendedContentItems(type: ContentType = ContentType.DEFAULT): ListItems? =
        content?.getRecommendedContentItems(type.contentType)?.awaitResult(this.coroutineContext)

    fun getFlowOfAllChildrenOfItem(listItem: ListItem, perPage: Int = 50): Flow<ListItem> = listItem.flowAll(perPage)

    suspend fun getChildrenOfItem(listItem: ListItem, perPage: Int, offset: Int): ListItems? =
        content?.getChildrenOfItem(listItem, perPage, offset)?.awaitResult(this.coroutineContext)

    private fun ListItem.flowAll(perPage: Int) = flow {
        var offset = 0
        var page = getChildrenOfItem(this@flowAll, perPage, offset)
        while (page != null && page.items.isNotEmpty()) {
            emitAll(page.items.asFlow())
            offset += perPage
            page = getChildrenOfItem(this@flowAll, perPage, offset)
        }
    }

    suspend fun playContentItem(listItem: ListItem) = content?.playContentItem(listItem)?.awaitCompletion(this.coroutineContext)
    //</editor-fold>

    //<editor-fold desc="Connect API">
    private val connect get() = connection.value?.connectApi
    suspend fun connectSwitchToLocalDevice() = connect?.connectSwitchToLocalDevice()?.awaitCompletion(this.coroutineContext)
    //</editor-fold>

}