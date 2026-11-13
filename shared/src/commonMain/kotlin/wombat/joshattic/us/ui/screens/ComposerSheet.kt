@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package wombat.joshattic.us.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import wombat.joshattic.us.data.model.AuthSession
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import kotlinx.coroutines.launch
import wombat.joshattic.us.data.model.Post
import wombat.joshattic.us.platform.rememberMultiImagePicker
import wombat.joshattic.us.ui.components.QUOTE_STRIPE_WIDTH_DP
import wombat.joshattic.us.ui.components.RichFormat
import wombat.joshattic.us.ui.components.RichTextEditorState
import wombat.joshattic.us.ui.components.drawQuoteBars
import wombat.joshattic.us.ui.viewmodel.HomeViewModel

@Composable
fun ComposerSheet(
    draft: String,
    onDraftChange: (String) -> Unit,
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit,
    drafts: List<String>,
    onRestoreDraft: (String) -> Unit,
    onDeleteDraft: (String) -> Unit,
    currentUsername: String? = null,
    onDeletePost: ((Post) -> Unit)? = null,
    isEditing: Boolean = false,
    viewModel: HomeViewModel? = null,
    savedAccounts: List<AuthSession> = emptyList(),
    onSwitchAccount: ((AuthSession) -> Unit)? = null,
    repostOf: Post? = null,
    errorMessage: String? = null
) {
    val haptic = LocalHapticFeedback.current
    var accountMenuExpanded by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var imageUploadError by remember { mutableStateOf<String?>(null) }
    var isUploadingImage by remember { mutableStateOf(false) }
    var uploadCurrent by remember { mutableStateOf(0) }
    var uploadTotal by remember { mutableStateOf(0) }
    var showDraftsDialog by remember { mutableStateOf(false) }
    var currentImages by remember { mutableStateOf(extractImages(draft)) }
    var lastSyncedDraft by remember { mutableStateOf(draft) }
    val editorState = remember { RichTextEditorState(stripImages(draft)) }
    var editorValue by remember { mutableStateOf(editorState.value) }
    var formatRevision by remember { mutableStateOf(0) }
    val charCount = editorValue.text.length

    LaunchedEffect(draft) {
        if (draft != lastSyncedDraft) {
            lastSyncedDraft = draft
            val newImages = extractImages(draft)
            if (!isUploadingImage && newImages != currentImages) {
                currentImages = newImages
            }
            editorState.setFromHtml(stripImages(draft))
            editorValue = editorState.value
            formatRevision++
        }
    }

    val isBoldActive = remember(editorValue.selection, formatRevision) {
        editorState.isFormatActive(RichFormat.BOLD)
    }
    val isItalicActive = remember(editorValue.selection, formatRevision) {
        editorState.isFormatActive(RichFormat.ITALIC)
    }
    val isUnderlineActive = remember(editorValue.selection, formatRevision) {
        editorState.isFormatActive(RichFormat.UNDERLINE)
    }
    val isStrikethroughActive = remember(editorValue.selection, formatRevision) {
        editorState.isFormatActive(RichFormat.STRIKETHROUGH)
    }
    val isQuoteActive = remember(editorValue.selection, formatRevision) {
        editorState.isFormatActive(RichFormat.QUOTE)
    }

    fun buildDraftHtml(textHtml: String?, images: List<String>): String {
        val resolvedHtml = textHtml ?: editorState.toHtml()
        val imgTags = images.joinToString("\n") { "<img src=\"$it\" alt=\"\">" }
        return if (imgTags.isBlank()) resolvedHtml else "$resolvedHtml\n$imgTags"
    }

    fun updateDraft(textHtml: String?, images: List<String>) {
        val combinedHtml = buildDraftHtml(textHtml, images)
        lastSyncedDraft = combinedHtml
        onDraftChange(combinedHtml)
    }

    val launchImagePicker = rememberMultiImagePicker(maxItems = 8) { images ->
        if (images.isNotEmpty() && viewModel != null) {
            isUploadingImage = true
            imageUploadError = null
            uploadTotal = images.size
            uploadCurrent = 0
            coroutineScope.launch {
                try {
                    images.forEachIndexed { index, img ->
                        uploadCurrent = index + 1
                        val uploadedUrl = viewModel.uploadImage(img)
                        currentImages = currentImages + uploadedUrl
                        updateDraft(null, currentImages)
                    }
                } catch (e: Exception) {
                    imageUploadError = e.message ?: "Upload failed"
                } finally {
                    isUploadingImage = false
                }
            }
        }
    }

    if (showDraftsDialog) {
        val draftsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val dismissDraftsSheet: () -> Unit = {
            coroutineScope.launch {
                draftsSheetState.hide()
            }.invokeOnCompletion {
                showDraftsDialog = false
            }
        }

        ModalBottomSheet(
            onDismissRequest = { showDraftsDialog = false },
            sheetState = draftsSheetState,
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
                    IconButton(onClick = { dismissDraftsSheet() }) {
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
                                    HtmlText(
                                        html = d,
                                        modifier = Modifier.padding(bottom = 8.dp),
                                        maxLines = 10
                                    )
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        IconButton(onClick = {
                                            onRestoreDraft(d)
                                            dismissDraftsSheet()
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
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
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
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable(enabled = savedAccounts.isNotEmpty() && !isEditing) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    accountMenuExpanded = true
                                }
                                .padding(vertical = 4.dp, horizontal = 6.dp)
                        ) {
                            Box {
                                currentUsername?.let { username ->
                                    ProfilePicture(username = username, size = 40.dp)
                                }
                                if (savedAccounts.size > 1 && !isEditing) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .align(Alignment.BottomEnd)
                                            .offset(x = 2.dp, y = 2.dp)
                                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                                            .border(1.5.dp, MaterialTheme.colorScheme.surfaceContainerLow, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = if (isEditing) "Edit post" else "New post",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                    )
                                    if (savedAccounts.size > 1 && !isEditing) {
                                        Icon(
                                            Icons.Filled.ArrowDropDown,
                                            contentDescription = "Switch account",
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }

                        DropdownMenu(
                            expanded = accountMenuExpanded,
                            onDismissRequest = { accountMenuExpanded = false },
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                            tonalElevation = 3.dp
                        ) {
                            Text(
                                text = "Switch Account",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            savedAccounts.forEach { acc ->
                                val isActiveAcc = acc.username.equals(currentUsername, ignoreCase = true)
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            ProfilePicture(username = acc.username, size = 28.dp)
                                            Text(
                                                text = "@${acc.username}",
                                                fontWeight = if (isActiveAcc) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                                            )
                                        }
                                    },
                                    onClick = {
                                        accountMenuExpanded = false
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onSwitchAccount?.invoke(acc)
                                    },
                                    trailingIcon = {
                                        if (isActiveAcc) {
                                            Icon(
                                                Icons.Filled.Check,
                                                contentDescription = "Current account",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                    if (!isEditing) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.clickable { showDraftsDialog = true }
                        ) {
                            Text(
                                text = "Drafts (${drafts.size})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                if (repostOf != null && !isEditing) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            ProfilePicture(username = repostOf.poster.name, size = 20.dp)
                            Text(
                                text = "Reposting post from @${repostOf.poster.name}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                editorState.toggleFormat(RichFormat.BOLD)
                                editorValue = editorState.value
                                formatRevision++
                                updateDraft(null, currentImages)
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (isBoldActive) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                contentColor = if (isBoldActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(Icons.Filled.FormatBold, contentDescription = "Bold", modifier = Modifier.size(20.dp))
                        }
                        IconButton(
                            onClick = {
                                editorState.toggleFormat(RichFormat.ITALIC)
                                editorValue = editorState.value
                                formatRevision++
                                updateDraft(null, currentImages)
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (isItalicActive) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                contentColor = if (isItalicActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(Icons.Filled.FormatItalic, contentDescription = "Italic", modifier = Modifier.size(20.dp))
                        }
                        IconButton(
                            onClick = {
                                editorState.toggleFormat(RichFormat.STRIKETHROUGH)
                                editorValue = editorState.value
                                formatRevision++
                                updateDraft(null, currentImages)
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (isStrikethroughActive) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                contentColor = if (isStrikethroughActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(Icons.Filled.FormatStrikethrough, contentDescription = "Strikethrough", modifier = Modifier.size(20.dp))
                        }
                        IconButton(
                            onClick = {
                                editorState.toggleFormat(RichFormat.UNDERLINE)
                                editorValue = editorState.value
                                formatRevision++
                                updateDraft(null, currentImages)
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (isUnderlineActive) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                contentColor = if (isUnderlineActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(Icons.Filled.FormatUnderlined, contentDescription = "Underline", modifier = Modifier.size(20.dp))
                        }
                        IconButton(
                            onClick = {
                                editorState.toggleFormat(RichFormat.QUOTE)
                                editorValue = editorState.value
                                formatRevision++
                                updateDraft(null, currentImages)
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (isQuoteActive) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                contentColor = if (isQuoteActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(Icons.Filled.FormatQuote, contentDescription = "Blockquote", modifier = Modifier.size(20.dp))
                        }
                        IconButton(
                            onClick = {
                                imageUploadError = null
                                launchImagePicker()
                            },
                            enabled = !isUploadingImage,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(Icons.Filled.Image, contentDescription = "Add image", modifier = Modifier.size(20.dp))
                        }
                    }
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
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
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

                if (imageUploadError != null) {
                    Text(
                        text = imageUploadError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    var isFocused by remember { mutableStateOf(false) }
                    val borderColors = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    val borderWidth = if (isFocused) 2.dp else 1.dp
                    val textColor = MaterialTheme.colorScheme.onSurface
                    val focusRequester = remember { FocusRequester() }
                    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
                    val density = LocalDensity.current
                    val quoteStripeWidthPx = with(density) { QUOTE_STRIPE_WIDTH_DP.dp.toPx().coerceAtLeast(1f) }
                    val editorScrollState = rememberScrollState()

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 220.dp, max = 360.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(20.dp))
                            .border(borderWidth, borderColors, RoundedCornerShape(20.dp))
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) {
                                focusRequester.requestFocus()
                            }
                            .verticalScroll(editorScrollState)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        if (editorValue.text.isEmpty()) {
                            Text(
                                text = "What's happening? Write words here.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = textColor.copy(alpha = 0.6f)
                            )
                        }
                        BasicTextField(
                            value = editorValue,
                            onValueChange = { newValue ->
                                editorState.onValueChange(newValue)
                                editorValue = editorState.value
                                formatRevision++
                                updateDraft(null, currentImages)
                            },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = textColor),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                            onTextLayout = { textLayoutResult = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                                .onFocusChanged { isFocused = it.isFocused }
                                .drawBehind {
                                    drawQuoteBars(
                                        textLayoutResult = textLayoutResult,
                                        annotatedString = editorValue.annotatedString,
                                        stripeWidthPx = quoteStripeWidthPx
                                    )
                                }
                        )
                    }

                    Text(
                        text = "$charCount / ${HomeViewModel.MAX_CHAR_COUNT} characters",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (charCount > HomeViewModel.MAX_CHAR_COUNT) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.End)
                    )
                }

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
                                        val updatedImages = currentImages.toMutableList().apply { removeAt(index) }
                                        currentImages = updatedImages
                                        updateDraft(null, updatedImages)
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
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
                Button(
                    onClick = {
                        // Rebuild at submit time so attached images always make it
                        // into the outgoing HTML even if a draft sync was missed.
                        onSubmit(buildDraftHtml(null, currentImages))
                    },
                    enabled = (charCount > 0 || currentImages.isNotEmpty()) && charCount <= HomeViewModel.MAX_CHAR_COUNT,
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.End)
                        .height(48.dp)
                        .padding(horizontal = 4.dp)
                ) {
                    Icon(Icons.Filled.PostAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isEditing) "Save" else "Post", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
