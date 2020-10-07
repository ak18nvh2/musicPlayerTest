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
    private var mMusic: MediaPlayer? = null
    private var mNotificationForeground: NotificationForeground? = null


    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var intent = intent
        var bundle = intent?.extras
        if (bundle != null) {
            mSong = bundle.getSerializable("SONG") as Song?
            mNotificationForeground = NotificationForeground(this, mSong?.songName!!)
            startForeground(1, mNotificationForeground?.buildNotification())
            mMusic = MediaPlayer.create(this, Uri.parse(mSong?.songLocation))
            mMusic?.start()
            Thread {
                val min = this.mMusic?.duration!! / 60000
                val sec = (this.mMusic?.duration!! / 1000) % 60
                val broadcastIntent = Intent()
                broadcastIntent.action = PlayMusicActivity.mBroadcastAction
                broadcastIntent.putExtra("MINUTES", min)
                broadcastIntent.putExtra("SECONDS", sec)
                broadcastIntent.putExtra("STATUS", 0) // 0 là lần đầu run
                sendBroadcast(broadcastIntent)
            }.start()
        } else {
            val stt = intent?.getIntExtra("STT", 0)
            if (stt == 1) {
                mMusic?.pause()
                mSong?.pausePosition = mMusic?.currentPosition!!
                Thread {
                    val min = this.mMusic?.currentPosition!! / 60000
                    val sec = (this.mMusic?.currentPosition!! / 1000) % 60
                    val broadcastIntent = Intent()
                    broadcastIntent.action = PlayMusicActivity.mBroadcastAction
                    broadcastIntent.putExtra("MINUTES", min)
                    broadcastIntent.putExtra("SECONDS", sec)
                    broadcastIntent.putExtra("STATUS", 1) // 1 la pause
                    sendBroadcast(broadcastIntent)
                }.start()
            } else if (stt == 2) {
                mMusic?.seekTo(mSong?.pausePosition!!)
                mMusic?.start()
            }
        }
        mMusic?.setOnCompletionListener {
            this.mMusic?.seekTo(0)
            this.mMusic?.start()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        this.mMusic?.stop()
        super.onDestroy()
        Log.d("music", "service destroy")
    }
}