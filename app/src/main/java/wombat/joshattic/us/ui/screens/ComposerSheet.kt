@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package wombat.joshattic.us.ui.screens

import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.Spannable
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.CharacterStyle
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.EditText
import android.widget.OverScroller
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import coil.compose.SubcomposeAsyncImage
import kotlinx.coroutines.launch
import wombat.joshattic.us.data.model.Post
import wombat.joshattic.us.ui.theme.applyGoogleSansFlexTypeface
import wombat.joshattic.us.ui.viewmodel.HomeViewModel

class RichEditText(context: Context) : EditText(context) {
    var onSelectionChangedListener: ((start: Int, end: Int) -> Unit)? = null
    private val flingScroller = OverScroller(context)
    private val flingDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(event: MotionEvent): Boolean = true

        override fun onFling(
            start: MotionEvent?,
            end: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            flingScroller.fling(
                scrollX,
                scrollY,
                0,
                -velocityY.toInt(),
                0,
                0,
                0,
                (verticalScrollRange() - height).coerceAtLeast(0)
            )
            postInvalidateOnAnimation()
            return true
        }
    })

    fun verticalScrollRange(): Int = computeVerticalScrollRange()

    override fun onTouchEvent(event: MotionEvent): Boolean {
        flingDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    override fun computeScroll() {
        if (flingScroller.computeScrollOffset()) {
            scrollTo(flingScroller.currX, flingScroller.currY)
            postInvalidateOnAnimation()
        }
        super.computeScroll()
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        onSelectionChangedListener?.invoke(selStart, selEnd)
    }
}

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
    errorMessage: String? = null
) {
    val haptic = LocalHapticFeedback.current
    var accountMenuExpanded by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var imageUploadError by remember { mutableStateOf<String?>(null) }
    var isUploadingImage by remember { mutableStateOf(false) }
    var uploadCurrent by remember { mutableStateOf(0) }
    var uploadTotal by remember { mutableStateOf(0) }
    var showDraftsDialog by remember { mutableStateOf(false) }
    var currentImages by remember { mutableStateOf(extractImages(draft)) }
    var lastSyncedDraft by remember { mutableStateOf("") }
    var richEditTextRef by remember { mutableStateOf<RichEditText?>(null) }
    var charCount by remember { mutableStateOf(0) }
    var isBoldActive by remember { mutableStateOf(false) }
    var isItalicActive by remember { mutableStateOf(false) }
    var isUnderlineActive by remember { mutableStateOf(false) }
    var isStrikethroughActive by remember { mutableStateOf(false) }
    var isQuoteActive by remember { mutableStateOf(false) }

    fun updateFormattingStates(editText: EditText) {
        val text = editText.text ?: return
        val start = editText.selectionStart
        val end = editText.selectionEnd
        if (start < 0 || end < 0) return

        isBoldActive = hasSpan(text, start, end, StyleSpan::class.java) { it.style == Typeface.BOLD }
        isItalicActive = hasSpan(text, start, end, StyleSpan::class.java) { it.style == Typeface.ITALIC }
        isUnderlineActive = hasSpan(text, start, end, UnderlineSpan::class.java)
        isStrikethroughActive = hasSpan(text, start, end, StrikethroughSpan::class.java)
        isQuoteActive = hasSpan(text, start, end, android.text.style.QuoteSpan::class.java)
    }

    fun updateDraft(text: Spanned, images: List<String>) {
        val cleanText = cleanSpannedForHtml(text)
        val textHtml = HtmlCompat.toHtml(cleanText, HtmlCompat.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL)
        val convertedHtml = convertStrikethroughToSTag(textHtml)
        val imgTags = images.joinToString("\n") { "<img src=\"$it\" alt=\"\">" }
        val combinedHtml = if (imgTags.isBlank()) convertedHtml else "$convertedHtml\n$imgTags"
        
        lastSyncedDraft = combinedHtml
        onDraftChange(combinedHtml)
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 8)
    ) { uris ->
        if (uris.isNotEmpty() && viewModel != null) {
            isUploadingImage = true
            imageUploadError = null
            uploadTotal = uris.size
            uploadCurrent = 0
            coroutineScope.launch {
                try {
                    val updatedImages = currentImages
                    uris.forEachIndexed { index, uri ->
                        uploadCurrent = index + 1
                        val uploadedUrl = viewModel.uploadImage(context, uri)
                        currentImages = currentImages + uploadedUrl
                        richEditTextRef?.let { updateDraft(it.text, currentImages) }
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
                                richEditTextRef?.let { editText ->
                                    toggleSpan(editText, StyleSpan::class.java, { StyleSpan(Typeface.BOLD) }, { it.style == Typeface.BOLD })
                                    updateFormattingStates(editText)
                                }
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
                                richEditTextRef?.let { editText ->
                                    toggleSpan(editText, StyleSpan::class.java, { StyleSpan(Typeface.ITALIC) }, { it.style == Typeface.ITALIC })
                                    updateFormattingStates(editText)
                                }
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
                                richEditTextRef?.let { editText ->
                                    toggleSpan(editText, StrikethroughSpan::class.java, { StrikethroughSpan() })
                                    updateFormattingStates(editText)
                                }
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
                                richEditTextRef?.let { editText ->
                                    toggleSpan(editText, UnderlineSpan::class.java, { UnderlineSpan() })
                                    updateFormattingStates(editText)
                                }
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
                                richEditTextRef?.let { editText ->
                                    toggleBlockquote(editText)
                                    updateFormattingStates(editText)
                                }
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
                                imagePickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
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

                    val context = androidx.compose.ui.platform.LocalContext.current
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 220.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(20.dp))
                            .border(borderWidth, borderColors, RoundedCornerShape(20.dp))
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) {
                                richEditTextRef?.let { editText ->
                                    editText.requestFocus()
                                    editText.setSelection(editText.text?.length ?: 0)
                                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
                                    imm?.showSoftInput(editText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        AndroidView(
                            factory = { context ->
                                RichEditText(context).apply {
                                    richEditTextRef = this
                                    applyGoogleSansFlexTypeface(this, roundness = 50f)
                                    this.setBackground(null)
                                    this.setGravity(android.view.Gravity.TOP or android.view.Gravity.START)
                                    this.inputType = android.text.InputType.TYPE_CLASS_TEXT or 
                                                android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE or 
                                                android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                                    this.setHorizontallyScrolling(false)
                                    this.hint = "What's happening? Write words here."
                                    this.setHintTextColor(textColor.copy(alpha = 0.6f).toArgb())
                                    this.setTextColor(textColor.toArgb())
                                    this.textSize = 16f
                                    
                                    this.layoutParams = android.view.ViewGroup.LayoutParams(
                                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                    )

                                    this.addTextChangedListener(object : TextWatcher {
                                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                                            val currentText = this@apply.text ?: return
                                            charCount = currentText.length
                                            val cleanText = cleanSpannedForHtml(currentText as Spanned)
                                            val textHtml = HtmlCompat.toHtml(cleanText, HtmlCompat.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL)
                                            val convertedHtml = convertStrikethroughToSTag(textHtml)
                                            val imgTags = currentImages.joinToString("\n") { "<img src=\"$it\" alt=\"\">" }
                                            val combinedHtml = if (imgTags.isBlank()) convertedHtml else "$convertedHtml\n$imgTags"
                                            if (combinedHtml != lastSyncedDraft) {
                                                lastSyncedDraft = combinedHtml
                                                onDraftChange(combinedHtml)
                                            }
                                        }
                                        override fun afterTextChanged(s: Editable?) {}
                                    })

                                    this.onSelectionChangedListener = { _, _ ->
                                        updateFormattingStates(this)
                                    }

                                    this.onFocusChangeListener = android.view.View.OnFocusChangeListener { _, hasFocus ->
                                        isFocused = hasFocus
                                    }
                                }
                            },
                            update = { editText ->
                                if (draft != lastSyncedDraft) {
                                    lastSyncedDraft = draft
                                    val newImages = extractImages(draft)
                                    if (newImages != currentImages) {
                                        currentImages = newImages
                                    }
                                    val textHtml = stripImages(draft)
                                    val spanned = HtmlCompat.fromHtml(textHtml, HtmlCompat.FROM_HTML_MODE_COMPACT)
                                    val cleanSpanned = trimTrailingNewlines(spanned)
                                    cleanSpanned.getSpans(0, cleanSpanned.length, android.text.style.URLSpan::class.java).forEach { urlSpan ->
                                        val s = cleanSpanned.getSpanStart(urlSpan)
                                        val e = cleanSpanned.getSpanEnd(urlSpan)
                                        cleanSpanned.getSpans(s, e, UnderlineSpan::class.java).forEach { uSpan ->
                                            cleanSpanned.removeSpan(uSpan)
                                        }
                                    }
                                    replaceQuoteSpans(cleanSpanned)
                                    
                                    if (editText.text.toString() != cleanSpanned.toString()) {
                                        editText.setText(cleanSpanned)
                                        editText.setSelection(cleanSpanned.length)
                                    }
                                    charCount = cleanSpanned.length
                                    updateFormattingStates(editText)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
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
                                        richEditTextRef?.let { updateDraft(it.text, updatedImages) }
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
                        onSubmit(draft)
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


fun <T> hasSpan(
    text: Spanned,
    start: Int,
    end: Int,
    spanClass: Class<T>,
    predicate: (T) -> Boolean = { true }
): Boolean {
    if (start < 0 || end < 0) return false
    val spans = text.getSpans(start, end, spanClass)
    return if (start == end) {
        spans.any { span ->
            if (!predicate(span)) return@any false
            val s = text.getSpanStart(span)
            val e = text.getSpanEnd(span)
            val flags = text.getSpanFlags(span)
            val inclusiveRight = (flags and Spanned.SPAN_INCLUSIVE_INCLUSIVE) == Spanned.SPAN_INCLUSIVE_INCLUSIVE
            when {
                s == start && e == start -> true
                s < start && e > start  -> true
                e == start && inclusiveRight -> true
                else -> false
            }
        }
    } else {
        spans.any { span ->
            val s = text.getSpanStart(span)
            val e = text.getSpanEnd(span)
            s <= start && e >= end && predicate(span)
        }
    }
}


fun <T : CharacterStyle> toggleSpan(
    editText: EditText,
    spanClass: Class<T>,
    creator: () -> T,
    matcher: (T) -> Boolean = { true }
) {
    val text = editText.text ?: return
    val start = editText.selectionStart
    val end = editText.selectionEnd
    if (start < 0 || end < 0) return

    if (start == end) {
        val spans = text.getSpans(start, start, spanClass)
        val activeSpan = spans.firstOrNull(matcher)
        if (activeSpan != null) {
            val spanStart = text.getSpanStart(activeSpan)
            val spanEnd = text.getSpanEnd(activeSpan)
            text.removeSpan(activeSpan)
            if (spanStart < start) {
                text.setSpan(creator(), spanStart, start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            if (spanEnd > start) {
                text.setSpan(creator(), start, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        } else {
            text.setSpan(creator(), start, start, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        }
    } else {
        val spans = text.getSpans(start, end, spanClass)
        val matchingSpans = spans.filter(matcher)
        
        var isFullyStyled = false
        for (span in matchingSpans) {
            val s = text.getSpanStart(span)
            val e = text.getSpanEnd(span)
            if (s <= start && e >= end) {
                isFullyStyled = true
                break
            }
        }

        if (isFullyStyled) {
            for (span in matchingSpans) {
                val s = text.getSpanStart(span)
                val e = text.getSpanEnd(span)
                text.removeSpan(span)
                if (s < start) {
                    text.setSpan(creator(), s, start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                if (e > end) {
                    text.setSpan(creator(), end, e, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        } else {
            for (span in matchingSpans) {
                text.removeSpan(span)
            }
            text.setSpan(creator(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
}

fun toggleBlockquote(editText: EditText) {
    val text = editText.text ?: return
    val start = editText.selectionStart
    val end = editText.selectionEnd
    if (start < 0 || end < 0) return

    val (lineStart, lineEnd) = getLineBoundaries(text, start, end)

    val spans = text.getSpans(lineStart, lineEnd, android.text.style.QuoteSpan::class.java)
    if (spans.isNotEmpty()) {
        for (span in spans) {
            text.removeSpan(span)
        }
    } else {
        val brandColor = 0xFF6366F1.toInt()
        val quoteSpan = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            android.text.style.QuoteSpan(brandColor, 6, 24)
        } else {
            android.text.style.QuoteSpan(brandColor)
        }
        text.setSpan(quoteSpan, lineStart, lineEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}

fun getLineBoundaries(text: CharSequence, start: Int, end: Int): Pair<Int, Int> {
    var lineStart = start
    while (lineStart > 0 && text[lineStart - 1] != '\n') {
        lineStart--
    }
    var lineEnd = end
    while (lineEnd < text.length && text[lineEnd] != '\n') {
        lineEnd++
    }
    if (lineEnd < text.length && text[lineEnd] == '\n') {
        lineEnd++
    }
    return Pair(lineStart, lineEnd)
}

fun trimTrailingNewlines(s: CharSequence): Spannable {
    var end = s.length
    while (end > 0 && (s[end - 1] == '\n' || s[end - 1] == '\r')) {
        end--
    }
    val builder = android.text.SpannableStringBuilder(s)
    if (end < s.length) {
        builder.delete(end, s.length)
    }
    return builder
}

fun replaceQuoteSpans(spannable: Spannable) {
    val brandColor = 0xFF6366F1.toInt()
    spannable.getSpans(0, spannable.length, android.text.style.QuoteSpan::class.java).forEach { span ->
        val start = spannable.getSpanStart(span)
        val end = spannable.getSpanEnd(span)
        val flags = spannable.getSpanFlags(span)
        spannable.removeSpan(span)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            spannable.setSpan(
                android.text.style.QuoteSpan(brandColor, 6, 24),
                start, end, flags
            )
        } else {
            spannable.setSpan(
                android.text.style.QuoteSpan(brandColor),
                start, end, flags
            )
        }
    }
}

fun cleanSpannedForHtml(spanned: Spanned): Spanned {
    val builder = android.text.SpannableStringBuilder(spanned.toString())
    spanned.getSpans(0, spanned.length, Any::class.java).forEach { span ->
        val start = spanned.getSpanStart(span)
        val end = spanned.getSpanEnd(span)
        val flags = spanned.getSpanFlags(span)
        if ((flags and Spanned.SPAN_COMPOSING) != 0) {
            return@forEach
        }
        val isSupportedSpan = when (span) {
            is StyleSpan -> true
            is UnderlineSpan -> true
            is StrikethroughSpan -> true
            is android.text.style.QuoteSpan -> true
            is android.text.style.URLSpan -> true
            else -> false
        }
        if (isSupportedSpan) {
            builder.setSpan(span, start, end, flags)
        }
    }
    return builder
}

fun convertStrikethroughToSTag(html: String): String {
    var processed = html
    processed = processed.replace(
        Regex("<span\\s+style=\"[^\"]*text-decoration:\\s*line-through;?[^\"]*\"\\s*>(.*?)</span>", 
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        ), 
        "<s>$1</s>"
    )
    processed = processed.replace(
        Regex("<strike\\s*>(.*?)</strike>", 
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        ), 
        "<s>$1</s>"
    )
    processed = processed.replace(
        Regex("<del\\s*>(.*?)</del>", 
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        ), 
        "<s>$1</s>"
    )
    return processed
}
