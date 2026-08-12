package com.mononote.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class SettingsDataStoreTest {
    private fun withDataStore(block: suspend (SettingsDataStore) -> Unit) =
        runBlocking {
            val tempFile = File.createTempFile("mononote-test", ".preferences_pb")
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            try {
                val dataStore: DataStore<Preferences> =
                    PreferenceDataStoreFactory.create(
                        scope = scope,
                        produceFile = { tempFile },
                    )
                block(SettingsDataStore(dataStore))
            } finally {
                scope.cancel()
                tempFile.delete()
            }
        }

    @Test
    fun snapshotIsEmptyByDefault() =
        withDataStore { settings ->
            assertEquals("", settings.activeNoteSnapshot.first())
        }

    @Test
    fun savedSnapshotCanBeReadBack() =
        withDataStore { settings ->
            settings.saveActiveNoteSnapshot("buy milk")
            assertEquals("buy milk", settings.activeNoteSnapshot.first())
        }

    @Test
    fun laterSaveOverwritesSnapshot() =
        withDataStore { settings ->
            settings.saveActiveNoteSnapshot("first")
            settings.saveActiveNoteSnapshot("second")
            assertEquals("second", settings.activeNoteSnapshot.first())
        }
}
