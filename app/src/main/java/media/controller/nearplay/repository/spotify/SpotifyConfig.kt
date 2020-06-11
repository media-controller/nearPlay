package media.controller.nearplay.repository.spotify

import com.adamratzman.spotify.SpotifyScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpotifyConfig @Inject constructor() {

    val CLIENT_ID: String = "d963d5076bb541ea85cb988b3e3d9e7f"
    val REDIRECT_URI: String = "nearplay://callback"
    val REQUEST_CODE: Int = 7562

    val scopeArray = arrayOf(
        SpotifyScope.APP_REMOTE_CONTROL,
        SpotifyScope.PLAYLIST_MODIFY_PRIVATE,
        SpotifyScope.PLAYLIST_MODIFY_PUBLIC,
        SpotifyScope.PLAYLIST_READ_COLLABORATIVE,
        SpotifyScope.PLAYLIST_READ_PRIVATE,
        SpotifyScope.STREAMING,
        SpotifyScope.UGC_IMAGE_UPLOAD,
        SpotifyScope.USER_FOLLOW_MODIFY,
        SpotifyScope.USER_FOLLOW_READ,
        SpotifyScope.USER_LIBRARY_MODIFY,
        SpotifyScope.USER_LIBRARY_READ,
        SpotifyScope.USER_MODIFY_PLAYBACK_STATE,
        //USER_READ_BIRTHDATE, // TODO: Invalid Scope?
        SpotifyScope.USER_READ_CURRENTLY_PLAYING,
        SpotifyScope.USER_READ_EMAIL,
        SpotifyScope.USER_READ_PLAYBACK_STATE,
        SpotifyScope.USER_READ_PRIVATE,
        SpotifyScope.USER_READ_RECENTLY_PLAYED,
        SpotifyScope.USER_TOP_READ
    )
    val scopeUris = scopeArray.map { it.uri }

}