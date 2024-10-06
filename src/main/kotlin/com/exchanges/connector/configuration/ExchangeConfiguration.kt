package com.exchanges.connector.configuration

import com.exchanges.connector.CryptoExchange
import com.exchanges.connector.impl.binance.BybitExchange
import com.exchanges.connector.impl.CoinbaseExchange
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class ExchangeConfiguration(private val exchangeConfigProperties: ExchangeConfigProperties) {

    @Bean
    fun webClient(): WebClient.Builder {
        return WebClient.builder()
    }

    @Bean
    @Qualifier(value = "exchangeClients")
    fun exchangeClients(webClientBuilder: WebClient.Builder): Map<String, CryptoExchange> {
        val exchanges = mutableMapOf<String, CryptoExchange>()

        exchangeConfigProperties.exchanges.forEach { (exchangeName, exchangeProps) ->
            val webClient = webClientBuilder
                .baseUrl(exchangeProps.apiBaseUrl)
                .build()

            // Instantiate specific exchange implementations based on the exchange name
            val exchangeClient: CryptoExchange = when (exchangeName.lowercase()) {
                "binance" -> BybitExchange(webClient, exchangeProps)
                "coinbase" -> CoinbaseExchange(webClient, exchangeProps)
                // Add more exchanges as needed
                else -> throw IllegalArgumentException("Unsupported exchange: $exchangeName")
            }

            exchanges[exchangeName] = exchangeClient
        }

        return exchanges
    }
}
