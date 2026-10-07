@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package wombat.joshattic.us.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import wombat.joshattic.us.R
import wombat.joshattic.us.data.network.IndexApi
import wombat.joshattic.us.data.network.WasteOfIndexSearchResponse
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ADVANCED_SEARCH_KINDS = listOf("post", "comment", "wall", "user")
private val ADVANCED_SEARCH_SORTS = listOf("newest", "oldest", "loves", "comments")

data class AdvancedSearchFilters(
    val query: String = "",
    val kind: String = "post",
    val user: String = "",
    val sort: String = "newest",
    val minLoves: String = "0",
    val after: Long = 0,
    val before: Long = 0,
    val hasMedia: Boolean = false,
    val hasImages: Boolean = false,
    val hasLinks: Boolean = false,
    val isEdited: Boolean = false,
    val isLocked: Boolean = false,
    val includeDeleted: Boolean = false,
    val regex: Boolean = false
)

class AdvancedSearchViewModel : ViewModel() {
    private val _filters = MutableStateFlow(AdvancedSearchFilters())
    val filters: StateFlow<AdvancedSearchFilters> = _filters

    var results = MutableStateFlow<List<JsonObject>>(emptyList())
        private set
    var resultKind = MutableStateFlow("post")
        private set
    var loading = MutableStateFlow(false)
        private set
    var loadingMore = MutableStateFlow(false)
        private set
    var errorMessage = MutableStateFlow<String?>(null)
        private set
    var showingResults = MutableStateFlow(false)
        private set
    var searched = MutableStateFlow(false)
        private set

    private var lastFilters = AdvancedSearchFilters()
    private var page = 0
    private var exhausted = false

    fun updateFilters(transform: (AdvancedSearchFilters) -> AdvancedSearchFilters) {
        _filters.value = transform(_filters.value)
    }

    fun search() {
        val f = _filters.value
        if (f.query.isBlank() && f.user.isBlank()) return
        lastFilters = f
        page = 0
        exhausted = false
        loading.value = true
        errorMessage.value = null
        showingResults.value = true
        viewModelScope.launch {
            runCatching {
                IndexApi.service.advancedSearch(
                    q = f.query,
                    kind = f.kind,
                    user = f.user.trim(),
                    sort = f.sort,
                    minLoves = f.minLoves.toIntOrNull() ?: 0,
                    after = f.after,
                    before = f.before,
                    hasMedia = f.hasMedia,
                    hasImages = f.hasImages,
                    hasLinks = f.hasLinks,
                    isEdited = f.isEdited,
                    isLocked = f.isLocked,
                    deleted = f.includeDeleted,
                    regex = f.regex,
                    page = 0,
                    limit = 50
                )
            }
                .onSuccess { response ->
                    results.value = response.results ?: emptyList()
                    resultKind.value = response.kind ?: f.kind
                    searched.value = true
                }
                .onFailure {
                    errorMessage.value = "Search failed: ${it.message ?: "unknown error"}"
                    showingResults.value = false
                }
            loading.value = false
        }
    }

    fun loadNextPage() {
        if (loading.value || loadingMore.value || exhausted) return
        loadingMore.value = true
        val nextPage = page + 1
        val f = lastFilters
        viewModelScope.launch {
            runCatching {
                IndexApi.service.advancedSearch(
                    q = f.query,
                    kind = f.kind,
                    user = f.user.trim(),
                    sort = f.sort,
                    minLoves = f.minLoves.toIntOrNull() ?: 0,
                    after = f.after,
                    before = f.before,
                    hasMedia = f.hasMedia,
                    hasImages = f.hasImages,
                    hasLinks = f.hasLinks,
                    isEdited = f.isEdited,
                    isLocked = f.isLocked,
                    deleted = f.includeDeleted,
                    regex = f.regex,
                    page = nextPage,
                    limit = 50
                )
            }
                .onSuccess { response ->
                    val newResults = response.results ?: emptyList()
                    if (newResults.isEmpty()) exhausted = true
                    results.value = results.value + newResults
                    page = nextPage
                }
                .onFailure { exhausted = true }
            loadingMore.value = false
        }
    }

    fun backToForm() {
        showingResults.value = false
    }
}

