package com.exchanges.connector.configuration

import okhttp3.*
import reactor.core.publisher.Flux
import reactor.util.retry.Retry
import java.time.Duration
import java.util.concurrent.TimeUnit

class WebSocketConnector<T : Any> {
    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS) // For WebSockets, we want no read timeout
        .build()

    fun connectWebSocket(
        url: String,
        onOpen: (WebSocket, Response) -> Unit = { _, _ -> },
        onMessage: (WebSocket, T) -> Unit = { _, _ -> },
        onFailure: (WebSocket, Throwable, Response?) -> Unit = { _, _, _ -> },
        onClosed: (WebSocket, Int, String) -> Unit = { _, _, _ -> },
        deserialize: (String) -> T, // Function to deserialize String into T
        retryConfig: Retry = Retry.fixedDelay(3, Duration.ofSeconds(5)) // Retry logic
    ): Flux<T> {
        return Flux.create<T> { sink ->
            fun createWebSocketConnection() {
                val request = Request.Builder().url(url).build()

                val listener = object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        onOpen(webSocket, response)
                    }

                    override fun onMessage(webSocket: WebSocket, text: String) {
                        try {
                            val deserializedMessage = deserialize(text)
                            onMessage(webSocket, deserializedMessage)
                            sink.next(deserializedMessage) // Emit the deserialized message into the Flux
                        } catch (e: Exception) {
                            println("Failed to deserialize message: ${e.message}")
                        }
                    }

                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                        onFailure(webSocket, t, response)
                        sink.error(t) // Signal error to trigger retry
                    }

                    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                        onClosed(webSocket, code, reason)
                        sink.complete() // Complete the Flux
                    }
                }

                // Create the WebSocket connection
                val webSocket = okHttpClient.newWebSocket(request, listener)

                // Handle Flux termination by closing the WebSocket
                sink.onDispose {
                    webSocket.close(1000, "Flux closed")
                }
            }

            // Start the initial WebSocket connection
            createWebSocketConnection()
        }
            .retryWhen(retryConfig) // Apply the retry configuration
    }
}
