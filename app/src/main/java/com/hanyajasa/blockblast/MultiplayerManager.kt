package com.hanyajasa.blockblast

import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.CopyOnWriteArrayList

class MultiplayerManager(private val onEventReceived: (GameEvent) -> Unit) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private val connectedClients = CopyOnWriteArrayList<Socket>()
    
    private var isRunning = false

    fun startHosting(port: Int = 8080) {
        isRunning = true
        scope.launch {
            try {
                serverSocket = ServerSocket(port)
                while (isRunning) {
                    val socket = serverSocket?.accept() ?: break
                    connectedClients.add(socket)
                    handleSocket(socket)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun joinGame(hostIp: String, port: Int = 8080) {
        isRunning = true
        scope.launch {
            try {
                val socket = Socket(hostIp, port)
                clientSocket = socket
                handleSocket(socket)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun handleSocket(socket: Socket) {
        scope.launch {
            try {
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                while (isRunning) {
                    val line = reader.readLine() ?: break
                    val event = GameEvent.fromJson(line)
                    if (event != null) {
                        withContext(Dispatchers.Main) {
                            onEventReceived(event)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                connectedClients.remove(socket)
                socket.close()
            }
        }
    }

    fun sendEvent(event: GameEvent) {
        scope.launch {
            val json = event.toJson()
            // Send to all connected clients (if we are host)
            connectedClients.forEach { socket ->
                try {
                    val writer = PrintWriter(socket.getOutputStream(), true)
                    writer.println(json)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            // Send to host (if we are client)
            clientSocket?.let { socket ->
                try {
                    val writer = PrintWriter(socket.getOutputStream(), true)
                    writer.println(json)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun stop() {
        isRunning = false
        scope.launch {
            serverSocket?.close()
            clientSocket?.close()
            connectedClients.forEach { it.close() }
            connectedClients.clear()
        }
    }
}
