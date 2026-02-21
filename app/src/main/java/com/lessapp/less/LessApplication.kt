package com.lessapp.less

import android.app.Application
import com.lessapp.less.service.AdMobService
import com.lessapp.less.service.NotificationService

class LessApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize AdMob
        AdMobService.initialize(this)

        // Create notification channel
        NotificationService.createNotificationChannel(this)
    }
}
