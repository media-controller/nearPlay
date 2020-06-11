package media.controller.nearplay.repository.nearby

import android.content.Context
import com.google.android.gms.nearby.Nearby
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Connections @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun getClient() =
        Nearby.getConnectionsClient(context)

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