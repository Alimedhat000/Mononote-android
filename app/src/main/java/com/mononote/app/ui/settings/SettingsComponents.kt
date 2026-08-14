package com.mononote.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mononote.app.R
import com.mononote.app.data.FontFamilyOption
import com.mononote.app.data.FontSizeOption
import com.mononote.app.data.ThemeMode
import com.mononote.app.ui.theme.LocalMononoteColors
import com.mononote.app.ui.theme.MononoteAlertDialog
import com.composables.icons.lucide.R as LucideR

/** A muted section label above a settings card. */
@Composable
internal fun SettingsSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        modifier = modifier.padding(start = 16.dp, top = 20.dp, bottom = 6.dp),
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.SemiBold,
        color = LocalMononoteColors.current.secondaryText,
    )
}

/** A settings card: the app's card surface holding a column of rows. */
@Composable
internal fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = LocalMononoteColors.current.cardSurface),
    ) {
        Column(content = content)
    }
}

/**
 * One settings row: a muted leading icon, title with an optional subtitle, and
 * a chevron. [divider] draws a thin divider below the row.
 */
@Composable
internal fun SettingsItem(
    icon: Int,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    divider: Boolean = false,
) {
    val colors = LocalMononoteColors.current
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = colors.secondaryText,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.primaryText,
                )
                if (subtitle != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.secondaryText,
                    )
                }
            }
            Icon(
                painter = painterResource(LucideR.drawable.lucide_ic_chevron_right),
                contentDescription = null,
                tint = colors.secondaryText,
                modifier = Modifier.size(18.dp),
            )
        }
        if (divider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = colors.secondaryText.copy(alpha = 0.25f),
            )
        }
    }
}

/** A single-choice dialog; tapping an option picks it and closes. */
@Composable
internal fun OptionPickerDialog(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalMononoteColors.current
    MononoteAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEachIndexed { index, label ->
                    val selected = index == selectedIndex
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelect(index) }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = colors.primaryText,
                        )
                        if (selected) {
                            Icon(
                                painter = painterResource(LucideR.drawable.lucide_ic_check),
                                contentDescription = null,
                                tint = colors.primaryText,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

/** The current theme mode's display label. */
@Composable
internal fun themeLabel(mode: ThemeMode): String =
    when (mode) {
        ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
        ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
        ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
    }

/** The current font family's display label. */
@Composable
internal fun fontLabel(family: FontFamilyOption): String =
    when (family) {
        FontFamilyOption.DEFAULT -> stringResource(R.string.settings_font_default)
        FontFamilyOption.SERIF -> stringResource(R.string.settings_font_serif)
        FontFamilyOption.MONOSPACE -> stringResource(R.string.settings_font_monospace)
    }

/** The current text size's display label. */
@Composable
internal fun fontSizeLabel(size: FontSizeOption): String =
    when (size) {
        FontSizeOption.SMALL -> stringResource(R.string.settings_text_size_small)
        FontSizeOption.MEDIUM -> stringResource(R.string.settings_text_size_medium)
        FontSizeOption.LARGE -> stringResource(R.string.settings_text_size_large)
    }
