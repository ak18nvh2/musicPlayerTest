package com.example.appmusic

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
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
        var intent = intent
        var bundle = intent.extras
        if (bundle != null) {
            song = bundle.getSerializable("SONG") as Song
        }
        btn_Handle.setOnClickListener(this)
        playMusic(0,0)
        mIntentFilter = IntentFilter()
        mIntentFilter.addAction(mBroadcastAction)
        registerLiveDataListener()

        sb_SongHandler.setOnSeekBarChangeListener(@SuppressLint("AppCompatCustomView")
        object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {

                if (mCountHandleSeekBar == 0 ) {
                    mCountHandleSeekBar++
                } else {
                    stopServiceSong()
                    playMusic(mCountHandleClick,i)
                    Toast.makeText(applicationContext, "asdf", Toast.LENGTH_SHORT).show()
                    mSongViewModel.runASong(song.songLength, i)
                }

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
    }
    private fun stopServiceSong() {

    }
    private fun registerLiveDataListener() {
        val songObserver = Observer<Int> { songLength ->
            var sec = songLength % 60
            var min = songLength / 60
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
        mSongViewModel.songLength.observe(this, songObserver)

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
                val sec = intent.getIntExtra("SECONDS", 0)
                val min = intent.getIntExtra("MINUTES", 0)
                var secString = if (sec < 10) {
                    "0$sec"
                } else {
                    "$sec"
                }
                var minString = if (min < 10) {
                    "0$min:"
                } else {
                    "$min:"
                }
                song.songLength = min * 60 + sec
                tv_SongLength.text = minString + secString
                sb_SongHandler.max = min * 60 + sec
                sb_SongHandler.min = 0
                mSongViewModel.runASong(min * 60 + sec,0)

            }
        }
    }

//    private fun runMusic(songLength: Int){
////        this.song.songLength = songLength
////        var countTime = 0
////        object : CountDownTimer(songLength.toLong(),1){
////            override fun onFinish() {
////                tv_CurrentPosition.text = tv_SongLength.text
////            }
////
////            override fun onTick(p0: Long) {
////                countTime++
////                var sec = countTime % 60
////                var min = countTime / 60
////                var secStringCur = if (sec < 10) {
////                    "0$sec"
////                } else {
////                    "$sec"
////                }
////                var minStringCur = if (min < 10) {
////                    "0$min:"
////                } else {
////                    "$min:"
////                }
////                tv_CurrentPosition.text = p0.toString()
////            }
////
////        }.start()
////    }

    private fun playMusic(countHandle: Int, position: Int) {
        var serviceIntent = Intent(this, MyService::class.java)
        var bundleService = Bundle()
        bundleService.putInt("COUNT_HANDLE", countHandle)
        bundleService.putInt("POSITION_CURRENT",position)
        bundleService.putSerializable("SONG", song)
        serviceIntent.putExtras(bundleService)
        ContextCompat.startForegroundService(this, serviceIntent)
        // startService(serviceIntent)

    }

    override fun onClick(p0: View?) {
        when (p0) {
            btn_Handle -> {
                mCountHandleClick++
                if (mCountHandleClick % 2 == 1) {
                    playMusic(mCountHandleClick,-1)
                    btn_Handle.setImageResource(R.drawable.play)
                    Toast.makeText(this, "dang pause", Toast.LENGTH_SHORT).show()

                } else {
                    playMusic(mCountHandleClick,-1)
                    btn_Handle.setImageResource(R.drawable.pause)
                    Toast.makeText(this, "dang play", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}