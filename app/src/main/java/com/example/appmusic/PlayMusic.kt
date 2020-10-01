package com.example.appmusic

import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_play_music.*

class PlayMusic : AppCompatActivity(), View.OnClickListener {
    private lateinit var song: Song
    private var countHandleClick = 0
    private var music: MediaPlayer? = null
    private var pausePosition = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_music)
        var intent = intent
        var bundle = intent.extras
        if (bundle != null) {
            song = bundle.getSerializable("SONG") as Song
        }
//        music = MediaPlayer.create(this, Uri.parse(song.songLocation))
//        this.music?.start()
        btn_Handle.setOnClickListener(this)
        playMusic(0)

    }

    override fun onStop() {
        super.onStop()
        Log.d("music", "on stop")
    }

    override fun onPause() {
        super.onPause()
        Log.d("music","on pause")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("music", "on destroy")
    }
    private fun playMusic(countHandle : Int) {
        var serviceIntent = Intent(this, MyService::class.java)
        var bundleService = Bundle()
        bundleService.putInt("COUNT_HANDLE", countHandle)
        bundleService.putSerializable("SONG", song)
        serviceIntent.putExtras(bundleService)
        ContextCompat.startForegroundService(this,serviceIntent)
       // startService(serviceIntent)

    }

    override fun onClick(p0: View?) {
        when (p0) {
            btn_Handle -> {
                countHandleClick++
                if (countHandleClick % 2 == 1) {
                    playMusic(countHandleClick)
                    btn_Handle.setImageResource(R.drawable.play)
                    Toast.makeText(this, "dang pause", Toast.LENGTH_SHORT).show()

                } else {
                    playMusic(countHandleClick)
                    btn_Handle.setImageResource(R.drawable.pause)
                    Toast.makeText(this, "dang play", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}