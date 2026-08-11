package wombat.joshattic.us.wear

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import wombat.joshattic.us.wear.ui.utils.NetworkConnectivityObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import wombat.joshattic.us.wear.data.WearAuthPreferences
import wombat.joshattic.us.wear.data.WearRepository
import wombat.joshattic.us.wear.data.model.AuthSession
import wombat.joshattic.us.wear.data.model.Comment
import wombat.joshattic.us.wear.data.model.Notification
import wombat.joshattic.us.wear.data.model.Post

data class WearUiState(
    val session: AuthSession? = null,
    val isSessionLoaded: Boolean = false,
    val feed: List<Post> = emptyList(),
    val feedLoading: Boolean = false,
    val feedPage: Int = 1,
    val feedHasMore: Boolean = true,
    val notifications: List<Notification> = emptyList(),
    val notificationsLoading: Boolean = false,
    val selectedPost: Post? = null,
    val comments: List<Comment> = emptyList(),
    val commentsLoading: Boolean = false,
    val errorMessage: String? = null,
    val postSuccess: Boolean = false,
    val composeQuotePostId: String? = null,
    val showImages: Boolean = false,
    val showPfp: Boolean = true,
    val feedType: String = "Home",
    val isOnline: Boolean = true
)

class WearViewModel(
    private val repository: WearRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(WearUiState())
    val uiState: StateFlow<WearUiState> = _uiState.asStateFlow()
    private val settingsPreferences = wombat.joshattic.us.wear.data.WearSettingsPreferences(context)
    private val connectivityObserver = NetworkConnectivityObserver(context)

    init {
        // Query existing data on startup in case it was pushed before the app was running
        val dataClient = com.google.android.gms.wearable.Wearable.getDataClient(context)
        dataClient.dataItems.addOnSuccessListener { dataItems ->
            for (item in dataItems) {
                if (item.uri.path == wombat.joshattic.us.wear.data.WearAuthListenerService.PATH_AUTH) {
                    val dataMap = com.google.android.gms.wearable.DataMapItem.fromDataItem(item).dataMap
                    val token = dataMap.getString(wombat.joshattic.us.wear.data.WearAuthListenerService.KEY_TOKEN, "")
                    val username = dataMap.getString(wombat.joshattic.us.wear.data.WearAuthListenerService.KEY_USERNAME, "")
                    if (!token.isNullOrEmpty() && !username.isNullOrEmpty()) {
                        viewModelScope.launch {
                            wombat.joshattic.us.wear.data.WearAuthPreferences(context).saveSession(token, username)
                        }
                    }
                } else if (item.uri.path == wombat.joshattic.us.wear.data.WearAuthListenerService.PATH_SETTINGS) {
                    val dataMap = com.google.android.gms.wearable.DataMapItem.fromDataItem(item).dataMap
                    val showImages = dataMap.getBoolean(wombat.joshattic.us.wear.data.WearAuthListenerService.KEY_SHOW_IMAGES, false)
                    val showPfp = dataMap.getBoolean(wombat.joshattic.us.wear.data.WearAuthListenerService.KEY_SHOW_PFP, true)
                    val feedType = dataMap.getString(wombat.joshattic.us.wear.data.WearAuthListenerService.KEY_FEED_TYPE, "Home")
                    viewModelScope.launch {
                        settingsPreferences.saveSettings(showImages, showPfp, feedType)
                    }
                }
            }
        }

        // Observe session changes from the Data Layer listener
        viewModelScope.launch {
            repository.sessionFlow.collect { session ->
                _uiState.update { it.copy(session = session, isSessionLoaded = true) }
                loadFeed(refresh = true)
                loadNotifications()
            }
        }

        // Observe settings changes
        viewModelScope.launch {
            settingsPreferences.showImages.collect { value ->
                _uiState.update { it.copy(showImages = value) }
            }
        }
        viewModelScope.launch {
            settingsPreferences.showPfp.collect { value ->
                _uiState.update { it.copy(showPfp = value) }
            }
        }
        viewModelScope.launch {
            settingsPreferences.feedType.collect { value ->
                val prevFeedType = _uiState.value.feedType
                _uiState.update { it.copy(feedType = value) }
                if (prevFeedType != value) {
                    loadFeed(refresh = true)
                }
            }
        }

        // Observe connectivity changes
        viewModelScope.launch {
            connectivityObserver.isConnected.collect { isConnected ->
                _uiState.update { state -> state.copy(isOnline = isConnected) }
                if (isConnected) {
                    loadFeed(refresh = true)
                }
            }
        }
    }

    fun loadFeed(refresh: Boolean = false) {
        viewModelScope.launch {
            val isExplore = _uiState.value.feedType == "Explore"
            val page = if (refresh) 1 else _uiState.value.feedPage
            val session = _uiState.value.session
            _uiState.update { it.copy(feedLoading = true) }
            runCatching {
                if (isExplore) {
                    repository.loadTrendingFeed(session)
                } else {
                    repository.loadFeed(session, page)
                }
            }.onSuccess { posts ->
                _uiState.update { state ->
                    val newFeed = if (refresh) posts else state.feed + posts
                    state.copy(
                        feed = newFeed,
                        feedLoading = false,
                        feedPage = if (isExplore) 1 else page + 1,
                        feedHasMore = if (isExplore) false else posts.isNotEmpty()
                    )
                }
                // Augment love statuses asynchronously in background so feed loads instantly
                if (session != null && posts.isNotEmpty()) {
                    viewModelScope.launch {
                        val augmentedPosts = augmentLoveStatuses(posts, session)
                        _uiState.update { state ->
                            val updatedMap = augmentedPosts.associateBy { it.id }
                            val updatedFeed = state.feed.map { post -> updatedMap[post.id] ?: post }
                            state.copy(feed = updatedFeed)
                        }
                    }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(feedLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun openPost(post: Post) {
        _uiState.update { it.copy(selectedPost = post, comments = emptyList()) }
        viewModelScope.launch {
            _uiState.update { it.copy(commentsLoading = true) }
            
            val session = _uiState.value.session
            if (session != null && post.isLoving == null) {
                runCatching { augmentLoveStatuses(listOf(post), session).first() }
                    .onSuccess { augmentedPost ->
                        _uiState.update { state ->
                            if (state.selectedPost?.id == post.id) state.copy(selectedPost = augmentedPost) else state
                        }
                    }
            }

            runCatching {
                repository.loadComments(_uiState.value.session, post.id)
            }.onSuccess { comments ->
                _uiState.update { it.copy(comments = comments, commentsLoading = false) }
            }.onFailure { e ->
                _uiState.update { it.copy(commentsLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun closePost() = _uiState.update { it.copy(selectedPost = null, comments = emptyList()) }

    fun toggleLove(post: Post) {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            runCatching { repository.toggleLove(session, post.id) }
                .onSuccess { response ->
                    _uiState.update { state ->
                        val updatedFeed = state.feed.map { p ->
                            if (p.id == post.id) p.copy(
                                isLoving = response.new.isLoving,
                                loves = response.new.loves
                            ) else p
                        }
                        val updatedSelectedPost = if (state.selectedPost?.id == post.id) {
                            state.selectedPost.copy(
                                isLoving = response.new.isLoving,
                                loves = response.new.loves
                            )
                        } else state.selectedPost

                        state.copy(
                            feed = updatedFeed,
                            selectedPost = updatedSelectedPost
                        )
                    }
                }
        }
    }

    fun submitPost(text: String) {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            runCatching { repository.createPost(session, "<p>$text</p>") }
                .onSuccess {
                    _uiState.update { it.copy(postSuccess = true) }
                    loadFeed(refresh = true)
                }.onFailure { e ->
                    _uiState.update { it.copy(errorMessage = e.message) }
                }
        }
    }

    fun submitRepost(postId: String) {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            runCatching { repository.createRepost(session, postId) }
                .onSuccess {
                    _uiState.update { it.copy(postSuccess = true) }
                    loadFeed(refresh = true)
                }.onFailure { e ->
                    _uiState.update { it.copy(errorMessage = e.message) }
                }
        }
    }

    fun submitQuote(postId: String, text: String) {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            runCatching { repository.createQuote(session, postId, "<p>$text</p>") }
                .onSuccess {
                    _uiState.update { it.copy(postSuccess = true) }
                    loadFeed(refresh = true)
                }.onFailure { e ->
                    _uiState.update { it.copy(errorMessage = e.message) }
                }
        }
    }

    fun submitComment(postId: String, text: String, parentId: String? = null) {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            runCatching { 
                repository.createComment(session, postId, text, parentId)
                val id = _uiState.value.selectedPost?.id ?: return@runCatching
                val comments = repository.loadComments(session, id)
                _uiState.update { it.copy(comments = comments) }
            }.onFailure { e ->
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun loadNotifications() {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(notificationsLoading = true) }
            runCatching { repository.loadUnreadNotifications(session) }
                .onSuccess { notifs ->
                    _uiState.update { it.copy(notifications = notifs, notificationsLoading = false) }
                }.onFailure { e ->
                    _uiState.update { it.copy(notificationsLoading = false, errorMessage = e.message) }
                }
        }
    }

    fun markNotificationRead(id: String) {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            runCatching { repository.markNotificationsRead(session, listOf(id)) }
            _uiState.update { it.copy(notifications = it.notifications.filter { n -> n.id != id }) }
        }
    }

    fun clearError() = _uiState.update { it.copy(errorMessage = null) }
    fun clearPostSuccess() = _uiState.update { it.copy(postSuccess = false) }
    fun setComposeQuote(postId: String?) = _uiState.update { it.copy(composeQuotePostId = postId) }

    private suspend fun augmentLoveStatuses(posts: List<Post>, session: AuthSession): List<Post> = kotlinx.coroutines.coroutineScope {
        posts.map { post ->
            async(Dispatchers.IO) {
                val loved = runCatching {
                    repository.getPostLoveStatus(session, post.id, session.username)
                }.getOrDefault(post.isLoving ?: false)
                
                val repostLoved = if (post.repost != null) {
                    runCatching {
                        repository.getPostLoveStatus(session, post.repost.id, session.username)
                    }.getOrDefault(post.repost.isLoving ?: false)
                } else null
                
                var p = post.copy(isLoving = loved)
                if (repostLoved != null) {
                    p = p.copy(repost = p.repost?.copy(isLoving = repostLoved))
                }
                p
            }
        }.awaitAll()
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory {
            val authPrefs = WearAuthPreferences(context.applicationContext)
            val repo = WearRepository(
                api = wombat.joshattic.us.wear.data.network.RetrofitClient.apiService,
                authPreferences = authPrefs
            )
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    WearViewModel(repo, context.applicationContext) as T
            }
        }
    }
}
