package wombat.joshattic.us.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import wombat.joshattic.us.data.model.AuthSession
import wombat.joshattic.us.data.model.Comment
import wombat.joshattic.us.data.model.CommentResponse
import wombat.joshattic.us.data.model.CreateCommentRequest
import wombat.joshattic.us.data.model.CreatePostRequest
import wombat.joshattic.us.data.model.FeedResponse
import wombat.joshattic.us.data.model.FollowersResponse
import wombat.joshattic.us.data.model.FollowingResponse
import wombat.joshattic.us.data.model.LoginRequest
import wombat.joshattic.us.data.model.LoveToggleResponse
import wombat.joshattic.us.data.model.Notification
import wombat.joshattic.us.data.model.Permissions
import wombat.joshattic.us.data.model.Post
import wombat.joshattic.us.data.model.User
import wombat.joshattic.us.data.network.ApiService
import wombat.joshattic.us.data.storage.AuthPreferences
import wombat.joshattic.us.data.storage.BlockedUsersDatabase

class WombatRepository(
    private val apiService: ApiService,
    private val authPreferences: AuthPreferences,
    private val blockedUsersDatabase: BlockedUsersDatabase
) {
    private val socketManager = wombat.joshattic.us.data.network.WasteofSocketManager()
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    val sessionFlow: Flow<AuthSession?> = authPreferences.sessionFlow
    val sessionsFlow: Flow<List<AuthSession>> = authPreferences.sessionsFlow
    val blockedUsernamesFlow: Flow<Set<String>> = blockedUsersDatabase.blockedUsernamesFlow
    val unreadSocketCount: StateFlow<Int> = socketManager.unreadCount

    init {
        repositoryScope.launch {
            sessionFlow.collect { session ->
                if (session != null) {
                    socketManager.connect(session.token)
                } else {
                    socketManager.disconnect()
                }
            }
        }
    }

    suspend fun login(username: String, password: String): Result<AuthSession> = runCatching {
        val loginResponse = apiService.login(LoginRequest(username = username, password = password))
        authPreferences.saveSession(loginResponse.token, username)
        AuthSession(token = loginResponse.token, username = username)
    }

    suspend fun loadFeed(session: AuthSession?, page: Int = 1): FeedResponse {
        val username = session?.username ?: DEFAULT_GUEST_USER
        return apiService.getFeed(username = username, page = page, token = session?.token)
    }

    suspend fun loadUserProfile(username: String): User {
        return apiService.getUserProfile(username)
    }

    suspend fun loadUserPosts(session: AuthSession?, username: String, page: Int = 1): FeedResponse {
        return apiService.getUserPosts(username = username, page = page, token = session?.token)
    }

    suspend fun loadComments(session: AuthSession?, postId: String) =
        apiService.getComments(postId = postId, token = session?.token)

    suspend fun loadCommentReplies(session: AuthSession?, commentId: String, page: Int = 1): CommentResponse =
        apiService.getCommentReplies(commentId, page, session?.token)

    suspend fun loadTrendingPosts(session: AuthSession?): FeedResponse {
        return apiService.getTrendingPosts(session?.token)
    }

    suspend fun loadUnreadNotifications(session: AuthSession?): List<Notification> {
        val token = session?.token ?: return emptyList()
        return apiService.getUnreadNotifications(token).unread.orEmpty()
    }

    suspend fun getUnreadCount(token: String): Int {
        return try {
            apiService.getUnreadNotifications(token).unread.orEmpty().size
        } catch (e: Exception) {
            0
        }
    }

    suspend fun loadReadNotifications(session: AuthSession?): List<Notification> {
        val token = session?.token ?: return emptyList()
        return apiService.getReadNotifications(token).read.orEmpty()
    }

    suspend fun createPost(session: AuthSession, htmlContent: String, repostId: String? = null): Post {
        return apiService.makePost(
            token = session.token,
            request = CreatePostRequest(post = htmlContent, repost = repostId)
        )
    }

    suspend fun deletePost(session: AuthSession, postId: String) {
        apiService.deletePost(session.token, postId)
    }

    suspend fun reportPost(session: AuthSession, postId: String, reason: String) {
        apiService.reportPost(
            postId = postId,
            token = session.token,
            request = wombat.joshattic.us.data.model.ReportRequest(reason = reason)
        )
    }

    suspend fun createComment(session: AuthSession, postId: String, content: String, parent: String?): Comment {
        return apiService.makeComment(
            postId = postId,
            token = session.token,
            request = CreateCommentRequest(content = content, parent = parent)
        )
    }

    suspend fun markNotificationsRead(session: AuthSession, notificationIds: List<String>) {
        if (notificationIds.isNotEmpty()) {
            apiService.markRead(session.token, wombat.joshattic.us.data.model.MarkReadRequest(notificationIds))
        }
    }

    suspend fun toggleLove(session: AuthSession, postId: String): LoveToggleResponse {
        return apiService.togglePostLove(postId, session.token)
    }

    suspend fun getPostLoveStatus(session: AuthSession?, postId: String, username: String): Boolean {
        return apiService.getPostLoveStatus(postId, username, session?.token)
    }

    suspend fun getFollowStatus(session: AuthSession?, username: String, follower: String): Boolean {
        return apiService.getFollowStatus(username, follower, session?.token)
    }

    suspend fun toggleFollow(session: AuthSession, username: String) =
        apiService.toggleFollow(username, session.token)

    suspend fun getFollowers(session: AuthSession?, username: String, page: Int = 1): FollowersResponse {
        return apiService.getFollowers(username, session?.token, page)
    }

    suspend fun getFollowing(session: AuthSession?, username: String, page: Int = 1): FollowingResponse {
        return apiService.getFollowing(username, session?.token, page)
    }

    suspend fun blockUser(username: String) {
        blockedUsersDatabase.block(username)
    }

    suspend fun unblockUser(username: String) {
        blockedUsersDatabase.unblock(username)
    }

    suspend fun switchAccount(username: String) {
        authPreferences.switchAccount(username)
    }

    suspend fun logout(username: String? = null) {
        if (username != null) {
            authPreferences.removeSession(username)
        } else {
            val active = sessionFlow.firstOrNull()
            if (active != null) {
                authPreferences.removeSession(active.username)
            } else {
                authPreferences.clearSession()
            }
        }
    }

    suspend fun getAdminMessages(token: String): List<Notification> {
        return apiService.getAdminMessages(token)
    }

    suspend fun getSession(token: String): wombat.joshattic.us.data.model.SessionResponse {
        return apiService.getSession(token)
    }

    companion object {
        private const val DEFAULT_GUEST_USER = "jeffalo"
    }
}
