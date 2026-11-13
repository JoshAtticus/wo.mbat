package wombat.joshattic.us.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import wombat.joshattic.us.data.model.CommentResponse
import wombat.joshattic.us.data.model.CreateCommentRequest
import wombat.joshattic.us.data.model.CreateCommentResponse
import wombat.joshattic.us.data.model.CreatePostRequest
import wombat.joshattic.us.data.model.CreatePostResponse
import wombat.joshattic.us.data.model.CreateWallCommentResponse
import wombat.joshattic.us.data.model.EditPostRequest
import wombat.joshattic.us.data.model.EditPostResponse
import wombat.joshattic.us.data.model.FeedResponse
import wombat.joshattic.us.data.model.FollowToggleResponse
import wombat.joshattic.us.data.model.FollowersResponse
import wombat.joshattic.us.data.model.FollowingResponse
import wombat.joshattic.us.data.model.FrogResponse
import wombat.joshattic.us.data.model.ImageUploadResponse
import wombat.joshattic.us.data.model.LoginRequest
import wombat.joshattic.us.data.model.LoginResponse
import wombat.joshattic.us.data.model.LoveToggleResponse
import wombat.joshattic.us.data.model.MarkReadRequest
import wombat.joshattic.us.data.model.Notification
import wombat.joshattic.us.data.model.NotificationResponse
import wombat.joshattic.us.data.model.OpenGraphResponse
import wombat.joshattic.us.data.model.Post
import wombat.joshattic.us.data.model.ReportRequest
import wombat.joshattic.us.data.model.SearchPostsResponse
import wombat.joshattic.us.data.model.SearchUsersResponse
import wombat.joshattic.us.data.model.SessionResponse
import wombat.joshattic.us.data.model.UpdateBioRequest
import wombat.joshattic.us.data.model.User
import wombat.joshattic.us.data.model.UsernameAvailableResponse

class ApiService(private val client: HttpClient) {
    suspend fun login(request: LoginRequest): LoginResponse =
        client.post("session") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun checkUsernameAvailable(username: String): UsernameAvailableResponse =
        client.get("username-available") {
            parameter("username", username)
        }.body()

    suspend fun getSession(token: String): SessionResponse =
        client.get("session") {
            header("authorization", token)
        }.body()

    suspend fun getUserProfile(username: String): User =
        client.get("users/$username").body()

    suspend fun getFeed(
        username: String,
        page: Int = 1,
        token: String? = null
    ): FeedResponse = client.get("users/$username/following/posts") {
        parameter("page", page)
        token?.let { header("authorization", it) }
    }.body()

    suspend fun getUserPosts(
        username: String,
        page: Int = 1,
        token: String? = null
    ): FeedResponse = client.get("users/$username/posts") {
        parameter("page", page)
        token?.let { header("authorization", it) }
    }.body()

    suspend fun getPost(
        postId: String,
        token: String? = null
    ): Post = client.get("posts/$postId") {
        token?.let { header("authorization", it) }
    }.body()

