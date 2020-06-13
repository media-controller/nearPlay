package media.controller.nearplay.ui

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import media.controller.nearplay.BuildConfig
import media.controller.nearplay.R
import media.controller.nearplay.databinding.FragmentPartyBinding
import media.controller.nearplay.repository.spotify.AppRemote
import media.controller.nearplay.viewModels.MainViewModel
import media.controller.nearplay.viewModels.SpotifyAppRemoteViewModel
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class Party : Fragment(R.layout.fragment_party) {

    private val mainViewModel: MainViewModel by viewModels()
    private val remoteVM: SpotifyAppRemoteViewModel by viewModels()
    @Inject lateinit var remote: AppRemote

    private var currentNightModeStatus: Boolean? = null
    private var observeProgressUpdates: Boolean = true


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(FragmentPartyBinding.bind(view)) {

        remoteVM.trackArt.observe(viewLifecycleOwner, Observer { albumArt ->
            nowPlayingAlbumArt.setImageBitmap(albumArt)
        })

        remoteVM.userCanPlayOnDemand.observe(viewLifecycleOwner, Observer { })
        remoteVM.userStatus.observe(viewLifecycleOwner, Observer { userStatus ->
            userStatus?.code
            userStatus?.isLoggedIn
            userStatus?.longMessage
            userStatus?.shortMessage
        })

        remoteVM.playerContext.observe(viewLifecycleOwner, Observer { playerContext ->
            subtitle.text = playerContext?.subtitle
            title.text = playerContext?.title
        })

        remoteVM.trackPalette.observe(viewLifecycleOwner, Observer { trackPalette ->
            val vibrantColor = trackPalette?.getVibrantColor(Color.WHITE)
            val lightVibrantColor = trackPalette?.getLightVibrantColor(Color.WHITE)
            val darkVibrantColor = trackPalette?.getDarkVibrantColor(Color.WHITE)
            val mutedColor = trackPalette?.getMutedColor(Color.WHITE)
            val lightMutedColor = trackPalette?.getLightMutedColor(Color.WHITE)
            val darkMutedColor = trackPalette?.getDarkMutedColor(Color.WHITE)

            val tintMode = PorterDuff.Mode.MULTIPLY

            vibrantColor?.let {
                playerLayout.background?.setTint(it)
                playerLayout.background?.setTintMode(tintMode)
            }

            if (BuildConfig.DEBUG) {
                paletteDebugView.visibility = View.VISIBLE

                vibrantColor?.let {
                    vibrant.background?.setTint(vibrantColor)
                    vibrant.background?.setTintMode(tintMode)
                }
                lightVibrantColor?.let {
                    lightVibrant.background?.setTint(lightVibrantColor)
                    lightVibrant.background?.setTintMode(tintMode)
                }
                darkVibrantColor?.let {
                    darkVibrant.background?.setTint(darkVibrantColor)
                    darkVibrant.background?.setTintMode(tintMode)
                }
                mutedColor?.let {
                    muted.background?.setTint(mutedColor)
                    muted.background?.setTintMode(tintMode)
                }
                lightMutedColor?.let {
                    lightMuted.background?.setTint(lightMutedColor)
                    lightMuted.background?.setTintMode(tintMode)
                }
                darkMutedColor?.let {
                    darkMuted.background?.setTint(darkMutedColor)
                    darkMuted.background?.setTintMode(tintMode)
                }
            }
        })

        remoteVM.nowPlayingLibraryState.observe(viewLifecycleOwner, Observer { nowPlayingLibraryState ->
            nowPlayingLibraryState?.run {
                favoriteCheckbox.isChecked = isAdded
                favoriteCheckbox.setOnClickListener {
                    remoteVM.toggleTrackFavorite()
                }
            }
        })

        fun convertSecondsToHMmSs(milliseconds: Long): String? {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
            val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60
            return String.format("%d:%02d", minutes, seconds)
        }

        remoteVM.interpolatedPlaybackPosition.observe(viewLifecycleOwner, Observer { position ->
            playbackTime.text = position?.let { convertSecondsToHMmSs(it) }

            if (observeProgressUpdates) {
                seekBar.progress = position?.toInt() ?: -1
            }
        })

//        track.isSelected = true // marquee overflow scrolling doesn't work without this...

        remoteVM.playerState.observe(viewLifecycleOwner, Observer { playerState ->
            playerLayout.visibility = View.INVISIBLE

            playerState?.let { state ->

                playerLayout.visibility = View.VISIBLE

                artist.text = state.track?.artists?.joinToString { it.name }
                track.text = state.track?.name

                state.track?.duration?.run {
                    playbackLength.text = convertSecondsToHMmSs(this)
                    seekBar.max = toInt()
                }
                seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    var lastUserProgress: Int? = null

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        observeProgressUpdates = false
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        lastUserProgress?.let {
                            lifecycleScope.launch {
                                try {
                                    remote.seekTo(it.toLong())
                                } finally {
                                    observeProgressUpdates = true
                                    lastUserProgress = null
                                }
                            }
                        }
                    }

                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (!fromUser) return else lastUserProgress = progress
                    }
                })

                shuffleButton.setOnClickListener { lifecycleScope.launch { remote.toggleShuffle() } }
                shuffleButton.setBackgroundResource(
                    when (state.playbackOptions?.isShuffling) {
                        null,
                        false -> R.drawable.ic_baseline_shuffle_inactive_24
                        true  -> R.drawable.ic_baseline_shuffle_24
                    }
                )

                previousButton.setOnClickListener { lifecycleScope.launch { remote.skipPrevious() } }
                previousButton.setBackgroundResource(R.drawable.ic_baseline_skip_previous_24)
                if (state.playbackRestrictions.canSkipPrev) {
                    previousButton.isEnabled = true
                    previousButton.isClickable = true
                } else {
                    previousButton.isEnabled = false
                    previousButton.isClickable = false
                }

                if (state.isPaused) {
                    playButton.setBackgroundResource(R.drawable.ic_baseline_play_circle_filled_24)
                    playButton.setOnClickListener { lifecycleScope.launch { remote.resume() } }
                } else {
                    playButton.setBackgroundResource(R.drawable.ic_baseline_pause_circle_filled_24)
                    playButton.setOnClickListener { lifecycleScope.launch { remote.pause() } }
                }

                nextButton.setOnClickListener { lifecycleScope.launch { remote.skipNext() } }
                nextButton.setBackgroundResource(R.drawable.ic_baseline_skip_next_24)
                if (state.playbackRestrictions.canSkipNext) {
                    nextButton.isEnabled = true
                    nextButton.isClickable = true
                } else {
                    nextButton.isEnabled = false
                    nextButton.isClickable = false
                }

                AppRemote.RepeatMode.values().getOrNull(state.playbackOptions.repeatMode)?.let {
                    when (it) {
                        AppRemote.RepeatMode.OFF -> {
                            repeatModeButton.setOnClickListener { lifecycleScope.launch { remote.setRepeat(AppRemote.RepeatMode.ALL) } }
                            repeatModeButton.setBackgroundResource(R.drawable.ic_baseline_repeat_inactive_24)
                        }
                        AppRemote.RepeatMode.ALL -> {
                            repeatModeButton.setOnClickListener { lifecycleScope.launch { remote.setRepeat(AppRemote.RepeatMode.ONE) } }
                            repeatModeButton.setBackgroundResource(R.drawable.ic_baseline_repeat_24)
                        }
                        AppRemote.RepeatMode.ONE -> {
                            repeatModeButton.setOnClickListener { lifecycleScope.launch { remote.setRepeat(AppRemote.RepeatMode.OFF) } }
                            repeatModeButton.setBackgroundResource(R.drawable.ic_baseline_repeat_one_24)
                        }
                    }
                }
            }
        })

        remoteVM.playerContext.observe(viewLifecycleOwner, Observer { })
        remoteVM.interpolatedPlaybackPosition.observe(viewLifecycleOwner, Observer { })

        remoteVM.recommendedFlow.observe(viewLifecycleOwner, Observer {
            it
        })

        mainViewModel.isNightMode.observe(viewLifecycleOwner, Observer { isNightMode ->
            if (currentNightModeStatus != isNightMode) {
                currentNightModeStatus = isNightMode
                val nightMode = if (isNightMode) MODE_NIGHT_YES else MODE_NIGHT_NO
                AppCompatDelegate.setDefaultNightMode(nightMode)
            }
        })
    }
}
