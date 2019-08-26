package `in`.wordmug.bitter.Fragments

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import `in`.wordmug.bitter.R
import `in`.wordmug.bitter.databinding.ViewVideoFragmentBinding
import android.net.Uri
import androidx.navigation.fragment.findNavController
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import timber.log.Timber

class ViewVideoFragment : Fragment() {

    companion object {
        fun newInstance() = ViewVideoFragment()
    }

    private lateinit var binding: ViewVideoFragmentBinding
    private lateinit var viewModel: ViewVideoViewModel
    private lateinit var videoUrl: String
    private lateinit var player: SimpleExoPlayer

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ViewVideoFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ViewVideoViewModel::class.java)
        val args = ViewVideoFragmentArgs.fromBundle(arguments!!)
        videoUrl = args.videoUrl
        Timber.i("trying to load view $videoUrl")

        binding.back.setOnClickListener { findNavController().navigateUp() }
    }

    override fun onStart() {
        super.onStart()
        player = ExoPlayerFactory.newSimpleInstance(context!!, DefaultTrackSelector())
        binding.playerView.player = player
        val dataSourceFactory = DefaultDataSourceFactory(context!!, Util.getUserAgent(context!!,"exo-player"))

        val videoSource = ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(videoUrl))
        player.prepare(videoSource)
        player.playWhenReady = true
        player.addListener(object : Player.EventListener {
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {

            }

            override fun onSeekProcessed() {

            }

            override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {

            }

            override fun onPlayerError(error: ExoPlaybackException?) {

            }

            override fun onLoadingChanged(isLoading: Boolean) {

            }

            override fun onPositionDiscontinuity(reason: Int) {

            }

            override fun onRepeatModeChanged(repeatMode: Int) {

            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {

            }

            override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {

            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if(playbackState == ExoPlayer.STATE_BUFFERING)
                {
                    Timber.i("loading show")
                    //binding.loading.visibility = View.VISIBLE
                }
                else
                {
                    Timber.i("loading hide")
                    //binding.loading.visibility = View.GONE
                }
            }
        })
    }

    override fun onStop() {
        super.onStop()
        binding.playerView.player =null
        player.release()
    }
}
