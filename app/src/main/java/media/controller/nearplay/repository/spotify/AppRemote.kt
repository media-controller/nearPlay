package media.controller.nearplay.repository.spotify

import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.ConnectionParams.AuthMethod
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.PlayerApi
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.*
import com.spotify.protocol.types.PlaybackSpeed.PodcastPlaybackSpeed
import kotlinx.coroutines.flow.*
import media.controller.nearplay.util.asFlow
import media.controller.nearplay.util.awaitCompletion
import media.controller.nearplay.util.awaitResult

object AppRemote {

    private val _connection = MutableStateFlow<SpotifyAppRemote?>(null)
    val connection: StateFlow<SpotifyAppRemote?> get() = _connection

    //<editor-fold desc="Images API">
    private val images get() = connection.value?.imagesApi
    suspend fun getImage(imageUri: ImageUri, imageDimension: Image.Dimension = Image.Dimension.LARGE) =
        images?.getImage(imageUri, imageDimension)?.awaitResult()
    //</editor-fold>

    //<editor-fold desc="User API">
    private val user get() = connection.value?.userApi

    val capabilities = connection.flatMapLatest { subscribeToCapabilities() ?: flowOf(null) }
    private fun subscribeToCapabilities() = user?.subscribeToCapabilities()?.asFlow()

    val userStatus = connection.flatMapLatest { subscribeToUserStatus() ?: flowOf(null) }
    private fun subscribeToUserStatus() = user?.subscribeToUserStatus()?.asFlow()

    suspend fun addToLibrary(uri: String) = user?.addToLibrary(uri)?.awaitCompletion()
    suspend fun removeFromLibrary(uri: String) = user?.removeFromLibrary(uri)?.awaitCompletion()
    suspend fun getLibraryState(uri: String) = user?.getLibraryState(uri)?.awaitResult()
    //</editor-fold>

    //<editor-fold desc="Player API">
    private val player get() = connection.value?.playerApi

    val playerState: Flow<PlayerState?> = connection.flatMapLatest { subscribeToPlayerState() ?: flowOf(null) }
    private fun subscribeToPlayerState() = player?.subscribeToPlayerState()?.asFlow()

    val playerContext: Flow<PlayerContext?> = connection.flatMapLatest { subscribeToPlayerContext() ?: flowOf(null) }
    private fun subscribeToPlayerContext() = player?.subscribeToPlayerContext()?.asFlow()

    suspend fun play(uri: String) = player?.play(uri)?.awaitCompletion()
    suspend fun play(uri: String, streamType: PlayerApi.StreamType) = player?.play(uri, streamType)?.awaitCompletion()
    suspend fun queue(uri: String) = player?.queue(uri)?.awaitCompletion()
    suspend fun resume() = player?.resume()?.awaitCompletion()
    suspend fun pause() = player?.pause()?.awaitCompletion()
    suspend fun setPodcastPlaybackSpeed(speed: PodcastPlaybackSpeed?) = player?.setPodcastPlaybackSpeed(speed)?.awaitCompletion()
    suspend fun skipNext() = player?.skipNext()?.awaitCompletion()
    suspend fun skipPrevious() = player?.skipPrevious()?.awaitCompletion()
    suspend fun skipToIndex(uri: String, index: Int) = player?.skipToIndex(uri, index)?.awaitCompletion()
    suspend fun setShuffle(enabled: Boolean) = player?.setShuffle(enabled)?.awaitCompletion()
    suspend fun toggleShuffle() = player?.toggleShuffle()?.awaitCompletion()
    enum class RepeatMode(val int: Int) { OFF(0), ONE(1), ALL(2) }

    suspend fun setRepeat(repeatMode: RepeatMode) = player?.setRepeat(repeatMode.int)?.awaitCompletion()
    suspend fun toggleRepeat() = player?.toggleRepeat()?.awaitCompletion()
    suspend fun seekTo(positionMs: Long) = player?.seekTo(positionMs)?.awaitCompletion()
    suspend fun seekToRelativePosition(milliseconds: Long) = player?.seekToRelativePosition(milliseconds)?.awaitCompletion()
    suspend fun getCrossfadeState(): CrossfadeState? = player?.crossfadeState?.awaitResult()
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
        content?.getRecommendedContentItems(type.contentType)?.awaitResult()

    fun getFlowOfAllChildrenOfItem(listItem: ListItem, perPage: Int = 50): Flow<ListItem> = listItem.flowAll(perPage)

    suspend fun getChildrenOfItem(listItem: ListItem, perPage: Int, offset: Int): ListItems? =
        content?.getChildrenOfItem(listItem, perPage, offset)?.awaitResult()

    private fun ListItem.flowAll(perPage: Int) = flow {
        var offset = 0
        var page = getChildrenOfItem(this@flowAll, perPage, offset)
        while (page != null && page.items.isNotEmpty()) {
            emitAll(page.items.asFlow())
            offset += perPage
            page = getChildrenOfItem(this@flowAll, perPage, offset)
        }
    }

    suspend fun playContentItem(listItem: ListItem) = content?.playContentItem(listItem)?.awaitCompletion()
    //</editor-fold>

    //<editor-fold desc="Connect API">
    private val connect get() = connection.value?.connectApi
    suspend fun connectSwitchToLocalDevice() = connect?.connectSwitchToLocalDevice()?.awaitCompletion()
    //</editor-fold>

    //<editor-fold desc="Connection Logic">
    private val connector = object : Connector.ConnectionListener {
        override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
            _connection.value = spotifyAppRemote
        }

        override fun onFailure(throwable: Throwable) {
            _connection.value = null
        }
    }

    fun connectToSpotifyAppRemote(connectionParameters: ConnectionParams = defaultConnectionParameters) {
        SpotifyAppRemote.connect(Configuration.context, connectionParameters, connector)
    }

    fun disconnect() {
        SpotifyAppRemote.disconnect(connection.value)
    }

    private val defaultConnectionParameters = ConnectionParams
        .Builder(Configuration.CLIENT_ID)
        .setRedirectUri(Configuration.REDIRECT_URI)
        .setAuthMethod(AuthMethod.APP_ID)
        .showAuthView(false)
        //.setRequiredFeatures() TODO: Broken, un-needed?
        //.setJsonMapper() TODO
        .build()
    //</editor-fold>

}