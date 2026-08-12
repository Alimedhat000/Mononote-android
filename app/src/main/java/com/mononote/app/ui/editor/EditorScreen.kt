package com.mononote.app.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mononote.app.R
import com.mononote.app.ui.theme.MononoteTheme

@Composable
fun EditorScreen(
    onOpenArchive: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
        )
        TextButton(onClick = onOpenArchive) {
            Text(stringResource(R.string.view_archived_notes))
        }
    }
}

@Preview(showBackground = true, name = "Editor placeholder")
@Composable
private fun EditorScreenPreview() {
    MononoteTheme {
        EditorScreen(onOpenArchive = {})
    }
}
