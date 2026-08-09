package wombat.joshattic.us.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import wombat.joshattic.us.ui.utils.NetworkConnectivityObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.firstOrNull
import wombat.joshattic.us.data.model.AuthSession
import wombat.joshattic.us.data.model.Comment
import wombat.joshattic.us.data.model.CommentResponse
import wombat.joshattic.us.data.model.Notification
import wombat.joshattic.us.data.model.Permissions
import wombat.joshattic.us.data.model.Post
import wombat.joshattic.us.data.model.User
import wombat.joshattic.us.data.repository.WombatRepository
import wombat.joshattic.us.ui.state.BottomTab
import wombat.joshattic.us.ui.state.HomeUiState
import wombat.joshattic.us.ui.state.BlockedWarningTarget

class HomeViewModel(
    private val repository: WombatRepository,
    private val connectivityObserver: NetworkConnectivityObserver
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        val initialSession = runBlocking {
            repository.sessionFlow.firstOrNull()
        }
        val initialSessions = runBlocking {
            repository.sessionsFlow.firstOrNull()
        } ?: emptyList()

        _uiState.value = _uiState.value.copy(
            session = initialSession,
            savedAccounts = initialSessions
        )

        observeSessionAndRefresh()
        observeBlockedUsers()
        observeSessions()
        observeUnreadSocketCount()
        observeSettings()
        observeConnectivity()
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            connectivityObserver.isConnected.collect { isConnected ->
                _uiState.value = _uiState.value.copy(isOnline = isConnected)
                if (isConnected) {
                    refreshFeedAndExplore()
                }
            }
        }
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
                // Always update the badge count from socket directly
                _uiState.value = _uiState.value.copy(socketUnreadCount = count)
                // Only trigger a refresh if the count changed meaningfully
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
        val isSameTab = _uiState.value.selectedTab == tab
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
            focusedComment = null,
            feedPage = 1,
            feedLast = false,
            accountPage = 1,
            accountLast = false,
            viewingProfilePage = 1,
            viewingProfileLast = false,
            scrollToTop = if (isSameTab && tab == BottomTab.Home) true else _uiState.value.scrollToTop
        )
        if (tab == BottomTab.Notifications && _uiState.value.markReadWhenTabOpened) {
            markAllNotificationsRead()
        } else {
            refreshForSelectedTab(tab)
        }
    }

    fun setLoginUsername(username: String) {
        _uiState.value = _uiState.value.copy(loginUsername = username, loginError = null)
    }

    fun setLoginPassword(password: String) {
        _uiState.value = _uiState.value.copy(loginPassword = password, loginError = null)
    }

    fun login() {
        val snapshot = _uiState.value
        val username = snapshot.loginUsername.trim().lowercase()
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
            composeRepostId = if (!nextVisible) null else _uiState.value.composeRepostId,
            composeEditPostId = if (!nextVisible) null else _uiState.value.composeEditPostId,
            composeEditPostAuthor = if (!nextVisible) null else _uiState.value.composeEditPostAuthor,
            composeOriginalContent = if (!nextVisible) null else _uiState.value.composeOriginalContent
        )
    }

    fun openEditComposer(post: Post) {
        _uiState.value = _uiState.value.copy(
            showComposer = true,
            composeDraft = post.content,
            composeOriginalContent = post.content,
            composeEditPostId = post.id,
            composeEditPostAuthor = post.poster.name,
            composeRepostId = null,
            selectedPost = null
        )
    }

    fun clearEditPostId() {
        _uiState.value = _uiState.value.copy(
            composeEditPostId = null,
            composeEditPostAuthor = null,
            composeOriginalContent = null
        )
    }

    private fun updatePostInState(updatedPost: Post, fallbackId: String? = null, fallbackContent: String? = null) {
        val updatedId = updatedPost.id ?: fallbackId ?: return
        val updatedContent = updatedPost.content ?: fallbackContent ?: ""
        val current = _uiState.value
        fun transform(p: Post): Post {
            var pUpdated = if (p.id == updatedId) {
                p.copy(content = updatedContent)
            } else p
            val repost = pUpdated.repost
            if (repost != null && repost.id == updatedId) {
                pUpdated = pUpdated.copy(repost = repost.copy(content = updatedContent))
            }
            return pUpdated
        }
        _uiState.value = current.copy(
            feed = current.feed.map(::transform),
            exploreTrendingPosts = current.exploreTrendingPosts.map(::transform),
            accountPosts = current.accountPosts.map(::transform),
            viewingProfilePosts = current.viewingProfilePosts.map(::transform),
            selectedPost = if (current.selectedPost?.id == updatedId) transform(current.selectedPost) else current.selectedPost
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
            val editPostId = _uiState.value.composeEditPostId
            val editPostAuthor = _uiState.value.composeEditPostAuthor
            if (draft.isBlank() && repostId == null && editPostId == null) {
                _uiState.value = _uiState.value.copy(errorMessage = "Write something before posting.")
                return@launch
            }

            if (editPostId != null) {
                val targetSession = if (editPostAuthor != null) {
                    _uiState.value.savedAccounts.firstOrNull {
                        it.username.trim().equals(editPostAuthor.trim(), ignoreCase = true)
                    } ?: session
                } else {
                    session
                }

                runCatching { repository.editPost(targetSession, editPostId, draft) }
                    .onSuccess { updatedPost ->
                        _uiState.value = _uiState.value.copy(
                            showComposer = false,
                            composeDraft = "",
                            composeEditPostId = null,
                            composeEditPostAuthor = null
                        )
                        updatePostInState(updatedPost, fallbackId = editPostId, fallbackContent = draft)
                    }
                    .onFailure { throwable ->
                        _uiState.value = _uiState.value.copy(errorMessage = throwable.message)
                    }
            } else {
                runCatching { repository.createPost(session, draft, repostId) }
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(
                            showComposer = false,
                            composeDraft = "",
                            composeRepostId = null,
                            scrollToTop = true
                        )
                        refreshFeed()
                        refreshAccount()
                    }
                    .onFailure { throwable ->
                        _uiState.value = _uiState.value.copy(errorMessage = throwable.message)
                    }
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

    fun setExploreSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(
            exploreSearchQuery = query,
            exploreSearchActive = query.isNotEmpty()
        )
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                exploreSearchPostResults = emptyList(),
                exploreSearchUserResults = emptyList(),
                exploreSearchLoading = false
            )
            return
        }
        viewModelScope.launch {
            delay(300L) // debounce
            if (_uiState.value.exploreSearchQuery != query) return@launch // stale
            _uiState.value = _uiState.value.copy(exploreSearchLoading = true)
            val session = _uiState.value.session
            val postsResult = runCatching { repository.searchPosts(session, query) }
            val usersResult = runCatching { repository.searchUsers(session, query) }
            _uiState.value = _uiState.value.copy(
                exploreSearchLoading = false,
                exploreSearchPostResults = postsResult.getOrNull()?.results?.let { filterBlockedPosts(it) } ?: _uiState.value.exploreSearchPostResults,
                exploreSearchUserResults = usersResult.getOrNull()?.results ?: _uiState.value.exploreSearchUserResults
            )
        }
    }

    fun setExploreTrendingTimeframe(timeframe: String?) {
        _uiState.value = _uiState.value.copy(exploreTrendingTimeframe = timeframe)
        loadExploreTrending()
    }

    fun loadFrog() {
        if (_uiState.value.exploreFrogMessage != null) return // already loaded
        viewModelScope.launch {
            runCatching { repository.getFrog() }
                .onSuccess { _uiState.value = _uiState.value.copy(exploreFrogMessage = it.frog) }
        }
    }

    fun loadExploreTrending() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(exploreTrendingLoading = true, errorMessage = null)
            val currentSession = _uiState.value.session
            val timeframe = _uiState.value.exploreTrendingTimeframe
            runCatching {
                val response = repository.loadTrendingPosts(currentSession, timeframe)
                val posts = if (currentSession != null) augmentLoveStatuses(response.posts, currentSession) else response.posts
                val filteredPosts = filterBlockedPosts(posts)
                
                val followedMap = if (currentSession != null) {
                    val uniquePosters = filteredPosts
                        .map { it.poster.name }
                        .distinct()
                        .filterNot { it.equals(currentSession.username, ignoreCase = true) }
                    
                    coroutineScope {
                        uniquePosters.map { username ->
                            async(Dispatchers.IO) {
                                username.lowercase() to runCatching {
                                    repository.getFollowStatus(currentSession, username, currentSession.username)
                                }.getOrDefault(false)
                            }
                        }.awaitAll().toMap()
                    }
                } else {
                    emptyMap()
                }
                
                Triple(filteredPosts, followedMap, currentSession)
            }.onSuccess { (posts, followedMap, session) ->
                val currentFollowed = _uiState.value.followedUsernames.toMutableSet()
                if (session != null) {
                    followedMap.forEach { (username, isFollowing) ->
                        if (isFollowing) {
                            currentFollowed.add(username)
                        } else {
                            currentFollowed.remove(username)
                        }
                    }
                }
                _uiState.value = _uiState.value.copy(
                    exploreTrendingPosts = posts,
                    exploreTrendingLoading = false,
                    followedUsernames = currentFollowed
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
        if (isBlocked) {
            _uiState.value = _uiState.value.copy(
                blockedWarningTarget = BlockedWarningTarget.Profile(normalizedUsername)
            )
            return
        }
        openProfileBypassingBlock(normalizedUsername)
    }

    fun openProfileBypassingBlock(username: String) {
        val normalizedUsername = username.trim()
        _uiState.value = _uiState.value.copy(
            viewingProfileUsername = normalizedUsername,
            viewingProfile = null,
            viewingProfilePosts = emptyList(),
            viewingProfileLoading = true,
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
                val isFollowing = followAndLast.first ?: false
                val currentFollowed = _uiState.value.followedUsernames.toMutableSet()
                if (isFollowing) {
                    currentFollowed.add(normalizedUsername.lowercase())
                } else {
                    currentFollowed.remove(normalizedUsername.lowercase())
                }
                _uiState.value = _uiState.value.copy(
                    viewingProfile = profile,
                    viewingProfilePosts = posts, // bypass filter
                    viewingProfileLoading = false,
                    viewingProfileIsFollowing = followAndLast.first,
                    viewingProfileLast = followAndLast.second,
                    followedUsernames = currentFollowed
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
                val filtered = posts // bypass filter
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
        val isBlocked = _uiState.value.blockedUsernames.contains(post.poster.name.lowercase())
        if (isBlocked) {
            _uiState.value = _uiState.value.copy(
                blockedWarningTarget = BlockedWarningTarget.Post(post)
            )
            return
        }
        openPostBypassingBlock(post, scrollToCommentId)
    }

    fun openPostBypassingBlock(post: Post, scrollToCommentId: String? = null) {
        // If this is a pure repost wrapper (empty content + nested post), open the inner post
        // so that comments, love counts, and repost counts are loaded for the correct post ID.
        val isPureRepostWrapper = post.repost != null &&
            post.content.replace(Regex("<.*?>"), "").trim().isBlank()
        val effectivePost = if (isPureRepostWrapper) post.repost!! else post

        _uiState.value = _uiState.value.copy(
            selectedPost = effectivePost,
            showComposer = false,
            commentDraft = "",
            comments = emptyList(),
            commentsLoading = false,
            commentReplyParent = null,
            scrollToCommentId = scrollToCommentId
        )
        // Comments are loaded lazily when user swipes up in the details sheet to expand

        // Verify/augment love status for the effective post if we have a session
        val session = _uiState.value.session
        if (session != null) {
            viewModelScope.launch {
                runCatching {
                    repository.getPostLoveStatus(session, effectivePost.id, session.username)
                }.onSuccess { loved ->
                    updatePostsWithLove(effectivePost.id, effectivePost.loves, loved)
                }
            }
        }
    }

    fun openPostById(postId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(commentsLoading = true)
            runCatching {
                repository.loadPost(_uiState.value.session, postId)
            }.onSuccess { post ->
                if (_uiState.value.blockedUsernames.contains(post.poster.name.lowercase())) {
                    _uiState.value = _uiState.value.copy(
                        commentsLoading = false,
                        blockedWarningTarget = BlockedWarningTarget.Post(post)
                    )
                } else {
                    openPost(post)
                }
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    commentsLoading = false,
                    errorMessage = throwable.message ?: "Unable to load post"
                )
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
        if (!notification.read && _uiState.value.markReadWhenOpened) {
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
            "comment", "comment_reply", "comment_mention" -> {
                notification.data.post?.let { post ->
                    openPost(post, scrollToCommentId = notification.data.comment?.id)
                }
            }
            "post_mention", "mention", "repost" -> {
                notification.data.post?.let { post ->
                    openPost(post)
                }
            }
            "follow" -> {
                notification.data.actor?.name?.let { openProfile(it) }
            }
            "wall_comment", "wall_comment_reply" -> {
                val wallUsername = notification.data.wall?.name ?: notification.to.name
                openWall(wallUsername)
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
            commentReplyParent = null,
            focusedComment = null
        )
    }

    fun focusComment(comment: Comment?) {
        _uiState.value = _uiState.value.copy(
            focusedComment = comment,
            commentReplyParent = comment
        )
    }

    fun clearFocusComment() {
        _uiState.value = _uiState.value.copy(
            focusedComment = null,
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

    fun deletePost(postId: String, postAuthor: String? = null) {
        val currentSession = _uiState.value.session
        val savedAccounts = _uiState.value.savedAccounts
        val targetSession = if (postAuthor != null) {
            savedAccounts.firstOrNull { it.username.trim().equals(postAuthor.trim(), ignoreCase = true) } ?: currentSession
        } else {
            currentSession
        } ?: return

        viewModelScope.launch {
            runCatching {
                repository.deletePost(targetSession, postId)
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

            val formattedHtml = draft.split("\n\n").joinToString("\n") { p ->
                val lineBreaks = p.replace("\n", "<br />")
                "<p dir=\"ltr\">$lineBreaks</p>"
            }

            val parent = _uiState.value.commentReplyParent?.id
            runCatching { repository.createComment(session, selectedPost.id, formattedHtml, parent) }
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

    fun openWall(username: String) {
        _uiState.value = _uiState.value.copy(
            viewingWallUsername = username,
            wallComments = emptyList(),
            wallCommentsLoading = true,
            wallCommentsPage = 1,
            wallCommentsLast = false,
            wallCommentDraft = "",
            wallCommentReplyParent = null
        )
        loadWallCommentsPage(username, 1)
    }

    fun closeWall() {
        _uiState.value = _uiState.value.copy(
            viewingWallUsername = null,
            wallComments = emptyList(),
            wallCommentsLoading = false,
            wallCommentsPage = 1,
            wallCommentsLast = false,
            wallCommentDraft = "",
            wallCommentReplyParent = null,
            focusedComment = null
        )
    }

    fun setWallCommentDraft(comment: String) {
        _uiState.value = _uiState.value.copy(wallCommentDraft = comment, errorMessage = null)
    }

    fun setWallCommentReplyParent(comment: Comment?) {
        _uiState.value = _uiState.value.copy(wallCommentReplyParent = comment)
    }

    private fun loadWallCommentsPage(username: String, page: Int) {
        val session = _uiState.value.session
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(wallCommentsLoading = true)
            runCatching {
                val response = repository.loadWallComments(session, username, page)
                val topLevel = response.comments.map { it.copy(replies = it.replies ?: emptyList()) }
                val fullComments = topLevel.map { loadRepliesRecursively(it, session) }
                fullComments to response.last
            }.onSuccess { (fullComments, isLast) ->
                _uiState.value = _uiState.value.copy(
                    wallComments = if (page == 1) fullComments else _uiState.value.wallComments + fullComments,
                    wallCommentsLoading = false,
                    wallCommentsPage = page,
                    wallCommentsLast = isLast
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    wallCommentsLoading = false,
                    errorMessage = throwable.message ?: "Unable to load wall comments"
                )
            }
        }
    }

    fun loadNextWallCommentsPage() {
        val current = _uiState.value
        val username = current.viewingWallUsername ?: return
        if (current.wallCommentsLoading || current.wallCommentsLast) return
        loadWallCommentsPage(username, current.wallCommentsPage + 1)
    }

    fun submitWallComment() {
        viewModelScope.launch {
            val session = _uiState.value.session ?: return@launch
            val username = _uiState.value.viewingWallUsername ?: return@launch
            val draft = _uiState.value.wallCommentDraft.trim()
            if (draft.isBlank()) {
                _uiState.value = _uiState.value.copy(errorMessage = "Write a wall comment before sending it.")
                return@launch
            }

            val formattedHtml = draft.split("\n\n").joinToString("\n") { p ->
                val lineBreaks = p.replace("\n", "<br />")
                "<p dir=\"ltr\">$lineBreaks</p>"
            }

            val parent = _uiState.value.wallCommentReplyParent?.id
            runCatching { repository.createWallComment(session, username, formattedHtml, parent) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        wallCommentDraft = "",
                        wallCommentReplyParent = null
                    )
                    loadWallCommentsPage(username, 1)
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(errorMessage = throwable.message ?: "Unable to post wall comment")
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
            // Reset pagination and reload from page 1
            _uiState.value = _uiState.value.copy(
                unreadNotificationsPage = 1,
                unreadNotificationsLast = false,
                readNotificationsPage = 1,
                readNotificationsLast = false,
                socketUnreadCount = 0
            )
            loadNotifications(session)
        }
    }

    fun loadNextNotificationsPage() {
        val current = _uiState.value
        if (current.notificationsLoadingMore) return
        // Load more unread first, then read
        val session = current.session ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(notificationsLoadingMore = true)
            if (!current.unreadNotificationsLast) {
                val nextPage = current.unreadNotificationsPage + 1
                val response = runCatching { repository.loadUnreadNotifications(session, nextPage) }.getOrNull()
                if (response != null) {
                    val rawUnread = response.unread.orEmpty()
                    val filteredUnread = handleBlockedNotifications(rawUnread, session, current.blockedUsernames)
                    _uiState.value = _uiState.value.copy(
                        unreadNotifications = _uiState.value.unreadNotifications + filteredUnread,
                        unreadNotificationsPage = nextPage,
                        unreadNotificationsLast = response.last
                    )
                    // If we reached the end of unread notifications, fetch page 1 of read notifications
                    if (response.last) {
                        val readResp = runCatching { repository.loadReadNotifications(session, 1) }.getOrNull()
                        if (readResp != null) {
                            val filteredRead = readResp.read.orEmpty().filterNot { notification ->
                                val actorName = notification.data.actor?.name?.lowercase()
                                actorName != null && current.blockedUsernames.contains(actorName)
                            }
                            _uiState.value = _uiState.value.copy(
                                readNotifications = filteredRead,
                                readNotificationsPage = 1,
                                readNotificationsLast = readResp.last
                            )
                        }
                    }
                }
            } else if (!current.readNotificationsLast) {
                val nextPage = current.readNotificationsPage + 1
                val response = runCatching { repository.loadReadNotifications(session, nextPage) }.getOrNull()
                if (response != null) {
                    val filteredRead = response.read.orEmpty().filterNot { notification ->
                        val actorName = notification.data.actor?.name?.lowercase()
                        actorName != null && current.blockedUsernames.contains(actorName)
                    }
                    _uiState.value = _uiState.value.copy(
                        readNotifications = _uiState.value.readNotifications + filteredRead,
                        readNotificationsPage = nextPage,
                        readNotificationsLast = response.last
                    )
                }
            }
            _uiState.value = _uiState.value.copy(notificationsLoadingMore = false)
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

    fun clearScrollToTop() {
        _uiState.value = _uiState.value.copy(scrollToTop = false)
    }

    fun clearNewPostsUsernames() {
        _uiState.value = _uiState.value.copy(newPostsUsernames = emptyList())
    }

    fun openEditProfile() {
        val currentProfile = _uiState.value.accountProfile ?: return
        _uiState.value = _uiState.value.copy(
            showEditProfile = true,
            editProfileBio = currentProfile.bio ?: "",
            editProfileError = null
        )
    }

    fun closeEditProfile() {
        _uiState.value = _uiState.value.copy(
            showEditProfile = false,
            editProfileBio = "",
            editProfileError = null
        )
    }

    fun setEditProfileBio(bio: String) {
        _uiState.value = _uiState.value.copy(editProfileBio = bio)
    }

    fun updateProfileBio() {
        val session = _uiState.value.session ?: return
        val bio = _uiState.value.editProfileBio
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(editProfileLoading = true, editProfileError = null)
            runCatching { repository.updateBio(session, bio) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        editProfileLoading = false,
                        showEditProfile = false,
                        toastMessage = "Bio updated!"
                    )
                    refreshAccount()
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        editProfileLoading = false,
                        editProfileError = throwable.message ?: "Failed to update bio"
                    )
                }
        }
    }

    fun deleteProfilePicture() {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(editProfileLoading = true, editProfileError = null)
            runCatching { repository.deleteProfilePicture(session) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        editProfileLoading = false,
                        profileCacheBuster = System.currentTimeMillis(),
                        toastMessage = "Profile picture deleted!"
                    )
                    refreshAccount()
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        editProfileLoading = false,
                        editProfileError = throwable.message ?: "Failed to delete profile picture"
                    )
                }
        }
    }

    fun uploadProfilePicture(bytes: ByteArray) {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(editProfileLoading = true, editProfileError = null)
            runCatching { repository.uploadProfilePicture(session, bytes) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        editProfileLoading = false,
                        profileCacheBuster = System.currentTimeMillis(),
                        toastMessage = "Profile picture updated!"
                    )
                    refreshAccount()
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        editProfileLoading = false,
                        editProfileError = throwable.message ?: "Failed to upload profile picture"
                    )
                }
        }
    }

    fun deleteBanner() {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(editProfileLoading = true, editProfileError = null)
            runCatching { repository.deleteBanner(session) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        editProfileLoading = false,
                        profileCacheBuster = System.currentTimeMillis(),
                        toastMessage = "Banner deleted!"
                    )
                    refreshAccount()
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        editProfileLoading = false,
                        editProfileError = throwable.message ?: "Failed to delete banner"
                    )
                }
        }
    }

    fun uploadBanner(bytes: ByteArray) {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(editProfileLoading = true, editProfileError = null)
            runCatching { repository.uploadBanner(session, bytes) }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        editProfileLoading = false,
                        profileCacheBuster = System.currentTimeMillis(),
                        toastMessage = "Banner updated!"
                    )
                    refreshAccount()
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        editProfileLoading = false,
                        editProfileError = throwable.message ?: "Failed to upload banner"
                    )
                }
        }
    }

    fun saveCurrentDraft() {
        if (_uiState.value.composeEditPostId != null) return
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
                followedUsernames = current.followedUsernames - normalized,
                toastMessage = if (reported) "You've reported this user" else null
            )

            // Background automatic unfollow
            val session = current.session
            if (session != null) {
                viewModelScope.launch(Dispatchers.IO) {
                    runCatching {
                        val isFollowing = repository.getFollowStatus(session, username, session.username)
                        if (isFollowing) {
                            repository.toggleFollow(session, username)
                        }
                    }
                }
            }
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
                val filtered = response.followers.filterNot { _uiState.value.blockedUsernames.contains(it.name.lowercase()) }
                _uiState.value = _uiState.value.copy(
                    userListToShow = filtered,
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
                val filtered = response.following.filterNot { _uiState.value.blockedUsernames.contains(it.name.lowercase()) }
                _uiState.value = _uiState.value.copy(
                    userListToShow = filtered,
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
                        val filtered = response.followers.filterNot { _uiState.value.blockedUsernames.contains(it.name.lowercase()) }
                        _uiState.value = _uiState.value.copy(
                            userListToShow = (_uiState.value.userListToShow ?: emptyList()) + filtered,
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
                        val filtered = response.following.filterNot { _uiState.value.blockedUsernames.contains(it.name.lowercase()) }
                        _uiState.value = _uiState.value.copy(
                            userListToShow = (_uiState.value.userListToShow ?: emptyList()) + filtered,
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

    fun toggleFollowUser(username: String) {
        val session = _uiState.value.session ?: return
        if (session.username.equals(username, ignoreCase = true)) return
        val normalized = username.lowercase()
        val isViewingThisProfile = _uiState.value.viewingProfileUsername?.equals(username, ignoreCase = true) == true
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                followLoadingUsernames = _uiState.value.followLoadingUsernames + normalized,
                viewingProfileFollowLoading = if (isViewingThisProfile) true else _uiState.value.viewingProfileFollowLoading
            )
            runCatching { repository.toggleFollow(session, username) }
                .onSuccess { response ->
                    val isNowFollowing = response.new.isFollowing
                    val currentFollowed = _uiState.value.followedUsernames.toMutableSet()
                    if (isNowFollowing) {
                        currentFollowed.add(normalized)
                    } else {
                        currentFollowed.remove(normalized)
                    }
                    
                    val currentProfile = _uiState.value.viewingProfile
                    
                    _uiState.value = _uiState.value.copy(
                        followedUsernames = currentFollowed,
                        followLoadingUsernames = _uiState.value.followLoadingUsernames - normalized,
                        viewingProfileIsFollowing = if (isViewingThisProfile) isNowFollowing else _uiState.value.viewingProfileIsFollowing,
                        viewingProfileFollowLoading = if (isViewingThisProfile) false else _uiState.value.viewingProfileFollowLoading,
                        viewingProfile = if (isViewingThisProfile && currentProfile != null) {
                            currentProfile.copy(stats = currentProfile.stats?.copy(followers = response.new.followers))
                        } else currentProfile
                    )
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        followLoadingUsernames = _uiState.value.followLoadingUsernames - normalized,
                        viewingProfileFollowLoading = if (isViewingThisProfile) false else _uiState.value.viewingProfileFollowLoading,
                        errorMessage = throwable.message ?: "Unable to update follow"
                    )
                }
        }
    }

    fun toggleViewedProfileFollow() {
        val username = _uiState.value.viewingProfileUsername ?: return
        toggleFollowUser(username)
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
            // Always apply transform to selectedPost so that liking an embedded repost
            // (where selectedPost.id != postId but selectedPost.repost.id == postId) also
            // updates the love state shown in the details sheet.
            selectedPost = current.selectedPost?.let { transform(it) }
        )
    }

    private fun filterBlockedPosts(posts: List<Post>): List<Post> =
        posts.filterNotBlocked(_uiState.value.blockedUsernames, _uiState.value.blockedQuoteHandling)

    private fun List<Post>.filterNotBlocked(username: String): List<Post> =
        filterNot { it.poster.name.equals(username, ignoreCase = true) }

    private fun List<Post>.filterNotBlocked(blockedUsernames: Set<String>, quoteHandling: String = "warning"): List<Post> {
        return filter { post ->
            val posterBlocked = blockedUsernames.contains(post.poster.name.lowercase())
            if (posterBlocked) return@filter false
            
            val repost = post.repost
            if (repost != null) {
                val repostPosterBlocked = blockedUsernames.contains(repost.poster.name.lowercase())
                if (repostPosterBlocked) {
                    val isPureRepost = post.content.replace(Regex("<.*?>"), "").trim().isBlank()
                    if (isPureRepost) {
                        return@filter false
                    } else {
                        if (quoteHandling == "hide_post") {
                            return@filter false
                        }
                    }
                }
            }
            true
        }
    }

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
                    if (session == null) {
                        delay(300)
                    }
                    val oldSession = _uiState.value.session
                    _uiState.value = _uiState.value.copy(session = session)
                    val sessionChanged = oldSession?.username != session?.username
                    if (sessionChanged) {
                        _uiState.value = _uiState.value.copy(feed = emptyList())
                    }
                    coroutineScope {
                        launch { loadFeedForCurrentSession(session, forceClear = sessionChanged) }
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
                    feed = current.feed.filterNotBlocked(blockedUsernames, current.blockedQuoteHandling),
                    exploreTrendingPosts = current.exploreTrendingPosts.filterNotBlocked(blockedUsernames, current.blockedQuoteHandling),
                    accountPosts = current.accountPosts.filterNotBlocked(blockedUsernames, current.blockedQuoteHandling),
                    viewingProfilePosts = current.viewingProfilePosts.filterNotBlocked(blockedUsernames, current.blockedQuoteHandling),
                    userListToShow = current.userListToShow?.filterNot { blockedUsernames.contains(it.name.lowercase()) },
                    selectedPost = current.selectedPost?.takeUnless {
                        blockedUsernames.contains(it.poster.name.lowercase())
                    }
                )
            }
        }
    }

    private suspend fun loadFeedForCurrentSession(session: AuthSession?, forceClear: Boolean = false) {
        if (forceClear) {
            _uiState.value = _uiState.value.copy(feed = emptyList())
        }
        _uiState.value = _uiState.value.copy(feedLoading = true, errorMessage = null)
        runCatching {
            val response = repository.loadFeed(session, 1)
            val posts = if (session != null) augmentLoveStatuses(response.posts, session) else response.posts
            filterBlockedPosts(posts) to response.last
        }.onSuccess { (posts, isLast) ->
            val oldFeedIds = _uiState.value.feed.map { it.id }.toSet()
            val newPosts = if (oldFeedIds.isNotEmpty()) {
                posts.filter { it.id !in oldFeedIds }
            } else {
                emptyList()
            }
            val newPostsUsernames = newPosts.map { it.poster.name }.distinct().take(3)

            _uiState.value = _uiState.value.copy(
                feed = posts,
                feedLoading = false,
                feedLast = isLast,
                feedPage = 1,
                newPostsUsernames = newPostsUsernames
            )
        }.onFailure { throwable ->
            _uiState.value = _uiState.value.copy(
                feedLoading = false,
                errorMessage = throwable.message ?: "Unable to load feed"
            )
        }
    }

    private fun handleBlockedNotifications(
        notifications: List<Notification>,
        session: AuthSession?,
        blockedUsernames: Set<String>
    ): List<Notification> {
        if (blockedUsernames.isEmpty()) return notifications

        val blockedList = notifications.filter { notification ->
            val actorName = notification.data.actor?.name?.lowercase()
            actorName != null && blockedUsernames.contains(actorName)
        }

        if (blockedList.isNotEmpty() && session != null) {
            val blockedIds = blockedList.map { it.id }
            viewModelScope.launch(Dispatchers.IO) {
                runCatching {
                    repository.markNotificationsRead(session, blockedIds)
                }
            }
        }

        return notifications.filterNot { notification ->
            val actorName = notification.data.actor?.name?.lowercase()
            actorName != null && blockedUsernames.contains(actorName)
        }
    }

    private suspend fun loadNotifications(session: AuthSession?) {
        if (session == null) {
            _uiState.value = _uiState.value.copy(
                unreadNotifications = emptyList(),
                readNotifications = emptyList(),
                notificationsLoading = false,
                unreadNotificationsPage = 1,
                unreadNotificationsLast = false,
                readNotificationsPage = 1,
                readNotificationsLast = false
            )
            return
        }

        _uiState.value = _uiState.value.copy(notificationsLoading = true)
        val unreadResponse = runCatching { repository.loadUnreadNotifications(session, 1) }
            .getOrDefault(wombat.joshattic.us.data.model.NotificationResponse(emptyList(), null, true))

        val rawUnread = unreadResponse.unread.orEmpty()
        val unread = handleBlockedNotifications(rawUnread, session, _uiState.value.blockedUsernames)
        val unreadLast = unreadResponse.last

        val readResponse = if (unreadLast) {
            runCatching { repository.loadReadNotifications(session, 1) }.getOrNull()
        } else {
            null
        }

        val rawRead = readResponse?.read.orEmpty()
        val read = rawRead.filterNot { notification ->
            val actorName = notification.data.actor?.name?.lowercase()
            actorName != null && _uiState.value.blockedUsernames.contains(actorName)
        }
        val readLast = readResponse?.last ?: false

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
            unreadNotificationsPage = 1,
            unreadNotificationsLast = unreadLast,
            readNotificationsPage = 1,
            readNotificationsLast = readLast,
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

    private fun filterBlockedComments(comments: List<Comment>, blockedUsernames: Set<String>): List<Comment> {
        return comments.mapNotNull { comment ->
            val posterNameLower = comment.poster.name.lowercase()
            val isBlocked = blockedUsernames.contains(posterNameLower)
            val filteredReplies = filterBlockedComments(comment.replies ?: emptyList(), blockedUsernames)
            if (isBlocked) {
                if (filteredReplies.isNotEmpty()) {
                    comment.copy(
                        content = "This comment is from a user you blocked",
                        replies = filteredReplies
                    )
                } else {
                    null
                }
            } else {
                comment.copy(replies = filteredReplies)
            }
        }
    }

    private suspend fun loadComments(postId: String) {
        val session = _uiState.value.session
        _uiState.value = _uiState.value.copy(commentsLoading = true)
        runCatching {
            val topLevel = repository.loadComments(session, postId).comments
                .map { it.copy(replies = it.replies ?: emptyList()) }
            val fullComments = topLevel.map { loadRepliesRecursively(it, session) }
            filterBlockedComments(fullComments, _uiState.value.blockedUsernames)
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

    private fun observeSettings() {
        val prefs = repository.settingsPreferences
        viewModelScope.launch {
            prefs.showImagesInFeed.collectLatest { value ->
                _uiState.value = _uiState.value.copy(showImagesInFeed = value)
            }
        }
        viewModelScope.launch {
            prefs.showNewPostsPopup.collectLatest { value ->
                _uiState.value = _uiState.value.copy(showNewPostsPopup = value)
            }
        }
        viewModelScope.launch {
            prefs.inAppNotifications.collectLatest { value ->
                _uiState.value = _uiState.value.copy(inAppNotifications = value)
            }
        }
        viewModelScope.launch {
            prefs.markReadWhenOpened.collectLatest { value ->
                _uiState.value = _uiState.value.copy(markReadWhenOpened = value)
            }
        }
        viewModelScope.launch {
            prefs.markReadWhenTabOpened.collectLatest { value ->
                _uiState.value = _uiState.value.copy(markReadWhenTabOpened = value)
            }
        }
        viewModelScope.launch {
            prefs.openLinksInApp.collectLatest { value ->
                _uiState.value = _uiState.value.copy(openLinksInApp = value)
            }
        }
        viewModelScope.launch {
            prefs.wearAccount.collectLatest { value ->
                _uiState.value = _uiState.value.copy(wearAccount = value)
            }
        }
        viewModelScope.launch {
            prefs.wearShowImages.collectLatest { value ->
                _uiState.value = _uiState.value.copy(wearShowImages = value)
            }
        }
        viewModelScope.launch {
            prefs.wearShowProfilePictures.collectLatest { value ->
                _uiState.value = _uiState.value.copy(wearShowProfilePictures = value)
            }
        }
        viewModelScope.launch {
            prefs.wearFeedType.collectLatest { value ->
                _uiState.value = _uiState.value.copy(wearFeedType = value)
            }
        }
        viewModelScope.launch {
            prefs.blockedQuoteHandling.collectLatest { value ->
                val current = _uiState.value
                _uiState.value = current.copy(
                    blockedQuoteHandling = value,
                    feed = current.feed.filterNotBlocked(current.blockedUsernames, value),
                    exploreTrendingPosts = current.exploreTrendingPosts.filterNotBlocked(current.blockedUsernames, value),
                    accountPosts = current.accountPosts.filterNotBlocked(current.blockedUsernames, value),
                    viewingProfilePosts = current.viewingProfilePosts.filterNotBlocked(current.blockedUsernames, value)
                )
            }
        }
    }

    fun openSettings() {
        _uiState.value = _uiState.value.copy(showSettings = true, settingsCategory = null)
    }

    fun closeSettings() {
        _uiState.value = _uiState.value.copy(showSettings = false, settingsCategory = null)
    }

    fun selectSettingsCategory(category: wombat.joshattic.us.ui.state.SettingsCategory?) {
        _uiState.value = _uiState.value.copy(settingsCategory = category)
    }

    fun setShowImagesInFeed(value: Boolean) {
        viewModelScope.launch { repository.settingsPreferences.setShowImagesInFeed(value) }
    }

    fun setShowNewPostsPopup(value: Boolean) {
        viewModelScope.launch { repository.settingsPreferences.setShowNewPostsPopup(value) }
    }

    fun setInAppNotifications(value: Boolean) {
        viewModelScope.launch { repository.settingsPreferences.setInAppNotifications(value) }
    }

    fun setMarkReadWhenOpened(value: Boolean) {
        viewModelScope.launch { repository.settingsPreferences.setMarkReadWhenOpened(value) }
    }

    fun setMarkReadWhenTabOpened(value: Boolean) {
        viewModelScope.launch { repository.settingsPreferences.setMarkReadWhenTabOpened(value) }
    }

    fun setOpenLinksInApp(value: Boolean) {
        viewModelScope.launch { repository.settingsPreferences.setOpenLinksInApp(value) }
    }

    fun setWearAccount(value: String) {
        viewModelScope.launch { repository.settingsPreferences.setWearAccount(value) }
    }

    fun setWearShowImages(value: Boolean) {
        viewModelScope.launch { repository.settingsPreferences.setWearShowImages(value) }
    }

    fun setWearShowProfilePictures(value: Boolean) {
        viewModelScope.launch { repository.settingsPreferences.setWearShowProfilePictures(value) }
    }

    fun setWearFeedType(value: String) {
        viewModelScope.launch { repository.settingsPreferences.setWearFeedType(value) }
    }

    fun setBlockedQuoteHandling(value: String) {
        viewModelScope.launch { repository.settingsPreferences.setBlockedQuoteHandling(value) }
    }

    suspend fun uploadImage(context: Context, uri: Uri): String {
        return repository.uploadImageToProxy(context, uri)
    }

    fun unblockUser(username: String) {
        viewModelScope.launch {
            repository.unblockUser(username)
        }
    }

    fun followJoshAtticus() {
        val session = _uiState.value.session
        if (session == null) {
            _uiState.value = _uiState.value.copy(toastMessage = "You're not signed in!")
            return
        }
        viewModelScope.launch {
            runCatching {
                val isFollowing = repository.getFollowStatus(session, "joshatticus", session.username)
                if (isFollowing) {
                    "You're already following me, thanks :D"
                } else {
                    repository.toggleFollow(session, "joshatticus")
                    "Thanks for following me :D"
                }
            }.onSuccess { message ->
                _uiState.value = _uiState.value.copy(toastMessage = message)
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(toastMessage = throwable.message ?: "Oh no! Something went wrong D:")
            }
        }
    }

    private var lastBackgroundTime = 0L

    fun onAppBackgrounded() {
        lastBackgroundTime = System.currentTimeMillis()
    }

    fun onAppResumed(onRefreshTriggered: () -> Unit = {}) {
        val currentTime = System.currentTimeMillis()
        // Auto refresh if we've been in the background for more than 5 minutes
        if (lastBackgroundTime > 0 && currentTime - lastBackgroundTime > 5 * 60 * 1000) {
            refreshFeed()
            loadExploreTrending()
            onRefreshTriggered()
        }
    }

    fun clearBlockedWarning() {
        _uiState.value = _uiState.value.copy(
            blockedWarningTarget = null,
            commentsLoading = false
        )
    }

    fun bypassBlockedWarning() {
        val target = _uiState.value.blockedWarningTarget ?: return
        _uiState.value = _uiState.value.copy(blockedWarningTarget = null)
        when (target) {
            is BlockedWarningTarget.Profile -> {
                openProfileBypassingBlock(target.username)
            }
            is BlockedWarningTarget.Post -> {
                openPostBypassingBlock(target.post)
            }
        }
    }

    companion object {
        private const val DEFAULT_GUEST_USER = "jeffalo"
        const val MAX_CHAR_COUNT = 1500

        fun factory(repository: WombatRepository, context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(repository, NetworkConnectivityObserver(context)) as T
                }
            }
        }
    }
}
