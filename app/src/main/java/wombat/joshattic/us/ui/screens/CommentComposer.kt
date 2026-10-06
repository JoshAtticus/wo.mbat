package wombat.joshattic.us.ui.screens

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.Gravity
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import coil.compose.SubcomposeAsyncImage
import kotlinx.coroutines.launch

@Composable
fun CommentComposer(
    draft: String,
    onDraftChange: (String) -> Unit,
    onSubmit: () -> Unit,
    placeholder: String,
    onUploadImage: (suspend (Uri) -> String)? = null,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var richEditTextRef by remember { mutableStateOf<RichEditText?>(null) }
    var lastSyncedDraft by remember { mutableStateOf("") }
    var currentImages by remember { mutableStateOf(extractImages(draft)) }
    var isUploadingImage by remember { mutableStateOf(false) }
    var uploadCurrent by remember { mutableStateOf(0) }
    var uploadTotal by remember { mutableStateOf(0) }
    var imageUploadError by remember { mutableStateOf<String?>(null) }
    var isBoldActive by remember { mutableStateOf(false) }
    var isItalicActive by remember { mutableStateOf(false) }
    var isStrikethroughActive by remember { mutableStateOf(false) }
    var isUnderlineActive by remember { mutableStateOf(false) }
    var editorScrollY by remember { mutableStateOf(0) }
    var editorContentHeight by remember { mutableStateOf(0) }
    var editorHeight by remember { mutableStateOf(0) }
    val plainDraft = HtmlCompat.fromHtml(stripImages(draft), HtmlCompat.FROM_HTML_MODE_COMPACT).toString()
    val maxCharacterCount = 12_000

    fun updateFormattingStates(editText: android.widget.EditText) {
        val text = editText.text ?: return
        val start = editText.selectionStart
        val end = editText.selectionEnd
        if (start < 0 || end < 0) return
        isBoldActive = hasSpan(text, start, end, StyleSpan::class.java) { it.style == Typeface.BOLD }
        isItalicActive = hasSpan(text, start, end, StyleSpan::class.java) { it.style == Typeface.ITALIC }
        isStrikethroughActive = hasSpan(text, start, end, StrikethroughSpan::class.java)
        isUnderlineActive = hasSpan(text, start, end, UnderlineSpan::class.java)
    }

    fun updateDraft(editText: android.widget.EditText, images: List<String>) {
        val text = editText.text ?: return
        val textHtml = convertStrikethroughToSTag(
            HtmlCompat.toHtml(cleanSpannedForHtml(text), HtmlCompat.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL)
        )
        val imgTags = images.joinToString("\n") { "<img src=\"$it\" alt=\"\">" }
        val combinedHtml = if (imgTags.isBlank()) textHtml else "$textHtml\n$imgTags"
        lastSyncedDraft = combinedHtml
        onDraftChange(combinedHtml)
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 8)
    ) { uris ->
        val upload = onUploadImage
        if (uris.isNotEmpty() && upload != null) {
            isUploadingImage = true
            imageUploadError = null
            uploadTotal = uris.size
            uploadCurrent = 0
            coroutineScope.launch {
                try {
                    uris.forEachIndexed { index, uri ->
                        uploadCurrent = index + 1
                        val uploadedUrl = upload(uri)
                        currentImages = currentImages + uploadedUrl
                        richEditTextRef?.let { updateDraft(it, currentImages) }
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
                    richEditTextRef?.let { editText ->
                        toggleSpan(editText, StyleSpan::class.java, { StyleSpan(Typeface.BOLD) }, { it.style == Typeface.BOLD })
                        updateFormattingStates(editText)
                    }
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
                    richEditTextRef?.let { editText ->
                        toggleSpan(editText, StyleSpan::class.java, { StyleSpan(Typeface.ITALIC) }, { it.style == Typeface.ITALIC })
                        updateFormattingStates(editText)
                    }
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
                    richEditTextRef?.let { editText ->
                        toggleSpan(editText, StrikethroughSpan::class.java, { StrikethroughSpan() })
                        updateFormattingStates(editText)
                    }
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
                    richEditTextRef?.let { editText ->
                        toggleSpan(editText, UnderlineSpan::class.java, { UnderlineSpan() })
                        updateFormattingStates(editText)
                    }
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
                        imagePickerLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
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
                    Box(modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp))) {
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
                                richEditTextRef?.let { updateDraft(it, updatedImages) }
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
        val scrollThumbColor = MaterialTheme.colorScheme.primary
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
                AndroidView(
                factory = { ctx ->
                    RichEditText(ctx).apply {
                        richEditTextRef = this
                        setBackground(null)
                        setGravity(android.view.Gravity.TOP or android.view.Gravity.START)
                        inputType = android.text.InputType.TYPE_CLASS_TEXT or
                            android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                            android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                        setHorizontallyScrolling(false)
                        setScrollContainer(true)
                        setOnScrollChangeListener { view, _, scrollY, _, _ ->
                            editorScrollY = scrollY
                            editorContentHeight = (view as RichEditText).verticalScrollRange()
                            editorHeight = view.height
                        }
                        setOnTouchListener { view, event ->
                            when (event.actionMasked) {
                                android.view.MotionEvent.ACTION_DOWN,
                                android.view.MotionEvent.ACTION_MOVE ->
                                    view.parent?.requestDisallowInterceptTouchEvent(true)
                                android.view.MotionEvent.ACTION_UP -> {
                                    view.parent?.requestDisallowInterceptTouchEvent(true)
                                    view.postDelayed({
                                        view.parent?.requestDisallowInterceptTouchEvent(false)
                                    }, 250L)
                                }
                                android.view.MotionEvent.ACTION_CANCEL ->
                                    view.parent?.requestDisallowInterceptTouchEvent(false)
                            }
                            false
                        }
                        hint = placeholder
                        setHintTextColor(textColor.copy(alpha = 0.6f).toArgb())
                        setTextColor(textColor.toArgb())
                        textSize = 15f
                        layoutParams = android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                        addTextChangedListener(object : android.text.TextWatcher {
                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                                updateDraft(this@apply, currentImages)
                                post {
                                    editorContentHeight = verticalScrollRange()
                                    editorHeight = height
                                    editorScrollY = scrollY
                                }
                            }
                            override fun afterTextChanged(s: android.text.Editable?) {}
                        })
                        onSelectionChangedListener = { _, _ -> updateFormattingStates(this) }
                    }
                },
                update = { editText ->
                    if (draft != lastSyncedDraft) {
                        lastSyncedDraft = draft
                        currentImages = extractImages(draft)
                        val spanned = HtmlCompat.fromHtml(stripImages(draft), HtmlCompat.FROM_HTML_MODE_COMPACT)
                        val cleanSpanned = trimTrailingNewlines(spanned)
                        if (editText.text.toString() != cleanSpanned.toString()) {
                            editText.setText(cleanSpanned)
                            editText.setSelection(cleanSpanned.length)
                        }
                        updateFormattingStates(editText)
                        editorContentHeight = editText.verticalScrollRange()
                        editorHeight = editText.height
                        editorScrollY = editText.scrollY
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            )
                if (editorContentHeight > editorHeight && editorHeight > 0) {
                    Canvas(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .fillMaxHeight()
                            .padding(vertical = 10.dp)
                            .width(4.dp)
                    ) {
                        val scrollableHeight = editorContentHeight - editorHeight
                        if (size.height > 0f && scrollableHeight > 0) {
                            val thumbHeight = (size.height * editorHeight / editorContentHeight)
                                .coerceAtLeast(18f)
                                .coerceAtMost(size.height)
                            val thumbOffset = ((size.height - thumbHeight) * editorScrollY / scrollableHeight)
                                .coerceIn(0f, (size.height - thumbHeight).coerceAtLeast(0f))
                            drawRoundRect(
                                color = scrollThumbColor,
                                topLeft = Offset(0f, thumbOffset),
                                size = Size(size.width, thumbHeight),
                                cornerRadius = CornerRadius(size.width / 2f)
                            )
                        }
                    }
                }
                FilledIconButton(
                    onClick = onSubmit,
                    enabled = draft.isNotBlank() && plainDraft.length <= maxCharacterCount,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .padding(bottom = 8.dp)
                        .size(40.dp)
                ) {
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
