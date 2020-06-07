package media.controller.nearplay.services

import android.os.Build
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import timber.log.Timber

@RequiresApi(Build.VERSION_CODES.N)
class QuickSettingsTile : TileService() {
    override fun onClick() {
        super.onClick()
        Timber.d("onClick")
        // Called when the user click the tile
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
        Timber.d("onTileRemoved")
        // Do something when the user removes the Tile
    }

    override fun onTileAdded() {
        super.onTileAdded()
        Timber.d("onTileAdded")
        // Do something when the user add the Tile
    }

    override fun onStartListening() {
        super.onStartListening()
        Timber.d("onStartListening")
        // Called when the Tile becomes visible
    }

    override fun onStopListening() {
        super.onStopListening()
        Timber.d("onStopListening")
        // Called when the tile is no longer visible
    }
}