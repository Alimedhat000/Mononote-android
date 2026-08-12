package com.mononote.app

import android.app.Application
import timber.log.Timber

/**
 * Application class. Installs Timber logging in debug builds. The Room
 * database, DataStore, and repository singletons are wired here (Phase 2).
 */
class MononoteApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
