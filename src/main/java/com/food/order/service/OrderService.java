package com.food.order.service;

import com.food.order.dto.OrderRequest;
import org.springframework.context.ApplicationEventPublisher;
import com.food.order.kafka.OrderProducer;
import com.food.order.mapper.OrderMapper;
import com.food.order.model.Order;
import com.food.order.model.OrderItem;
import com.food.order.model.OrderStatus;
import com.food.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderProducer orderProducer;
    private final OrderMapper orderMapper;
    private final ApplicationEventPublisher eventPublisher;
    public Order createOrder(OrderRequest request) {

        validateOrderRequest(request);

        Order order = orderMapper.toOrder(request);

        Order savedOrder = orderRepository.save(order);

        eventPublisher.publishEvent(new OrderCreatedEvent(savedOrder));

        return savedOrder;
    }


    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    public List<Order> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);

        orderProducer.sendOrderStatusChangedEvent(
                buildOrderStatusChangedEvent(updatedOrder, oldStatus)
        );

        log.info("Order {} status changed from {} to {}", orderId, oldStatus, newStatus);
        return updatedOrder;
    }

    public Order cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        if (order.getStatus() == OrderStatus.DELIVERED ||
            order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderStatusException(
                    "Cannot cancel order with status: " + order.getStatus()
            );
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());

        Order cancelledOrder = orderRepository.save(order);

      

        log.info("Order {} cancelled successfully", orderId);
        return cancelledOrder;
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public List<Order> getOrdersByRestaurantId(Long restaurantId) {
        return orderRepository.findByRestaurantId(restaurantId);
    }

    private void validateOrderRequest(OrderRequest request) {
        // add custom validation if needed
    }

   

  

   

    private Object buildOrderStatusChangedEvent(Order order, OrderStatus oldStatus) {
        return new OrderStatusChangedEvent(
                order.getId(),
                order.getOrderNumber(),
                oldStatus,
                order.getStatus(),
                order.getUpdatedAt()
        );
    }

   

    /* ---------- Events ---------- */

    public record OrderCreatedEvent(
            Long orderId,
            String orderNumber,
            Long customerId,
            Long restaurantId,
            BigDecimal totalAmount,
            LocalDateTime createdAt
    ) {}

    public record OrderStatusChangedEvent(
            Long orderId,
            String orderNumber,
            OrderStatus oldStatus,
            OrderStatus newStatus,
            LocalDateTime changedAt
    ) {}

    public record OrderCancelledEvent(
            Long orderId,
            String orderNumber,
            Long customerId,
            Long restaurantId,
            BigDecimal totalAmount,
            LocalDateTime cancelledAt
    ) {}

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
