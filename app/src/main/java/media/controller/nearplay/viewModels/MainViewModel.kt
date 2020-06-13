package media.controller.nearplay.viewModels

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.spotify.sdk.android.auth.AuthorizationResponse

class MainViewModel @ViewModelInject constructor(
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    fun setAuthentication(response: AuthorizationResponse) {

    }


    val isNightMode = MutableLiveData(true)

}
