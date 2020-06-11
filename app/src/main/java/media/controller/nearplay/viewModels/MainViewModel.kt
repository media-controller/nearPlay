package media.controller.nearplay.viewModels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.spotify.sdk.android.auth.AuthorizationResponse

class MainViewModel @ViewModelInject constructor(
//    savedStateHandle: SavedStateHandle
) : ViewModel() {

    fun setAuthentication(response: AuthorizationResponse) {

    }


    val isNightMode = MutableLiveData(true)

}
