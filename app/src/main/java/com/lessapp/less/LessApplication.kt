package com.lessapp.less

import android.app.Application
import com.lessapp.less.service.AdMobService
import com.lessapp.less.service.RevenueCatService

class LessApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize AdMob
        AdMobService.initialize(this)

        // Initialize RevenueCat
        RevenueCatService.configure(this)
    }
}
