package media.controller.nearplay.repository.nearby

import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.messages.MessagesOptions
import com.google.android.gms.nearby.messages.NearbyPermissions.*
import media.controller.nearplay.Configuration

object Messages {
    fun getAllMessageOptions() = MessagesOptions.Builder().setPermissions(BLE + BLUETOOTH + MICROPHONE).build()
    fun getBluetoothMessageOptions() = MessagesOptions.Builder().setPermissions(BLE + BLUETOOTH).build()
    fun getBleMessageOptions() = MessagesOptions.Builder().setPermissions(BLE).build()
    fun getMicMessageOptions() = MessagesOptions.Builder().setPermissions(BLE).build()

    fun getClient() = Nearby.getMessagesClient(Configuration.context, getAllMessageOptions())

}