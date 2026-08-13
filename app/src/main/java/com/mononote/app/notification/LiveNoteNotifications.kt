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
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mononote.app.MainActivity
import com.mononote.app.R
import timber.log.Timber

/**
 * Builds and posts the live-note notification: a persistent, non-dismissable
 * notification that shows the note's current text as its title, opens the
 * editor on tap, and offers a Stop action.
 *
 * On API 36+ the notification requests promotion to a Live Update (status-bar
 * chip, expanded on the lock screen) using an eligible BigTextStyle with
 * `setRequestPromotedOngoing`; on older platforms it posts the same expandable
 * BigTextStyle without the promotion request. Promotion is an enhancement, not
 * a requirement: when the user has disabled live updates in settings the same
 * notification still posts as a regular ongoing one.
 */
object LiveNoteNotifications {
    const val CHANNEL_ID = "live_note"

    /** Notification id of the live-note foreground notification. */
    const val NOTIFICATION_ID = 1001

    /**
     * Title length cap: notes longer than this are split into a truncated
     * title (with an ellipsis) and the remainder as the expanded body, so the
     * collapsed card never relies on the system's own title truncation. The
     * cut prefers the closest space at or before the cap so words are kept
     * whole; only falls back to a hard cut when the text has no space there.
     */
    private const val MAX_TITLE_CHARS = 49

    /**
     * Creates the channel the live note posts on. Default importance is
     * required for promoted-ongoing (Live Update) eligibility.
     */
    fun createChannel(context: Context) {
        val channel =
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.live_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = context.getString(R.string.live_notification_channel_description)
                setShowBadge(false)
            }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /**
     * True on API 36+ when the user has not disabled live updates for the app
     * in settings; false on older platforms.
     */
    fun canPostPromoted(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.BAKLAVA) return false
        return context.getSystemService(NotificationManager::class.java).canPostPromotedNotifications()
    }

    /**
     * Intent that deep-links the user to the live-updates setting for this app
     * on API 36+, or null on older platforms.
     */
    fun promotionSettingsIntent(context: Context): Intent? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            Intent(Settings.ACTION_APP_NOTIFICATION_PROMOTION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        } else {
            null
        }

    /** Builds the live-note notification for [noteText]. */
    fun build(
        context: Context,
        noteText: String,
    ): Notification {
        val noteTitle = noteText.ifBlank { context.getString(R.string.placeholder_text) }
        val (titleText, bodyText) =
            if (noteTitle.length > MAX_TITLE_CHARS) {
                val cutIndex = noteTitle.take(MAX_TITLE_CHARS).indexOfLast { it == ' ' }
                if (cutIndex > 0) {
                    noteTitle.substring(0, cutIndex).trimEnd() + "…" to
                        noteTitle.substring(cutIndex).trimStart()
                } else {
                    noteTitle.take(MAX_TITLE_CHARS) + "…" to noteTitle.drop(MAX_TITLE_CHARS)
                }
            } else {
                noteTitle to ""
            }
        val bigTextStyle =
            NotificationCompat
                .BigTextStyle()
                .setBigContentTitle(titleText)
                .bigText(bodyText)
        val openApp =
            PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        val builder =
            NotificationCompat
                .Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_monochrome)
                .setContentTitle(titleText)
                .setContentText(null)
                .setContentIntent(openApp)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setStyle(bigTextStyle)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            builder
                .setRequestPromotedOngoing(true)
                .setShortCriticalText(context.getString(R.string.live))
        }
        return builder.build()
    }

    /**
     * Refreshes the live-note notification with [noteText]. A no-op when the
     * notification permission has been revoked since the note went live. On
     * API 36+ logs whether the posted notification actually qualifies for
     * promotion, purely as a diagnostic.
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
        val notification = build(context, noteText)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            Timber.d(
                "Live note notification promotable: ${notification.hasPromotableCharacteristics()}",
            )
        }
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}
