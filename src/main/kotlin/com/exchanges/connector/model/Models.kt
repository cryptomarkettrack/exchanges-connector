package com.exchanges.connector.model

import java.math.BigDecimal

data class OrderRequest(
    val symbol: String,
    val quantity: Double,
    val side: OrderSide,
    val type: OrderType,
    val timeInForce: String,
    val price: Double,
    val recvWindow: Int,
    var timestamp: Long
)

data class OrderResponse(
    val orderId: String,
    val status: String,
    val symbol: String
)

data class TradeUpdate(val symbol: String, val price: BigDecimal, val quantity: BigDecimal)

data class OrderUpdate(
    val symbol: String,        // Symbol
    val orderId: String,        // Order ID
    val price: BigDecimal,        // Price
    val quantity: BigDecimal,     // Quantity
    val filledQuantity: BigDecimal,     // Quantity
    val status: String?        // Order status
)

enum class OrderSide {
    BUY, SELL
}

enum class OrderType {
    MARKET, LIMIT
}

data class ErrorResponse(
    val message: String,
    val code: Int
)

data class WebSocketEvent(val message: String)
