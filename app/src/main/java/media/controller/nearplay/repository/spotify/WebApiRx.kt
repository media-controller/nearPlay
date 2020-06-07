package media.controller.nearplay.repository.spotify

//import com.adamratzman.spotify.SpotifyClientApi
//import com.adamratzman.spotify.models.Artist
//import com.adamratzman.spotify.models.SavedAlbum
//import com.adamratzman.spotify.models.SavedTrack
//import com.adamratzman.spotify.models.Track
//import com.adamratzman.spotify.spotifyClientApi
//import com.spotify.sdk.android.auth.AuthorizationResponse
//import io.reactivex.BackpressureStrategy
//import io.reactivex.Observable
//import io.reactivex.subjects.BehaviorSubject
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.flatMapLatest
//import kotlinx.coroutines.flow.mapLatest
//import kotlinx.coroutines.reactive.asFlow
//import kotlinx.coroutines.rx2.rxSingle
//import timber.log.Timber
//
//
//object WebApiRx {
//    private val authSubject = BehaviorSubject.create<SpotifyAuthentication>()
//    val authObservable: Observable<SpotifyAuthentication>
//        get() = authSubject
//
//    fun setAuthentication(response: AuthorizationResponse) = with(response) {
//        when (type) {
//            AuthorizationResponse.Type.CODE          -> authSubject.onNext(SpotifyAuthentication.Code(code).also {Timber.d("%s", it)})
//            AuthorizationResponse.Type.TOKEN         -> authSubject.onNext(SpotifyAuthentication.Token(accessToken, expiresIn).also {Timber.d("%s", it)})
//            AuthorizationResponse.Type.ERROR         -> Timber.e("got response.type == %s with error msg = %s", type, error)
//            AuthorizationResponse.Type.EMPTY,
//            AuthorizationResponse.Type.UNKNOWN, null -> Timber.e(
//                "got response.type == %s",
//                type
//            )
//        }
//    }
//
//    sealed class SpotifyAuthentication(val whenGranted: Long) {
//        data class Code(val code: String) : SpotifyAuthentication(System.currentTimeMillis())
//        data class Token(val accessToken: String, val expiresIn: Int) : SpotifyAuthentication(System.currentTimeMillis())
//        data class ExpiredToken(val whenExpired: Long) : SpotifyAuthentication(whenExpired) // TODO
//        val Token.isExpired get() = System.currentTimeMillis() > whenGranted + (expiresIn * 1000)
//    }
//
//    private val spotifyClientApiSubject: Observable<SpotifyClientApi> = authSubject.flatMapSingle {
//        when (it) {
//            is SpotifyAuthentication.Token     -> {
//                rxSingle {
//                    spotifyClientApi {
//                        credentials {
//                            clientId = Configuration.CLIENT_ID
//                            redirectUri = Configuration.REDIRECT_URI
//                        }
//                        authorization {
//                            tokenString = it.accessToken
//                            authorizationCode =
//                                getAuthorizationUrl(*Configuration.scopeArray)
//                        }
//                        options {
//                            enableLogger = true
//                            testTokenValidity = true
//                        }
//                    }.suspendBuild()
//                }
//            }
//            is SpotifyAuthentication.Code      -> {
//                TODO()
//            }
//            is SpotifyAuthentication.ExpiredToken -> TODO()
//        }
//    }.share()
//
//    private val spotifyClientApiFlow: Flow<SpotifyClientApi> = spotifyClientApiSubject.toFlowable(BackpressureStrategy.DROP).asFlow()
//
//    val userId: Flow<String> = spotifyClientApiFlow.mapLatest { it.userId }
//    val savedTracks: Flow<SavedTrack> = spotifyClientApiFlow.flatMapLatest { it.library.getSavedTracks().flow() }
//    val savedAlbums: Flow<SavedAlbum> = spotifyClientApiFlow.flatMapLatest { it.library.getSavedAlbums().flow() }
//    val topTracks: Flow<Track> = spotifyClientApiFlow.flatMapLatest { it.personalization.getTopTracks().flow() }
//    val topArtists: Flow<Artist> = spotifyClientApiFlow.flatMapLatest { it.personalization.getTopArtists().flow() }
//}