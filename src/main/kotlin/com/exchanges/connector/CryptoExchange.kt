package com.exchanges.connector

import com.exchanges.connector.model.*
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux

typealias OrderUpdateHandler = (OrderUpdate) -> Unit
typealias TradeUpdateHandler = (TradeUpdate) -> Unit
typealias ErrorHandler = (ErrorResponse) -> Unit


interface CryptoExchange {
    fun placeOrder(orderRequest: OrderRequest): Mono<String>
    fun cancelOrder(orderId: String): Mono<Boolean>
    fun listenToOrders(onUpdate: OrderUpdateHandler): Flux<OrderUpdate>
    fun listenToTrades(onUpdate: TradeUpdateHandler, onError: ErrorHandler): Flux<TradeUpdate>
}
