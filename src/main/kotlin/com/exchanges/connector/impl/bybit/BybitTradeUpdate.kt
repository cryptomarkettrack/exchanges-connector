package com.exchanges.connector.impl.bybit

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class BybitTradeUpdate(
    @JsonProperty("topic") val topic: String, // Topic of the message
    @JsonProperty("id") val id: String, // Message ID
    @JsonProperty("creationTime") val creationTime: Long, // Message creation time
    @JsonProperty("data") val trades: List<TradeData> // List of trade data
) {
    // Optional: Add any additional methods or computed properties if needed
}

@JsonIgnoreProperties(ignoreUnknown = true) // This will ignore unknown fields
data class TradeData(
    @JsonProperty("category") val category: String, // Category of the trade (e.g., "spot")
    @JsonProperty("symbol") val symbol: String, // Trading pair symbol
    @JsonProperty("execFee") val execFee: BigDecimal, // Execution fee
    @JsonProperty("execId") val execId: String, // Execution ID
    @JsonProperty("execPrice") val execPrice: BigDecimal, // Execution price
    @JsonProperty("execQty") val execQty: BigDecimal, // Execution quantity
    @JsonProperty("execType") val execType: String, // Type of execution (e.g., "Trade")
    @JsonProperty("execValue") val execValue: BigDecimal, // Execution value
    @JsonProperty("feeRate") val feeRate: String, // Fee rate
    @JsonProperty("leavesQty") val leavesQty: BigDecimal, // Quantity left on the order
    @JsonProperty("orderId") val orderId: String, // Associated order ID
    @JsonProperty("orderLinkId") val orderLinkId: String, // Order link ID
    @JsonProperty("orderPrice") val orderPrice: BigDecimal, // Order price
    @JsonProperty("orderQty") val orderQty: BigDecimal, // Order quantity
    @JsonProperty("orderType") val orderType: String, // Type of the order
    @JsonProperty("side") val side: String, // Buy or Sell side
    @JsonProperty("execTime") val execTime: Long, // Execution time
    @JsonProperty("isLeverage") val isLeverage: String, // Leverage information
    @JsonProperty("isMaker") val isMaker: Boolean, // Is the buyer the market maker?
    @JsonProperty("seq") val seq: Long, // Sequence number of the trade
    @JsonProperty("marketUnit") val marketUnit: String // Market unit (e.g., "quoteCoin")
)

