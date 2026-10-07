package wombat.joshattic.us.data.network

import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// wasteof.index (the archive) exposes a far more powerful search than the live API
interface WasteOfIndexApiService {
    @GET("api/search")
    suspend fun advancedSearch(
        @Query("q") q: String = "",
        @Query("kind") kind: String = "post",
        @Query("user") user: String = "",
        @Query("after") after: Long = 0,
        @Query("before") before: Long = 0,
        @Query("min_loves") minLoves: Int = 0,
        @Query("has_media") hasMedia: Boolean = false,
        @Query("has_images") hasImages: Boolean = false,
        @Query("has_links") hasLinks: Boolean = false,
        @Query("is_edited") isEdited: Boolean = false,
        @Query("is_locked") isLocked: Boolean = false,
        @Query("deleted") deleted: Boolean = false,
        @Query("regex") regex: Boolean = false,
        @Query("sort") sort: String = "newest",
        @Query("page") page: Int = 0,
        @Query("limit") limit: Int = 50
    ): WasteOfIndexSearchResponse
}

// Results are a heterogeneous list (posts / comments / users) depending on kind,
// so we keep each raw JsonObject and extract fields where they're consumed
data class WasteOfIndexSearchResponse(
    val kind: String? = null,
    val total: Int? = null,
    val page: Int? = null,
    val results: List<JsonObject>? = null
)

object IndexApi {
    val service: WasteOfIndexApiService by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()
        Retrofit.Builder()
            .baseUrl("https://wasteofindex.joshattic.us/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WasteOfIndexApiService::class.java)
    }
}
