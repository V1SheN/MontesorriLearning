package com.example.montesorrilearning.data.remote

import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.json.JSONObject

class SocketManager(private val serverUrl: String) {

    private var socket: Socket? = null

    private val _events = MutableSharedFlow<SocketEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<SocketEvent> = _events

    fun connect(token: String) {
        if (socket?.connected() == true) return

        val opts = IO.Options().apply {
            auth = mapOf("token" to token)
            forceNew = true
            reconnection = true
            reconnectionAttempts = 5
            reconnectionDelay = 1000
        }

        socket = IO.socket(serverUrl, opts).apply {
            on(Socket.EVENT_CONNECT) {
                _events.tryEmit(SocketEvent.Connected)
            }
            on(Socket.EVENT_DISCONNECT) {
                _events.tryEmit(SocketEvent.Disconnected)
            }
            on("new_entry") { args ->
                if (args.isNotEmpty()) {
                    val data = args[0] as? JSONObject
                    _events.tryEmit(SocketEvent.NewEntry(data?.toString() ?: ""))
                }
            }
            connect()
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
    }

    fun isConnected(): Boolean = socket?.connected() ?: false
}

sealed class SocketEvent {
    data object Connected : SocketEvent()
    data object Disconnected : SocketEvent()
    data class NewEntry(val data: String) : SocketEvent()
}
