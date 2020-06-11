package media.controller.nearplay.viewModels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.adamratzman.spotify.endpoints.public.SearchApi.SearchType.*
import com.adamratzman.spotify.models.SpotifySearchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import media.controller.nearplay.repository.spotify.SpotifyClientWebApi

class SearchViewModel @ViewModelInject constructor(
//    private val auth: Auth,
    private val client: SpotifyClientWebApi
) : ViewModel() {

    private val searchFlow = MutableStateFlow<SpotifySearchResult?>(null)
    val searchResults = searchFlow.filterNotNull().asLiveData()

    fun search(query: String) {
        viewModelScope.launch {
            searchFlow.value = client.api.search.search(query, ALBUM, ARTIST, PLAYLIST, TRACK).suspendComplete()
        }
    }

}