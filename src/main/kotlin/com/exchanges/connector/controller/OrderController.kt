package com.exchanges.connector.controller

import com.exchanges.connector.OrderManager
import com.exchanges.connector.model.OrderRequest
import com.exchanges.connector.model.OrderResponse
import org.springframework.web.bind.annotation.*
import reactor.core.Disposable
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/orders")
class OrderController(private val orderManager: OrderManager) {

    @PostMapping("/place/{exchange}")
    fun placeOrder(@PathVariable exchange: String, @RequestBody orderRequest: OrderRequest): Mono<String> {
        return orderManager.placeOrder(exchange, orderRequest)
    }

    @DeleteMapping("/cancel/{exchange}/{orderId}")
    fun cancelOrder(@PathVariable exchange: String, @PathVariable orderId: String) {
        orderManager.cancelOrder(exchange, orderId).subscribe()
    }

    @GetMapping("/listen-orders/{exchange}")
    fun listenToOrders(@PathVariable exchange: String) {
        orderManager.listenToOrderUpdates(exchange).subscribe()
    }

    @GetMapping("/listen-trades/{exchange}")
    fun listenToTrades(@PathVariable exchange: String) {
        orderManager.listenToTradeUpdates(exchange).subscribe()
    }
}
