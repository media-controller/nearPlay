package media.controller.nearplay.repository.nearby

import com.google.android.gms.nearby.messages.MessagesClient
import com.google.android.gms.nearby.messages.MessagesOptions
import com.google.android.gms.nearby.messages.NearbyPermissions.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Messages @Inject constructor(
    private val client: MessagesClient,
    private val config: Configuration
) {
    fun getAllMessageOptions() = MessagesOptions.Builder().setPermissions(BLE + BLUETOOTH + MICROPHONE).build()
    fun getBluetoothMessageOptions() = MessagesOptions.Builder().setPermissions(BLE + BLUETOOTH).build()
    fun getBleMessageOptions() = MessagesOptions.Builder().setPermissions(BLE).build()
    fun getMicMessageOptions() = MessagesOptions.Builder().setPermissions(BLE).build()
}