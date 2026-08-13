package com.mononote.app.notification

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.mononote.app.MononoteApp
import com.mononote.app.data.NotesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Foreground service behind "Go live": keeps the active note visible as a
 * persistent live notification that stays in sync as the note is edited.
 *
 * Started with [ACTION_START] (the default when no action is set) it posts a
 * foreground notification with the current text and observes the repository so
 * every autosave refreshes the notification. When the active note disappears
 * (archived, deleted, or never created) the service stops itself; the Stop
 * notification action stops it on demand. On process death Android removes the
 * foreground notification, so a stale note can never linger.
 */
class LiveNoteService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var observationJob: Job? = null

    private val app: MononoteApp get() = applicationContext as MononoteApp
    private val controller: LiveNoteController get() = app.liveNoteController
    private val repository: NotesRepository get() = app.repository

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        LiveNoteNotifications.createChannel(this)
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        if (intent?.action == ACTION_STOP) {
            stopLive()
            return START_NOT_STICKY
        }
        val notification = LiveNoteNotifications.build(this, "")
        ServiceCompat.startForeground(
            this,
            LiveNoteNotifications.NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
        )
        controller.onLiveStarted()
        observeActiveNote()
        return START_NOT_STICKY
    }

    private fun observeActiveNote() {
        if (observationJob != null) return
        observationJob =
            serviceScope.launch {
                repository.observeActiveNote().collect { note ->
                    if (note == null) {
                        stopLive()
                    } else {
                        LiveNoteNotifications.notify(this@LiveNoteService, note.text)
                    }
                }
            }
    }

    private fun stopLive() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        observationJob?.cancel()
        serviceScope.cancel()
        controller.onLiveStopped()
        super.onDestroy()
    }

    companion object {
        /** Action sent to stop the live note, from the notification Stop button. */
        const val ACTION_STOP = "com.mononote.app.action.STOP_LIVE"
    }
}
