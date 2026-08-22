package wombat.joshattic.us.wear.data

import kotlinx.coroutines.async
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import wombat.joshattic.us.wear.data.model.*
import wombat.joshattic.us.wear.data.network.ApiService

class WearRepository(
    private val api: ApiService,
    private val authPreferences: WearAuthPreferences
) {
    val sessionFlow = authPreferences.sessionFlow

    /** Fetch a user's public profile (includes stats, history, and has-media flags). */
    suspend fun loadUserProfile(session: AuthSession?, username: String): User =
        api.getUser(username = username, includeHasMedia = "", token = session?.token)

    /** Paginated posts for a given user. Pinned posts are prepended on page 1. */
    suspend fun loadUserPosts(session: AuthSession?, username: String, page: Int = 1): UserPostsResponse =
        api.getUserPosts(username = username, page = page, token = session?.token)

    /** Full-text search over post content. */
    suspend fun searchPosts(session: AuthSession?, query: String, page: Int = 1): SearchPostsResponse =
        api.searchPosts(query = query, page = page, token = session?.token)

    /** Load feed page for the authenticated user, falling back to trending if guest. */
    suspend fun loadFeed(session: AuthSession?, page: Int = 1): List<Post> {
        return if (session != null) {
            api.getFeed(username = session.username, page = page, token = session.token).posts
        } else {
            api.getTrendingPosts().posts
        }
    }

    suspend fun loadTrendingFeed(session: AuthSession?): List<Post> {
        return api.getTrendingPosts(token = session?.token).posts
    }

    suspend fun loadComments(session: AuthSession?, postId: String): List<Comment> =
        loadCommentsWithReplies(session, postId)

    suspend fun loadCommentReplies(session: AuthSession?, commentId: String, page: Int = 1): CommentResponse =
        api.getCommentReplies(commentId, page, session?.token)

    suspend fun loadCommentsWithReplies(session: AuthSession?, postId: String): List<Comment> {
        val rootComments = api.getComments(postId = postId, token = session?.token).comments
        return rootComments.map { loadRepliesRecursively(it, session) }
    }

    private suspend fun loadRepliesRecursively(comment: Comment, session: AuthSession?): Comment {
        if (!comment.hasReplies) return comment.copy(replies = comment.replies ?: emptyList())
        val allReplies = mutableListOf<Comment>()
        var page = 1
        while (true) {
            val resp = runCatching { api.getCommentReplies(comment.id, page, session?.token) }.getOrNull() ?: break
            allReplies.addAll(resp.comments)
            if (resp.last) break
            page++
        }
        val loaded = allReplies.map { loadRepliesRecursively(it, session) }
        return comment.copy(replies = loaded)
    }

    suspend fun toggleLove(session: AuthSession, postId: String): LoveToggleResponse =
        api.togglePostLove(postId = postId, token = session.token)

    suspend fun getPostLoveStatus(session: AuthSession?, postId: String, username: String): Boolean =
        api.getPostLoveStatus(postId, username, session?.token)

    suspend fun createPost(session: AuthSession, htmlContent: String): Post =
        api.makePost(token = session.token, request = CreatePostRequest(post = htmlContent))

    suspend fun createRepost(session: AuthSession, postId: String): Post =
        api.createRepost(token = session.token, postId = postId)

    suspend fun createQuote(session: AuthSession, postId: String, htmlContent: String): Post =
        api.makePost(token = session.token, request = CreatePostRequest(post = htmlContent, repost = postId))

    suspend fun createComment(
        session: AuthSession,
        postId: String,
        content: String,
        parent: String? = null
    ): Comment = api.makeComment(
        postId = postId,
        token = session.token,
        request = CreateCommentRequest(content = content, parent = parent)
    )

    suspend fun loadUnreadNotifications(session: AuthSession): List<Notification> = coroutineScope {
        val unreadDeferred = async(Dispatchers.IO) { api.getUnreadNotifications(session.token).unread.orEmpty() }
        val readDeferred = async(Dispatchers.IO) { api.getReadNotifications(session.token).read.orEmpty() }
        val unread = unreadDeferred.await()
        val read = readDeferred.await()
        (unread + read).sortedWith(compareBy({ it.read }, { -it.time }))
    }

    suspend fun markNotificationsRead(session: AuthSession, ids: List<String>) {
        if (ids.isNotEmpty()) api.markRead(session.token, MarkReadRequest(ids))
    }
}
