package wombat.joshattic.us.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import wombat.joshattic.us.data.model.AuthSession
import wombat.joshattic.us.data.model.Comment
import wombat.joshattic.us.data.model.CommentResponse
import wombat.joshattic.us.data.model.Post
import wombat.joshattic.us.data.repository.WombatRepository
import wombat.joshattic.us.ui.state.BottomTab
import wombat.joshattic.us.ui.state.HomeUiState

class HomeViewModel(
    private val repository: WombatRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeSessionAndRefresh()
    }

    fun selectTab(tab: BottomTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
        refreshForSelectedTab(tab)
    }

    fun setLoginUsername(username: String) {
        _uiState.value = _uiState.value.copy(loginUsername = username, loginError = null)
    }

    fun setLoginPassword(password: String) {
        _uiState.value = _uiState.value.copy(loginPassword = password, loginError = null)
    }

    fun login() {
        val snapshot = _uiState.value
        if (snapshot.loginUsername.isBlank() || snapshot.loginPassword.isBlank()) {
            _uiState.value = snapshot.copy(loginError = "Enter both username and password.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(authLoading = true, loginError = null)
            repository.login(snapshot.loginUsername.trim(), snapshot.loginPassword)
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        authLoading = false,
                        loginError = throwable.message ?: "Unable to sign in"
                    )
                }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        authLoading = false,
                        loginPassword = ""
                    )
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun toggleComposer() {
        val nextVisible = !_uiState.value.showComposer
        _uiState.value = _uiState.value.copy(
            showComposer = nextVisible,
            selectedPost = if (nextVisible) null else _uiState.value.selectedPost
        )
    }

    fun setComposeDraft(draft: String) {
        _uiState.value = _uiState.value.copy(composeDraft = draft, errorMessage = null)
    }

    fun submitPost() {
        viewModelScope.launch {
            val session = _uiState.value.session ?: return@launch
            val draft = _uiState.value.composeDraft.trim()
            if (draft.isBlank()) {
                _uiState.value = _uiState.value.copy(errorMessage = "Write something before posting.")
                return@launch
            }

            runCatching { repository.createPost(session, draft) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(showComposer = false, composeDraft = "")
                    refreshFeed()
                    refreshAccount()
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(errorMessage = throwable.message)
                }
        }
    }

    fun refreshFeed() {
        viewModelScope.launch {
            loadFeedForCurrentSession(_uiState.value.session)
        }
    }

    fun setExploreQuery(query: String) {
        // no longer used for search in explore; kept for compatibility if needed elsewhere
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun loadExploreTrending() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(exploreTrendingLoading = true, errorMessage = null)
            val currentSession = _uiState.value.session
            runCatching {
                val response = repository.loadTrendingPosts(currentSession)
                if (currentSession != null) augmentLoveStatuses(response.posts, currentSession) else response.posts
            }.onSuccess { posts ->
                _uiState.value = _uiState.value.copy(
                    exploreTrendingPosts = posts,
                    exploreTrendingLoading = false
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    exploreTrendingLoading = false,
                    errorMessage = throwable.message ?: "Unable to load trending"
                )
            }
        }
    }

    fun openProfile(username: String) {
        _uiState.value = _uiState.value.copy(
            viewingProfileUsername = username,
            viewingProfile = null,
            viewingProfilePosts = emptyList(),
            viewingProfileLoading = true
        )
        viewModelScope.launch {
            val currentSession = _uiState.value.session
            runCatching {
                val profile = repository.loadUserProfile(username)
                val rawPosts = repository.loadUserPosts(currentSession, username).posts
                val posts = if (currentSession != null) augmentLoveStatuses(rawPosts, currentSession) else rawPosts
                profile to posts
            }.onSuccess { (profile, posts) ->
                _uiState.value = _uiState.value.copy(
                    viewingProfile = profile,
                    viewingProfilePosts = posts,
                    viewingProfileLoading = false
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    viewingProfileLoading = false,
                    errorMessage = throwable.message ?: "Unable to load profile"
                )
            }
        }
    }

    fun closeProfile() {
        _uiState.value = _uiState.value.copy(
            viewingProfileUsername = null,
            viewingProfile = null,
            viewingProfilePosts = emptyList(),
            viewingProfileLoading = false
        )
    }

    fun openPost(post: Post) {
        _uiState.value = _uiState.value.copy(
            selectedPost = post,
            showComposer = false,
            commentDraft = "",
            comments = emptyList(),
            commentsLoading = false,
            commentReplyParent = null
        )
        // Comments are loaded lazily when user swipes up in the details sheet to expand
    }

    fun closePost() {
        _uiState.value = _uiState.value.copy(
            selectedPost = null,
            comments = emptyList(),
            commentDraft = "",
            commentsLoading = false,
            commentReplyParent = null
        )
    }

    fun setCommentDraft(comment: String) {
        _uiState.value = _uiState.value.copy(commentDraft = comment, errorMessage = null)
    }

    fun submitComment() {
        viewModelScope.launch {
            val session = _uiState.value.session ?: return@launch
            val selectedPost = _uiState.value.selectedPost ?: return@launch
            val draft = _uiState.value.commentDraft.trim()
            if (draft.isBlank()) {
                _uiState.value = _uiState.value.copy(errorMessage = "Write a reply before sending it.")
                return@launch
            }

            val parent = _uiState.value.commentReplyParent?.id
            runCatching { repository.createComment(session, selectedPost.id, draft, parent) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        commentDraft = "",
                        commentReplyParent = null
                    )
                    loadComments(selectedPost.id)
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(errorMessage = throwable.message ?: "Unable to post comment")
                }
        }
    }

    fun refreshNotifications() {
        viewModelScope.launch {
            loadNotifications(_uiState.value.session)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            val session = _uiState.value.session ?: return@launch
            val notificationIds = _uiState.value.unreadNotifications.map { it.id }
            repository.markNotificationsRead(session, notificationIds)
            loadNotifications(session)
        }
    }

    fun refreshAccount() {
        viewModelScope.launch {
            loadAccountProfile(_uiState.value.session)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun saveCurrentDraft() {
        val draft = _uiState.value.composeDraft.trim()
        if (draft.isBlank()) return
        val currentDrafts = _uiState.value.composerDrafts
        if (!currentDrafts.contains(draft)) {
            _uiState.value = _uiState.value.copy(
                composerDrafts = listOf(draft) + currentDrafts
            )
        }
    }

    fun restoreDraft(draft: String) {
        _uiState.value = _uiState.value.copy(composeDraft = draft)
    }

    fun deleteDraft(draft: String) {
        _uiState.value = _uiState.value.copy(
            composerDrafts = _uiState.value.composerDrafts.filter { it != draft }
        )
    }

    fun loadCommentsForCurrentPost() {
        val post = _uiState.value.selectedPost ?: return
        if (_uiState.value.comments.isEmpty()) {
            viewModelScope.launch {
                loadComments(post.id)
            }
        }
    }

    fun clearComments() {
        _uiState.value = _uiState.value.copy(comments = emptyList(), commentsLoading = false, commentReplyParent = null)
    }

    fun setCommentReplyParent(comment: Comment?) {
        _uiState.value = _uiState.value.copy(commentReplyParent = comment)
    }

    fun togglePostLove(post: Post) {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            runCatching { repository.toggleLove(session, post.id) }
                .onSuccess { response ->
                    val newLoves = response.new.loves
                    val newIsLoving = response.new.isLoving
                    updatePostsWithLove(post.id, newLoves, newIsLoving)
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = throwable.message ?: "Failed to toggle love"
                    )
                }
        }
    }

    private fun updatePostsWithLove(postId: String, newLoves: Int, newIsLoving: Boolean) {
        val current = _uiState.value
        fun transform(p: Post) = if (p.id == postId) p.copy(loves = newLoves, isLoving = newIsLoving) else p
        _uiState.value = current.copy(
            feed = current.feed.map(::transform),
            exploreTrendingPosts = current.exploreTrendingPosts.map(::transform),
            accountPosts = current.accountPosts.map(::transform),
            viewingProfilePosts = current.viewingProfilePosts.map(::transform),
            selectedPost = if (current.selectedPost?.id == postId) transform(current.selectedPost) else current.selectedPost
        )
    }

    private suspend fun augmentLoveStatuses(posts: List<Post>, session: AuthSession): List<Post> = coroutineScope {
        posts.map { post ->
            async {
                val loved = runCatching {
                    repository.getPostLoveStatus(session, post.id, session.username)
                }.getOrDefault(post.isLoving ?: false)
                post.copy(isLoving = loved)
            }
        }.map { it.await() }
    }

    private fun observeSessionAndRefresh() {
        viewModelScope.launch {
            repository.sessionFlow
                .catch { throwable ->
                    _uiState.value = _uiState.value.copy(errorMessage = throwable.message)
                }
                .collectLatest { session ->
                    _uiState.value = _uiState.value.copy(session = session)
                    loadFeedForCurrentSession(session)
                    loadNotifications(session)
                    loadAccountProfile(session)
                    if (_uiState.value.selectedTab == BottomTab.Explore) {
                        loadExploreTrending()
                    }
                }
        }
    }

    private suspend fun loadFeedForCurrentSession(session: AuthSession?) {
        _uiState.value = _uiState.value.copy(feedLoading = true, errorMessage = null)
        runCatching {
            val response = repository.loadFeed(session)
            if (session != null) augmentLoveStatuses(response.posts, session) else response.posts
        }.onSuccess { posts ->
            _uiState.value = _uiState.value.copy(
                feed = posts,
                feedLoading = false
            )
        }.onFailure { throwable ->
            _uiState.value = _uiState.value.copy(
                feedLoading = false,
                errorMessage = throwable.message ?: "Unable to load feed"
            )
        }
    }

    private suspend fun loadNotifications(session: AuthSession?) {
        if (session == null) {
            _uiState.value = _uiState.value.copy(unreadNotifications = emptyList(), notificationsLoading = false)
            return
        }

        _uiState.value = _uiState.value.copy(notificationsLoading = true)
        val unread = runCatching { repository.loadUnreadNotifications(session) }.getOrDefault(emptyList())
        _uiState.value = _uiState.value.copy(unreadNotifications = unread, notificationsLoading = false)
    }

    private suspend fun loadAccountProfile(session: AuthSession?) {
        if (session == null) {
            _uiState.value = _uiState.value.copy(accountProfile = null, accountPosts = emptyList(), accountLoading = false)
            return
        }

        _uiState.value = _uiState.value.copy(accountLoading = true)
        runCatching { repository.loadUserProfile(session.username) }
            .onSuccess { profile ->
                _uiState.value = _uiState.value.copy(accountProfile = profile)
            }
        runCatching {
            val rawPosts = repository.loadUserPosts(session, session.username).posts
            augmentLoveStatuses(rawPosts, session)
        }.onSuccess { posts ->
            _uiState.value = _uiState.value.copy(accountPosts = posts)
        }
        _uiState.value = _uiState.value.copy(accountLoading = false)
    }

    private suspend fun loadComments(postId: String) {
        val session = _uiState.value.session
        _uiState.value = _uiState.value.copy(commentsLoading = true)
        runCatching {
            val topLevel = repository.loadComments(session, postId).comments
                .map { it.copy(replies = it.replies ?: emptyList()) }
            topLevel.map { loadRepliesRecursively(it, session) }
        }.onSuccess { fullComments ->
            _uiState.value = _uiState.value.copy(
                comments = fullComments,
                commentsLoading = false
            )
        }.onFailure { throwable ->
            _uiState.value = _uiState.value.copy(
                commentsLoading = false,
                errorMessage = throwable.message ?: "Unable to load comments"
            )
        }
    }

    private suspend fun loadRepliesRecursively(comment: Comment, session: AuthSession?): Comment {
        val safeReplies = comment.replies ?: emptyList()
        if (!comment.hasReplies || safeReplies.isNotEmpty()) {
            return comment.copy(replies = safeReplies)
        }
        val allReplies = mutableListOf<Comment>()
        var page = 1
        while (true) {
            val resp: CommentResponse = runCatching { repository.loadCommentReplies(session, comment.id, page) }.getOrNull() ?: break
            allReplies.addAll(resp.comments)
            if (resp.last) break
            page++
        }
        val loaded = allReplies.map { loadRepliesRecursively(it, session) }
        return comment.copy(replies = loaded)
    }

    private fun refreshForSelectedTab(tab: BottomTab) {
        when (tab) {
            BottomTab.Home -> refreshFeed()
            BottomTab.Explore -> loadExploreTrending()
            BottomTab.Notifications -> refreshNotifications()
            BottomTab.Account -> refreshAccount()
        }
    }

    companion object {
        private const val DEFAULT_GUEST_USER = "jeffalo"

        fun factory(repository: WombatRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(repository) as T
                }
            }
        }
    }
}