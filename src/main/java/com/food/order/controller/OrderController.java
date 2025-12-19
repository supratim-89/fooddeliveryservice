package com.food.order.controller;

import com.food.order.dto.OrderRequest;
import com.food.order.model.Order;
import com.food.order.model.OrderStatus;
import com.food.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Management", description = "APIs for managing food orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create a new order")
    public ResponseEntity<Order> createOrder(@Valid @RequestBody OrderRequest request) {
        log.info("Received order creation request for customer: {}", request.getCustomerId());
        Order order = orderService.createOrder(request);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId) {
        return orderService.getOrderById(orderId)
                .map(order -> ResponseEntity.ok(order))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get orders by customer ID")
    public ResponseEntity<List<Order>> getOrdersByCustomerId(@PathVariable Long customerId) {
        List<Order> orders = orderService.getOrdersByCustomerId(customerId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/restaurant/{restaurantId}")
    @Operation(summary = "Get orders by restaurant ID")
    public ResponseEntity<List<Order>> getOrdersByRestaurantId(@PathVariable Long restaurantId) {
        List<Order> orders = orderService.getOrdersByRestaurantId(restaurantId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get orders by status")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable OrderStatus status) {
        List<Order> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{orderId}/status")
    @Operation(summary = "Update order status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long orderId,
                                                 @RequestParam OrderStatus status) {
        log.info("Updating order {} status to {}", orderId, status);
        Order updatedOrder = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(updatedOrder);
    }

    @PutMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel an order")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long orderId) {
        log.info("Cancelling order {}", orderId);
        Order cancelledOrder = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(cancelledOrder);
    }

    @GetMapping
    @Operation(summary = "Get all orders with optional filtering")
    public ResponseEntity<List<Order>> getAllOrders(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long restaurantId,
            @RequestParam(required = false) OrderStatus status) {

        List<Order> orders;

        if (customerId != null) {
            orders = orderService.getOrdersByCustomerId(customerId);
        } else if (restaurantId != null) {
            orders = orderService.getOrdersByRestaurantId(restaurantId);
        } else if (status != null) {
            orders = orderService.getOrdersByStatus(status);
        } else {
            // This would typically be paginated, but for simplicity returning all
            orders = List.of(); // In a real implementation, you'd have a method to get all orders
        }

        return ResponseEntity.ok(orders);
    }
}


