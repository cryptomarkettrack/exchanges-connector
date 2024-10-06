package com.exchanges.connector

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import reactor.core.Disposable

@Configuration
class AOMConfiguration {
    @Bean
    @DependsOn(value = ["exchangeClients"])
    fun listenToTradeUpdates(exchangeClients: Map<String, CryptoExchange>): List<Disposable> {
        return exchangeClients.map { (exchangeName, exchange) ->
            exchange.listenToTrades(
                { tradeUpdate ->
                    // Process each trade update from different exchanges
                    println("Handling trade update from $exchangeName: $tradeUpdate")
                },
                { error ->
                    println("Error occurred on $exchangeName: ${error.message}")
                }
            ).subscribe()
        }
    }

//    @Bean
//    @DependsOn(value = ["exchangeClients"])
//    fun listenToOrderUpdates(exchangeClients: Map<String, CryptoExchange>): List<Disposable> {
//        return exchangeClients.map { (exchangeName, exchange) ->
//            exchange.listenTo   Orders(
//                { orderUpdate ->
//                    // Process each trade update from different exchanges
//                    println("Handling order update from $exchangeName: $orderUpdate")
//                },
//                { error ->
//                    println("Error occurred on $exchangeName: ${error.message}")
//                }
//            ).subscribe()
//        }
//    }
}