package com.mononote.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * The app-wide Preferences DataStore, shared between the repository (writer),
 * the home-screen widget (reader), and the settings screen so they all see the
 * same snapshot and preferences.
 */
val Context.mononoteDataStore: DataStore<Preferences> by preferencesDataStore(name = "mononote")

/**
 * DataStore-backed preferences for Mononote.
 *
 * Holds the widget snapshot (the last-saved text of the active note, written
 * by [NotesRepository] and read by the home-screen widget) and the settings
 * screen's preferences: theme mode, note-text font family and size.
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

    /** Emits the theme mode, defaulting to following the system. */
    val themeMode: Flow<ThemeMode> =
        dataStore.data.map { prefs ->
            prefs[KEY_THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM
        }

    /** Persists [mode] as the theme mode. */
    suspend fun saveThemeMode(mode: ThemeMode) {
        dataStore.edit { it[KEY_THEME_MODE] = mode.name }
    }

    /** Emits the note-text font family, defaulting to the platform default. */
    val fontFamily: Flow<FontFamilyOption> =
        dataStore.data.map { prefs ->
            prefs[KEY_FONT_FAMILY]?.let { runCatching { FontFamilyOption.valueOf(it) }.getOrNull() }
                ?: FontFamilyOption.DEFAULT
        }

    /** Persists [family] as the note-text font family. */
    suspend fun saveFontFamily(family: FontFamilyOption) {
        dataStore.edit { it[KEY_FONT_FAMILY] = family.name }
    }

    /** Emits the note-text size, defaulting to medium. */
    val fontSize: Flow<FontSizeOption> =
        dataStore.data.map { prefs ->
            prefs[KEY_FONT_SIZE]?.let { runCatching { FontSizeOption.valueOf(it) }.getOrNull() }
                ?: FontSizeOption.MEDIUM
        }

    /** Persists [size] as the note-text size. */
    suspend fun saveFontSize(size: FontSizeOption) {
        dataStore.edit { it[KEY_FONT_SIZE] = size.name }
    }

    private companion object {
        val KEY_ACTIVE_NOTE_SNAPSHOT = stringPreferencesKey("active_note_snapshot")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_FONT_FAMILY = stringPreferencesKey("font_family")
        val KEY_FONT_SIZE = stringPreferencesKey("font_size")
    }
}
