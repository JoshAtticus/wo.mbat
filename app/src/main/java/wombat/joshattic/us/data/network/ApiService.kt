package wombat.joshattic.us.data.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import wombat.joshattic.us.data.model.Comment
import wombat.joshattic.us.data.model.CommentResponse
import wombat.joshattic.us.data.model.CreateCommentRequest
import wombat.joshattic.us.data.model.CreatePostRequest
import wombat.joshattic.us.data.model.FeedResponse
import wombat.joshattic.us.data.model.FollowToggleResponse
import wombat.joshattic.us.data.model.LoginRequest
import wombat.joshattic.us.data.model.ReportRequest
import wombat.joshattic.us.data.model.LoginResponse
import wombat.joshattic.us.data.model.MarkReadRequest
import wombat.joshattic.us.data.model.LoveToggleResponse
import wombat.joshattic.us.data.model.Notification
import wombat.joshattic.us.data.model.NotificationResponse
import wombat.joshattic.us.data.model.Post
import wombat.joshattic.us.data.model.User

interface ApiService {
    @POST("session")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("session")
    suspend fun getSession(@Header("authorization") token: String): wombat.joshattic.us.data.model.SessionResponse

    @GET("users/{username}")
    suspend fun getUserProfile(@Path("username") username: String): User

    @GET("users/{username}/following/posts")
    suspend fun getFeed(
        @Path("username") username: String,
        @Query("page") page: Int = 1,
        @Header("authorization") token: String? = null
    ): FeedResponse

    @GET("users/{username}/posts")
    suspend fun getUserPosts(
        @Path("username") username: String,
        @Query("page") page: Int = 1,
        @Header("authorization") token: String? = null
    ): FeedResponse

    @POST("posts")
    suspend fun makePost(
        @Header("authorization") token: String,
        @Body request: CreatePostRequest
    ): Post

    @POST("posts/{post_id}/report")
    suspend fun reportPost(
        @Header("authorization") token: String,
        @Path("post_id") postId: String,
        @Body request: ReportRequest
    )

    @GET("messages/unread")
    suspend fun getUnreadNotifications(
        @Header("authorization") token: String
    ): NotificationResponse

    @GET("messages/admin")
    suspend fun getAdminMessages(
        @Header("authorization") token: String
    ): List<Notification>

    @GET("messages/read")
    suspend fun getReadNotifications(
        @Header("authorization") token: String
    ): NotificationResponse

    @POST("messages/mark/read")
    suspend fun markRead(
        @Header("authorization") token: String,
        @Body request: MarkReadRequest
    ): NotificationResponse

    @GET("posts/{post_id}/comments")
    suspend fun getComments(
        @Path("post_id") postId: String,
        @Header("authorization") token: String? = null
    ): CommentResponse

    @GET("comments/{comment_id}/replies")
    suspend fun getCommentReplies(
        @Path("comment_id") commentId: String,
        @Query("page") page: Int = 1,
        @Header("authorization") token: String? = null
    ): CommentResponse

    @POST("posts/{post_id}/comments")
    suspend fun makeComment(
        @Path("post_id") postId: String,
        @Header("authorization") token: String,
        @Body request: CreateCommentRequest
    ): Comment

    @GET("explore/posts/trending")
    suspend fun getTrendingPosts(
        @Header("authorization") token: String? = null
    ): FeedResponse

    @POST("posts/{post_id}/loves")
    suspend fun togglePostLove(
        @Path("post_id") postId: String,
        @Header("authorization") token: String
    ): LoveToggleResponse

    @GET("posts/{post_id}/loves/{username}")
    suspend fun getPostLoveStatus(
        @Path("post_id") postId: String,
        @Path("username") username: String,
        @Header("authorization") token: String? = null
    ): Boolean

    @GET("users/{username}/followers/{follower}")
    suspend fun getFollowStatus(
        @Path("username") username: String,
        @Path("follower") follower: String,
        @Header("authorization") token: String? = null
    ): Boolean

    @POST("users/{username}/followers")
    suspend fun toggleFollow(
        @Path("username") username: String,
        @Header("authorization") token: String
    ): FollowToggleResponse
}
