package wombat.joshattic.us.data.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object RetrofitClient {
    private const val BASE_URL = "https://api.wasteof.money/"
    private const val ALPHA_BASE_URL = "https://alpha.wasteof.money/"
    private const val OPEN_GRAPH_BASE_URL = "https://og.joshattic.us/"

    val networkJson = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
    }

    private fun buildClient(
        baseUrl: String,
        connectTimeoutMs: Long = 15_000,
        requestTimeoutMs: Long = 30_000,
        socketTimeoutMs: Long = 30_000
    ): HttpClient = HttpClient {
        expectSuccess = true
        install(HttpTimeout) {
            connectTimeoutMillis = connectTimeoutMs
            requestTimeoutMillis = requestTimeoutMs
            socketTimeoutMillis = socketTimeoutMs
        }
        install(ContentNegotiation) {
            json(networkJson)
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    println("KtorHttp: $message")
                }
            }
            level = LogLevel.INFO
        }
        defaultRequest {
            url(baseUrl)
        }
    }

    private val defaultClient = buildClient(BASE_URL)
    private val alphaClient = buildClient(ALPHA_BASE_URL)
    private val uploadClient = buildClient(
        BASE_URL,
        connectTimeoutMs = 30_000,
        requestTimeoutMs = 300_000,
        socketTimeoutMs = 300_000
    )
    private val openGraphClient = buildClient(
        OPEN_GRAPH_BASE_URL,
        connectTimeoutMs = 8_000,
        requestTimeoutMs = 10_000,
        socketTimeoutMs = 8_000
    )

    val apiService: ApiService = ApiService(defaultClient)
    val alphaApiService: AlphaApiService = AlphaApiService(alphaClient)
    val uploadApiService: ApiService = ApiService(uploadClient)
    val openGraphApiService: OpenGraphApiService = OpenGraphApiService(openGraphClient)
}
