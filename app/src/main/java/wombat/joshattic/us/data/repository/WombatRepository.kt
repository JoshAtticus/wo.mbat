package wombat.joshattic.us.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import wombat.joshattic.us.data.model.AuthSession
import wombat.joshattic.us.data.model.EditPostRequest
import wombat.joshattic.us.data.model.UpdateBioRequest
import wombat.joshattic.us.data.model.Comment
import wombat.joshattic.us.data.model.CommentResponse
import wombat.joshattic.us.data.model.CreateCommentRequest
import wombat.joshattic.us.data.model.CreatePostRequest
import wombat.joshattic.us.data.model.CreateWallCommentResponse
import wombat.joshattic.us.data.model.FeedResponse
import wombat.joshattic.us.data.model.FollowersResponse
import wombat.joshattic.us.data.model.FollowingResponse
import wombat.joshattic.us.data.model.LoginRequest
import wombat.joshattic.us.data.model.LoveToggleResponse
import wombat.joshattic.us.data.model.Notification
import wombat.joshattic.us.data.model.NotificationResponse
import wombat.joshattic.us.data.model.Permissions
import wombat.joshattic.us.data.model.Post
import wombat.joshattic.us.data.model.SearchPostsResponse
import wombat.joshattic.us.data.model.SearchUsersResponse
import wombat.joshattic.us.data.model.FrogResponse
import wombat.joshattic.us.data.model.User
import wombat.joshattic.us.data.network.ApiService
import wombat.joshattic.us.data.network.RetrofitClient
import wombat.joshattic.us.data.storage.AuthPreferences
import wombat.joshattic.us.data.storage.BlockedUsersDatabase
import wombat.joshattic.us.data.storage.SettingsPreferences

class WombatRepository(
    private val apiService: ApiService,
    private val authPreferences: AuthPreferences,
    private val blockedUsersDatabase: BlockedUsersDatabase,
    val settingsPreferences: SettingsPreferences,
    private val uploadApiService: ApiService = RetrofitClient.uploadApiService
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
        val cleanUsername = username.trim()
        authPreferences.saveSession(loginResponse.token, cleanUsername)
        AuthSession(token = loginResponse.token, username = cleanUsername)
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

    suspend fun loadPost(session: AuthSession?, postId: String): Post {
        return apiService.getPost(postId = postId, token = session?.token)
    }

    suspend fun loadComments(session: AuthSession?, postId: String) =
        apiService.getComments(postId = postId, token = session?.token)

    suspend fun loadCommentReplies(session: AuthSession?, commentId: String, page: Int = 1): CommentResponse =
        apiService.getCommentReplies(commentId, page, session?.token)

    suspend fun loadTrendingPosts(session: AuthSession?, timeframe: String? = null): FeedResponse {
        return apiService.getTrendingPosts(session?.token, timeframe)
    }

    suspend fun searchPosts(session: AuthSession?, query: String, page: Int = 1): SearchPostsResponse {
        return apiService.searchPosts(query = query, page = page, token = session?.token)
    }

    suspend fun searchUsers(session: AuthSession?, query: String, page: Int = 1): SearchUsersResponse {
        return apiService.searchUsers(query = query, page = page, token = session?.token)
    }

    suspend fun getFrog(): FrogResponse {
        return apiService.getFrog()
    }

    suspend fun loadUnreadNotifications(session: AuthSession?, page: Int = 1): NotificationResponse {
        val token = session?.token ?: return NotificationResponse(unread = emptyList(), read = null, last = true)
        return apiService.getUnreadNotifications(token, page)
    }

    suspend fun getUnreadCount(token: String): Int {
        return try {
            apiService.getUnreadNotifications(token, 1).unread.orEmpty().size
        } catch (e: Exception) {
            0
        }
    }

    suspend fun loadReadNotifications(session: AuthSession?, page: Int = 1): NotificationResponse {
        val token = session?.token ?: return NotificationResponse(unread = null, read = emptyList(), last = true)
        return apiService.getReadNotifications(token, page)
    }

    suspend fun createPost(session: AuthSession, htmlContent: String, repostId: String? = null): Post {
        return apiService.makePost(
            token = session.token,
            request = CreatePostRequest(post = htmlContent, repost = repostId)
        )
    }

    suspend fun editPost(session: AuthSession, postId: String, htmlContent: String): Post {
        return apiService.editPost(
            token = session.token,
            postId = postId,
            request = EditPostRequest(post = htmlContent)
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

    suspend fun loadWallComments(session: AuthSession?, username: String, page: Int = 1): CommentResponse {
        return apiService.getWallComments(username = username, page = page, token = session?.token)
    }

    suspend fun createWallComment(session: AuthSession, username: String, content: String, parentId: String?): CreateWallCommentResponse {
        return apiService.makeWallComment(
            username = username,
            token = session.token,
            request = CreateCommentRequest(content = content, parent = parentId)
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

    suspend fun updateBio(session: AuthSession, bio: String) {
        apiService.updateBio(session.username, session.token, UpdateBioRequest(bio))
    }

    suspend fun uploadProfilePicture(session: AuthSession, bytes: ByteArray) {
        val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("picture", "picture.jpg", requestBody)
        apiService.uploadProfilePicture(session.username, session.token, part)
    }

    suspend fun deleteProfilePicture(session: AuthSession) {
        apiService.deleteProfilePicture(session.username, session.token)
    }

    suspend fun uploadBanner(session: AuthSession, bytes: ByteArray) {
        val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("banner", "banner.jpg", requestBody)
        apiService.uploadBanner(session.username, session.token, part)
    }

    suspend fun deleteBanner(session: AuthSession) {
        apiService.deleteBanner(session.username, session.token)
    }

    suspend fun uploadImageToProxy(context: android.content.Context, uri: android.net.Uri): String {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalArgumentException("Could not read image from URI")
        val mimeType = context.contentResolver.getType(uri) ?: "image/*"
        val filename = uri.lastPathSegment ?: "image.jpg"
        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", filename, requestBody)
        val response = uploadApiService.uploadImageToProxy(
            url = IBBWOM_UPLOAD_URL,
            apiKey = IBBWOM_API_KEY,
            file = part
        )
        if (!response.success) throw Exception("Image upload failed")
        return response.url
    }

    companion object {
        private const val DEFAULT_GUEST_USER = "jeffalo"
        private const val IBBWOM_UPLOAD_URL = "https://ibbwom.joshattic.us/api/upload"
        private const val IBBWOM_API_KEY = "srv_Ucj2d4Um65VXtwY7VSRgK1FxKxXbzTfXHV-ZJhQNSqk"
    }
}
