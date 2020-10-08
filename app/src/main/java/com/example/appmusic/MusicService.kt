package com.example.appmusic

import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import kotlin.math.min


class MusicService : Service() {
    private var mSong: Song? = null
    private var mMusic: MediaPlayer = MediaPlayer()
    private var mNotificationForeground: NotificationForeground? = null


    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val intent = intent
        when (intent?.getIntExtra("STT", 0)) {
            0 -> {
                if(mMusic.isPlaying) {
                    mMusic.stop()
                }
                val bundle = intent.extras
                mSong = bundle?.getSerializable("SONG") as Song?
                mNotificationForeground = NotificationForeground(this, mSong?.songName!!)
                startForeground(1, mNotificationForeground?.buildNotification())
                mMusic = MediaPlayer.create(this, Uri.parse(mSong?.songLocation))
                mMusic.start()
                Thread {
                    val min = this.mMusic.duration / 60000
                    val sec = (this.mMusic.duration / 1000) % 60
                    val broadcastIntent = Intent()
                    broadcastIntent.action = MainActivity.mBroadcastAction
                    broadcastIntent.putExtra("MINUTES", min)
                    broadcastIntent.putExtra("SECONDS", sec)
                    broadcastIntent.putExtra("STATUS", 0) // 0 là lần đầu run
                    sendBroadcast(broadcastIntent)
                }.start()
            }
            1 -> {// pause
                mMusic.pause()
                mSong?.pausePosition = mMusic.currentPosition
                Thread {
                    val min = this.mMusic.currentPosition / 60000
                    val sec = (this.mMusic.currentPosition / 1000) % 60
                    val broadcastIntent = Intent()
                    broadcastIntent.action = MainActivity.mBroadcastAction
                    broadcastIntent.putExtra("MINUTES", min)
                    broadcastIntent.putExtra("SECONDS", sec)
                    broadcastIntent.putExtra("STATUS", 1) // 1 la pause
                    sendBroadcast(broadcastIntent)
                }.start()
            }
            2 -> { // play again
                Thread {
                    val broadcastIntent = Intent()
                    broadcastIntent.action = MainActivity.mBroadcastAction
                    broadcastIntent.putExtra("STATUS", 2) // 2 la play again
                    sendBroadcast(broadcastIntent)
                }.start()
                mMusic.seekTo(mSong?.pausePosition!!)
                mMusic.start()
            }
            3 -> {
                val pos = intent.getIntExtra("POS", 0)
                val isPlay = intent.getBooleanExtra("IS_PLAY", true)
                mSong?.pausePosition = pos * 1000
                mMusic.seekTo(mSong?.pausePosition!!)
                if (isPlay) {
                    mMusic.start()
                }

                Thread {
                    val broadcastIntent = Intent()
                    broadcastIntent.action = MainActivity.mBroadcastAction
                    broadcastIntent.putExtra("STATUS", 3) // 3 la play seek bar
                    broadcastIntent.putExtra("IS_PLAY", isPlay)
                    sendBroadcast(broadcastIntent)
                }.start()
            }
        }
        mMusic.setOnCompletionListener(){
            Thread {
                val broadcastIntent = Intent()
                broadcastIntent.action = MainActivity.mBroadcastAction
                broadcastIntent.putExtra("STATUS", 4) // 4 la end song
                sendBroadcast(broadcastIntent)
            }.start()



        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        this.mMusic.stop()
        super.onDestroy()
        Log.d("music", "service destroy")
    }
}