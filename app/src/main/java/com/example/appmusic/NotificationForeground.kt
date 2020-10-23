package com.example.appmusic

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.appmusic.views.MainActivity

class NotificationForeground(context: Context, mess: String, service: MusicService) {
    private val mContext: Context = context
    private val mMessage = mess
    private val mMusicService = service

    fun buildNotification(): Notification {
        createNotificationChannel()
        val resultIntent = Intent(mContext, MainActivity::class.java)
        resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val resultPendingIntent =
            PendingIntent.getActivity(
                mContext,
                1,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentText(mMessage)
                .setColor(Color.BLUE)
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .addAction(
                    notificationAction(MusicService.FLAG_ACTION_PRE_SONG)
                )
                .addAction(
                    notificationAction(MusicService.FLAG_ACTION_PAUSE)
                )
                .addAction(notificationAction(MusicService.FLAG_ACTION_NEXT_SONG))
                .setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                )
        synchronized(builder) {}
        return builder.build()
    }

    private fun notificationAction(action: String): NotificationCompat.Action {
        val icon: Int = when (action) {
            MusicService.FLAG_ACTION_NEXT_SONG -> R.drawable.ic_baseline_skip_next_24
            MusicService.FLAG_ACTION_PAUSE -> {
                if (mMusicService.isPlaying) {
                    R.drawable.ic_baseline_pause_circle_outline_24
                } else {
                    R.drawable.ic_baseline_play_arrow_24
                }
            }
            else -> R.drawable.ic_baseline_skip_previous_24
        }
        return NotificationCompat.Action.Builder(icon, action, playerAction(action)).build()
    }

    private fun playerAction(action: String): PendingIntent? {
        return PendingIntent.getBroadcast(
            mContext,
            0,
            Intent().setAction(action),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
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