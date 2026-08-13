package com.mononote.app

import android.app.Application
import androidx.glance.appwidget.updateAll
import androidx.room.Room
import com.mononote.app.data.NotesDatabase
import com.mononote.app.data.NotesRepository
import com.mononote.app.data.SettingsDataStore
import com.mononote.app.data.mononoteDataStore
import com.mononote.app.notification.LiveNoteController
import com.mononote.app.widget.MononoteWidget
import com.mononote.app.widget.MononoteWidgetSync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import timber.log.Timber

/**
 * Application class. Installs Timber logging in debug builds and owns the
 * app-wide singletons: the Room database, the notes repository with its
 * DataStore-backed settings store, the go-live state, and the home-screen
 * widget refresh bridge.
 */
class MononoteApp : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        widgetSync.start(appScope)
    }

    private val database: NotesDatabase by lazy {
        Room.databaseBuilder(this, NotesDatabase::class.java, "mononote.db").build()
    }

    /**
     * App-wide DataStore-backed settings store. The repository writes the
     * widget snapshot into it; the home-screen widget reads it.
     */
    val settingsDataStore: SettingsDataStore by lazy {
        SettingsDataStore(applicationContext.mononoteDataStore)
    }

    /** App-wide notes repository; the single entry point for note data. */
    val repository: NotesRepository by lazy {
        NotesRepository(
            dao = database.notesDao(),
            settingsDataStore = settingsDataStore,
        )
    }

    /**
     * Refreshes every placed home-screen widget whenever the widget snapshot
     * changes, so the widget always shows the latest saved note text.
     */
    private val widgetSync: MononoteWidgetSync by lazy {
        MononoteWidgetSync(settingsDataStore) {
            MononoteWidget().updateAll(applicationContext)
        }
    }

    /** App-wide go-live state: whether the live-note service is running. */
    val liveNoteController: LiveNoteController by lazy {
        LiveNoteController()
    }
}
