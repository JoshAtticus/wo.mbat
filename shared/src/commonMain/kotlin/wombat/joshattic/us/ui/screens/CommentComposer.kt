package wombat.joshattic.us.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import kotlinx.coroutines.launch
import wombat.joshattic.us.platform.PickedImage
import wombat.joshattic.us.platform.rememberMultiImagePicker
import wombat.joshattic.us.ui.components.RichFormat
import wombat.joshattic.us.ui.components.RichTextEditorState

@Composable
fun CommentComposer(
    draft: String,
    onDraftChange: (String) -> Unit,
    onSubmit: () -> Unit,
    placeholder: String,
    onUploadImage: (suspend (PickedImage) -> String)? = null,
    onImageClick: (List<String>, Int) -> Unit = { _, _ -> },
    isSending: Boolean = false,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val editorState = remember { RichTextEditorState(stripImages(draft)) }
    var editorValue by remember { mutableStateOf(editorState.value) }
    var lastSyncedDraft by remember { mutableStateOf(draft) }
    var currentImages by remember { mutableStateOf(extractImages(draft)) }
    var isUploadingImage by remember { mutableStateOf(false) }
    var uploadCurrent by remember { mutableStateOf(0) }
    var uploadTotal by remember { mutableStateOf(0) }
    var imageUploadError by remember { mutableStateOf<String?>(null) }
    var formatRevision by remember { mutableStateOf(0) }
    val plainDraft = editorValue.text
    val maxCharacterCount = 12_000

    LaunchedEffect(draft) {
        if (draft != lastSyncedDraft) {
            lastSyncedDraft = draft
            if (!isUploadingImage) {
                currentImages = extractImages(draft)
            }
            val stripped = stripImages(draft)
            editorState.setFromHtml(stripped)
            editorValue = editorState.value
            formatRevision++
        }
    }

    fun syncDraft(images: List<String>) {
        val textHtml = editorState.toHtml()
        val imgTags = images.joinToString("\n") { "<img src=\"$it\" alt=\"\">" }
        val combinedHtml = if (imgTags.isBlank()) textHtml else "$textHtml\n$imgTags"
        lastSyncedDraft = combinedHtml
        onDraftChange(combinedHtml)
    }

    val isBoldActive = remember(editorValue.selection, formatRevision) {
        editorState.isFormatActive(RichFormat.BOLD)
    }
    val isItalicActive = remember(editorValue.selection, formatRevision) {
        editorState.isFormatActive(RichFormat.ITALIC)
    }
    val isStrikethroughActive = remember(editorValue.selection, formatRevision) {
        editorState.isFormatActive(RichFormat.STRIKETHROUGH)
    }
    val isUnderlineActive = remember(editorValue.selection, formatRevision) {
        editorState.isFormatActive(RichFormat.UNDERLINE)
    }

    val launchImagePicker = rememberMultiImagePicker(maxItems = 8) { images ->
        val upload = onUploadImage
        if (images.isNotEmpty() && upload != null) {
            isUploadingImage = true
            imageUploadError = null
            uploadTotal = images.size
            uploadCurrent = 0
            coroutineScope.launch {
                try {
                    images.forEachIndexed { index, img ->
                        uploadCurrent = index + 1
                        val uploadedUrl = upload(img)
                        currentImages = currentImages + uploadedUrl
                        syncDraft(currentImages)
                    }
                } catch (e: Exception) {
                    imageUploadError = e.message ?: "Upload failed"
                } finally {
                    isUploadingImage = false
                }
            }
        }
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
            IconButton(
                onClick = {
                    editorState.toggleFormat(RichFormat.BOLD)
                    editorValue = editorState.value
                    formatRevision++
                    syncDraft(currentImages)
                },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (isBoldActive) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    contentColor = if (isBoldActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Filled.FormatBold, contentDescription = "Bold", modifier = Modifier.size(18.dp))
            }
            IconButton(
                onClick = {
                    editorState.toggleFormat(RichFormat.ITALIC)
                    editorValue = editorState.value
                    formatRevision++
                    syncDraft(currentImages)
                },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (isItalicActive) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    contentColor = if (isItalicActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Filled.FormatItalic, contentDescription = "Italic", modifier = Modifier.size(18.dp))
            }
            IconButton(
                onClick = {
                    editorState.toggleFormat(RichFormat.STRIKETHROUGH)
                    editorValue = editorState.value
                    formatRevision++
                    syncDraft(currentImages)
                },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (isStrikethroughActive) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    contentColor = if (isStrikethroughActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Filled.FormatStrikethrough, contentDescription = "Strikethrough", modifier = Modifier.size(18.dp))
            }
            IconButton(
                onClick = {
                    editorState.toggleFormat(RichFormat.UNDERLINE)
                    editorValue = editorState.value
                    formatRevision++
                    syncDraft(currentImages)
                },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (isUnderlineActive) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    contentColor = if (isUnderlineActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Filled.FormatUnderlined, contentDescription = "Underline", modifier = Modifier.size(18.dp))
            }
            if (onUploadImage != null) {
                IconButton(
                    onClick = {
                        imageUploadError = null
                        launchImagePicker()
                    },
                    enabled = !isUploadingImage,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Filled.Image, contentDescription = "Add image", modifier = Modifier.size(18.dp))
                }
            }
            }
            Text(
                text = "${plainDraft.length} / $maxCharacterCount",
                style = MaterialTheme.typography.labelSmall,
                color = if (plainDraft.length > maxCharacterCount) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.padding(start = 6.dp, end = 4.dp)
            )
        }

        if (isUploadingImage) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Text(
                        if (uploadTotal > 1) "Uploading image $uploadCurrent of $uploadTotal…" else "Uploading image…",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (uploadTotal > 1) {
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { uploadCurrent.toFloat() / uploadTotal },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        imageUploadError?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        if (currentImages.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                currentImages.forEachIndexed { index, url ->
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onImageClick(currentImages, index) }
                    ) {
                        SubcomposeAsyncImage(
                            model = url,
                            contentDescription = "Attached image",
                            contentScale = ContentScale.Crop,
                            loading = { Box(contentAlignment = Alignment.Center) { CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp) } },
                            modifier = Modifier.fillMaxSize()
                        )
                        IconButton(
                            onClick = {
                                val updatedImages = currentImages.toMutableList().apply { removeAt(index) }
                                currentImages = updatedImages
                                syncDraft(updatedImages)
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

        val textColor = MaterialTheme.colorScheme.onSurface
        val borderColor = MaterialTheme.colorScheme.outlineVariant
        val estimatedLines = plainDraft.split('\n').sumOf { (it.length / 38) + 1 }.coerceAtLeast(1)
        val composerHeight = (56 + (estimatedLines - 1).coerceAtMost(8) * 22).dp
        val scrollState = rememberScrollState()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(composerHeight)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(20.dp))
                .border(1.dp, borderColor, RoundedCornerShape(20.dp))
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    if (editorValue.text.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor.copy(alpha = 0.6f)
                        )
                    }
                    BasicTextField(
                        value = editorValue,
                        onValueChange = { newValue ->
                            editorState.onValueChange(newValue)
                            editorValue = editorState.value
                            formatRevision++
                            syncDraft(currentImages)
                        },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                FilledIconButton(
                    onClick = onSubmit,
                    enabled = !isSending && draft.isNotBlank() && plainDraft.length <= maxCharacterCount,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .padding(bottom = 8.dp)
                        .size(40.dp)
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