@Composable
fun AdvancedSearchScreen(
    onClose: () -> Unit,
    onOpenPostById: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    vm: AdvancedSearchViewModel = viewModel()
) {
    val filters by vm.filters.collectAsStateIsh()
    val results by vm.results.collectAsStateIsh()
    val resultKind by vm.resultKind.collectAsStateIsh()
    val loading by vm.loading.collectAsStateIsh()
    val loadingMore by vm.loadingMore.collectAsStateIsh()
    val errorMessage by vm.errorMessage.collectAsStateIsh()
    val showingResults by vm.showingResults.collectAsStateIsh()

    // Back first closes the results, then the whole screen
    BackHandler(enabled = showingResults) { vm.backToForm() }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (showingResults) {
            AdvancedSearchResults(
                results = results,
                kind = resultKind,
                loading = loading,
                loadingMore = loadingMore,
                errorMessage = errorMessage,
                onBack = { vm.backToForm() },
                onLoadNext = vm::loadNextPage,
                onOpenPostById = onOpenPostById,
                onProfileClick = onProfileClick
            )
        } else {
            AdvancedSearchForm(
                filters = filters,
                errorMessage = errorMessage,
                onFiltersChange = vm::updateFilters,
                onSearch = vm::search,
                onClose = onClose
            )
        }
    }
}

// Small helper so the ViewModel flows can be read with plain delegate syntax
@Composable
private fun <T> StateFlow<T>.collectAsStateIsh(): State<T> = collectAsStateWithLifecycle()

