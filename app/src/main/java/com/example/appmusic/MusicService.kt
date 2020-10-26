package com.example.appmusic

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import com.example.appmusic.models.Song
import com.example.appmusic.views.MainActivity
import kotlin.random.Random


class MusicService : Service() {
    companion object {
        const val FLAG_FIRST_START = "fst"
        const val FLAG_PAUSE = "pause"
        const val FLAG_PLAY_CONTINUE = "play_continue"
        const val FLAG_PLAY_WITH_SEEK_BAR = "play_with_seek_bar"
        const val FLAG_PLAY_WHEN_START_APP_AGAIN = "play_when_start_app_again"
        const val FLAG_NO_REPEAT = "no_repeat"
        const val FLAG_REPEAT_ALL = "repeat_all"
        const val FLAG_REPEAT_ONE = "repeat_one"
        const val FLAG_RANDOM_MUSIC = "random_music"
        const val FLAG_NO_RANDOM_MUSIC = "no_random_music"
        const val FLAG_CHANGE_SONG = "flag_change_song"
        const val FLAG_LOCAL_MUSIC = "local_music"
        const val FLAG_ONLINE_MUSIC = "online_music"
        const val FLAG_CAN_NOT_PLAY_MUSIC = "can't play music"
        const val FLAG_STATE_ALIVE = "alive"
        const val FLAG_STATE_DIE = "die"
        const val FLAG_ACTION_NEXT_SONG = "next_song"
        const val FLAG_ACTION_PRE_SONG = "pre_song"
        const val FLAG_ACTION_PAUSE = "notification_pause"
        const val FLAG_PAUSE_IN_APP = "pause_in_app"
        const val FLAG_PAUSE_IN_NOTIFICATION = "pause_in_notification"
    }


    private var mTypePause = ""
    var typePause: String
        get() = mTypePause
        set(value) {
            mTypePause = value
        }
    private var mCountHandlerClick = 0
    private var mState = FLAG_STATE_ALIVE
    var stateMusic: String
        get() = mState
        set(value) {
            mState = value
        }
    private var mTypeMusic = FLAG_LOCAL_MUSIC
    var typeMusic: String
        get() = mTypeMusic
        set(value) {
            mTypeMusic = value
        }
    private var mSong: Song? = null
    var song: Song
        get() = mSong!!
        set(value) {
            mSong = value
        }

    private var mMusic: MediaPlayer? = null
    val music: MediaPlayer
        get() = mMusic!!

    private var mNotificationForeground: NotificationForeground? = null
    private val binder = LocalBinder()

    private var mIsPlaying = false
    val isPlaying: Boolean
        get() = mIsPlaying

    private var mSongCurrentPosition = 0
    val songCurrentPosition: Int
        get() = (mSongCurrentPosition / 60000) * 60 + (mSongCurrentPosition / 1000) % 60

    private var mPositionOfList = 0
    var positionOfList: Int
        set(value) {
            mPositionOfList = value
        }
        get() = mPositionOfList

    private var mRepeatState = FLAG_NO_REPEAT
    var repeatState: String
        get() = mRepeatState
        set(value) {
            mRepeatState = value
        }

    private var mRandomState = FLAG_NO_RANDOM_MUSIC
    var randomState: String
        get() = mRandomState
        set(value) {
            mRandomState = value
        }

    private var mArrayListSongOffline: ArrayList<Song>? = ArrayList()
    var arrayListSong: ArrayList<Song>
        get() = mArrayListSongOffline!!
        set(value) {
            mArrayListSongOffline = value
        }

    private var mIsEndMusic = false


