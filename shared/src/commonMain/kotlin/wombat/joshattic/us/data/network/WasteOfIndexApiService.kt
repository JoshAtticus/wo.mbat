package wombat.joshattic.us.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

// wasteof.index (the archive) exposes a far more powerful search than the live API
class WasteOfIndexApiService(private val client: HttpClient) {
    suspend fun advancedSearch(
        q: String = "",
        kind: String = "post",
        user: String = "",
        after: Long = 0,
        before: Long = 0,
        minLoves: Int = 0,
        hasMedia: Boolean = false,
        hasImages: Boolean = false,
        hasLinks: Boolean = false,
        isEdited: Boolean = false,
        isLocked: Boolean = false,
        deleted: Boolean = false,
        regex: Boolean = false,
        sort: String = "newest",
        page: Int = 0,
        limit: Int = 50
    ): WasteOfIndexSearchResponse = client.get("api/search") {
        parameter("q", q)
        parameter("kind", kind)
        parameter("user", user)
        parameter("after", after)
        parameter("before", before)
        parameter("min_loves", minLoves)
        parameter("has_media", hasMedia)
        parameter("has_images", hasImages)
        parameter("has_links", hasLinks)
        parameter("is_edited", isEdited)
        parameter("is_locked", isLocked)
        parameter("deleted", deleted)
        parameter("regex", regex)
        parameter("sort", sort)
        parameter("page", page)
        parameter("limit", limit)
    }.body()
}

// Results are a heterogeneous list (posts / comments / users) depending on kind,
// so we keep each raw JsonObject and extract fields where they're consumed
@Serializable
data class WasteOfIndexSearchResponse(
    val kind: String? = null,
    val total: Int? = null,
    val page: Int? = null,
    val results: List<JsonObject>? = null
)

object IndexApi {
    val service: WasteOfIndexApiService by lazy {
        val client = HttpClient {
            expectSuccess = true
            install(HttpTimeout) {
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 60_000
                requestTimeoutMillis = 60_000
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        coerceInputValues = true
                    }
                )
            }
            defaultRequest {
                url("https://wasteofindex.joshattic.us/")
            }
        }
        WasteOfIndexApiService(client)
    }
}
