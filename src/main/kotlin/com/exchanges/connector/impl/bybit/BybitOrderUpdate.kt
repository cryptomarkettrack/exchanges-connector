package com.exchanges.connector.impl.binance

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class BybitOrderUpdate(
    @JsonProperty("e") val eventType: String, // Event type
    @JsonProperty("E") val eventTime: Long, // Event time
    @JsonProperty("s") val symbol: String, // Symbol
    @JsonProperty("o") val orderId: Long, // Order ID
    @JsonProperty("p") val price: BigDecimal, // Price
    @JsonProperty("q") val quantity: BigDecimal, // Quantity
    @JsonProperty("s") val status: String? // Order status
) {
    // Add any additional methods or computed properties if needed
}
