package com.mononote.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.mononote.app.data.FontFamilyOption
import com.mononote.app.data.FontSizeOption
import com.mononote.app.data.SettingsDataStore
import com.mononote.app.data.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The settings screen's current preference values. */
data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val fontFamily: FontFamilyOption = FontFamilyOption.DEFAULT,
    val fontSize: FontSizeOption = FontSizeOption.MEDIUM,
)

/**
 * Drives the settings screen: exposes the persisted preferences and writes
 * back every selection. The active values flow straight into the app theme via
 * [com.mononote.app.ui.theme.MononoteTheme], so changes apply immediately.
 *
 * @param settingsDataStore Source and sink of the preferences.
 */
class SettingsViewModel(
    private val settingsDataStore: SettingsDataStore,
) : ViewModel() {
    /** The persisted theme mode, font family, and text size. */
    val uiState: StateFlow<SettingsUiState> =
        combine(
            settingsDataStore.themeMode,
            settingsDataStore.fontFamily,
            settingsDataStore.fontSize,
        ) { themeMode, fontFamily, fontSize ->
            SettingsUiState(themeMode, fontFamily, fontSize)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    /** Persists [mode] as the theme mode. */
    fun onThemeModeSelected(mode: ThemeMode) {
        viewModelScope.launch { settingsDataStore.saveThemeMode(mode) }
    }

    /** Persists [family] as the note-text font family. */
    fun onFontFamilySelected(family: FontFamilyOption) {
        viewModelScope.launch { settingsDataStore.saveFontFamily(family) }
    }

    /** Persists [size] as the note-text size. */
    fun onFontSizeSelected(size: FontSizeOption) {
        viewModelScope.launch { settingsDataStore.saveFontSize(size) }
    }

    companion object {
        /** [ViewModelProvider.Factory] providing [SettingsViewModel]. */
        fun factory(settingsDataStore: SettingsDataStore): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    SettingsViewModel(settingsDataStore)
                }
            }
    }
}
