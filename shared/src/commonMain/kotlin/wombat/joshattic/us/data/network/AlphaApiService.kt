package wombat.joshattic.us.data.network

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText

class AlphaApiService(private val client: HttpClient) {
    suspend fun getReposts(
        postId: String,
        invalidated: String = "001"
    ): String = client.get("posts/$postId/reposts/__data.json") {
        parameter("x-sveltekit-invalidated", invalidated)
    }.bodyAsText()
}
