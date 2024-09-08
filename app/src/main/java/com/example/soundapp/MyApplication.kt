package com.example.soundapp

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.core.app.NotificationManagerCompat
import com.example.soundapp.utils.NOTIFICATION_ID
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityDestroyed(activity: Activity) {
                // Clear all notifications when any app is destroyed
                val notificationManager = NotificationManagerCompat.from(activity)
                notificationManager.cancel(NOTIFICATION_ID)
            }
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
        })
    }
}
