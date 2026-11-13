package wombat.joshattic.us.data.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

class WasteofSocketManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val json = Json { ignoreUnknownKeys = true }
    private val client = HttpClient {
        install(WebSockets)
    }

    private var connectionJob: Job? = null

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    fun connect(token: String) {
        connectionJob?.cancel()
        connectionJob = scope.launch {
            while (isActive) {
                try {
                    client.webSocket("wss://api.wasteof.money/socket.io/?EIO=4&transport=websocket") {
                        for (frame in incoming) {
                            if (frame !is Frame.Text) continue
                            val text = frame.readText()
                            when {
                                // Engine.IO OPEN packet -> send Socket.IO CONNECT with auth token
                                text.startsWith("0") -> {
                                    val escapedToken = token.replace("\\", "\\\\").replace("\"", "\\\"")
                                    send(Frame.Text("""40{"token":"$escapedToken"}"""))
                                }
                                // Engine.IO PING -> PONG
                                text == "2" -> {
                                    send(Frame.Text("3"))
                                }
                                // Socket.IO EVENT packet: 42["updateMessageCount", 5]
                                text.startsWith("42") -> {
                                    val payload = text.removePrefix("42")
                                    val arr = runCatching { json.parseToJsonElement(payload) as? JsonArray }.getOrNull()
                                    if (arr != null && arr.size >= 2) {
                                        val eventName = (arr[0] as? JsonPrimitive)?.content
                                        if (eventName == "updateMessageCount") {
                                            val count = arr[1].jsonPrimitive.intOrNull
                                                ?: arr[1].jsonPrimitive.content.toIntOrNull()
                                            if (count != null) {
                                                _unreadCount.value = count
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("Socket connect error: ${e.message}")
                }
                delay(5_000)
            }
        }
    }

    fun disconnect() {
        connectionJob?.cancel()
        connectionJob = null
        _unreadCount.value = 0
    }
}
