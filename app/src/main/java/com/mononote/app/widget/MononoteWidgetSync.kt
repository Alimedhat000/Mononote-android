package com.mononote.app.widget

import com.mononote.app.data.SettingsDataStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Keeps every placed home-screen widget in sync with the widget snapshot.
 *
 * Glance 1.1 widget compositions go idle shortly after they render, so a
 * widget cannot observe the snapshot by itself while the app is writing it.
 * This app-scope collector runs [onSnapshotChanged] after every snapshot write
 * (each autosave, archive, delete, and restore), which refreshes the widgets
 * with the latest note text.
 *
 * A failing refresh is logged and swallowed: the sync must outlive any single
 * widget-update error, and an update failure is never worth crashing the app.
 *
 * @param settingsDataStore Source of the snapshot the widget mirrors.
 * @param onSnapshotChanged Refreshes the widgets with the new [String] text.
 */
class MononoteWidgetSync(
    private val settingsDataStore: SettingsDataStore,
    private val onSnapshotChanged: suspend (String) -> Unit,
) {
    /** Starts observing snapshot changes; call once from the Application. */
    @Suppress("TooGenericExceptionCaught")
    fun start(scope: CoroutineScope) {
        scope.launch {
            settingsDataStore.activeNoteSnapshot.collect { text ->
                try {
                    onSnapshotChanged(text)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Throwable) {
                    Timber.w(error, "Failed to refresh the home-screen widget")
                }
            }
        }
    }
}
