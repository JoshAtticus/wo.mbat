package wombat.joshattic.us.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import wombat.joshattic.us.data.model.AuthSession
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
        _uiState.value = _uiState.value.copy(exploreQuery = query, errorMessage = null)
    }

    fun searchExplore() {
        viewModelScope.launch {
            val snapshot = _uiState.value
            val username = snapshot.exploreQuery.trim().ifBlank {
                snapshot.session?.username ?: DEFAULT_GUEST_USER
            }

            _uiState.value = _uiState.value.copy(exploreLoading = true, errorMessage = null)
            runCatching {
                val profile = repository.loadUserProfile(username)
                val posts = repository.loadUserPosts(snapshot.session, username).posts
                profile to posts
            }.onSuccess { (profile, posts) ->
                _uiState.value = _uiState.value.copy(
                    exploreProfile = profile,
                    explorePosts = posts,
                    exploreLoading = false
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    exploreLoading = false,
                    errorMessage = throwable.message ?: "Unable to search user"
                )
            }
        }
    }

    fun openPost(post: Post) {
        _uiState.value = _uiState.value.copy(
            selectedPost = post,
            showComposer = false,
            commentDraft = "",
            comments = emptyList(),
            commentsLoading = false
        )
        // Comments are loaded lazily when user swipes up in the details sheet to expand
    }

    fun closePost() {
        _uiState.value = _uiState.value.copy(
            selectedPost = null,
            comments = emptyList(),
            commentDraft = "",
            commentsLoading = false
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

            runCatching { repository.createComment(session, selectedPost.id, draft, null) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(commentDraft = "")
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
        _uiState.value = _uiState.value.copy(comments = emptyList(), commentsLoading = false)
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
                    if (_uiState.value.selectedTab == BottomTab.Explore && _uiState.value.exploreQuery.isNotBlank()) {
                        searchExplore()
                    }
                }
        }
    }

    private suspend fun loadFeedForCurrentSession(session: AuthSession?) {
        _uiState.value = _uiState.value.copy(feedLoading = true, errorMessage = null)
        runCatching { repository.loadFeed(session) }
            .onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    feed = response.posts,
                    feedLoading = false
                )
            }
            .onFailure { throwable ->
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
        runCatching { repository.loadUserPosts(session, session.username).posts }
            .onSuccess { posts ->
                _uiState.value = _uiState.value.copy(accountPosts = posts)
            }
        _uiState.value = _uiState.value.copy(accountLoading = false)
    }

    private suspend fun loadComments(postId: String) {
        val session = _uiState.value.session
        _uiState.value = _uiState.value.copy(commentsLoading = true)
        runCatching { repository.loadComments(session, postId) }
            .onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    comments = response.comments,
                    commentsLoading = false
                )
            }
            .onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    commentsLoading = false,
                    errorMessage = throwable.message ?: "Unable to load comments"
                )
            }
    }

    private fun refreshForSelectedTab(tab: BottomTab) {
        when (tab) {
            BottomTab.Home -> refreshFeed()
            BottomTab.Explore -> if (_uiState.value.exploreQuery.isNotBlank()) searchExplore()
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