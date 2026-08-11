package wombat.joshattic.us.wear.ui.screens

import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import wombat.joshattic.us.wear.WearUiState

@Composable
fun ComposeScreen(
    uiState: WearUiState,
    onSubmit: (String) -> Unit,
    onCancel: () -> Unit
) {
    var draft by remember { mutableStateOf("") }
    
    val KEY_POST = "post_text"
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val bundle: Bundle? = RemoteInput.getResultsFromIntent(result.data ?: Intent())
            bundle?.getCharSequence(KEY_POST)?.toString()?.takeIf { it.isNotBlank() }?.let {
                draft = it
            } ?: run {
                if (draft.isEmpty()) onCancel()
            }
        } else {
            if (draft.isEmpty()) {
                onCancel()
            }
        }
    }

    fun launchInput() {
        val remoteInput = RemoteInput.Builder(KEY_POST)
            .setLabel("Write post...")
            .build()
        val intent = androidx.wear.input.RemoteInputIntentHelper.createActionRemoteInputIntent()
        androidx.wear.input.RemoteInputIntentHelper.putRemoteInputsExtra(intent, listOf(remoteInput))
        launcher.launch(intent)
    }

    // Auto-launch input when opening the screen
    LaunchedEffect(Unit) {
        if (draft.isEmpty()) {
            launchInput()
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            if (draft.isEmpty()) {
                Text(
                    "What's on your mind?",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurfaceVariant
                )
                Button(onClick = { launchInput() }) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Write post")
                }
            } else {
                Text(
                    text = draft,
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface,
                    maxLines = 3,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Text(
                    text = "${draft.length} chars",
                    style = MaterialTheme.typography.caption2,
                    color = if (draft.length > 500) Color(0xFFEF4444) else MaterialTheme.colors.onSurfaceVariant
                )
                
                Chip(
                    onClick = { onSubmit(draft) },
                    label = { Text("Post", style = MaterialTheme.typography.caption1) },
                    icon = { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null) },
                    colors = ChipDefaults.primaryChipColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                Chip(
                    onClick = { draft = ""; launchInput() },
                    label = { Text("Edit", style = MaterialTheme.typography.caption1) },
                    colors = ChipDefaults.secondaryChipColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
