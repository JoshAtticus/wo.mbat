package wombat.joshattic.us.data.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Immutable
@Serializable
data class User(
    val id: String = "",
    val name: String = "",
    val bio: String? = null,
    val verified: Boolean = false,
    val color: String = "",
    val stats: UserStats? = null,
    val online: Boolean = false,
    val permissions: Permissions? = null,
    val links: List<JsonElement> = emptyList(),
    val history: UserHistory? = null,
    val beta: Boolean = false
)

@Immutable
@Serializable
data class UserLink(
    val label: String? = null,
    val url: String = ""
)

@Immutable
@Serializable
data class UserHistory(val joined: Long = 0L)

@Immutable
@Serializable
data class UserStats(val followers: Int = 0, val following: Int = 0, val posts: Int = 0)

@Serializable
data class FeedResponse(val posts: List<Post> = emptyList(), val pinned: List<Post>? = null, val last: Boolean = false)

@Immutable
@Serializable
data class Post(
    @SerialName("_id") val id: String = "",
    val poster: Poster = Poster(),
    val content: String = "",
    val repost: Post? = null,
    val time: Long = 0L,
    val comments: Int = 0,
    val loves: Int = 0,
    val reposts: Int = 0,
    val pinned: Boolean? = null,
    val isLoving: Boolean? = null,
    val loveLoading: Boolean = false
)

@Immutable
@Serializable
data class Poster(
    val id: String = "",
    val name: String = "",
    val color: String = ""
)

@Immutable
@Serializable
data class OpenGraphMetadata(
    val title: String? = null,
    val description: String? = null,
    val image: String? = null,
    val url: String? = null
)

@Immutable
@Serializable
data class OpenGraphResponse(
    @SerialName("requested_url") val requestedUrl: String? = null,
    @SerialName("status_code") val statusCode: Int? = null,
    @SerialName("fetched_at_unix") val fetchedAtUnix: Long? = null,
    val metadata: OpenGraphMetadata? = null,
    val cached: Boolean? = null
)

@Serializable
data class NotificationResponse(
    val unread: List<Notification>? = null,
    val read: List<Notification>? = null,
    val last: Boolean = false
)

@Immutable
@Serializable
data class Notification(
    @SerialName("_id") val id: String = "",
    val type: String = "",
    val to: Poster = Poster(),
    val data: NotificationData = NotificationData(),
    val read: Boolean = false,
    val time: Long = 0L
)

@Immutable
@Serializable
data class NotificationData(
    val actor: Poster? = null,
    val post: Post? = null,
    val comment: Comment? = null,
    val content: String? = null,
    val wall: Poster? = null
)

@Serializable
data class CommentResponse(val comments: List<Comment> = emptyList(), val last: Boolean = false)

@Immutable
@Serializable
data class Comment(
    @SerialName("_id") val id: String = "",
    val post: String? = null,
    val poster: Poster = Poster(),
    val parent: String? = null,
    val content: String = "",
    val time: Long = 0L,
    val hasReplies: Boolean = false,
    val top: String? = null,
    val replies: List<Comment> = emptyList(),
    val repliesLoading: Boolean = false,
    val blocked: Boolean = false
)

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class LoginResponse(val token: String = "")

@Serializable
data class UsernameAvailableResponse(val available: Boolean = false)

@Serializable
data class SessionResponse(val user: User? = null)

@Serializable
data class Permissions(val admin: Boolean = false, val banned: Boolean = false)

@Serializable
data class CreatePostRequest(val post: String, val repost: String? = null)

@Serializable
data class EditPostRequest(val post: String)

@Serializable
data class MarkReadRequest(val messages: List<String>)

@Serializable
data class CreateCommentRequest(val content: String, val parent: String? = null)

@Serializable
data class CreateWallCommentResponse(val ok: String = "", val id: String = "")

@Serializable
data class CreateCommentResponse(val ok: String = "", val id: String = "")

@Serializable
data class CreatePostResponse(val ok: String = "", val id: String = "")

@Serializable
data class EditPostResponse(val ok: String = "", val new: Post = Post())

@Serializable
data class AuthSession(
    val token: String,
    val username: String
)

@Serializable
data class LoveToggleResponse(
    val ok: String = "",
    val new: LoveState = LoveState()
)

@Serializable
data class LoveState(
    val isLoving: Boolean = false,
    val loves: Int = 0
)

@Serializable
data class FollowToggleResponse(
    val ok: String = "",
    val new: FollowState = FollowState()
)

@Serializable
data class FollowState(
    val isFollowing: Boolean = false,
    val followers: Int = 0,
    val following: Int = 0
)

@Serializable
data class ReportRequest(
    val type: String = "none",
    val reason: String
)

@Serializable
data class FollowersResponse(
    val followers: List<User> = emptyList(),
    val last: Boolean = false
)

@Serializable
data class FollowingResponse(
    val following: List<User> = emptyList(),
    val last: Boolean = false
)

@Serializable
data class UpdateBioRequest(val bio: String)

@Serializable
data class SearchPostsResponse(val results: List<Post> = emptyList(), val last: Boolean = false)

@Serializable
data class SearchUsersResponse(val results: List<User> = emptyList(), val last: Boolean = false)

@Serializable
data class TrendingResponse(val posts: List<Post> = emptyList(), val since: String = "")

@Serializable
data class FrogResponse(val frog: String = "")

@Serializable
data class ImageUploadResponse(
    val success: Boolean = false,
    val url: String = "",
    val filename: String = "",
    val size: Long = 0L,
    @SerialName("mime_type") val mimeType: String = ""
)
