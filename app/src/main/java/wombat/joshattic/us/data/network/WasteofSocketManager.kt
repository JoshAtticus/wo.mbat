package wombat.joshattic.us.data.network

import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.URI

class WasteofSocketManager {
    private var socket: Socket? = null

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    fun connect(token: String) {
        socket?.disconnect()

        val options = IO.Options.builder()
            .setTransports(arrayOf("websocket"))
            .setAuth(mapOf("token" to token))
            .build()

        val newSocket = IO.socket(URI.create("https://api.wasteof.money"), options)

        newSocket.on(Socket.EVENT_CONNECT) {
            println("Socket connected!")
        }

        newSocket.on("updateMessageCount") { args ->
            if (args.isNotEmpty()) {
                val count = args[0] as? Int ?: (args[0] as? String)?.toIntOrNull()
                if (count != null) {
                    _unreadCount.value = count
                }
            }
        }

        newSocket.on(Socket.EVENT_DISCONNECT) {
            println("Socket disconnected!")
        }

        newSocket.on(Socket.EVENT_CONNECT_ERROR) { args ->
            println("Socket connect error: ${args.firstOrNull()}")
        }

        newSocket.connect()
        socket = newSocket
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
        _unreadCount.value = 0
    }
}
