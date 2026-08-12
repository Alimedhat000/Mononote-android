package com.mononote.app.ui.archive

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mononote.app.R
import com.mononote.app.ui.theme.MononoteTheme

@Composable
fun ArchiveScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
        }
        Text(
            text = stringResource(R.string.archived_notes),
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Preview(showBackground = true, name = "Archive placeholder")
@Composable
private fun ArchiveScreenPreview() {
    MononoteTheme {
        ArchiveScreen(onBack = {})
    }
}
