package com.example.appmusic

import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.widget.Toast


class MusicService : Service() {
    private var mSong: Song? = null
    private var mMusic: MediaPlayer? = null
    private var mNotificationForeground: NotificationForeground? = null
    private val binder = LocalBinder()
    private var mIsPlaying = false
    private var mSongCurrentPosition = 0

    val songCurrentPosition: Int
        get() = (mSongCurrentPosition / 60000)*60 + (mSongCurrentPosition / 1000) % 60

    val isPlaying: Boolean
        get() = mIsPlaying


    inner class LocalBinder : Binder() {
        fun getService(): MusicService  = this@MusicService
    }

    companion object {
        const val FLAG_FIRST_START = "fst"
        const val FLAG_PAUSE = "pause"
        const val FLAG_PLAY_CONTINUE = "play_continue"
        const val FLAG_END ="end_song"
        const val FLAG_PLAY_WITH_SEEK_BAR = "play_with_seek_bar"
    }

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }


    override fun onCreate() {
        super.onCreate()
        mSong = Song()
        mMusic = MediaPlayer()
    }
    fun startMusicFirstTime(song: Song) {
        if(mMusic?.isPlaying!!) {
            mMusic?.stop()
        }
        mSong = song
        mNotificationForeground = NotificationForeground(this, mSong?.songName!!)
        startForeground(1, mNotificationForeground?.buildNotification())
        mMusic = MediaPlayer.create(this, Uri.parse(mSong?.songLocation))
        mMusic?.start()
        mIsPlaying = true
        Thread {
            val min = this.mMusic?.duration!! / 60000
            val sec = (this.mMusic?.duration!! / 1000) % 60
            val broadcastIntent = Intent()
            broadcastIntent.action = MainActivity.mBroadcastAction
            broadcastIntent.putExtra("MINUTES", min)
            broadcastIntent.putExtra("SECONDS", sec)
            broadcastIntent.putExtra("STATUS", FLAG_FIRST_START)
            sendBroadcast(broadcastIntent)
        }.start()
    }

    fun pauseMusic(){
        mMusic?.pause()
        mSongCurrentPosition = mMusic?.currentPosition!!
        mIsPlaying = false
        Thread {
            val broadcastIntent = Intent()
            broadcastIntent.action = MainActivity.mBroadcastAction
            broadcastIntent.putExtra("STATUS", FLAG_PAUSE)
            sendBroadcast(broadcastIntent)
        }.start()
    }

    fun playMusicContinue(){
        if (mSongCurrentPosition == mMusic?.duration) {
            mSongCurrentPosition = 0
        }
        mMusic?.seekTo(mSongCurrentPosition)
        mMusic?.start()
        mIsPlaying = true
        Thread {
            val broadcastIntent = Intent()
            broadcastIntent.action = MainActivity.mBroadcastAction
            broadcastIntent.putExtra("STATUS", FLAG_PLAY_CONTINUE)
            sendBroadcast(broadcastIntent)
        }.start()

    }

    fun playWithSeekBar(pos: Int){
        mSongCurrentPosition = pos * 1000
        mMusic?.seekTo(mSongCurrentPosition)
        if (mIsPlaying) {
            mMusic?.start()
        }
        Thread {
            val broadcastIntent = Intent()
            broadcastIntent.action = MainActivity.mBroadcastAction
            broadcastIntent.putExtra("STATUS", FLAG_PLAY_WITH_SEEK_BAR)
            sendBroadcast(broadcastIntent)
        }.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mMusic?.setOnCompletionListener(){
            Toast.makeText(this, "het roi", Toast.LENGTH_SHORT).show()
            mIsPlaying = false
            mSongCurrentPosition = mMusic?.currentPosition!!
            Thread {
                val broadcastIntent = Intent()
                broadcastIntent.action = MainActivity.mBroadcastAction
                broadcastIntent.putExtra("STATUS", FLAG_END)
                sendBroadcast(broadcastIntent)
            }.start()
        }
        return START_REDELIVER_INTENT
    }
}
