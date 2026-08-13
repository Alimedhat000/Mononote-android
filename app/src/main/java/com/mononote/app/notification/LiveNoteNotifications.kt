package com.mononote.app.notification

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mononote.app.MainActivity
import com.mononote.app.R

/**
 * Builds and posts the live-note notification: the note's current text shown
 * as a persistent, non-dismissable notification that opens the editor on tap
 * and offers a Stop action.
 */
object LiveNoteNotifications {
    const val CHANNEL_ID = "live_note"

    /** Notification id of the live-note foreground notification. */
    const val NOTIFICATION_ID = 1001

    /** Creates the low-importance channel the live note posts on. */
    fun createChannel(context: Context) {
        val channel =
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.live_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = context.getString(R.string.live_notification_channel_description)
                setShowBadge(false)
            }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /** Builds the live-note notification for [noteText]. */
    fun build(
        context: Context,
        noteText: String,
    ): Notification {
        val contentText =
            noteText.ifBlank { context.getString(R.string.placeholder_text) }
        val openApp =
            PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        val stop =
            PendingIntent.getService(
                context,
                1,
                Intent(context, LiveNoteService::class.java).setAction(LiveNoteService.ACTION_STOP),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        return NotificationCompat
            .Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(context.getString(R.string.live_note_title))
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setContentIntent(openApp)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .addAction(0, context.getString(R.string.live_note_stop), stop)
            .build()
    }

    /**
     * Refreshes the live-note notification with [noteText]. A no-op when the
     * notification permission has been revoked since the note went live.
     */
    fun notify(
        context: Context,
        noteText: String,
    ) {
        if (
            Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, build(context, noteText))
    }
}
