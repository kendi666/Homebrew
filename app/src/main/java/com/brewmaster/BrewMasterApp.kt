package com.brewmaster

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BrewMasterApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            TIMER_CHANNEL_ID,
            "Brew Timer",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Timer notifications for active brewing sessions"
            enableVibration(true)
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val TIMER_CHANNEL_ID = "brew_timer_channel"
    }
}