    inner class LocalBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }


    override fun onCreate() {
        super.onCreate()
        mSong = Song()
        mMusic = MediaPlayer()
        val intentFilter = IntentFilter()
        intentFilter.addAction(FLAG_ACTION_NEXT_SONG)
        intentFilter.addAction(FLAG_ACTION_PAUSE)
        intentFilter.addAction(FLAG_ACTION_PRE_SONG)
        registerReceiver(receiver, intentFilter)

    }

    private fun startNotification() {
        mNotificationForeground =
            NotificationForeground(this, mSong?.songName!!, MainActivity.mMusicService)
        startForeground(1, mNotificationForeground?.buildNotification())
    }

    private val receiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                FLAG_ACTION_PRE_SONG -> {
                    preSong()
                }
                FLAG_ACTION_NEXT_SONG -> {
                    nextSong()
                }
                FLAG_ACTION_PAUSE -> {
                    mTypePause = FLAG_PAUSE_IN_NOTIFICATION
                    pauseMusic()
                    mCountHandlerClick++
                    if (mCountHandlerClick % 2 == 1) {
                        pauseMusic()
                    } else {
                        playMusicContinue()
                    }
                }
            }
        }
    }

    private fun sendInformationChangeSong() {
        Thread {
            val broadcastIntent = Intent()
            broadcastIntent.action = MainActivity.mBroadcastAction
            broadcastIntent.putExtra("STATUS", FLAG_CHANGE_SONG)
            sendBroadcast(broadcastIntent)
        }.start()
    }

    fun nextSong() {
        if (this.positionOfList == this.arrayListSong.size - 1) {
            this.song = this.arrayListSong[0]
            this.positionOfList = 0
        } else {
            this.song = this.arrayListSong[++this.positionOfList]
        }
        this.startMusicFirstTime(this.song, this.positionOfList)
    }

    fun preSong() {
        if (this.positionOfList == 0) {
            this.song =
                this.arrayListSong[this.arrayListSong.size - 1]
            this.positionOfList = this.arrayListSong.size - 1
        } else {
            this.song = this.arrayListSong[--this.positionOfList]

        }
        this.startMusicFirstTime(this.song, this.positionOfList)
    }

    fun startMusicFirstTime(song: Song, positionOfList: Int) {
        mIsEndMusic = false
        if (mIsPlaying) {
            mMusic?.stop()
        }
        mSong = song
        mPositionOfList = positionOfList

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
                    if (mRandomState == FLAG_NO_RANDOM_MUSIC) {
                        if (mPositionOfList + 1 == mArrayListSongOffline?.size) {
                            mPositionOfList = 0
                        } else {
                            mPositionOfList++
                        }
                    } else {
                        mPositionOfList = Random.nextInt(0, mArrayListSongOffline?.size!! - 1)
                    }
                    mSong = mArrayListSongOffline!![mPositionOfList]
                    startMusicFirstTime(mSong!!, mPositionOfList)
                }
            }
        }
        if (mMusic != null) {
            mMusic?.seekTo(0)
            mMusic?.start()
            mIsPlaying = true
            sendInformationChangeSong()
            startNotification()
            mState = FLAG_STATE_ALIVE
            Thread {
                val broadcastIntent = Intent()
                broadcastIntent.action = MainActivity.mBroadcastAction
                broadcastIntent.putExtra("STATUS", FLAG_FIRST_START)
                sendBroadcast(broadcastIntent)
            }.start()
        } else if (mTypeMusic == FLAG_ONLINE_MUSIC) {
            mState = FLAG_STATE_DIE
            Thread {
                val broadcastIntent = Intent()
                broadcastIntent.action = MainActivity.mBroadcastAction
                broadcastIntent.putExtra("STATUS", FLAG_CAN_NOT_PLAY_MUSIC)
                sendBroadcast(broadcastIntent)
            }.start()
            mIsPlaying = false
        }
    }

    fun stopService() {
        stopForeground(true)
    }

    fun pauseMusic() {
        if (mState == FLAG_STATE_ALIVE) {
            mMusic?.pause()
            mSongCurrentPosition = mMusic?.currentPosition!!
            mIsPlaying = false
        }
        Thread {
            val broadcastIntent = Intent()
            broadcastIntent.action = MainActivity.mBroadcastAction
            broadcastIntent.putExtra("STATUS", FLAG_PAUSE)
            sendBroadcast(broadcastIntent)
        }.start()
    }

    fun playMusicContinue() {
        if (mState == FLAG_STATE_ALIVE) {
            mIsPlaying = true
            if (mIsEndMusic) {
                mIsEndMusic = false
                mSongCurrentPosition = 0
            }
            mMusic?.seekTo(mSongCurrentPosition)
            mMusic?.start()
            mIsPlaying = true
        } else {
            Thread {
                val broadcastIntent = Intent()
                broadcastIntent.action = MainActivity.mBroadcastAction
                broadcastIntent.putExtra("STATUS", FLAG_CAN_NOT_PLAY_MUSIC)
                sendBroadcast(broadcastIntent)
            }.start()
        }
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
        if (mIsPlaying || mTypePause == FLAG_PAUSE_IN_NOTIFICATION) {
            Thread {
                val broadcastIntent = Intent()
                broadcastIntent.action = MainActivity.mBroadcastAction
                broadcastIntent.putExtra("STATUS", FLAG_PLAY_WHEN_START_APP_AGAIN)
                sendBroadcast(broadcastIntent)
            }.start()
        }
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

}
