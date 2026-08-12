package com.mononote.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore-backed preferences for Mononote.
 *
 * Currently holds the widget snapshot: the last-saved text of the active
 * note, written by [NotesRepository] and read by the home-screen widget.
 *
 * @param dataStore Preferences DataStore this settings store reads and writes.
 */
class SettingsDataStore(
    private val dataStore: DataStore<Preferences>,
) {
    /** Emits the widget snapshot, or an empty string when unset. */
    val activeNoteSnapshot: Flow<String> =
        dataStore.data.map { it[KEY_ACTIVE_NOTE_SNAPSHOT] ?: "" }

    /** Persists [text] as the widget snapshot. */
    suspend fun saveActiveNoteSnapshot(text: String) {
        dataStore.edit { it[KEY_ACTIVE_NOTE_SNAPSHOT] = text }
    }

    private companion object {
        val KEY_ACTIVE_NOTE_SNAPSHOT = stringPreferencesKey("active_note_snapshot")
    }
}
