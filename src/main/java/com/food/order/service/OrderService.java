package com.food.order.service;

import com.food.order.dto.OrderRequest;
import com.food.order.mapper.OrderMapper;
import com.food.order.model.Order;
import com.food.order.model.OrderStatus;
import com.food.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ApplicationEventPublisher eventPublisher;

    /* ---------- Create Order ---------- */

    public Order createOrder(OrderRequest request) {

        log.info("Creating order for customerId={}, restaurantId={}",
                request.getCustomerId(), request.getRestaurantId());

        validateOrderRequest(request);

        Order order = orderMapper.toOrder(request);

        Order savedOrder = orderRepository.save(order);

        log.info("Order persisted successfully. orderId={}, orderNumber={}",
                savedOrder.getId(), savedOrder.getOrderNumber());

        // 🔥 Domain event (Kafka will be AFTER_COMMIT)
        eventPublisher.publishEvent(new OrderCreatedEvent(savedOrder));

        return savedOrder;
    }

    /* ---------- Queries ---------- */

    @Transactional(readOnly = true)
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByRestaurantId(Long restaurantId) {
        return orderRepository.findByRestaurantId(restaurantId);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    /* ---------- Update Status ---------- */

    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new OrderNotFoundException("Order not found with id: " + orderId));

        OrderStatus oldStatus = order.getStatus();

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);

        log.info("Order status updated. orderId={}, {} -> {}",
                orderId, oldStatus, newStatus);

        eventPublisher.publishEvent(
                new OrderStatusChangedEvent(updatedOrder, oldStatus)
        );

        return updatedOrder;
    }

    /* ---------- Cancel Order ---------- */

    public Order cancelOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new OrderNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() == OrderStatus.DELIVERED ||
            order.getStatus() == OrderStatus.CANCELLED) {

            log.warn("Invalid cancel attempt. orderId={}, status={}",
                    orderId, order.getStatus());

            throw new InvalidOrderStatusException(
                    "Cannot cancel order with status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());

        Order cancelledOrder = orderRepository.save(order);

        log.info("Order cancelled successfully. orderId={}", orderId);

        eventPublisher.publishEvent(new OrderCancelledEvent(cancelledOrder));

        return cancelledOrder;
    }

    /* ---------- Validation ---------- */

    private void validateOrderRequest(OrderRequest request) {
        // Custom validation hooks (inventory, restaurant availability, etc.)
    }

    /* ---------- Domain Events ---------- */

    public record OrderCreatedEvent(Order order) {}

    public record OrderStatusChangedEvent(
            Order order,
            OrderStatus oldStatus
    ) {}

    public record OrderCancelledEvent(Order order) {}

    /* ---------- Exceptions ---------- */

    public static class OrderNotFoundException extends RuntimeException {
        public OrderNotFoundException(String message) {
            super(message);
        }
    }

    public static class InvalidOrderStatusException extends RuntimeException {
        public InvalidOrderStatusException(String message) {
            super(message);
        }
    }
}
