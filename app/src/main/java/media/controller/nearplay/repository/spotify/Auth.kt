package media.controller.nearplay.repository.spotify

import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Auth @Inject constructor() {

    val authStateFlow = MutableStateFlow<State?>(null)

    sealed class State {
        data class Code(
            val authCode: String,
            val state: String
        ) : State()

        data class Token(
            val authToken: String,
            val expiresIn: Int,
            val state: String
        ) : State()
    }
}