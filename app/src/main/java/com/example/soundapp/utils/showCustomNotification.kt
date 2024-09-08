package com.example.soundapp.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.soundapp.R
import com.example.soundapp.entity.Sound

const val CHANNEL_ID = "Sound_Channel"
const val NOTIFICATION_ID = 1
const val ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE"
const val ACTION_STOP = "ACTION_STOP"

fun showCustomNotification(
    context: Context,
    soundList: ArrayList<Sound>,
    soundName: String,
    isPlaying: Boolean
) {
    val notificationManager = NotificationManagerCompat.from(context)
    // Create intents for notification actions
    val playPauseIntent = Intent(context, NotificationReceiver::class.java).apply {
        action = ACTION_PLAY_PAUSE
        putParcelableArrayListExtra("SOUND_LIST", soundList)
        putExtra("SOUND_NAME", soundName)
        putExtra("IS_PLAYING", isPlaying)
    }
    val stopIntent = Intent(context, NotificationReceiver::class.java).apply {
        action = ACTION_STOP
    }
    // Create PendingIntents
    val playPausePendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        playPauseIntent,
        PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
    val stopPendingIntent = PendingIntent.getBroadcast(
        context,
        1,
        stopIntent,
        PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
    // Create the notification layout
    val customView = RemoteViews(context.packageName, R.layout.notification_layout).apply {
        setTextViewText(R.id.sound_name, soundName)
        setImageViewResource(
            R.id.play_pause_button,
            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        )
        setOnClickPendingIntent(R.id.play_pause_button, playPausePendingIntent)
        setOnClickPendingIntent(R.id.stop_button, stopPendingIntent)
    }
    val bigTextStyle = NotificationCompat.BigTextStyle()
        .bigText("Sounds...")
    // Create the notification
    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.sound_icon)
        .setCustomContentView(customView)
        .setCustomBigContentView(customView)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setOngoing(isPlaying)
        .setStyle(bigTextStyle)
        .setSound(null)
        .build()

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }
    notificationManager.notify(NOTIFICATION_ID, notification)
}

fun createNotificationChannel(context: Context) {
    /*For Notification*/
    val name = "Sound Notifications"
    val descriptionText = "Notification channel for sound playback"
    val importance = NotificationManager.IMPORTANCE_LOW
    val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
        description = descriptionText
        setSound(null, null)
    }
    val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)
}
