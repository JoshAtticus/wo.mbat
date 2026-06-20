package wombat.joshattic.us.data.model

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName

@Immutable
data class User(
    val id: String,
    val name: String,
    val bio: String?,
    val verified: Boolean,
    val color: String,
    val stats: UserStats?,
    val online: Boolean,
    val permissions: Permissions? = null
)

@Immutable
data class UserStats(val followers: Int, val following: Int, val posts: Int)

data class FeedResponse(val posts: List<Post>, val pinned: List<Post>? = null, val last: Boolean)

@Immutable
data class Post(
    @SerializedName("_id") val id: String,
    val poster: Poster,
    val content: String,
    val repost: Post? = null,
    val time: Long,
    val comments: Int,
    val loves: Int,
    val reposts: Int,
    val pinned: Boolean? = null,
    val isLoving: Boolean? = null
)

@Immutable
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

@Immutable
data class Notification(
    @SerializedName("_id") val id: String,
    val type: String,
    val to: Poster,
    val data: NotificationData,
    val read: Boolean,
    val time: Long
)

@Immutable
data class NotificationData(
    val actor: Poster? = null,
    val post: Post? = null,
    val comment: Comment? = null,
    val content: String? = null,
    val wall: Poster? = null
)

data class CommentResponse(val comments: List<Comment>, val last: Boolean)

@Immutable
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

data class SessionResponse(val user: User? = null)

data class Permissions(val admin: Boolean, val banned: Boolean)

data class CreatePostRequest(val post: String, val repost: String? = null)

data class EditPostRequest(val post: String)

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

data class FollowToggleResponse(
    val ok: String,
    val new: FollowState
)

data class FollowState(
    val isFollowing: Boolean,
    val followers: Int,
    val following: Int
)

data class ReportRequest(
    val type: String = "none",
    val reason: String
)

data class FollowersResponse(
    val followers: List<User>,
    val last: Boolean
)

data class FollowingResponse(
    val following: List<User>,
    val last: Boolean
)
