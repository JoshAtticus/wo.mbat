package wombat.joshattic.us.wear.ui.screens

import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyColumnDefaults
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CircularProgressIndicator
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
    onPosted: () -> Unit,
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

    // Navigate to the newly created post once it's been submitted successfully
    LaunchedEffect(uiState.postSuccess) {
        if (uiState.postSuccess) {
            onPosted()
        }
    }

    // ScalingLazyColumn so the confirmation stays centered when short, but
    // becomes scrollable when a long draft would otherwise overflow the
    // circular screen and clip the text / buttons at the bezel
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(top = 28.dp, bottom = 28.dp),
        scalingParams = ScalingLazyColumnDefaults.scalingParams(edgeScale = 0.75f)
    ) {
        if (draft.isEmpty()) {
            item {
                Text(
                    "What's on your mind?",
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurfaceVariant
                )
            }
            item {
                Button(onClick = { launchInput() }) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Write post")
                }
            }
        } else {
            item {
                Text(
                    text = draft,
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )
            }
            item {
                Text(
                    text = "${draft.length} chars",
                    style = MaterialTheme.typography.caption2,
                    color = if (draft.length > 500) Color(0xFFEF4444) else MaterialTheme.colors.onSurfaceVariant
                )
            }

            if (uiState.isPosting) {
                // Uploading — show a spinner instead of the action chips
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Posting…",
                            style = MaterialTheme.typography.caption2,
                            color = MaterialTheme.colors.onSurfaceVariant
                        )
                    }
                }
            } else {
                if (uiState.errorMessage != null) {
                    item {
                        Text(
                            text = uiState.errorMessage,
                            style = MaterialTheme.typography.caption2,
                            color = Color(0xFFEF4444),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                        )
                    }
                }
                item {
                    Chip(
                        onClick = { onSubmit(draft) },
                        label = { Text("Post", style = MaterialTheme.typography.caption1) },
                        icon = { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null) },
                        colors = ChipDefaults.primaryChipColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
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
}
