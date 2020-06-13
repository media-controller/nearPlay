package media.controller.nearplay.viewModels

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.adamratzman.spotify.endpoints.public.SearchApi.SearchType.*
import com.adamratzman.spotify.models.SpotifySearchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import media.controller.nearplay.repository.spotify.AppRemote
import media.controller.nearplay.repository.spotify.Auth
import media.controller.nearplay.repository.spotify.WebClient

class SearchViewModel @ViewModelInject constructor(
    @Assisted private val state: SavedStateHandle,
    private val auth: Auth,
    private val client: WebClient,
    private val remote: AppRemote
) : ViewModel() {

    private val searchFlow = MutableStateFlow<SpotifySearchResult?>(null)

    val artists = searchFlow.map { it?.artists?.items.orEmpty() }
    val artistsViews = artists
        .map { it.map { it.id } }
        .map { client.api.artists.getArtists(*it.toTypedArray()).suspendComplete() }
        .map { it.map { it?.images?.get(0)?.url } }
        .asLiveData()

    val searchResults = searchFlow.filterNotNull().asLiveData()

    fun search(query: String) {
        viewModelScope.launch {
            searchFlow.value = client.api.search.search(query, ALBUM, ARTIST, PLAYLIST, TRACK, limit = 5).suspendComplete()
        }
    }

}