    suspend fun makePost(
        token: String,
        request: CreatePostRequest
    ): CreatePostResponse = client.post("posts") {
        header("authorization", token)
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

    suspend fun createRepost(token: String, postId: String): Post =
        client.post("posts/$postId/reposts") {
            header("Authorization", token)
        }.body()

    suspend fun deletePost(token: String, postId: String) {
        client.delete("posts/$postId") {
            header("Authorization", token)
        }
    }

    suspend fun editPost(
        token: String,
        postId: String,
        request: EditPostRequest
    ): EditPostResponse = client.put("posts/$postId") {
        header("Authorization", token)
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

    suspend fun reportPost(
        token: String,
        postId: String,
        request: ReportRequest
    ) {
        client.post("posts/$postId/report") {
            header("authorization", token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun getUnreadNotifications(
        token: String,
        page: Int = 1
    ): NotificationResponse = client.get("messages/unread") {
        header("authorization", token)
        parameter("page", page)
    }.body()

    suspend fun getAdminMessages(
        token: String
    ): List<Notification> = client.get("messages/admin") {
        header("authorization", token)
    }.body()

    suspend fun getReadNotifications(
        token: String,
        page: Int = 1
    ): NotificationResponse = client.get("messages/read") {
        header("authorization", token)
        parameter("page", page)
    }.body()

    suspend fun markRead(
        token: String,
        request: MarkReadRequest
    ): NotificationResponse = client.post("messages/mark/read") {
        header("authorization", token)
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

    suspend fun getComments(
        postId: String,
        token: String? = null
    ): CommentResponse = client.get("posts/$postId/comments") {
        token?.let { header("authorization", it) }
    }.body()

    suspend fun getCommentReplies(
        commentId: String,
        page: Int = 1,
        token: String? = null
    ): CommentResponse = client.get("comments/$commentId/replies") {
        parameter("page", page)
        token?.let { header("authorization", it) }
    }.body()

    suspend fun makeComment(
        postId: String,
        token: String,
        request: CreateCommentRequest
    ): CreateCommentResponse = client.post("posts/$postId/comments") {
        header("authorization", token)
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

    suspend fun reportComment(
        token: String,
        commentId: String,
        request: ReportRequest
    ) {
        client.post("comments/$commentId/report") {
            header("authorization", token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun deleteComment(
        commentId: String,
        token: String
    ) {
        client.delete("comments/$commentId") {
            header("authorization", token)
        }
    }

    suspend fun getTrendingPosts(
        token: String? = null,
        timeframe: String? = null
    ): FeedResponse = client.get("explore/posts/trending") {
        token?.let { header("authorization", it) }
        timeframe?.let { parameter("timeframe", it) }
    }.body()

    suspend fun searchPosts(
        query: String,
        page: Int = 1,
        sort: String? = null,
        token: String? = null
    ): SearchPostsResponse = client.get("search/posts") {
        parameter("q", query)
        parameter("page", page)
        sort?.let { parameter("sort", it) }
        token?.let { header("authorization", it) }
    }.body()

    suspend fun searchUsers(
        query: String,
        page: Int = 1,
        token: String? = null
    ): SearchUsersResponse = client.get("search/users") {
        parameter("q", query)
        parameter("page", page)
        token?.let { header("authorization", it) }
    }.body()

    suspend fun getFrog(): FrogResponse =
        client.get("frog").body()

    suspend fun togglePostLove(
        postId: String,
        token: String
    ): LoveToggleResponse = client.post("posts/$postId/loves") {
        header("authorization", token)
    }.body()

    suspend fun getPostLoveStatus(
        postId: String,
        username: String,
        token: String? = null
    ): Boolean = client.get("posts/$postId/loves/$username") {
        token?.let { header("authorization", it) }
    }.body()

    suspend fun getFollowStatus(
        username: String,
        follower: String,
        token: String? = null
    ): Boolean = client.get("users/$username/followers/$follower") {
        token?.let { header("authorization", it) }
    }.body()

    suspend fun toggleFollow(
        username: String,
        token: String
    ): FollowToggleResponse = client.post("users/$username/followers") {
        header("authorization", token)
    }.body()

    suspend fun getFollowers(
        username: String,
        token: String? = null,
        page: Int = 1
    ): FollowersResponse = client.get("users/$username/followers") {
        token?.let { header("authorization", it) }
        parameter("page", page)
    }.body()

    suspend fun getFollowing(
        username: String,
        token: String? = null,
        page: Int = 1
    ): FollowingResponse = client.get("users/$username/following") {
        token?.let { header("authorization", it) }
        parameter("page", page)
    }.body()

    suspend fun getWallComments(
        username: String,
        page: Int = 1,
        token: String? = null
    ): CommentResponse = client.get("users/$username/wall") {
        parameter("page", page)
        token?.let { header("authorization", it) }
    }.body()

    suspend fun makeWallComment(
        username: String,
        token: String,
        request: CreateCommentRequest
    ): CreateWallCommentResponse = client.post("users/$username/wall") {
        header("authorization", token)
        contentType(ContentType.Application.Json)
        setBody(request)
    }.body()

    suspend fun updateBio(
        username: String,
        token: String,
        request: UpdateBioRequest
    ) {
        client.put("users/$username/bio") {
            header("authorization", token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun uploadProfilePicture(
        username: String,
        token: String,
        bytes: ByteArray,
        filename: String = "picture.jpg",
        mimeType: String = "image/jpeg"
    ) {
        client.submitFormWithBinaryData(
            url = "users/$username/picture",
            formData = formData {
                append(
                    key = "picture",
                    value = bytes,
                    headers = Headers.build {
                        append(HttpHeaders.ContentType, mimeType)
                        append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
                    }
                )
            }
        ) {
            method = HttpMethod.Put
            header("authorization", token)
        }
    }

    suspend fun deleteProfilePicture(
        username: String,
        token: String
    ) {
        client.delete("users/$username/picture") {
            header("authorization", token)
        }
    }

    suspend fun uploadBanner(
        username: String,
        token: String,
        bytes: ByteArray,
        filename: String = "banner.jpg",
        mimeType: String = "image/jpeg"
    ) {
        client.submitFormWithBinaryData(
            url = "users/$username/banner",
            formData = formData {
                append(
                    key = "banner",
                    value = bytes,
                    headers = Headers.build {
                        append(HttpHeaders.ContentType, mimeType)
                        append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
                    }
                )
            }
        ) {
            method = HttpMethod.Put
            header("authorization", token)
        }
    }

    suspend fun deleteBanner(
        username: String,
        token: String
    ) {
        client.delete("users/$username/banner") {
            header("authorization", token)
        }
    }

    suspend fun uploadImageToProxy(
        url: String,
        apiKey: String,
        bytes: ByteArray,
        filename: String,
        mimeType: String
    ): ImageUploadResponse = client.submitFormWithBinaryData(
        url = url,
        formData = formData {
            append(
                key = "file",
                value = bytes,
                headers = Headers.build {
                    append(HttpHeaders.ContentType, mimeType)
                    append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
                }
            )
        }
    ) {
        header("X-API-Key", apiKey)
    }.body()
}

const val OPEN_GRAPH_API_TOKEN = "3759146558600182"

class OpenGraphApiService(private val client: HttpClient) {
    suspend fun fetchOpenGraph(
        url: String,
        token: String = OPEN_GRAPH_API_TOKEN
    ): OpenGraphResponse = client.get("fetch") {
        parameter("url", url)
        parameter("t", token)
    }.body()
}
