package com.example.appmusic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_play_music.*


class PlayMusicActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var song: Song
    private var mCountHandleClick = 0
    private lateinit var mIntentFilter: IntentFilter
    private var mSongViewModel = SongViewModel()
    private var mCountHandleSeekBar = 0

    companion object {
        const val mBroadcastAction = "SEND_SONG_SIZE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_music)

        init()
        registerLiveDataListener()
        playNewMusic()


//        sb_SongHandler.setOnSeekBarChangeListener(@SuppressLint("AppCompatCustomView")
//        object : SeekBar.OnSeekBarChangeListener {
//
//            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
//
//                if (mCountHandleSeekBar == 0) {
//                    mCountHandleSeekBar++
//                } else {
//                    playNewMusic(i)
//                    Toast.makeText(applicationContext, "asdf", Toast.LENGTH_SHORT).show()
//                    mSongViewModel.runASong(song.songLength, i,true)
//                }
//            }
//
//            override fun onStartTrackingTouch(seekBar: SeekBar) {
//            }
//
//            override fun onStopTrackingTouch(seekBar: SeekBar) {
//            }
//        })
    }

    private fun init() {
        var intent = intent
        var bundle = intent.extras
        if (bundle != null) {
            song = bundle.getSerializable("SONG") as Song
        }
        mIntentFilter = IntentFilter()
        mIntentFilter.addAction(mBroadcastAction)
        btn_Handle.setOnClickListener(this)
    }

    private fun registerLiveDataListener() {
        val songObserver = Observer<Int> { currentLength ->
            var sec = currentLength % 60
            var min = currentLength / 60
            var secStringCur = if (sec < 10) {
                "0$sec"
            } else {
                "$sec"
            }
            var minStringCur = if (min < 10) {
                "0$min:"
            } else {
                "$min:"
            }
            tv_CurrentPosition.text = minStringCur + secStringCur
        }
        mSongViewModel.currentLength.observe(this, songObserver)

    }

    override fun onResume() {
        super.onResume()
        registerReceiver(mReceiver, mIntentFilter)
    }

    override fun onPause() {
        unregisterReceiver(mReceiver)
        super.onPause()
    }

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action == mBroadcastAction) {
                when (intent.getIntExtra("STATUS", 0)) {
                    0 -> {
                        val sec = intent.getIntExtra("SECONDS", 0)
                        val min = intent.getIntExtra("MINUTES", 0)
                        val secString = if (sec < 10) {
                            "0$sec"
                        } else {
                            "$sec"
                        }
                        val minString = if (min < 10) {
                            "0$min:"
                        } else {
                            "$min:"
                        }
                        song.songLength = min * 60 + sec
                        tv_SongLength.text = minString + secString
                        sb_SongHandler.max = song.songLength
                        sb_SongHandler.min = 0
                        mSongViewModel.runASong(song.songLength,0,true)
                    }
                    1 -> {
                        val sec = intent.getIntExtra("SECONDS", 0)
                        val min = intent.getIntExtra("MINUTES", 0)
                        val secString = if (sec < 10) {
                            "0$sec"
                        } else {
                            "$sec"
                        }
                        val minString = if (min < 10) {
                            "0$min:"
                        } else {
                            "$min:"
                        }
                        song.pausePosition = min * 60 + sec
                        mSongViewModel.runASong(song.songLength, song.pausePosition, false)
                    }
                    else -> {
                        mSongViewModel.runASong(song.songLength, song.pausePosition, true)
                    }
                }
            }
        }
    }

    private fun playNewMusic() {
        val serviceIntent = Intent(this, MusicService::class.java)
        val bundleService = Bundle()
        bundleService.putSerializable("SONG", song)
        serviceIntent.putExtras(bundleService)
        ContextCompat.startForegroundService(this, serviceIntent)
    }
    private fun pauseMusic(){
        Log.d("musiccccccc","vao ham pause roi")
        val serviceIntent = Intent(this, MusicService::class.java)
        serviceIntent.putExtra("STT", 1)// 1 la pause
        ContextCompat.startForegroundService(this, serviceIntent)
    }
    private fun runAgain(){
        val serviceIntent = Intent(this, MusicService::class.java)
        serviceIntent.putExtra("STT", 2)// 2 la play again
        ContextCompat.startForegroundService(this, serviceIntent)
    }
    override fun onClick(p0: View?) {
        when (p0) {
            btn_Handle -> {
                mCountHandleClick++
                if (mCountHandleClick % 2 == 1) {
                    Log.d("musiccccccc","vao ham click roi")
                    pauseMusic()
                } else {
                    runAgain()
                }
            }
        }
    }

//    override fun onClick(p0: View?) {
//        when (p0) {
//            btn_Handle -> {
//                mCountHandleClick++
//                if (mCountHandleClick % 2 == 1) {
//                    playNewMusic( -1)
//                    btn_Handle.setImageResource(R.drawable.play)
//                    Toast.makeText(this, "dang pause", Toast.LENGTH_SHORT).show()
//                } else {
//                    playNewMusic( -2)
//                    btn_Handle.setImageResource(R.drawable.pause)
//                    mSongViewModel.runASong(song.songLength, song.pausePosition,true)
//                    Toast.makeText(this, "dang play", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }

}