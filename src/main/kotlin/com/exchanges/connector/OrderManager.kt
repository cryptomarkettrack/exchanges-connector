package com.exchanges.connector

import com.exchanges.connector.model.OrderRequest
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class OrderManager(private val exchangeClients: Map<String, CryptoExchange>) {

    fun placeOrder(exchangeName: String, orderRequest: OrderRequest): Mono<String> {
        val exchange: CryptoExchange = getExchangeClient(exchangeName)
        return exchange.placeOrder(orderRequest)
    }

    fun cancelOrder(exchangeName: String, orderId: String): Mono<Void> {
        val exchange: CryptoExchange = getExchangeClient(exchangeName)
        return exchange.cancelOrder(orderId).then()
    }

    fun listenToOrderUpdates(exchangeName: String): Flux<Void> {
        val exchange: CryptoExchange = getExchangeClient(exchangeName)
        return exchange.listenToOrders { orderUpdate ->
            // Process the order update (e.g., log it, update the database, etc.)
            println("Handling order update: $orderUpdate")
        }.thenMany(Flux.empty())
    }

    fun listenToTradeUpdates(exchangeName: String): Flux<Void> {
        val exchange: CryptoExchange = getExchangeClient(exchangeName)
        return exchange.listenToTrades({ tradeUpdate ->
            // Process the order update (e.g., log it, update the database, etc.)
            println("Handling trade update: $tradeUpdate")
        }, { error ->
            println("Error occurred: ${error.message}")
        }).thenMany(Flux.empty())
    }

    private fun getExchangeClient(exchangeName: String): CryptoExchange {
        return exchangeClients[exchangeName]
            ?: throw IllegalArgumentException("Exchange client for $exchangeName not found")
    }
}
