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


class MyService : Service() {
    private var mSong: Song? = null
    private var mPausePosition = 0
    private var mMusic: MediaPlayer? = null
    private var mSongViewModel = SongViewModel()


    private val CHANNEL_ID = "ForegroundServiceChannel"
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )
        var intent = intent
        var bundle = intent?.extras
        if (bundle != null) {
            mSong = bundle.getSerializable("SONG") as Song
        }
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Foreground Service")
            .setContentText(mSong?.songName)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)

        var position = intent?.getIntExtra("POSITION_CURRENT",-1)
        if(position!! > 0 ) {
            this.mMusic?.stop()
            this.mMusic?.seekTo(position)
            this.mMusic?.start()
        }
        else {
            var countHandle = intent?.getIntExtra("COUNT_HANDLE", 0)
            if (countHandle!! % 2 == 0) {
                mMusic = MediaPlayer.create(this, Uri.parse(mSong?.songLocation))
                this.mMusic?.seekTo(this.mPausePosition)
                this.mMusic?.start()
            } else {
                this.mMusic?.pause()
                this.mPausePosition = this.mMusic?.currentPosition!!
            }
        }


        Thread {
            val min = this.mMusic?.duration!! / 60000
            val sec = (this.mMusic?.duration!! / 1000) % 60
            val broadcastIntent = Intent()
            broadcastIntent.action = PlayMusicActivity.mBroadcastAction
            broadcastIntent.putExtra("MINUTES", min)
            broadcastIntent.putExtra("SECONDS", sec)
            sendBroadcast(broadcastIntent)
        }.start()

        mMusic?.setOnCompletionListener {
            this.mMusic?.seekTo(0)
            this.mMusic?.start()
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        this.mMusic?.stop()
        super.onDestroy()
        Log.d("music", "service destroy")
    }
}