package com.mononote.app.ui.settings

import app.cash.turbine.test
import com.mononote.app.data.FontFamilyOption
import com.mononote.app.data.FontSizeOption
import com.mononote.app.data.SettingsDataStore
import com.mononote.app.data.ThemeMode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val mainDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun runSettingsTest(block: suspend TestScope.() -> Unit) = runTest(mainDispatcher.scheduler) { block() }

    /** The writable preference flows behind a mocked [SettingsDataStore]. */
    private class SettingsFlows(
        val themeMode: MutableStateFlow<ThemeMode>,
        val fontFamily: MutableStateFlow<FontFamilyOption>,
        val fontSize: MutableStateFlow<FontSizeOption>,
    )

    private fun settingsStoreWith(
        themeMode: ThemeMode = ThemeMode.SYSTEM,
        fontFamily: FontFamilyOption = FontFamilyOption.DEFAULT,
        fontSize: FontSizeOption = FontSizeOption.MEDIUM,
    ): Pair<SettingsDataStore, SettingsFlows> {
        val flows =
            SettingsFlows(
                themeMode = MutableStateFlow(themeMode),
                fontFamily = MutableStateFlow(fontFamily),
                fontSize = MutableStateFlow(fontSize),
            )
        val store = mockk<SettingsDataStore>(relaxed = true)
        every { store.themeMode } returns flows.themeMode
        every { store.fontFamily } returns flows.fontFamily
        every { store.fontSize } returns flows.fontSize
        coEvery { store.saveThemeMode(any()) } just runs
        coEvery { store.saveFontFamily(any()) } just runs
        coEvery { store.saveFontSize(any()) } just runs
        return store to flows
    }

    @Test
    fun `exposes the persisted defaults`() =
        runSettingsTest {
            val (store, _) = settingsStoreWith()
            val vm = SettingsViewModel(store)

            vm.uiState.test {
                assertEquals(SettingsUiState(), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `persists a selected theme mode`() =
        runSettingsTest {
            val (store, _) = settingsStoreWith()
            val vm = SettingsViewModel(store)

            vm.onThemeModeSelected(ThemeMode.DARK)
            runCurrent()

            coVerify(exactly = 1) { store.saveThemeMode(ThemeMode.DARK) }
        }

    @Test
    fun `reflects the theme mode flow`() =
        runSettingsTest {
            val (store, flows) = settingsStoreWith()
            val vm = SettingsViewModel(store)

            vm.uiState.test {
                awaitItem()
                flows.themeMode.value = ThemeMode.LIGHT
                assertEquals(SettingsUiState(themeMode = ThemeMode.LIGHT), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `persists a selected font family`() =
        runSettingsTest {
            val (store, _) = settingsStoreWith()
            val vm = SettingsViewModel(store)

            vm.onFontFamilySelected(FontFamilyOption.SERIF)
            runCurrent()

            coVerify(exactly = 1) { store.saveFontFamily(FontFamilyOption.SERIF) }
        }

    @Test
    fun `reflects the font family flow`() =
        runSettingsTest {
            val (store, flows) = settingsStoreWith()
            val vm = SettingsViewModel(store)

            vm.uiState.test {
                awaitItem()
                flows.fontFamily.value = FontFamilyOption.MONOSPACE
                assertEquals(SettingsUiState(fontFamily = FontFamilyOption.MONOSPACE), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `persists a selected font size`() =
        runSettingsTest {
            val (store, _) = settingsStoreWith()
            val vm = SettingsViewModel(store)

            vm.onFontSizeSelected(FontSizeOption.LARGE)
            runCurrent()

            coVerify(exactly = 1) { store.saveFontSize(FontSizeOption.LARGE) }
        }

    @Test
    fun `reflects the font size flow`() =
        runSettingsTest {
            val (store, flows) = settingsStoreWith()
            val vm = SettingsViewModel(store)

            vm.uiState.test {
                awaitItem()
                flows.fontSize.value = FontSizeOption.SMALL
                assertEquals(SettingsUiState(fontSize = FontSizeOption.SMALL), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }
}
