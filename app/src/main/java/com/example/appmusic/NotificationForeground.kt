package com.example.appmusic

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationForeground(context: Context, mess: String) {
    private val mContext: Context = context
    private val mMessage = mess
    fun buildNotification(): Notification {
        createNotificationChannel()
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            mContext, 0,
            Intent(mContext, MusicService::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
        )
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Notification")
                .setContentText(mMessage)
                .setColor(Color.BLUE)
                .setPriority(NotificationCompat.PRIORITY_LOW)
        synchronized(builder) {}
        return builder.build()
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "1"
            val importance: Int = NotificationManager.IMPORTANCE_DEFAULT
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    name,
                    importance
                )
            channel.description = "description"
            val notificationManager: NotificationManager = mContext.getSystemService(
                NotificationManager::class.java
            )
            notificationManager.deleteNotificationChannel(CHANNEL_ID)
            notificationManager.createNotificationChannel(channel)
        }
    }
    companion object {
        private const val CHANNEL_ID = "Foreground"
    }


}