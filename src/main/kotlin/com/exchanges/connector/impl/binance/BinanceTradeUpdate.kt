package com.exchanges.connector.impl.binance

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class BinanceTradeUpdate(
    @JsonProperty("e") val eventType: String, // Event type
    @JsonProperty("E") val eventTime: Long, // Event time
    @JsonProperty("s") val symbol: String, // Symbol
    @JsonProperty("t") val tradeId: Long, // Trade ID
    @JsonProperty("p") val price: BigDecimal, // Price
    @JsonProperty("q") val quantity: BigDecimal, // Quantity
    @JsonProperty("T") val tradeTime: Long, // Trade time
    @JsonProperty("m") val isMarketMaker: Boolean, // Is the buyer the market maker?
    @JsonProperty("M") val ignore: Boolean // Ignore field
) {
    // Optional: Add any additional methods or computed properties if needed
}
