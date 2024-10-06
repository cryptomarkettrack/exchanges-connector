package com.exchanges.connector.impl.binance

import com.exchanges.connector.CryptoExchange
import com.exchanges.connector.ErrorHandler
import com.exchanges.connector.OrderUpdateHandler
import com.exchanges.connector.TradeUpdateHandler
import com.exchanges.connector.configuration.ExchangeConfigProperties
import com.exchanges.connector.model.ErrorResponse
import com.exchanges.connector.model.OrderRequest
import com.exchanges.connector.model.OrderUpdate
import com.exchanges.connector.model.TradeUpdate
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.*
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.net.URLEncoder
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class BinanceExchange(
    private val webClient: WebClient,
    private val exchangeProps: ExchangeConfigProperties.ExchangeProperties
) : CryptoExchange {

    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS) // For WebSockets, we want no read timeout
        .build()

    private val objectMapper = jacksonObjectMapper()

    override fun placeOrder(orderRequest: OrderRequest): Mono<String> {
        // Load API key and secret from your config
        val apiKey = exchangeProps.apiKey
        val secret = exchangeProps.apiSecret

        // Define the query parameters
        val formData: MultiValueMap<String, String> = LinkedMultiValueMap()
        formData.add("symbol", orderRequest.symbol)
        formData.add("side", orderRequest.side.toString())
        formData.add("type", orderRequest.type.toString())
//        formData.add("timeInForce", orderRequest.timeInForce)
        formData.add("quantity", orderRequest.quantity.toString())
//        formData.add("price", orderRequest.price.toString())
        formData.add("timestamp", Instant.now().toEpochMilli().toString()) // Current timestamp in milliseconds

        // Prepare the parameter string for signing
        val params = formData.entries.joinToString("&") { (key, value) ->
            "$key=${
                URLEncoder.encode(
                    value.joinToString(),
                    "UTF-8"
                )
            }"
        }

        // Generate the
        val signature = Signature().getSignature(params, exchangeProps.apiSecret)

        // Build the final URL with signature
        val finalUrl = "/api/v3/order/test?$params&signature=$signature"

        return webClient.post()
            .uri(finalUrl)
            .header("X-MBX-APIKEY", apiKey)
            .accept(MediaType.APPLICATION_JSON)
            .exchangeToMono { response ->
                if (response.statusCode() == HttpStatus.OK) {
                    return@exchangeToMono response.bodyToMono(String::class.java)
                } else {
                    println("Error: ${response.bodyToMono(String::class.java)}")
                    return@exchangeToMono response.createError()
                }
            }
            .retryWhen(
                Retry.fixedDelay(3, Duration.ofSeconds(2)) // Retry 3 times with a delay of 2 seconds
//                    .filter { throwable -> throwable is Exception } // Optional: Add a condition to retry only on specific exceptions
            )
            .doOnError { throwable ->
                println("Retry attempts exhausted. Last error: ${throwable.message}")
                // Handle the case when retry attempts are exhausted, e.g., log or alert
            }
            .onErrorResume { throwable ->
                // You can return a fallback value or alternate behavior if retry fails
                println("Handling fallback after retries exhausted. Error: ${throwable.message}")
                Mono.just("Fallback response due to failure")
            }
    }

    override fun cancelOrder(orderId: String): Mono<Boolean> {
        return webClient.delete()
            .uri("https://api.binance.com/api/v3/order?orderId=$orderId")
            .retrieve()
            .bodyToMono(Boolean::class.java)
    }

    override fun listenToOrders(handler: OrderUpdateHandler): Flux<OrderUpdate> {
        return Flux.create { sink ->
            val request = Request.Builder()
                .url("wss://stream.binance.com:9443/ws/btcusdt@order") // Adjust the endpoint for order updates
                .build()

            val listener = object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    println("Connected to Binance WebSocket for order updates.")
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    // Deserialize the order event JSON into BinanceOrderUpdate class
                    try {
                        val orderUpdate = objectMapper.readValue<BinanceOrderUpdate>(text)
                        val unifiedOrder = OrderUpdate(
                            symbol = orderUpdate.symbol,
                            orderId = orderUpdate.orderId.toString(),
                            price = orderUpdate.price,
                            quantity = orderUpdate.quantity,
                            filledQuantity = orderUpdate.quantity,
                            status = orderUpdate.status
                        )

                        // Call the handler with the unified order
                        handler(unifiedOrder)

                        sink.next(unifiedOrder) // Push the event into the Flux
                    } catch (e: Exception) {
                        println("Failed to parse order update: ${e.message}")
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    println("WebSocket connection failed: ${t.message}")
                    sink.error(t)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    println("WebSocket closed: $reason")
                    sink.complete()
                }
            }

            // Create WebSocket connection
            val webSocket = okHttpClient.newWebSocket(request, listener)

            // Close WebSocket on Flux termination
            sink.onDispose {
                webSocket.close(1000, "Flux closed")
            }
        }
            .retryWhen(
                Retry.fixedDelay(
                    3,
                    Duration.ofSeconds(5)
                ) // Retry up to 3 times, with a 5 second delay between attempts
                    .doBeforeRetry { retrySignal -> println("Reconnecting WebSocket due to error: ${retrySignal.failure().message}") }
            )
    }


    override fun listenToTrades(onUpdate: TradeUpdateHandler, onError: ErrorHandler): Flux<TradeUpdate> {
        return Flux.create<TradeUpdate> { sink ->
            // WebSocket connection and listener setup
            fun connect() {
                val request = Request.Builder()
                    .url(exchangeProps.wsBaseUrl + "/btcusdt@trade")
                    .build()

                val listener = object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        println("Connected to Binance WebSocket for trade updates.")
                    }

                    override fun onMessage(webSocket: WebSocket, text: String) {
                        try {
                            // Deserialize the trade event JSON into TradeUpdate class
                            val tradeUpdate = objectMapper.readValue<BinanceTradeUpdate>(text)
                            val unifiedTrade = TradeUpdate(tradeUpdate.symbol, tradeUpdate.price, tradeUpdate.quantity)
                            onUpdate(unifiedTrade)
                            sink.next(unifiedTrade) // Push the event into the Flux
                        } catch (e: Exception) {
                            println("Failed to parse trade update: ${e.message}")
                        }
                    }

                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                        println("WebSocket connection failed: ${t.message}")
                        t.message?.let {
                            onError(
                                ErrorResponse(
                                    message = it,
                                    code = 500
                                )
                            )
                        }
                        sink.error(t) // Trigger the retry
                    }

                    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                        println("WebSocket closed: $reason")
                        sink.complete()
                    }
                }

                // Create WebSocket connection
                val webSocket = okHttpClient.newWebSocket(request, listener)

                // Close WebSocket on Flux termination
                sink.onDispose {
                    webSocket.close(1000, "Flux closed")
                }
            }

            // Initial connection
            connect()
        }
            .retryWhen(
                Retry.fixedDelay(
                    3,
                    Duration.ofSeconds(5)
                ) // Retry up to 3 times, with a 5 second delay between attempts
                    .doBeforeRetry { retrySignal -> println("Reconnecting WebSocket due to error: ${retrySignal.failure().message}") }
            )
    }

}

private class Signature {
    private val HMAC_SHA256 = "HmacSHA256"

    // Convert byte array to hex string
    private fun bytesToHex(bytes: ByteArray): String {
        val hexArray = "0123456789abcdef".toCharArray()
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v = bytes[j].toInt() and 0xFF
            hexChars[j * 2] = hexArray[v ushr 4]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }

    fun getSignature(data: String, key: String): String {
        return try {
            val secretKeySpec = SecretKeySpec(key.toByteArray(), HMAC_SHA256)
            val mac = Mac.getInstance(HMAC_SHA256)
            mac.init(secretKeySpec)
            val hmacSha256 = mac.doFinal(data.toByteArray())
            bytesToHex(hmacSha256)
        } catch (e: Exception) {
            throw RuntimeException("Failed to calculate hmac-sha256", e)
        }
    }
}