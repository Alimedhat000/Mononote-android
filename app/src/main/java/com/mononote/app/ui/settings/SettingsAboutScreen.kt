package com.mononote.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mononote.app.BuildConfig
import com.mononote.app.R
import com.mononote.app.ui.theme.LocalMononoteColors

/**
 * The about screen: the app name, tagline, and installed version, centered
 * under the settings top bar.
 *
 * @param onBack Returns to the settings screen.
 * @param modifier Modifier for the root column.
 */
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalMononoteColors.current
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
    ) {
        SettingsTopBar(
            onBack = onBack,
            title = stringResource(R.string.about_mononote_title),
            modifier = Modifier.padding(vertical = 8.dp),
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.primaryText,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.about_mononote_tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.secondaryText,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.about_mononote_version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodySmall,
                color = colors.secondaryText,
            )
        }
    }
}