@Composable
private fun AdvancedSearchForm(
    filters: AdvancedSearchFilters,
    errorMessage: String?,
    onFiltersChange: ((AdvancedSearchFilters) -> AdvancedSearchFilters) -> Unit,
    onSearch: () -> Unit,
    onClose: () -> Unit
) {
    val scroll = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Advanced search",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scroll)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = filters.query,
                onValueChange = { q -> onFiltersChange { it.copy(query = q) } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Query") },
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge
            )

            AdvancedSearchDropdown(
                label = "Kind",
                selected = filters.kind,
                entries = ADVANCED_SEARCH_KINDS.associateWith { kindLabel(it) },
                onSelect = { k -> onFiltersChange { it.copy(kind = k) } }
            )

            OutlinedTextField(
                value = filters.user,
                onValueChange = { u -> onFiltersChange { it.copy(user = u) } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("By user") },
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge
            )

            AdvancedSearchDropdown(
                label = "Sort",
                selected = filters.sort,
                entries = ADVANCED_SEARCH_SORTS.associateWith { sortLabel(it) },
                onSelect = { s -> onFiltersChange { it.copy(sort = s) } }
            )

            OutlinedTextField(
                value = filters.minLoves,
                onValueChange = { v -> onFiltersChange { it.copy(minLoves = v.filter { c -> c.isDigit() }) } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Min loves") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = MaterialTheme.shapes.extraLarge
            )

            AdvancedSearchDateField(
                label = "After date",
                millis = filters.after,
                onChange = { m -> onFiltersChange { it.copy(after = m) } }
            )

            AdvancedSearchDateField(
                label = "Before date",
                millis = filters.before,
                onChange = { m -> onFiltersChange { it.copy(before = m) } }
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AdvancedSearchChip("Has media", filters.hasMedia) { v -> onFiltersChange { it.copy(hasMedia = v) } }
                AdvancedSearchChip("Has images", filters.hasImages) { v -> onFiltersChange { it.copy(hasImages = v) } }
                AdvancedSearchChip("Has links", filters.hasLinks) { v -> onFiltersChange { it.copy(hasLinks = v) } }
                AdvancedSearchChip("Is edited", filters.isEdited) { v -> onFiltersChange { it.copy(isEdited = v) } }
                AdvancedSearchChip("Is locked", filters.isLocked) { v -> onFiltersChange { it.copy(isLocked = v) } }
                AdvancedSearchChip("Include deleted", filters.includeDeleted) { v -> onFiltersChange { it.copy(includeDeleted = v) } }
                AdvancedSearchChip("Regex", filters.regex) { v -> onFiltersChange { it.copy(regex = v) } }
            }

            if (errorMessage != null) {
                Text(
                    errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Search button pinned to the bottom
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 3.dp
        ) {
            Button(
                onClick = onSearch,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .padding(bottom = 4.dp)
                    .height(52.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Search", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AdvancedSearchChip(label: String, selected: Boolean, onToggle: (Boolean) -> Unit) {
    FilterChip(
        selected = selected,
        onClick = { onToggle(!selected) },
        label = { Text(label) },
        shape = MaterialTheme.shapes.extraLarge,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
private fun AdvancedSearchDropdown(
    label: String,
    selected: String,
    entries: Map<String, String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = entries[selected] ?: selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = MaterialTheme.shapes.extraLarge
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            entries.forEach { (value, display) ->
                DropdownMenuItem(
                    text = { Text(display) },
                    onClick = {
                        onSelect(value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun AdvancedSearchDateField(label: String, millis: Long, onChange: (Long) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    val display = if (millis > 0) {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(millis))
    } else ""

    if (showPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = if (millis > 0) millis else null)
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onChange(state.selectedDateMillis ?: 0)
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = state)
        }
    }

    OutlinedTextField(
        value = display,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        placeholder = { Text("dd/mm/yyyy") },
        trailingIcon = {
            IconButton(onClick = { showPicker = true }) {
                Icon(Icons.Filled.CalendarToday, contentDescription = "Pick date")
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showPicker = true },
        shape = MaterialTheme.shapes.extraLarge
    )
}

@Composable
private fun AdvancedSearchResults(
    results: List<JsonObject>,
    kind: String,
    loading: Boolean,
    loadingMore: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onLoadNext: () -> Unit,
    onOpenPostById: (String) -> Unit,
    onProfileClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column {
                Text(
                    "Results",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    kindLabel(kind),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }
            results.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No results",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 12.dp,
                    vertical = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(results, key = {
                    (it.get("id")?.asStringOrNull() ?: it.get("name")?.asStringOrNull() ?: it.hashCode().toString())
                }) { item ->
                    when (kind) {
                        "user" -> AdvancedSearchUserCard(item) { onProfileClick(it) }
                        else -> AdvancedSearchPostCard(
                            item = item,
                            isWall = kind == "wall",
                            onClick = {
                                val postId = item.get("post_id")?.asStringOrNull() ?: item.get("id")?.asStringOrNull()
                                if (postId != null) onOpenPostById(postId)
                            }
                        )
                    }
                }
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (loadingMore) {
                            CircularProgressIndicator(modifier = Modifier.height(28.dp))
                        } else {
                            TextButton(onClick = onLoadNext) { Text("Load more") }
                        }
                    }
                }
            }
        }
    }
}

private fun com.google.gson.JsonElement.asStringOrNull(): String? =
    if (isJsonNull) null else try { asString } catch (_: Exception) { null }

private fun kindLabel(kind: String) = when (kind) {
    "post" -> "Posts"
    "comment" -> "Comments"
    "wall" -> "Wall comments"
    "user" -> "Users"
    else -> kind
}

private fun sortLabel(sort: String) = when (sort) {
    "newest" -> "Newest"
    "oldest" -> "Oldest"
    "loves" -> "Most loved"
    "comments" -> "Most commented"
    else -> sort
}

@Composable
private fun AdvancedSearchUserCard(user: JsonObject, onClick: (String) -> Unit) {
    val name = user.get("name")?.asStringOrNull() ?: return
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(name) },
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            ProfilePicture(username = name, size = 44.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "@$name",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                user.get("bio")?.asStringOrNull()?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
                Text(
                    "${user.get("followers")?.asInt ?: 0} followers",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AdvancedSearchPostCard(item: JsonObject, isWall: Boolean, onClick: () -> Unit) {
    val poster = item.get("poster")?.asStringOrNull() ?: "unknown"
    val content = item.get("content")?.asStringOrNull() ?: ""
    val time = item.get("time")?.asLong ?: 0
    val loves = item.get("loves")?.asInt ?: 0
    val comments = item.get("comments")?.asInt ?: 0
    val isDeleted = item.get("deleted")?.let { !it.isJsonNull && (it.asBoolean || it.asInt == 1) } ?: false

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfilePicture(username = poster, size = 36.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (isWall) "@$poster → wall" else "@$poster",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (time > 0) {
                        Text(
                            SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(time)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (isDeleted) {
                    Text(
                        "deleted",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            if (content.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                HtmlText(
                    html = content,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 10
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "♥ $loves",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (kindHasComments(item)) {
                    Text(
                        "💬 $comments",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun kindHasComments(item: JsonObject): Boolean = item.get("comments") != null
