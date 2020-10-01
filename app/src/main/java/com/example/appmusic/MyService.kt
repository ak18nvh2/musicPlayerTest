package com.example.appmusic

import android.R
import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat


class MyService : Service() {
    private  var song: Song? = null
    private var pausePosition = 0
    private var music: MediaPlayer? = null

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
        if (bundle!= null) {
            song = bundle.getSerializable("SONG") as Song
        }
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Foreground Service")
            .setContentText(song?.songName)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)
        var countHandle = intent?.getIntExtra("COUNT_HANDLE",0)
        if(countHandle!! % 2 == 0) {
            music = MediaPlayer.create(this, Uri.parse(song?.songLocation))
            this.music?.seekTo(this.pausePosition)
            this.music?.start()
        } else {
            this.music?.pause()
            this.pausePosition = this.music?.currentPosition!!
        }

        music?.setOnCompletionListener {
            Toast.makeText(this, "het roi", Toast.LENGTH_SHORT).show()
            Log.d("music","het roi")
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
        this.music?.stop()
        super.onDestroy()
        Log.d("music","service destroy")
    }
}