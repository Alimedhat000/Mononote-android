package com.mononote.app

import android.app.Application
import androidx.room.Room
import com.mononote.app.data.NotesDatabase
import com.mononote.app.data.NotesRepository
import com.mononote.app.data.SettingsDataStore
import com.mononote.app.data.mononoteDataStore
import timber.log.Timber

/**
 * Application class. Installs Timber logging in debug builds and owns the
 * app-wide singletons: the Room database and the notes repository with its
 * DataStore-backed settings store.
 */
class MononoteApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private val database: NotesDatabase by lazy {
        Room.databaseBuilder(this, NotesDatabase::class.java, "mononote.db").build()
    }

    /** App-wide notes repository; the single entry point for note data. */
    val repository: NotesRepository by lazy {
        NotesRepository(
            dao = database.notesDao(),
            settingsDataStore = SettingsDataStore(applicationContext.mononoteDataStore),
        )
    }
}
