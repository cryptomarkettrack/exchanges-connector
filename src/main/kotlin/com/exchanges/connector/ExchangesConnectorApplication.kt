package com.exchanges.connector

import com.exchanges.connector.configuration.ExchangeConfigProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@EnableConfigurationProperties(ExchangeConfigProperties::class)
@SpringBootApplication
class ExchangesConnectorApplication

fun main(args: Array<String>) {
    runApplication<ExchangesConnectorApplication>(*args)
}
