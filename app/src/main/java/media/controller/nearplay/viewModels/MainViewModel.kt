package media.controller.nearplay.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.spotify.sdk.android.auth.AuthorizationResponse

class MainViewModel(
//    savedStateHandle: SavedStateHandle
) : ViewModel() {

    fun setAuthentication(response: AuthorizationResponse) {

    }


    val isNightMode = MutableLiveData(true)

}
