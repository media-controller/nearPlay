package media.controller.nearplay.viewModels

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.scan
import media.controller.nearplay.repository.nearby.Connections
import media.controller.nearplay.repository.nearby.Connections.EndpointDiscoveryEvent.Found
import media.controller.nearplay.repository.nearby.Connections.EndpointDiscoveryEvent.Lost

class NearbyViewModel @ViewModelInject constructor(
    @Assisted private val savedStateHandle: SavedStateHandle,
    private val connections: Connections
) : ViewModel() {

    val devices = connections.discoverDevices()
        .scan(emptyMap()) { accumulator: Map<String, String>, event ->
            when (event) {
                is Found -> TODO()
                is Lost  -> TODO()
            }
        }

}
