package media.controller.nearplay.repository.nearby

import com.google.android.gms.nearby.Nearby
import media.controller.nearplay.Configuration

object Connections {

    fun getClient() =
        Nearby.getConnectionsClient(Configuration.context)

//    private suspend fun startAdvertising(strategy: Strategy) {
//        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(strategy).build()
//        Nearby.getConnectionsClient(Configuration.context)
//            .startAdvertising(
//                getUserNickname(), SERVICE_ID, connectionLifecycleCallback, advertisingOptions
//            ).addOnSuccessListener(
//                { unused: Void? -> })
//            .addOnFailureListener(
//                { e: Exception? -> })
//    }
//
//    private fun startDiscovery(strategy: Strategy) {
//        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(strategy).build()
//        Nearby.getConnectionsClient(Configuration.context)
//            .startDiscovery(SERVICE_ID, endpointDiscoveryCallback, discoveryOptions)
//            .addOnSuccessListener(
//                { unused: Void? -> })
//            .addOnFailureListener(
//                { e: java.lang.Exception? -> })
//    }
}