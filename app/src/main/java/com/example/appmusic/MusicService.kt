package com.example.appmusic

import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import java.util.jar.Manifest


class MusicService : Service() {
    private var mSong: Song? = null
    private var mMusic: MediaPlayer? = null
    private var mNotificationForeground: NotificationForeground? = null
    private val binder = LocalBinder()
    private var mIsPlaying = false
    private var mSongCurrentPosition = 0
    private var mPositionOfList = 0
    private var mRepeatState = FLAG_NO_REPEAT
    private var mArrayListSong: ArrayList<Song>? = ArrayList()
    private var mIsEndMusic = false
    var arrayListSong: ArrayList<Song>
        get() = mArrayListSong!!
        set(value) {
            mArrayListSong = value
        }

    val music: MediaPlayer
        get() = mMusic!!

    var positionOfList: Int
        set(value) {
            mPositionOfList = value
        }
        get() = mPositionOfList

    var repeatState: String
        get() = mRepeatState
        set(value) {
            mRepeatState = value
        }

    var song: Song
        get() = mSong!!
        set(value) {
            mSong = value
        }

    val songCurrentPosition: Int
        get() = (mSongCurrentPosition / 60000) * 60 + (mSongCurrentPosition / 1000) % 60

    val isPlaying: Boolean
        get() = mIsPlaying


    inner class LocalBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    companion object {
        const val FLAG_FIRST_START = "fst"
        const val FLAG_PAUSE = "pause"
        const val FLAG_PLAY_CONTINUE = "play_continue"
        const val FLAG_END = "end_song"
        const val FLAG_PLAY_WITH_SEEK_BAR = "play_with_seek_bar"
        const val FLAG_PLAY_WHEN_START_APP_AGAIN = "play_when_start_app_again"
        const val FLAG_NO_REPEAT = "no_repeat"
        const val FLAG_REPEAT_ALL = "repeat_all"
        const val FLAG_REPEAT_ONE = "repeat_one"
    }

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }


    override fun onCreate() {
        super.onCreate()
        mSong = Song()
        mMusic = MediaPlayer()

    }

    fun startMusicFirstTime(song: Song, positionOfList: Int) {
        mIsEndMusic = false
        if (mMusic?.isPlaying!!) {
            mMusic?.stop()
        }
        mSong = song
        mPositionOfList = positionOfList
        mNotificationForeground = NotificationForeground(this, mSong?.songName!!)
        startForeground(1, mNotificationForeground?.buildNotification())
        mMusic = MediaPlayer.create(this, Uri.parse(mSong?.songLocation))
        mMusic?.setOnCompletionListener() {
            mIsEndMusic = true
            when (mRepeatState) {
                FLAG_NO_REPEAT -> {
                    Thread {
                        val broadcastIntent = Intent()
                        broadcastIntent.action = MainActivity.mBroadcastAction
                        broadcastIntent.putExtra("STATUS", FLAG_NO_REPEAT)
                        sendBroadcast(broadcastIntent)
                    }.start()
                }
                FLAG_REPEAT_ONE -> {
                    playMusicContinue()
                }
                FLAG_REPEAT_ALL -> {
                    if (mPositionOfList + 1 == mArrayListSong?.size) {
                        mPositionOfList = 0
                    } else {
                        mPositionOfList++
                    }
                    mSong = mArrayListSong!![mPositionOfList]
                    startMusicFirstTime(mSong!!,mPositionOfList)
                }
            }
        }
        mMusic?.start()
        mIsPlaying = true
        Thread {
            val broadcastIntent = Intent()
            broadcastIntent.action = MainActivity.mBroadcastAction
            broadcastIntent.putExtra("STATUS", FLAG_FIRST_START)
            sendBroadcast(broadcastIntent)
        }.start()
    }
    fun pauseMusic() {
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

    fun playMusicContinue() {
        mIsPlaying = true
        if (mIsEndMusic) {
            mIsEndMusic = false
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

    fun playWithSeekBar(pos: Int) {
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
        if (mIsPlaying) {
            mSongCurrentPosition = mMusic?.currentPosition!!
            Thread {
                val broadcastIntent = Intent()
                broadcastIntent.action = MainActivity.mBroadcastAction
                broadcastIntent.putExtra("STATUS", FLAG_PLAY_WHEN_START_APP_AGAIN)
                sendBroadcast(broadcastIntent)
            }.start()
        }
        return START_REDELIVER_INTENT
    }
}
