package com.mononote.app.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mononote.app.R
import com.mononote.app.data.FontFamilyOption
import com.mononote.app.data.FontSizeOption
import com.mononote.app.data.SettingsDataStore
import com.mononote.app.data.ThemeMode
import com.mononote.app.ui.theme.LocalMononoteColors
import com.mononote.app.ui.theme.MononoteTheme
import com.composables.icons.lucide.R as LucideR

/** The settings dialog currently waiting for a choice. */
private enum class SettingsPicker {
    THEME,
    FONT_FAMILY,
    FONT_SIZE,
}

/** A picker dialog's contents: its title, options, and current selection. */
private data class PickerSpec(
    val title: String,
    val options: List<String>,
    val selectedIndex: Int,
    val onSelect: (Int) -> Unit,
)

/** The setting-write callbacks, passed down to the picker dialogs. */
private data class SettingsActions(
    val onSelectTheme: (ThemeMode) -> Unit,
    val onSelectFontFamily: (FontFamilyOption) -> Unit,
    val onSelectFontSize: (FontSizeOption) -> Unit,
)

/**
 * The settings screen: the app's preferences grouped into Appearance, Note
 * Text, Feedback, and About sections. Theme, font family, and text size open
 * picker dialogs; feedback is a placeholder for a later phase.
 *
 * @param settingsDataStore Source and sink of the preferences; injects the
 *   [SettingsViewModel].
 * @param onBack Returns to the editor.
 * @param onOpenAbout Opens the about screen.
 * @param modifier Modifier for the root column.
 */
@Composable
fun SettingsScreen(
    settingsDataStore: SettingsDataStore,
    onBack: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(settingsDataStore)),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var pendingPicker by remember { mutableStateOf<SettingsPicker?>(null) }
    val actions =
        SettingsActions(
            onSelectTheme = viewModel::onThemeModeSelected,
            onSelectFontFamily = viewModel::onFontFamilySelected,
            onSelectFontSize = viewModel::onFontSizeSelected,
        )

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp),
    ) {
        SettingsTopBar(
            onBack = onBack,
            title = stringResource(R.string.settings),
            modifier = Modifier.padding(vertical = 8.dp),
        )
        SettingsSections(
            uiState = uiState,
            onPick = { pendingPicker = it },
            onOpenAbout = onOpenAbout,
        )
    }

    SettingsPickerDialog(
        picker = pendingPicker,
        uiState = uiState,
        actions = actions,
        onDismiss = { pendingPicker = null },
    )
}

/** Back arrow and centered title, matching the archive screen's top bar. */
@Composable
internal fun SettingsTopBar(
    onBack: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalMononoteColors.current
    Box(modifier = modifier.fillMaxWidth()) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = colors.primaryText,
            )
        }
        Text(
            text = title,
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colors.primaryText,
        )
        Spacer(
            modifier = Modifier.align(Alignment.CenterEnd).size(48.dp),
        )
    }
}

/** The grouped settings sections, scrollable under the top bar. */
@Composable
private fun SettingsSections(
    uiState: SettingsUiState,
    onPick: (SettingsPicker) -> Unit,
    onOpenAbout: () -> Unit,
) {
    val theme = themeLabel(uiState.themeMode)
    val font = fontLabel(uiState.fontFamily)
    val size = fontSizeLabel(uiState.fontSize)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        item { SettingsSectionHeader(stringResource(R.string.settings_section_appearance)) }
        item { AppearanceSection(themeLabel = theme, onPickTheme = { onPick(SettingsPicker.THEME) }) }
        item { SettingsSectionHeader(stringResource(R.string.settings_section_note_text)) }
        item {
            NoteTextSection(
                fontLabel = font,
                sizeLabel = size,
                onPickFont = { onPick(SettingsPicker.FONT_FAMILY) },
                onPickSize = { onPick(SettingsPicker.FONT_SIZE) },
            )
        }
        item { SettingsSectionHeader(stringResource(R.string.settings_section_feedback)) }
        item { FeedbackSection() }
        item { SettingsSectionHeader(stringResource(R.string.settings_section_about)) }
        item { AboutSection(onOpenAbout = onOpenAbout) }
    }
}

