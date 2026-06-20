package wombat.joshattic.us.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import wombat.joshattic.us.data.model.AuthSession
import wombat.joshattic.us.data.model.Comment
import wombat.joshattic.us.data.model.CommentResponse
import wombat.joshattic.us.data.model.Notification
import wombat.joshattic.us.data.model.Permissions
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
        observeBlockedUsers()
        observeSessions()
        observeUnreadSocketCount()
    }

    private fun observeSessions() {
        viewModelScope.launch {
            repository.sessionsFlow.collectLatest { sessions ->
                _uiState.value = _uiState.value.copy(savedAccounts = sessions)
                fetchSavedAccountsUnreadCounts(sessions)
            }
        }
    }

    private fun fetchSavedAccountsUnreadCounts(sessions: List<AuthSession>) {
        viewModelScope.launch(Dispatchers.IO) {
            val counts = mutableMapOf<String, Int>()
            sessions.forEach { session ->
                if (session.username != _uiState.value.session?.username) {
                    val count = repository.getUnreadCount(session.token)
                    counts[session.username] = count
                }
            }
            _uiState.value = _uiState.value.copy(savedAccountUnreadCounts = counts)
        }
    }

    private fun observeUnreadSocketCount() {
        viewModelScope.launch {
            repository.unreadSocketCount.collectLatest { count ->
                val currentSize = _uiState.value.unreadNotifications.size
                if (count > currentSize || (count == 0 && currentSize > 0)) {
                    refreshNotifications()
                }
            }
        }
    }

    fun switchAccount(username: String) {
        viewModelScope.launch {
            repository.switchAccount(username)
        }
    }

    fun setAddingAccount(adding: Boolean) {
        _uiState.value = _uiState.value.copy(isAddingAccount = adding, loginError = null, loginUsername = "", loginPassword = "")
    }

    fun selectTab(tab: BottomTab) {
        _uiState.value = _uiState.value.copy(
            selectedTab = tab,
            viewingProfileUsername = null,
            viewingProfile = null,
            viewingProfilePosts = emptyList(),
            viewingProfileLoading = false,
            viewingProfileIsFollowing = null,
            viewingProfileFollowLoading = false,
            selectedPost = null,
            comments = emptyList(),
            commentDraft = "",
            commentsLoading = false,
            commentReplyParent = null,
            feedPage = 1,
            feedLast = false,
            accountPage = 1,
            accountLast = false,
            viewingProfilePage = 1,
            viewingProfileLast = false
        )
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
        val username = snapshot.loginUsername.trim()
        val password = snapshot.loginPassword
        if (username.isBlank()) {
            _uiState.value = snapshot.copy(loginError = "Enter your username.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(authLoading = true, loginError = null)
            repository.login(username, password)
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        authLoading = false,
                        loginError = throwable.message ?: "Unable to sign in"
                    )
                }
                .onSuccess { session ->
                    val isPasswordless = password.isBlank()
                    _uiState.value = _uiState.value.copy(
                        authLoading = false,
                        loginPassword = "",
                        isAddingAccount = false,
                        toastMessage = if (isPasswordless) "Your account is insecure, please set a password on wasteof.money" else _uiState.value.toastMessage
                    )
                }
        }
    }

    fun logout() {
        val username = _uiState.value.session?.username ?: return
        viewModelScope.launch {
            repository.logout(username)
        }
    }

    fun toggleComposer() {
        val nextVisible = !_uiState.value.showComposer
        _uiState.value = _uiState.value.copy(
            showComposer = nextVisible,
            selectedPost = if (nextVisible) null else _uiState.value.selectedPost,
            composeRepostId = if (!nextVisible) null else _uiState.value.composeRepostId
        )
    }

    fun setComposeDraft(draft: String) {
        _uiState.value = _uiState.value.copy(composeDraft = draft, errorMessage = null)
    }

    fun submitPost(contentOverride: String? = null) {
        viewModelScope.launch {
            val session = _uiState.value.session ?: return@launch
            val draft = contentOverride?.trim() ?: _uiState.value.composeDraft.trim()
            val repostId = _uiState.value.composeRepostId
            if (draft.isBlank() && repostId == null) {
                _uiState.value = _uiState.value.copy(errorMessage = "Write something before posting.")
                return@launch
            }

            runCatching { repository.createPost(session, draft, repostId) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(showComposer = false, composeDraft = "", composeRepostId = null)
                    refreshFeed()
                    refreshAccount()
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(errorMessage = throwable.message)
                }
        }
    }

    fun submitRepost(postId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val session = _uiState.value.session ?: return@launch
            runCatching { repository.createPost(session, "", postId) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(toastMessage = "Reposted!")
                    refreshFeed()
                    refreshAccount()
                    onSuccess()
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(errorMessage = throwable.message ?: "Failed to repost")
                }
        }
    }

    fun openQuoteComposer(postId: String) {
        _uiState.value = _uiState.value.copy(
            showComposer = true,
            composeRepostId = postId,
            selectedPost = null
        )
    }

    fun clearAllDrafts() {
        _uiState.value = _uiState.value.copy(composerDrafts = emptyList(), toastMessage = "All drafts cleared")
    }

    fun refreshFeed() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(feedPage = 1, feedLast = false)
            loadFeedForCurrentSession(_uiState.value.session)
        }
    }

    fun refreshFeedAndExplore() {
        refreshFeed()
        if (_uiState.value.selectedTab == BottomTab.Explore) {
            loadExploreTrending()
        }
    }

    fun loadNextFeedPage() {
        val current = _uiState.value
        if (current.feedLoading || current.feedLast) return
        viewModelScope.launch {
            val nextPage = current.feedPage + 1
            _uiState.value = current.copy(feedLoading = true)
            runCatching {
                val response = repository.loadFeed(current.session, nextPage)
                val posts = if (current.session != null) augmentLoveStatuses(response.posts, current.session) else response.posts
                val filtered = filterBlockedPosts(posts)
                Pair(filtered, response.last)
            }.onSuccess { (newPosts, isLast) ->
                _uiState.value = _uiState.value.copy(
                    feed = _uiState.value.feed + newPosts,
                    feedLoading = false,
                    feedPage = nextPage,
                    feedLast = isLast
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    feedLoading = false,
                    errorMessage = throwable.message ?: "Unable to load next page"
                )
            }
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
                val posts = if (currentSession != null) augmentLoveStatuses(response.posts, currentSession) else response.posts
                filterBlockedPosts(posts)
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
        val normalizedUsername = username.trim()
        val isBlocked = _uiState.value.blockedUsernames.contains(normalizedUsername.lowercase())
        _uiState.value = _uiState.value.copy(
            viewingProfileUsername = normalizedUsername,
            viewingProfile = null,
            viewingProfilePosts = emptyList(),
            viewingProfileLoading = !isBlocked,
            viewingProfileIsFollowing = null,
            viewingProfileFollowLoading = false,
            selectedPost = null,
            comments = emptyList(),
            commentDraft = "",
            commentsLoading = false,
            commentReplyParent = null,
            viewingProfilePage = 1,
            viewingProfileLast = false
        )
        if (isBlocked) return
        viewModelScope.launch {
            val currentSession = _uiState.value.session
            runCatching {
                coroutineScope {
                    val profileDeferred = async { repository.loadUserProfile(normalizedUsername) }
                    val postsDeferred = async {
                        val response = repository.loadUserPosts(currentSession, normalizedUsername, 1)
                        val allPosts = (response.pinned ?: emptyList()) + response.posts
                        val posts = if (currentSession != null) augmentLoveStatuses(allPosts, currentSession) else allPosts
                        posts to response.last
                    }
                    val followDeferred = async {
                        currentSession
                            ?.takeUnless { it.username.equals(normalizedUsername, ignoreCase = true) }
                            ?.let { repository.getFollowStatus(it, normalizedUsername, it.username) }
                    }

                    val profile = profileDeferred.await()
                    val (posts, isLast) = postsDeferred.await()
                    val isFollowing = followDeferred.await()

                    Triple(profile, posts, isFollowing to isLast)
                }
            }.onSuccess { (profile, posts, followAndLast) ->
                _uiState.value = _uiState.value.copy(
                    viewingProfile = profile,
                    viewingProfilePosts = filterBlockedPosts(posts),
                    viewingProfileLoading = false,
                    viewingProfileIsFollowing = followAndLast.first,
                    viewingProfileLast = followAndLast.second
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
            viewingProfileLoading = false,
            viewingProfileIsFollowing = null,
            viewingProfileFollowLoading = false,
            viewingProfilePage = 1,
            viewingProfileLast = false
        )
    }

    fun loadNextProfilePage() {
        val current = _uiState.value
        val username = current.viewingProfileUsername ?: return
        if (current.viewingProfileLoading || current.viewingProfileLast) return
        viewModelScope.launch {
            val nextPage = current.viewingProfilePage + 1
            _uiState.value = current.copy(viewingProfileLoading = true)
            runCatching {
                val response = repository.loadUserPosts(current.session, username, nextPage)
                val posts = if (current.session != null) augmentLoveStatuses(response.posts, current.session) else response.posts
                val filtered = filterBlockedPosts(posts)
                Pair(filtered, response.last)
            }.onSuccess { (newPosts, isLast) ->
                _uiState.value = _uiState.value.copy(
                    viewingProfilePosts = _uiState.value.viewingProfilePosts + newPosts,
                    viewingProfileLoading = false,
                    viewingProfilePage = nextPage,
                    viewingProfileLast = isLast
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    viewingProfileLoading = false,
                    errorMessage = throwable.message ?: "Unable to load next page"
                )
            }
        }
    }

    fun openPost(post: Post, scrollToCommentId: String? = null) {
        _uiState.value = _uiState.value.copy(
            selectedPost = post,
            showComposer = false,
            commentDraft = "",
            comments = emptyList(),
            commentsLoading = false,
            commentReplyParent = null,
            scrollToCommentId = scrollToCommentId
        )
        // Comments are loaded lazily when user swipes up in the details sheet to expand

        // Verify/augment love status for this post if we have a session
        val session = _uiState.value.session
        if (session != null) {
            viewModelScope.launch {
                runCatching {
                    repository.getPostLoveStatus(session, post.id, session.username)
                }.onSuccess { loved ->
                    updatePostsWithLove(post.id, post.loves, loved)
                }
            }
        }
    }

    fun clearScrollToComment() {
        _uiState.value = _uiState.value.copy(scrollToCommentId = null)
    }

    fun clearInAppNotification() {
        _uiState.value = _uiState.value.copy(inAppNotification = null)
    }

    fun handleNotificationClick(notification: Notification) {
        // Mark as read immediately
        if (!notification.read) {
            viewModelScope.launch {
                val session = _uiState.value.session ?: return@launch
                runCatching {
                    repository.markNotificationsRead(session, listOf(notification.id))
                }.onSuccess {
                    refreshNotifications()
                }
            }
        }

        clearInAppNotification()

        when (notification.type.lowercase()) {
            "comment" -> {
                notification.data.post?.let { post ->
                    openPost(post, scrollToCommentId = notification.data.comment?.id)
                }
            }
            "post_mention", "repost" -> {
                notification.data.post?.let { post ->
                    openPost(post)
                }
            }
            "follow" -> {
                notification.data.actor?.name?.let { openProfile(it) }
            }
            "wall_comment", "wall_comment_reply" -> {
                _uiState.value = _uiState.value.copy(toastMessage = "Wall support hasn't been added yet")
            }
            "love" -> {
                notification.data.post?.let { post ->
                    openPost(post)
                }
            }
            "admin_notification" -> {
                _uiState.value = _uiState.value.copy(
                    toastMessage = notification.data.content ?: "Admin notification"
                )
            }
        }
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

    fun openFullScreenImages(images: List<String>, index: Int, username: String? = null) {
        _uiState.value = _uiState.value.copy(
            fullScreenImages = images,
            fullScreenImageUsername = username,
            initialFullScreenImageIndex = index
        )
    }

    fun closeFullScreenImages() {
        _uiState.value = _uiState.value.copy(
            fullScreenImages = null,
            fullScreenImageUsername = null,
            initialFullScreenImageIndex = 0
        )
    }

    fun setCommentDraft(comment: String) {
        _uiState.value = _uiState.value.copy(commentDraft = comment, errorMessage = null)
    }

    fun deletePost(postId: String) {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            runCatching {
                repository.deletePost(session, postId)
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    feed = _uiState.value.feed.filter { it.id != postId },
                    explorePosts = _uiState.value.explorePosts.filter { it.id != postId },
                    exploreTrendingPosts = _uiState.value.exploreTrendingPosts.filter { it.id != postId },
                    viewingProfilePosts = _uiState.value.viewingProfilePosts.filter { it.id != postId },
                    accountPosts = _uiState.value.accountPosts.filter { it.id != postId },
                    toastMessage = "Post deleted"
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(toastMessage = "Failed to delete post: ${e.message}")
            }
        }
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
                composerDrafts = listOf(draft) + currentDrafts,
                toastMessage = "Post saved to drafts"
            )
        }
    }

    fun restoreDraft(draft: String) {
        _uiState.value = _uiState.value.copy(
            composeDraft = draft,
            composerDrafts = _uiState.value.composerDrafts.filter { it != draft }
        )
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

    fun blockUser(username: String, reported: Boolean = false) {
        viewModelScope.launch {
            repository.blockUser(username)
            val normalized = username.lowercase()
            val current = _uiState.value
            _uiState.value = current.copy(
                feed = current.feed.filterNotBlocked(normalized),
                exploreTrendingPosts = current.exploreTrendingPosts.filterNotBlocked(normalized),
                accountPosts = current.accountPosts.filterNotBlocked(normalized),
                viewingProfilePosts = current.viewingProfilePosts.filterNotBlocked(normalized),
                selectedPost = current.selectedPost?.takeUnless { it.poster.name.equals(username, ignoreCase = true) },
                toastMessage = if (reported) "You've reported this user" else null
            )
        }
    }

    fun unblockViewedProfile() {
        val username = _uiState.value.viewingProfileUsername ?: return
        viewModelScope.launch {
            repository.unblockUser(username)
            openProfile(username)
        }
    }

    fun showFollowers(username: String) {
        _uiState.value = _uiState.value.copy(
            userListTitle = "Followers",
            userListUsername = username,
            userListType = "followers",
            userListToShow = emptyList(),
            userListLoading = true,
            userListPage = 1,
            userListIsLastPage = false,
            userListLoadingMore = false
        )
        viewModelScope.launch {
            runCatching {
                repository.getFollowers(_uiState.value.session, username, 1)
            }.onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    userListToShow = response.followers,
                    userListIsLastPage = response.last,
                    userListLoading = false
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    userListToShow = null,
                    userListLoading = false,
                    toastMessage = throwable.message ?: "Failed to load followers"
                )
            }
        }
    }

    fun showFollowing(username: String) {
        _uiState.value = _uiState.value.copy(
            userListTitle = "Following",
            userListUsername = username,
            userListType = "following",
            userListToShow = emptyList(),
            userListLoading = true,
            userListPage = 1,
            userListIsLastPage = false,
            userListLoadingMore = false
        )
        viewModelScope.launch {
            runCatching {
                repository.getFollowing(_uiState.value.session, username, 1)
            }.onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    userListToShow = response.following,
                    userListIsLastPage = response.last,
                    userListLoading = false
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    userListToShow = null,
                    userListLoading = false,
                    toastMessage = throwable.message ?: "Failed to load following"
                )
            }
        }
    }

    fun loadNextUserListPage() {
        val currentState = _uiState.value
        val username = currentState.userListUsername ?: return
        val type = currentState.userListType ?: return
        
        if (currentState.userListLoadingMore || currentState.userListIsLastPage) return

        _uiState.value = currentState.copy(userListLoadingMore = true)
        val nextPage = currentState.userListPage + 1

        viewModelScope.launch {
            if (type == "followers") {
                runCatching { repository.getFollowers(currentState.session, username, nextPage) }
                    .onSuccess { response ->
                        _uiState.value = _uiState.value.copy(
                            userListToShow = (_uiState.value.userListToShow ?: emptyList()) + response.followers,
                            userListPage = nextPage,
                            userListIsLastPage = response.last,
                            userListLoadingMore = false
                        )
                    }.onFailure {
                        _uiState.value = _uiState.value.copy(userListLoadingMore = false)
                    }
            } else if (type == "following") {
                runCatching { repository.getFollowing(currentState.session, username, nextPage) }
                    .onSuccess { response ->
                        _uiState.value = _uiState.value.copy(
                            userListToShow = (_uiState.value.userListToShow ?: emptyList()) + response.following,
                            userListPage = nextPage,
                            userListIsLastPage = response.last,
                            userListLoadingMore = false
                        )
                    }.onFailure {
                        _uiState.value = _uiState.value.copy(userListLoadingMore = false)
                    }
            }
        }
    }

    fun dismissUserList() {
        _uiState.value = _uiState.value.copy(
            userListToShow = null,
            userListTitle = "",
            userListUsername = null,
            userListType = null,
            userListLoading = false,
            userListPage = 1,
            userListIsLastPage = true,
            userListLoadingMore = false
        )
    }

    fun toggleViewedProfileFollow() {
        val session = _uiState.value.session ?: return
        val username = _uiState.value.viewingProfileUsername ?: return
        if (session.username.equals(username, ignoreCase = true)) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(viewingProfileFollowLoading = true)
            runCatching { repository.toggleFollow(session, username) }
                .onSuccess { response ->
                    val currentProfile = _uiState.value.viewingProfile
                    _uiState.value = _uiState.value.copy(
                        viewingProfile = currentProfile?.copy(
                            stats = currentProfile.stats?.copy(followers = response.new.followers)
                        ),
                        viewingProfileIsFollowing = response.new.isFollowing,
                        viewingProfileFollowLoading = false
                    )
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        viewingProfileFollowLoading = false,
                        errorMessage = throwable.message ?: "Unable to update follow"
                    )
                }
        }
    }

    fun clearToast() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }

    fun dismissBannedPopup() {
        _uiState.value = _uiState.value.copy(showBannedPopup = false)
    }

    fun openReportDialog(postId: String) {
        _uiState.value = _uiState.value.copy(
            showReportDialog = true,
            reportPostId = postId,
            reportReason = ""
        )
    }

    fun closeReportDialog() {
        _uiState.value = _uiState.value.copy(
            showReportDialog = false,
            reportPostId = null,
            reportReason = "",
            reportLoading = false
        )
    }

    fun setReportReason(reason: String) {
        _uiState.value = _uiState.value.copy(reportReason = reason)
    }

    fun submitReport(reason: String) {
        val currentSession = _uiState.value.session ?: return
        val postId = _uiState.value.reportPostId ?: return
        // assuming isBlocked logic exists in your project context
        if (_uiState.value.blockedUsernames.contains(_uiState.value.selectedPost?.poster?.name?.lowercase())) return

        _uiState.value = _uiState.value.copy(reportLoading = true)
        viewModelScope.launch {
            runCatching {
                repository.reportPost(currentSession, postId, reason)
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    showReportDialog = false,
                    reportPostId = null,
                    reportReason = "",
                    reportLoading = false,
                    toastMessage = "Post reported"
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    reportLoading = false,
                    toastMessage = throwable.message ?: "Failed to report post"
                )
            }
        }
    }

    private fun updatePostsWithLove(postId: String, newLoves: Int, newIsLoving: Boolean) {
        val current = _uiState.value
        fun transform(p: Post): Post {
            var updated = if (p.id == postId) p.copy(loves = newLoves, isLoving = newIsLoving) else p
            if (updated.repost?.id == postId) {
                updated = updated.copy(repost = updated.repost.copy(loves = newLoves, isLoving = newIsLoving))
            }
            return updated
        }
        _uiState.value = current.copy(
            feed = current.feed.map(::transform),
            exploreTrendingPosts = current.exploreTrendingPosts.map(::transform),
            accountPosts = current.accountPosts.map(::transform),
            viewingProfilePosts = current.viewingProfilePosts.map(::transform),
            selectedPost = if (current.selectedPost?.id == postId) transform(current.selectedPost) else current.selectedPost
        )
    }

    private fun filterBlockedPosts(posts: List<Post>): List<Post> =
        posts.filterNotBlocked(_uiState.value.blockedUsernames)

    private fun List<Post>.filterNotBlocked(username: String): List<Post> =
        filterNot { it.poster.name.equals(username, ignoreCase = true) }

    private fun List<Post>.filterNotBlocked(blockedUsernames: Set<String>): List<Post> =
        filterNot { blockedUsernames.contains(it.poster.name.lowercase()) }

    private suspend fun augmentLoveStatuses(posts: List<Post>, session: AuthSession): List<Post> = coroutineScope {
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

    private fun observeSessionAndRefresh() {
        viewModelScope.launch {
            repository.sessionFlow
                .catch { throwable ->
                    _uiState.value = _uiState.value.copy(errorMessage = throwable.message)
                }
                .collectLatest { session ->
                    _uiState.value = _uiState.value.copy(session = session)
                    coroutineScope {
                        launch { loadFeedForCurrentSession(session) }
                        launch { loadNotifications(session) }
                        launch { loadAccountProfile(session) }
                    }
                    if (session != null) {
                        checkBanStatus(session)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isBanned = false,
                            banReason = null,
                            showBannedPopup = false
                        )
                    }
                    if (_uiState.value.selectedTab == BottomTab.Explore) {
                        loadExploreTrending()
                    }
                }
        }
    }

    private fun checkBanStatus(session: AuthSession) {
        viewModelScope.launch {
            val sessionResponse = runCatching { repository.getSession(session.token) }.getOrNull()
            val permissions = sessionResponse?.user?.permissions
            if (permissions?.banned == true) {
                val messages = runCatching { repository.getAdminMessages(session.token) }.getOrNull()
                val banReason = messages?.firstOrNull { it.type == "admin_notification" }?.data?.content
                _uiState.value = _uiState.value.copy(
                    isBanned = true,
                    banReason = banReason,
                    showBannedPopup = true
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isBanned = false,
                    banReason = null,
                    showBannedPopup = false
                )
            }
        }
    }

    private fun observeBlockedUsers() {
        viewModelScope.launch {
            repository.blockedUsernamesFlow.collectLatest { blockedUsernames ->
                val current = _uiState.value
                _uiState.value = current.copy(
                    blockedUsernames = blockedUsernames,
                    feed = current.feed.filterNotBlocked(blockedUsernames),
                    exploreTrendingPosts = current.exploreTrendingPosts.filterNotBlocked(blockedUsernames),
                    accountPosts = current.accountPosts.filterNotBlocked(blockedUsernames),
                    viewingProfilePosts = current.viewingProfilePosts.filterNotBlocked(blockedUsernames),
                    selectedPost = current.selectedPost?.takeUnless {
                        blockedUsernames.contains(it.poster.name.lowercase())
                    }
                )
            }
        }
    }

    private suspend fun loadFeedForCurrentSession(session: AuthSession?) {
        _uiState.value = _uiState.value.copy(feedLoading = true, errorMessage = null)
        runCatching {
            val response = repository.loadFeed(session, 1)
            val posts = if (session != null) augmentLoveStatuses(response.posts, session) else response.posts
            filterBlockedPosts(posts) to response.last
        }.onSuccess { (posts, isLast) ->
            _uiState.value = _uiState.value.copy(
                feed = posts,
                feedLoading = false,
                feedLast = isLast,
                feedPage = 1
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
            _uiState.value = _uiState.value.copy(unreadNotifications = emptyList(), readNotifications = emptyList(), notificationsLoading = false)
            return
        }

        _uiState.value = _uiState.value.copy(notificationsLoading = true)
        val unread = runCatching { repository.loadUnreadNotifications(session) }.getOrDefault(emptyList())
        val read = runCatching { repository.loadReadNotifications(session) }.getOrDefault(emptyList())
        val oldUnreadIds = _uiState.value.unreadNotifications.map { it.id }.toSet()
        val isInitialLoad = !_uiState.value.hasInitialNotificationsLoaded
        
        val newNotification = if (!isInitialLoad) {
            unread.firstOrNull { it.id !in oldUnreadIds }
        } else {
            null
        }

        _uiState.value = _uiState.value.copy(
            unreadNotifications = unread,
            readNotifications = read,
            notificationsLoading = false,
            hasInitialNotificationsLoaded = true,
            inAppNotification = newNotification ?: _uiState.value.inAppNotification
        )
    }

    private suspend fun loadAccountProfile(session: AuthSession?) {
        if (session == null) {
            _uiState.value = _uiState.value.copy(
                accountProfile = null,
                accountPosts = emptyList(),
                accountLoading = false,
                accountPage = 1,
                accountLast = false
            )
            return
        }

        _uiState.value = _uiState.value.copy(accountLoading = true)
        coroutineScope {
            val profileDeferred = async { runCatching { repository.loadUserProfile(session.username) } }
            val postsDeferred = async {
                runCatching {
                    val response = repository.loadUserPosts(session, session.username, 1)
                    val allPosts = (response.pinned ?: emptyList()) + response.posts
                    val posts = filterBlockedPosts(augmentLoveStatuses(allPosts, session))
                    posts to response.last
                }
            }

            profileDeferred.await().onSuccess { profile ->
                _uiState.value = _uiState.value.copy(accountProfile = profile)
            }
            postsDeferred.await().onSuccess { (posts, isLast) ->
                _uiState.value = _uiState.value.copy(
                    accountPosts = posts,
                    accountLast = isLast,
                    accountPage = 1
                )
            }
        }
        _uiState.value = _uiState.value.copy(accountLoading = false)
    }

    fun loadNextAccountPage() {
        val current = _uiState.value
        val session = current.session ?: return
        if (current.accountLoading || current.accountLast) return
        viewModelScope.launch {
            val nextPage = current.accountPage + 1
            _uiState.value = current.copy(accountLoading = true)
            runCatching {
                val response = repository.loadUserPosts(session, session.username, nextPage)
                val posts = augmentLoveStatuses(response.posts, session)
                val filtered = filterBlockedPosts(posts)
                Pair(filtered, response.last)
            }.onSuccess { (newPosts, isLast) ->
                _uiState.value = _uiState.value.copy(
                    accountPosts = _uiState.value.accountPosts + newPosts,
                    accountLoading = false,
                    accountPage = nextPage,
                    accountLast = isLast
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    accountLoading = false,
                    errorMessage = throwable.message ?: "Unable to load next page"
                )
            }
        }
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
        const val MAX_CHAR_COUNT = 1500

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
