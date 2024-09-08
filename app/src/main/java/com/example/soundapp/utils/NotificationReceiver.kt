package com.example.soundapp.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.soundapp.entity.Sound

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val soundList: ArrayList<Sound>? = intent.getParcelableArrayListExtra("SOUND_LIST")
        val soundName = intent.getStringExtra("SOUND_NAME")
        var isPlaying = intent.getBooleanExtra("IS_PLAYING", false)
        when (intent.action) {
            ACTION_PLAY_PAUSE -> {
                val newPlayingState = !isPlaying
                if (newPlayingState) {
                    // Play sound
                    soundList?.let { soundName?.let { it1 ->
                        SoundPlayer.playGroups(context, it,
                            it1
                        )
                    } }
                } else {
                    // Pause sound
                    SoundPlayer.pauseSound()
                }
                // Update notification with new state
                soundList?.let { soundName?.let { it1 ->
                    showCustomNotification(context, it,
                        it1, newPlayingState)
                } }
                val updateIntent = Intent("com.example.soundapp.ACTION_PLAY_PAUSE").apply {
                    putExtra("IS_PLAYING", isPlaying)
                }
                LocalBroadcastManager.getInstance(context).sendBroadcast(updateIntent)

            }

            ACTION_STOP -> {
                SoundPlayer.stopAllSounds()
                isPlaying = false
                val notificationManager = NotificationManagerCompat.from(context)
                notificationManager.cancel(NOTIFICATION_ID)
                val updateIntent = Intent("com.example.soundapp.ACTION_PLAY_PAUSE").apply {
                    putExtra("IS_PLAYING", isPlaying)
                }
                LocalBroadcastManager.getInstance(context).sendBroadcast(updateIntent)
            }
        }
    }
}
