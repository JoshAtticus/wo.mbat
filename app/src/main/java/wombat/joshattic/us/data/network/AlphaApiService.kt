package wombat.joshattic.us.data.network

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Endpoints on the Alpha (alpha.wasteof.money) host. These are read-only and
 * must be called WITHOUT an Authorization header: unauthenticated requests
 * succeed, while authenticated ones 401 for accounts without alpha access.
 */
interface AlphaApiService {
    @GET("posts/{id}/reposts/__data.json")
    suspend fun getReposts(
        @Path("id") postId: String,
        @Query("x-sveltekit-invalidated") invalidated: String = "001"
    ): ResponseBody
}
