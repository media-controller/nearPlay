package media.controller.nearplay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import timber.log.Timber

private const val SPOTIFY_PACKAGE = "com.spotify.music"
private const val PLAYBACK_STATE_CHANGED = "$SPOTIFY_PACKAGE.playbackstatechanged"
private const val QUEUE_CHANGED = "$SPOTIFY_PACKAGE.queuechanged"
private const val METADATA_CHANGED = "$SPOTIFY_PACKAGE.metadatachanged"

@Suppress("SpellCheckingInspection")
class SpotifyBroadcastReceiver : BroadcastReceiver() {


    fun register(context: Context) = with(context) {
        registerReceiver(this@SpotifyBroadcastReceiver, IntentFilter(QUEUE_CHANGED))
        registerReceiver(this@SpotifyBroadcastReceiver, IntentFilter(METADATA_CHANGED))
        registerReceiver(this@SpotifyBroadcastReceiver, IntentFilter(PLAYBACK_STATE_CHANGED))
    }

    sealed class Event(open val timeSent: Long) {
        data class SpotifyMetadataChanged(
            override val timeSent: Long = Long.MIN_VALUE,
            val id: String? = null,
            val artist: String? = null,
            val album: String? = null,
            val track: String? = null,
            val length: Int = Int.MIN_VALUE
        ) : Event(timeSent)

        data class SpotifyPlaybackStateChanged(
            override val timeSent: Long = Long.MIN_VALUE,
            val playing: Boolean? = null,
            val playbackPosition: Int = Int.MIN_VALUE
        ) : Event(timeSent)

        data class SpotifyQueueChanged(
            override val timeSent: Long = Long.MIN_VALUE
        ) : Event(timeSent)

        data class Unknown(
            override val timeSent: Long = Long.MIN_VALUE
        ) : Event(timeSent)
    }

    val MetadataChangedEvents: LiveData<Event.SpotifyMetadataChanged>
        get() = metadataChangedEvents
    private val metadataChangedEvents = MutableLiveData<Event.SpotifyMetadataChanged>()

    val PlaybackStateChangedEvents: LiveData<Event.SpotifyPlaybackStateChanged>
        get() = playbackStateChangedEvents
    private val playbackStateChangedEvents = MutableLiveData<Event.SpotifyPlaybackStateChanged>()

    val QueueChangedEvents: LiveData<Event.SpotifyQueueChanged>
        get() = queueChangedEvents
    private val queueChangedEvents = MutableLiveData<Event.SpotifyQueueChanged>()


    override fun onReceive(context: Context, intent: Intent) {
        val timeSentInMs = intent.getLongExtra("timeSent", 0L)
        val event = when (intent.action) {
            QUEUE_CHANGED          -> Event.SpotifyQueueChanged(timeSent = timeSentInMs).also { queueChangedEvents.postValue(it) }
            METADATA_CHANGED       -> {
                Event.SpotifyMetadataChanged(
                    id = intent.getStringExtra("id"),
                    artist = intent.getStringExtra("artist"),
                    album = intent.getStringExtra("album"),
                    track = intent.getStringExtra("track"),
                    length = intent.getIntExtra("length", 0),
                    timeSent = timeSentInMs
                ).also { metadataChangedEvents.postValue(it) }
            }
            PLAYBACK_STATE_CHANGED -> {
                Event.SpotifyPlaybackStateChanged(
                    playing = intent.getBooleanExtra("playing", false),
                    playbackPosition = intent.getIntExtra("playbackPosition", 0),
                    timeSent = timeSentInMs
                ).also { playbackStateChangedEvents.postValue(it) }
            }
            else                   -> Event.Unknown(timeSent = timeSentInMs).also { Timber.w("Received an unknown event: %s", it) }
        }
        Timber.d(event.toString())
    }
}