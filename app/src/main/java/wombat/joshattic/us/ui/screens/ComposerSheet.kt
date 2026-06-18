@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package wombat.joshattic.us.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import wombat.joshattic.us.data.model.Post
import wombat.joshattic.us.ui.viewmodel.HomeViewModel

@Composable
fun ComposerSheet(
    draft: String,
    onDraftChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
    drafts: List<String>,
    onRestoreDraft: (String) -> Unit,
    onDeleteDraft: (String) -> Unit,
    currentUsername: String? = null,
    onDeletePost: ((Post) -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showAddImage by remember { mutableStateOf(false) }
    var imageUrl by remember { mutableStateOf("") }
    var imageError by remember { mutableStateOf<String?>(null) }
    var showDraftsDialog by remember { mutableStateOf(false) }

    // Images attached (separate from text markdown, shown as thumbnails below)
    var currentImages by remember(draft) { mutableStateOf(extractImages(draft)) }

    fun addImage() {
        val url = imageUrl.trim()
        if (url.isBlank()) {
            imageError = "Enter an image URL"
            return
        }
        if (!isAllowedImageHost(url)) {
            imageError = "Image URL must be from i.ibb.co or u.cubeupload.com"
            return
        }
        currentImages = currentImages + url
        imageUrl = ""
        imageError = null
        showAddImage = false
    }

    if (showDraftsDialog) {
        ModalBottomSheet(
            onDismissRequest = { showDraftsDialog = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.background,
            dragHandle = null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Drafts Manager", style = MaterialTheme.typography.headlineSmall)
                    IconButton(onClick = { showDraftsDialog = false }) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                if (drafts.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Filled.PostAdd,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Text(
                                "No saved drafts yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Drafts are saved automatically when you\nclose the composer with text in it.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalItemSpacing = 8.dp
                    ) {
                        items(drafts) { d ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = d,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 10,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        IconButton(onClick = {
                                            onRestoreDraft(d)
                                            showDraftsDialog = false
                                        }) {
                                            Icon(Icons.Filled.Restore, contentDescription = "Restore", tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(onClick = { onDeleteDraft(d) }) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("New post", style = MaterialTheme.typography.titleLarge)
                    TextButton(onClick = { showDraftsDialog = true }) {
                        Text("Drafts (${drafts.size})")
                    }
                }

                // Image attach button only (styling removed - use markdown manually in the text box)
                IconButton(onClick = { showAddImage = !showAddImage; imageError = null }, modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Filled.Image, contentDescription = "Add image", modifier = Modifier.size(24.dp))
                }

                if (showAddImage) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = imageUrl,
                            onValueChange = { imageUrl = it; imageError = null },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("https://i.ibb.co/xxx or https://u.cubeupload.com/xxx") },
                            label = { Text("Image URL (i.ibb.co or u.cubeupload.com only)") }
                        )
                        if (imageError != null) {
                            Text(imageError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { addImage() }, enabled = imageUrl.isNotBlank()) {
                                Text("Insert Image")
                            }
                            TextButton(onClick = {
                                showAddImage = false
                                imageUrl = ""
                                imageError = null
                            }) {
                                Text("Cancel")
                            }
                        }
                    }
                }

                // Direct editable text input (no outer box, input itself sized and styled)
                val charCount = draft.length
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = onDraftChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 220.dp),
                        shape = RoundedCornerShape(16.dp),
                        placeholder = { Text("What's happening? Write words here, markdown supported.") }
                    )
                    Text(
                        text = "$charCount / ${HomeViewModel.MAX_CHAR_COUNT} characters",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (charCount > HomeViewModel.MAX_CHAR_COUNT) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.End)
                    )
                }

                // Image attachments preview - shown below the text editor like normal post composer (thumbnails, removable)
                if (currentImages.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currentImages.forEachIndexed { index, url ->
                            Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp))) {
                                SubcomposeAsyncImage(
                                    model = url,
                                    contentDescription = "Attached image",
                                    loading = { Box(contentAlignment = Alignment.Center) { CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp) } },
                                    modifier = Modifier.fillMaxSize()
                                )
                                IconButton(
                                    onClick = {
                                        currentImages = currentImages.toMutableList().apply { removeAt(index) }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(20.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                ) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "Remove image",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
            ) {
                Button(
                    onClick = {
                        // Convert markdown to HTML AFTER pressing post. Append attached images.
                        val md = draft
                        val htmlText = markdownToHtml(md)
                        val imgTags = currentImages.joinToString("\n") { "<img src=\"$it\" alt=\"\">" }
                        val fullHtml = if (imgTags.isBlank()) htmlText else "$htmlText\n$imgTags"
                        onDraftChange(fullHtml)
                        onSubmit()
                    },
                    enabled = (draft.isNotBlank() || currentImages.isNotEmpty()) && draft.length <= HomeViewModel.MAX_CHAR_COUNT,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Filled.PostAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Post")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
