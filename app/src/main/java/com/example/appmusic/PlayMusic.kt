package com.example.appmusic

import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
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
        if (bundle!= null) {
             song = bundle.getSerializable("SONG") as Song
        }
        music = MediaPlayer.create(this, Uri.parse(song.songLocation))
        this.music?.start()
        btn_Handle.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0) {
            btn_Handle -> {
                countHandleClick++
                if(countHandleClick % 2 == 1) {
                    music?.pause()
                    pausePosition = music?.currentPosition!!
                    btn_Handle.setImageResource(R.drawable.play)
                    Toast.makeText(this, "dang pause", Toast.LENGTH_SHORT).show()

                } else {
                    music?.seekTo(pausePosition)
                    music?.start()

                    btn_Handle.setImageResource(R.drawable.pause)
                    Toast.makeText(this, "dang play", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        music?.stop()

    }
}