package com.example.spotifyclone.ui

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bumptech.glide.RequestManager
import com.example.spotifyclone.R
import com.example.spotifyclone.adapters.SwipeSongAdapter
import com.example.spotifyclone.data.entities.Song
import com.example.spotifyclone.databinding.ActivityMainBinding
import com.example.spotifyclone.other.Status
import com.example.spotifyclone.ui.viewmodels.MainViewModel
import com.google.android.material.snackbar.Snackbar
import com.plcoding.spotifycloneyt.exoplayer.isPlaying
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    lateinit var binding:ActivityMainBinding

    private var playBackState:PlaybackStateCompat? = null

    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter

    @Inject
    lateinit var glide: RequestManager

    private var curPlayingSong: Song? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeToObservers()
        binding= ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.vpSong.adapter = swipeSongAdapter

        binding.vpSong.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if(playBackState?.isPlaying==true)
                {
                    mainViewModel.playOrToggleSong(swipeSongAdapter.songs[position])
                }
                else{
                    curPlayingSong= swipeSongAdapter.songs[position]
                }
            }
        })

        binding.ivPlayPause.setOnClickListener {
            curPlayingSong?.let{
                mainViewModel.playOrToggleSong(it,true)
            }
        }
    }

    private fun switchViewPagerToCurrentSong(song: Song) {
        val newItemIndex = swipeSongAdapter.songs.withIndex().find { it.value == song }?.index ?: -1

        if(newItemIndex != -1) {
            binding.vpSong.currentItem = newItemIndex
            curPlayingSong = song
        }
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(this) {
            it?.let { result ->
                when(result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { songs ->
                            swipeSongAdapter.songs = songs
                            if(songs.isNotEmpty()) {
                                glide.load((curPlayingSong ?: songs[0].imageUrl)).into(binding.ivCurSongImage)
                            }
                            switchViewPagerToCurrentSong(curPlayingSong ?: return@observe)
                        }
                    }
                    Status.ERROR -> Unit
                    Status.LOADING -> Unit
                }
            }
        }
        mainViewModel.curPlayingSong.observe(this) {
            if(it == null) return@observe

            curPlayingSong = it.toSong()
            glide.load(curPlayingSong?.imageUrl).into(binding.ivCurSongImage)
            switchViewPagerToCurrentSong(curPlayingSong ?: return@observe)
        }


        mainViewModel.playbackState.observe(this, Observer {
            playBackState=it

            binding.ivPlayPause.setImageResource(if(playBackState?.isPlaying==true) com.google.android.gms.base.R.drawable.common_full_open_on_phone else R.drawable.baseline_play_circle_24)
        })

        mainViewModel.isConnected.observe(this, Observer {
            it?.getContentIfNotHandled()?.let{
                result-> when(result.status){
                    Status.ERROR -> Snackbar.make(binding.rootLayout,result.message?: "Unknown Error occured",Snackbar.LENGTH_LONG).show()
                    else-> Unit
                }
            }
        })

        mainViewModel.networkError.observe(this, Observer {
            it?.getContentIfNotHandled()?.let{
                    result-> when(result.status){
                Status.ERROR -> Snackbar.make(binding.rootLayout,result.message?: "Unknown Error occured",Snackbar.LENGTH_LONG).show()
                else-> Unit
            }
            }
        })
    }



    private fun MediaMetadataCompat.toSong(): Song? {
        return description?.let {
            Song(
                it.mediaId ?: "",
                it.title.toString(),
                it.subtitle.toString(),
                it.mediaUri.toString(),
                it.iconUri.toString()
            )
        }
    }



}