/** The Appearance section: the theme picker row. */
@Composable
private fun AppearanceSection(
    themeLabel: String,
    onPickTheme: () -> Unit,
) {
    SettingsCard {
        SettingsItem(
            icon = LucideR.drawable.lucide_ic_palette,
            title = stringResource(R.string.settings_theme),
            subtitle = themeLabel,
            onClick = onPickTheme,
        )
    }
}

/** The Note Text section: font family and text size picker rows. */
@Composable
private fun NoteTextSection(
    fontLabel: String,
    sizeLabel: String,
    onPickFont: () -> Unit,
    onPickSize: () -> Unit,
) {
    SettingsCard {
        SettingsItem(
            icon = LucideR.drawable.lucide_ic_type,
            title = stringResource(R.string.settings_font),
            subtitle = fontLabel,
            onClick = onPickFont,
            divider = true,
        )
        SettingsItem(
            icon = LucideR.drawable.lucide_ic_type_outline,
            title = stringResource(R.string.settings_text_size),
            subtitle = sizeLabel,
            onClick = onPickSize,
        )
    }
}

/** The Feedback section: a single placeholder row for a later phase. */
@Composable
private fun FeedbackSection() {
    SettingsCard {
        SettingsItem(
            icon = LucideR.drawable.lucide_ic_mail,
            title = stringResource(R.string.settings_give_feedback),
            subtitle = stringResource(R.string.settings_give_feedback_subtitle),
            onClick = {},
        )
    }
}

/** The About section: the single about row. */
@Composable
private fun AboutSection(onOpenAbout: () -> Unit) {
    SettingsCard {
        SettingsItem(
            icon = LucideR.drawable.lucide_ic_info,
            title = stringResource(R.string.settings_about),
            onClick = onOpenAbout,
        )
    }
}

/** Shows the picker dialog for [picker], resolving its options and current value. */
@Composable
private fun SettingsPickerDialog(
    picker: SettingsPicker?,
    uiState: SettingsUiState,
    actions: SettingsActions,
    onDismiss: () -> Unit,
) {
    if (picker == null) return
    val spec =
        when (picker) {
            SettingsPicker.THEME ->
                PickerSpec(
                    title = stringResource(R.string.settings_theme),
                    options =
                        listOf(
                            stringResource(R.string.settings_theme_system),
                            stringResource(R.string.settings_theme_light),
                            stringResource(R.string.settings_theme_dark),
                        ),
                    selectedIndex = uiState.themeMode.ordinal,
                    onSelect = { actions.onSelectTheme(ThemeMode.entries[it]) },
                )
            SettingsPicker.FONT_FAMILY ->
                PickerSpec(
                    title = stringResource(R.string.settings_font),
                    options =
                        listOf(
                            stringResource(R.string.settings_font_default),
                            stringResource(R.string.settings_font_serif),
                            stringResource(R.string.settings_font_monospace),
                        ),
                    selectedIndex = uiState.fontFamily.ordinal,
                    onSelect = { actions.onSelectFontFamily(FontFamilyOption.entries[it]) },
                )
            SettingsPicker.FONT_SIZE ->
                PickerSpec(
                    title = stringResource(R.string.settings_text_size),
                    options =
                        listOf(
                            stringResource(R.string.settings_text_size_small),
                            stringResource(R.string.settings_text_size_medium),
                            stringResource(R.string.settings_text_size_large),
                        ),
                    selectedIndex = uiState.fontSize.ordinal,
                    onSelect = { actions.onSelectFontSize(FontSizeOption.entries[it]) },
                )
        }
    OptionPickerDialog(
        title = spec.title,
        options = spec.options,
        selectedIndex = spec.selectedIndex,
        onSelect = { index ->
            spec.onSelect(index)
            onDismiss()
        },
        onDismiss = onDismiss,
    )
}

/** Renders a sample settings card with placeholder rows. */
@Preview(showBackground = true, name = "Settings card")
@Composable
private fun SettingsCardPreview() {
    MononoteTheme {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            SettingsCard {
                SettingsItem(
                    icon = LucideR.drawable.lucide_ic_palette,
                    title = "Theme",
                    subtitle = "System",
                    onClick = {},
                )
            }
        }
    }
}
