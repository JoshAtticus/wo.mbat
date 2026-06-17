package wombat.joshattic.us.wear.data.network

import retrofit2.http.*
import wombat.joshattic.us.wear.data.model.*

interface ApiService {
    @GET("users/{username}/following/posts")
    suspend fun getFeed(
        @Path("username") username: String,
        @Query("page") page: Int = 1,
        @Header("authorization") token: String? = null
    ): FeedResponse

    @GET("explore/posts/trending")
    suspend fun getTrendingPosts(
        @Header("authorization") token: String? = null
    ): FeedResponse

    @GET("posts/{post_id}/comments")
    suspend fun getComments(
        @Path("post_id") postId: String,
        @Header("authorization") token: String? = null
    ): CommentResponse

    @POST("posts/{post_id}/comments")
    suspend fun makeComment(
        @Path("post_id") postId: String,
        @Header("authorization") token: String,
        @Body request: CreateCommentRequest
    ): Comment

    @POST("posts")
    suspend fun makePost(
        @Header("authorization") token: String,
        @Body request: CreatePostRequest
    ): Post

    @POST("posts/{post_id}/loves")
    suspend fun togglePostLove(
        @Path("post_id") postId: String,
        @Header("authorization") token: String
    ): LoveToggleResponse

    @GET("messages/unread")
    suspend fun getUnreadNotifications(
        @Header("authorization") token: String
    ): NotificationResponse

    @POST("messages/mark/read")
    suspend fun markRead(
        @Header("authorization") token: String,
        @Body request: MarkReadRequest
    ): NotificationResponse
}
