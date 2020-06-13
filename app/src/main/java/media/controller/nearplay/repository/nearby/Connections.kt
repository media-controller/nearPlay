package media.controller.nearplay.repository.nearby

import com.google.android.gms.nearby.connection.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import media.controller.nearplay.repository.nearby.Connections.EndpointDiscoveryEvent.Found
import media.controller.nearplay.repository.nearby.Connections.EndpointDiscoveryEvent.Lost
import media.controller.nearplay.repository.nearby.Connections.PayloadEvent.Received
import media.controller.nearplay.repository.nearby.Connections.PayloadEvent.TransferUpdate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ExperimentalCoroutinesApi
class Connections @Inject constructor(
    private val config: Configuration,
    private val client: ConnectionsClient
) {

    sealed class EndpointDiscoveryEvent {
        abstract val id: String

        data class Found(override val id: String, val serviceID: String, val name: String) : EndpointDiscoveryEvent()
        data class Lost(override val id: String) : EndpointDiscoveryEvent()
    }

    fun discoverDevices(strategy: Strategy = Strategy.P2P_STAR) = callbackFlow<EndpointDiscoveryEvent> {
        val options = DiscoveryOptions.Builder().setStrategy(strategy).build()
        val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                offer(Found(endpointId, info.serviceId, info.endpointName))
            }

            override fun onEndpointLost(endpointId: String) {
                offer(Lost(endpointId))
            }
        }
        client.startDiscovery(config.serviceID, endpointDiscoveryCallback, options).await()
        awaitClose { client.stopDiscovery() }
    }

    sealed class ConnectionLifeCycleEvent {
        abstract val endPointID: String

        data class ConnectionInitiated(override val endPointID: String, val connectionInfo: ConnectionInfo) : ConnectionLifeCycleEvent()
        data class ConnectionResult(override val endPointID: String, val resolution: ConnectionResolution) : ConnectionLifeCycleEvent()
        data class Disconnected(override val endPointID: String) : ConnectionLifeCycleEvent()
    }

    fun startAdvertising(
        nickname: String,
        strategy: Strategy = Strategy.P2P_STAR
    ) = callbackFlow<ConnectionLifeCycleEvent> {
        val options = AdvertisingOptions.Builder().setStrategy(strategy).build()
        val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
            override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
                offer(ConnectionLifeCycleEvent.ConnectionInitiated(endpointId, connectionInfo))
            }

            override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
                offer(ConnectionLifeCycleEvent.ConnectionResult(endpointId, resolution))
            }

            override fun onDisconnected(endpointId: String) {
                offer(ConnectionLifeCycleEvent.Disconnected(endpointId))
            }
        }
        client.startAdvertising(nickname, config.serviceID, connectionLifecycleCallback, options).await()
        awaitClose { client.stopAdvertising() }
    }

    fun requestConnection(myName: String, endpointID: String) = callbackFlow<ConnectionLifeCycleEvent> {
        val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
            override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
                offer(ConnectionLifeCycleEvent.ConnectionInitiated(endpointId, connectionInfo))
            }

            override fun onConnectionResult(endpointId: String, resolution: ConnectionResolution) {
                offer(ConnectionLifeCycleEvent.ConnectionResult(endpointId, resolution))
            }

            override fun onDisconnected(endpointId: String) {
                offer(ConnectionLifeCycleEvent.Disconnected(endpointId))
            }
        }
        client.requestConnection(myName, endpointID, connectionLifecycleCallback).await()
    }

    sealed class PayloadEvent {
        abstract val endPointID: String

        data class Received(override val endPointID: String, val payload: Payload) : PayloadEvent()
        data class TransferUpdate(override val endPointID: String, val update: PayloadTransferUpdate) : PayloadEvent()
    }

    fun acceptConnection(endpointID: String) = callbackFlow<PayloadEvent> {
        val payloadCallback = object : PayloadCallback() {
            override fun onPayloadReceived(endpointId: String, payload: Payload) {
                offer(Received(endpointID, payload))
            }

            override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
                offer(TransferUpdate(endpointID, update))
            }
        }
        client.acceptConnection(endpointID, payloadCallback).await()
        awaitClose { client.disconnectFromEndpoint(endpointID) }
    }

    suspend fun rejectConnection(endpointID: String) = client.rejectConnection(endpointID).await()

    suspend fun Payload.send(endpointID: String) = client.sendPayload(endpointID, this).await()
    suspend fun Payload.send(vararg endpointID: String) = client.sendPayload(endpointID.asList(), this).await()

}