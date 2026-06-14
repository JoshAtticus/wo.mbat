package wombat.joshattic.us.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: String,
    val name: String,
    val bio: String?,
    val verified: Boolean,
    val color: String,
    val stats: UserStats?,
    val online: Boolean
)

data class UserStats(val followers: Int, val following: Int, val posts: Int)

data class FeedResponse(val posts: List<Post>, val last: Boolean)

data class Post(
    @SerializedName("_id") val id: String,
    val poster: Poster,
    val content: String,
    val repost: Post? = null,
    val time: Long,
    val comments: Int,
    val loves: Int,
    val reposts: Int,
    val isLoving: Boolean? = null
)

data class Poster(
    val id: String,
    val name: String,
    val color: String
)

data class NotificationResponse(
    val unread: List<Notification>?,
    val read: List<Notification>?,
    val last: Boolean
)

data class Notification(
    @SerializedName("_id") val id: String,
    val type: String,
    val to: Poster,
    val data: NotificationData,
    val read: Boolean,
    val time: Long
)

data class NotificationData(val actor: Poster, val post: Post? = null)

data class CommentResponse(val comments: List<Comment>, val last: Boolean)

data class Comment(
    @SerializedName("_id") val id: String,
    val post: String,
    val poster: Poster,
    val parent: String?,
    val content: String,
    val time: Long,
    val hasReplies: Boolean,
    val top: String? = null,
    val replies: List<Comment> = emptyList()
)

data class LoginRequest(val username: String, val password: String)

data class LoginResponse(val token: String)

data class CreatePostRequest(val post: String, val repost: String? = null)

data class MarkReadRequest(val messages: List<String>)

data class CreateCommentRequest(val content: String, val parent: String? = null)

data class AuthSession(
    val token: String,
    val username: String
)

data class LoveToggleResponse(
    val ok: String,
    val new: LoveState
)

data class LoveState(
    val isLoving: Boolean,
    val loves: Int
)