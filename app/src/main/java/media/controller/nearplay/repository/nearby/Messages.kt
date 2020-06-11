package media.controller.nearplay.repository.nearby

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.messages.MessagesOptions
import com.google.android.gms.nearby.messages.NearbyPermissions.*
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Messages @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getAllMessageOptions() = MessagesOptions.Builder().setPermissions(BLE + BLUETOOTH + MICROPHONE).build()
    fun getBluetoothMessageOptions() = MessagesOptions.Builder().setPermissions(BLE + BLUETOOTH).build()
    fun getBleMessageOptions() = MessagesOptions.Builder().setPermissions(BLE).build()
    fun getMicMessageOptions() = MessagesOptions.Builder().setPermissions(BLE).build()

    fun getClient() = Nearby.getMessagesClient(context, getAllMessageOptions())

}