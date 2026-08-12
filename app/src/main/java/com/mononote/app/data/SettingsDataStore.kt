package com.mononote.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsDataStore(
    private val dataStore: DataStore<Preferences>,
) {
    val activeNoteSnapshot: Flow<String> =
        dataStore.data.map { it[KEY_ACTIVE_NOTE_SNAPSHOT] ?: "" }

    suspend fun saveActiveNoteSnapshot(text: String) {
        dataStore.edit { it[KEY_ACTIVE_NOTE_SNAPSHOT] = text }
    }

    private companion object {
        val KEY_ACTIVE_NOTE_SNAPSHOT = stringPreferencesKey("active_note_snapshot")
    }
}
