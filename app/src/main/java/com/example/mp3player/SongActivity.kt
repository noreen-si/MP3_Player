package com.example.mp3player

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.os.SystemClock.sleep
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.mp3player.databinding.ActivitySongBinding
import java.io.File

class SongActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySongBinding
    private lateinit var song : String
    private lateinit var allSongs : ArrayList<File>
    private var position = 0
    private var mediaPlayer : MediaPlayer? = null
    private lateinit var songTitle : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_song)

        val bundle = intent.extras
        if (bundle != null) {
            song = bundle.get("song name") as String

            @Suppress("UNCHECKED_CAST")
            allSongs = bundle.getParcelableArrayList<Parcelable>("all songs") as ArrayList<File> /* = java.util.ArrayList<java.io.File> */
            position = bundle.get("position") as Int

            binding.songName.isSelected = true

            val uri = Uri.parse(allSongs[position].toString())

            songTitle = allSongs[position].name.replace(".mp3", "")
            binding.songName.text = songTitle

            mediaPlayer = MediaPlayer.create(applicationContext, uri)
            initializeSeekBar()
            mediaPlayer?.start()

            // Listeners for seek bar
            binding.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        mediaPlayer?.seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                }
            })

            // Set on click listener for when the play button is pressed
            binding.playButton.setOnClickListener {
                playSong()
            }

            // Skip to the next song
            binding.nextButton.setOnClickListener {
                playNextSong()
            }
            // Go to the previous song
            binding.prevButton.setOnClickListener {
                playPrevSong()
            }
            // Listener for when the song finishes playing (play next song)
            mediaPlayer?.setOnCompletionListener { playNextSong() }
        }
    }
    private fun initializeSeekBar() {
        binding.seekBar.max = mediaPlayer!!.duration
        binding.tvEnd.text = milliTextConverter(mediaPlayer!!.duration)

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            override fun run() {
                try {
                    binding.seekBar.progress = mediaPlayer!!.currentPosition

                    var currentDuration: Int? = mediaPlayer?.currentPosition
                    // Displaying time completed playing
                    binding.tvStart.text = milliTextConverter(currentDuration)

                    handler.postDelayed(this, 1000)
                } catch (e : Exception) {
                    binding.seekBar.progress = 0
                }
            }
        }, 0)
    }

    private fun playSong() {
        if (mediaPlayer?.isPlaying == true) {
            binding.playButton.setImageResource(R.drawable.ic_baseline_play_arrow_24)
            mediaPlayer?.pause()
        } else {
            binding.playButton.setImageResource(R.drawable.ic_baseline_pause_24)
            mediaPlayer?.start()
        }
        // Listener for when the song finishes playing (play next song)
        mediaPlayer?.setOnCompletionListener { playNextSong() }
    }

    private fun playNextSong() {
        mediaPlayer?.stop()
        mediaPlayer?.reset()
        mediaPlayer?.release()

        binding.playButton.setImageResource(R.drawable.ic_baseline_pause_24)
        position = (position + 1) % allSongs.size
        val newUri : Uri = Uri.parse(allSongs[position].toString())

        songTitle = allSongs[position].name.replace(".mp3", "")
        binding.songName.text = songTitle

        mediaPlayer = MediaPlayer.create(applicationContext, newUri)
        initializeSeekBar()
        mediaPlayer?.start()
        // Listener for when the song finishes playing (play next song)
        mediaPlayer?.setOnCompletionListener { playNextSong() }
    }

    private fun playPrevSong() {
        mediaPlayer?.stop()
        mediaPlayer?.reset()
        mediaPlayer?.release()

        binding.playButton.setImageResource(R.drawable.ic_baseline_pause_24)
        if (position - 1 < 0) {
            position = allSongs.size - 1
        }
        else {
            position = position - 1
        }
        val newUri : Uri = Uri.parse(allSongs[position].toString())

        songTitle = allSongs[position].name.replace(".mp3", "")
        binding.songName.text = songTitle

        mediaPlayer = MediaPlayer.create(applicationContext, newUri)
        initializeSeekBar()
        mediaPlayer?.start()
        // Listener for when the song finishes playing (play next song)
        mediaPlayer?.setOnCompletionListener { playNextSong() }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }

    private fun milliTextConverter(time : Int?) : String {
        val minutes : Int = (time!!/60000) % 60
        val seconds : Int = (time /1000) % 60
        return if (seconds < 10) "$minutes:0$seconds" else "$minutes:$seconds"
    }

}