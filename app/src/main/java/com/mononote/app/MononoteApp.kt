package com.mononote.app

import android.app.Application
import timber.log.Timber

class MononoteApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
