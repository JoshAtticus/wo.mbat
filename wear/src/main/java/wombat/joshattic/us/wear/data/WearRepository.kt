package wombat.joshattic.us.wear.data

import wombat.joshattic.us.wear.data.model.*
import wombat.joshattic.us.wear.data.network.ApiService

class WearRepository(
    private val api: ApiService,
    private val authPreferences: WearAuthPreferences
) {
    val sessionFlow = authPreferences.sessionFlow

    /** Load feed page for the authenticated user, falling back to trending if guest. */
    suspend fun loadFeed(session: AuthSession?, page: Int = 1): List<Post> {
        return if (session != null) {
            api.getFeed(username = session.username, page = page, token = session.token).posts
        } else {
            api.getTrendingPosts().posts
        }
    }

    suspend fun loadComments(session: AuthSession?, postId: String): List<Comment> =
        api.getComments(postId = postId, token = session?.token).comments

    suspend fun toggleLove(session: AuthSession, postId: String): LoveToggleResponse =
        api.togglePostLove(postId = postId, token = session.token)

    suspend fun createPost(session: AuthSession, htmlContent: String): Post =
        api.makePost(token = session.token, request = CreatePostRequest(post = htmlContent))

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

    suspend fun loadUnreadNotifications(session: AuthSession): List<Notification> =
        api.getUnreadNotifications(session.token).unread.orEmpty()

    suspend fun markNotificationsRead(session: AuthSession, ids: List<String>) {
        if (ids.isNotEmpty()) api.markRead(session.token, MarkReadRequest(ids))
    }
}
