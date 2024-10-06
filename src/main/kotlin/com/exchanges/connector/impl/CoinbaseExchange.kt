package com.exchanges.connector.impl

import com.exchanges.connector.*
import com.exchanges.connector.configuration.ExchangeConfigProperties
import com.exchanges.connector.model.OrderRequest
import com.exchanges.connector.model.OrderResponse
import com.exchanges.connector.model.OrderUpdate
import com.exchanges.connector.model.TradeUpdate
import okhttp3.OkHttpClient
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import org.springframework.web.reactive.function.client.WebClient

class CoinbaseExchange(private val webClient: WebClient, exchangeProps: ExchangeConfigProperties.ExchangeProperties) : CryptoExchange {
    private val client = OkHttpClient()

    override fun placeOrder(orderRequest: OrderRequest): Mono<String> {
        return webClient.post()
            .uri("https://api.exchange.coinbase.com/orders")
            .bodyValue(orderRequest)
            .retrieve()
            .bodyToMono(String::class.java)
    }

    override fun cancelOrder(orderId: String): Mono<Boolean> {
        return webClient.delete()
            .uri("https://api.exchange.coinbase.com/orders/$orderId")
            .retrieve()
            .bodyToMono(Boolean::class.java)
    }

    override fun listenToOrders(handler: OrderUpdateHandler): Flux<OrderUpdate> {
        // WebSocket listening logic here using OkHttp or WebClient
        return Flux.empty() // Placeholder
    }

    override fun listenToTrades(onUpdate: TradeUpdateHandler, onError: ErrorHandler): Flux<TradeUpdate> {
        // WebSocket listening logic here using OkHttp or WebClient
        return Flux.empty() // Placeholder
    }
}
