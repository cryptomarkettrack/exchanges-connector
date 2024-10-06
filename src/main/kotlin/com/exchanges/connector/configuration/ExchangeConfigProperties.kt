package com.exchanges.connector.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
class ExchangeConfigProperties {
    var exchanges: Map<String, ExchangeProperties> = mutableMapOf()

    class ExchangeProperties {
        lateinit var apiBaseUrl: String
        lateinit var wsBaseUrl: String
        lateinit var apiKey: String
        lateinit var apiSecret: String
    }
}
